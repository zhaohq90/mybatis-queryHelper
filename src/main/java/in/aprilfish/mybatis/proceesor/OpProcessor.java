package in.aprilfish.mybatis.proceesor;

import com.squareup.javapoet.*;
import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.file.BaseFileObject;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Context;
import in.aprilfish.mybatis.annotation.OpEntity;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes("in.aprilfish.mybatis.annotation.OpEntity")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class OpProcessor extends AbstractProcessor {

    private Filer mFiler;
    private Elements elementUtils;         //工具类
    private Messager messager;
    private Types types;

    public static final String PATH = OpEntity.class.getCanonicalName();

    public OpProcessor() {
        super();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.mFiler = processingEnv.getFiler();
        this.elementUtils = processingEnv.getElementUtils();
        this.messager = processingEnv.getMessager();

        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.types = Types.instance(context);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        annotations.stream()
                .filter(typeElement -> typeElement.toString().equals(PATH))
                .forEach(typeElement -> roundEnv.getElementsAnnotatedWith(typeElement)
                        .forEach((this::handle)));
        return true;
    }

    public void handle(Element element) {
        if (!(element.getKind() == ElementKind.CLASS)) return;
        Symbol.ClassSymbol classSymbol = (Symbol.ClassSymbol) element;
        String className = classSymbol.getQualifiedName().toString();
        String clz = classSymbol.getSimpleName().toString();
        String pkg = className.substring(0, className.lastIndexOf("."));

        JavaFileObject javaFileObject = classSymbol.classfile;
        String fullPath = javaFileObject.toUri().toString();
        fullPath = fullPath.substring(6);
        String javaPath = "src/main/java/" + className + ".java";
        fullPath = fullPath.substring(0, fullPath.length() - javaPath.length());
        fullPath += "target/generated-sources/annotations";

        try {
            ClassName superclass = ClassName.bestGuess(className);
            ClassName superinterface = ClassName.bestGuess("in.aprilfish.mybatis.provider.OpEntity");
            FieldSpec entityField = FieldSpec.builder(superclass, "entity", Modifier.PRIVATE).build();
            TypeSpec opEntity = TypeSpec.classBuilder("Op" + clz)
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(superinterface)
                    .superclass(superclass)
                    .addField(entityField)
                    .addMethod(setEntity(clz))
                    .addMethod(isNotNull(classSymbol))
                    .build();

            JavaFile javaFile = JavaFile.builder(pkg, opEntity)
                    .build();

            generateToAnnotations(javaFile, fullPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MethodSpec setEntity(String simpleName) {
        return MethodSpec.methodBuilder("setEntity").addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Object.class, "obj")
                .returns(void.class)
                .addStatement("this.entity = ($L)obj", simpleName)
                .build();
    }

    private MethodSpec isNotNull(Symbol.ClassSymbol classSymbol) throws Exception {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("isNotNull").addAnnotation(Override.class);

        builder.addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "property")
                .returns(boolean.class)
                .addStatement("if(property==null || property.trim().equals(\"\")) return false");

        this.batchAddStatement(builder, classSymbol);

        builder.addStatement("return false");

        return builder.build();
    }

    private void batchAddStatement(MethodSpec.Builder builder, Symbol.ClassSymbol classSymbol) throws Exception {
        List<Symbol.VarSymbol> varSymbolList = this.getMember(Symbol.VarSymbol.class, ElementKind.FIELD, classSymbol);
        for (Symbol.VarSymbol symbol : varSymbolList) {
            builder.beginControlFlow("if(property.equals(\"$L\"))", symbol.name);
            builder.addStatement("return entity.get$L()!=null", captureName(symbol.name.toString()));
            builder.endControlFlow();
        }
    }

    private <T extends Symbol> List<T> getMember(Class<T> type, ElementKind kind, Symbol classSymbol) {
        List<T> results = new ArrayList<>();
        if (classSymbol.type == null || classSymbol.type.isPrimitiveOrVoid()) {
            return results;
        }
        for (Type t : types.closure(classSymbol.type)) {
            Scope scope = t.tsym.members();
            if (scope == null) continue;
            scope.getElements(symbol -> symbol.getKind() == kind)
                    .forEach(s -> results.add(type.cast(s)));
        }
        if (classSymbol.owner != null && classSymbol != classSymbol.owner
                && classSymbol.owner instanceof Symbol.ClassSymbol) {
            results.addAll(getMember(type, kind, classSymbol.owner));
        }
        if (classSymbol.type.getEnclosingType() != null && classSymbol.hasOuterInstance()) {
            results.addAll(getMember(type, kind, classSymbol.type.getEnclosingType().asElement()));
        }
        return results;
    }

    //首字母大写
    private String captureName(String name) {
        char[] cs = name.toCharArray();
        cs[0] -= 32;
        return String.valueOf(cs);
    }

    private static void generateToAnnotations(JavaFile javaFile, String path) throws IOException {
        File dir = new File(path);
        if (!dir.exists()) dir.mkdirs();

        javaFile.writeTo(dir);
    }

}
