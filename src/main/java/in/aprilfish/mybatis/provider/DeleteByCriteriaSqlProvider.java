package in.aprilfish.mybatis.provider;

import in.aprilfish.mybatis.util.ReflectionUtils;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.jdbc.SQL;

import java.util.stream.Stream;

public class DeleteByCriteriaSqlProvider extends BaseSqlProviderSupport {
    /**
     * sql
     * @param criteria  entity condition
     * @param context context
     * @return  sql
     */
    public String sql(Object criteria, ProviderContext context) {
        TableInfo table = tableInfo(context);

        return new SQL()
                .DELETE_FROM(table.getTableName())
                .WHERE(Stream.of(table.getFields())
                        .filter(field -> ReflectionUtils.getFieldValue(field, criteria) != null)
                        .map(TableInfo::assignParameter)
                        .toArray(String[]::new))
                .toString();
    }
}