package in.aprilfish.mybatis.utils;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;

public class LambdaUtils {

	/**
	 * 根据方法引用获取字段名
	 * @param func 方法引用
	 * @param <T> 入参类型
	 * @param <R> 返回值类型
	 */
	public static <T, R> String getProperty(SFunction<T, R> func) {
		SerializedLambda serializedLambda = doSFunction(func);
		String methodName = serializedLambda.getImplMethodName();

		String fieldName = methodName.substring(3).toLowerCase();
		System.out.println(fieldName);

		return fieldName;
	}

	/**
	 * 通过反射调用writeReplace，获取SerializedLambda类
	 * @param func 方法引用
	 * @param <T> 入参类型
	 * @param <R> 返回值类型
	 */
	public static <T, R> SerializedLambda doSFunction(SFunction<T, R> func) {
		try {
			// 直接调用writeReplace
			Method writeReplace;
			writeReplace = func.getClass().getDeclaredMethod("writeReplace");

			writeReplace.setAccessible(true);
			//反射调用
			Object sl = writeReplace.invoke(func);
			SerializedLambda serializedLambda = (SerializedLambda) sl;
			return serializedLambda;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

}
