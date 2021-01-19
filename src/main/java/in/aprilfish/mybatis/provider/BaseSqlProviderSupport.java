package in.aprilfish.mybatis.provider;

import org.apache.ibatis.builder.annotation.ProviderContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseSqlProviderSupport {
    /**
     * key -> mapper class   value -> tableInfo
     */
    private static Map<Class<?>, TableInfo> tableCache = new ConcurrentHashMap<>(128);

    /**
     * 获取表信息结构
     *
     * @param context  provider context
     * @return  表基本信息
     */
    protected TableInfo tableInfo(ProviderContext context) {
        // 如果不存在则创建
        return tableCache.computeIfAbsent(context.getMapperType(), TableInfo::of);
    }
}
