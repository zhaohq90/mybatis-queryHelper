package in.aprilfish.mybatis.provider;

import in.aprilfish.mybatis.util.ReflectionUtils;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.jdbc.SQL;

import java.util.stream.Stream;

public class UpdateSelectiveSqlProvider extends BaseSqlProviderSupport {
    /**
     * sql
     * @param entity  entity
     * @param context context
     * @return  sql
     */
    public String sql(Object entity, ProviderContext context) {
        TableInfo table = tableInfo(context);

        return new SQL()
                .UPDATE(table.getTableName())
                .SET(Stream.of(table.getFields())
                        .filter(field -> ReflectionUtils.getFieldValue(field, entity) != null && !table.getPrimaryKeyColumn().equals(TableInfo.columnName(field)))
                        .map(TableInfo::assignParameter).toArray(String[]::new))
                .WHERE(table.getPrimaryKeyWhere())
                .toString();
    }
}