package in.aprilfish.mybatis.proceesor;

import com.squareup.javapoet.*;
import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Context;
import in.aprilfish.mybatis.annotation.Generator;
import org.mybatis.dynamic.sql.BasicColumn;

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
import java.util.Collections;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes("in.aprilfish.mybatis.annotation.Generator")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class GeneratorProcessor extends AbstractProcessor {

    private Filer mFiler;
    private Elements elementUtils;         //工具类
    private Messager messager;
    private Types types;

    public static final String PATH = Generator.class.getCanonicalName();

    public GeneratorProcessor() {
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
        Generator generator = classSymbol.getAnnotation(Generator.class);
        String tableName = generator.name();
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
            ClassName superclass = ClassName.bestGuess("org.mybatis.dynamic.sql.SqlTable");
            ClassName supportClass = ClassName.bestGuess(className + "Support");

            FieldSpec supportField = FieldSpec.builder(supportClass, "SUPPORT", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL).initializer(String.format("new %sSupport()", clz)).build();

            TypeSpec.Builder builder = TypeSpec.classBuilder(clz + "Support")
                    .addModifiers(Modifier.PUBLIC)
                    .superclass(superclass)
                    .addField(supportField)
                    .addMethod(constructor(tableName));

            List<Symbol.VarSymbol> varSymbolList = this.getMember(Symbol.VarSymbol.class, ElementKind.FIELD, classSymbol);
            List<String> columnNameList = new ArrayList<>();
            for (Symbol.VarSymbol symbol : varSymbolList) {
                String type = symbol.type.toString();
                String typePkg = type.substring(0, type.lastIndexOf("."));
                String typeClz = type.substring(type.lastIndexOf(".") + 1);
                TypeName typeName = ParameterizedTypeName.get(ClassName.get("org.mybatis.dynamic.sql", "SqlColumn"), ClassName.get(typePkg, typeClz));
                FieldSpec columnField = FieldSpec.builder(typeName, symbol.name.toString(), Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL).initializer(String.format("SUPPORT.column(\"%s\")", symbol.name.toString())).build();
                builder.addField(columnField);
                columnNameList.add(symbol.name.toString());
            }
            TypeName basicColumnTypeName = ArrayTypeName.of(BasicColumn.class);
            FieldSpec basicColumnField = FieldSpec.builder(basicColumnTypeName, "selectList", Modifier.PUBLIC, Modifier.STATIC).initializer(String.format("BasicColumn.columnList(%s)", String.join(",", columnNameList.toArray(new String[0])))).build();
            builder.addField(basicColumnField);

            builder.addMethod(insertAllColumns(pkg, clz, columnNameList));

            JavaFile javaFile = JavaFile.builder(pkg, builder.build())
                    .build();

            generateToAnnotations(javaFile, fullPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static MethodSpec constructor(String tableName) {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("super(\"$N\")", tableName)
                .build();
    }

    private MethodSpec insertAllColumns(String typePkg, String typeClz, List<String> columnNameList) throws Exception {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("insertAllColumns");
        TypeName typeName = ParameterizedTypeName.get(ClassName.get("org.mybatis.dynamic.sql.insert", "InsertDSL"), ClassName.get(typePkg, typeClz));

        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                //.addParameter(Object.class, "obj")
                .addParameter(typeName, "dsl")
                .returns(typeName);

        for (String columnName : columnNameList) {
            builder.addStatement("dsl.map($N).toProperty(\"$N\")", columnName, columnName);
        }
        builder.addStatement("return dsl");

        return builder.build();
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

        Collections.reverse(results);

        return results;
    }

    private static void generateToAnnotations(JavaFile javaFile, String path) throws IOException {
        File dir = new File(path);
        if (!dir.exists()) dir.mkdirs();

        javaFile.writeTo(dir);
    }

}
