package in.aprilfish.mybatis.utils;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LambdaUtils {

    private static final Map<Class<?>, SerializedLambda> LAMBDA_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, ConcurrentHashMap<String, String>> COLUMN_CACHE = new HashMap<>();

    /**
     * 根据方法引用获取字段名
     *
     * @param func 方法引用
     * @param <T>  入参类型
     * @param <R>  返回值类型
     */
    public static <T, R> String getProperty(SFunction<T, R> func) {
        SerializedLambda serializedLambda = doSFunction(func);

        ConcurrentHashMap<String, String> columnMap = COLUMN_CACHE.get(serializedLambda.getImplClass());
        if (columnMap == null) {
            synchronized (LambdaUtils.class) {
                if (columnMap == null) {
                    columnMap = new ConcurrentHashMap<>();
                    COLUMN_CACHE.put(serializedLambda.getImplClass(), columnMap);
                }
            }
        }

        String methodName = serializedLambda.getImplMethodName();
        String fieldName = columnMap.get(methodName);
        if (fieldName == null) {
            fieldName = methodName.substring(3);
            fieldName = toLowerCaseFirstOne(fieldName);
            columnMap.put(methodName, fieldName);
        }

        return fieldName;
    }

    /**
     * 通过反射调用writeReplace，获取SerializedLambda类
     *
     * @param func 方法引用
     * @param <T>  入参类型
     * @param <R>  返回值类型
     */
    public static <T, R> SerializedLambda doSFunction(SFunction<T, R> func) {
        SerializedLambda serializedLambda = LAMBDA_CACHE.get(func.getClass());
        if (serializedLambda != null) {
            return serializedLambda;
        }

        try {
            // 直接调用writeReplace
            Method writeReplace;
            writeReplace = func.getClass().getDeclaredMethod("writeReplace");

            writeReplace.setAccessible(true);
            //反射调用
            Object sl = writeReplace.invoke(func);
            serializedLambda = (SerializedLambda) sl;

            LAMBDA_CACHE.put(func.getClass(), serializedLambda);

            return serializedLambda;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    //首字母转小写
    public static String toLowerCaseFirstOne(String s) {
        if (Character.isLowerCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
    }

}
