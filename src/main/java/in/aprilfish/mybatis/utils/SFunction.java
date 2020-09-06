package in.aprilfish.mybatis.utils;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 支持序列化的 Function，继承自Function并实现了Serializable接口
 *
 * 继承Serializable接口，lambda合成的类多出writeReplace方法，返回java.lang.invoke.SerializedLambda类，
 * 该类的serializedLambda.getImplMethodName()方法可以返回方法名称
 */
@FunctionalInterface
public interface SFunction<T, R> extends Function<T, R>, Serializable {
}
