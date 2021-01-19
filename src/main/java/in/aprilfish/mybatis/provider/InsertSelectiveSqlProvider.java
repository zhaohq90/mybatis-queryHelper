package in.aprilfish.mybatis.provider;

import in.aprilfish.mybatis.util.ReflectionUtils;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.jdbc.SQL;

import java.lang.reflect.Field;
import java.util.stream.Stream;

public class InsertSelectiveSqlProvider extends BaseSqlProviderSupport {
    /**
     * sql
     * @param entity  entity
     * @param context context
     * @return  sql
     */
    public String sql(Object entity, ProviderContext context) {
        TableInfo table = tableInfo(context);

        Field[] notNullFields = Stream.of(table.getFields())
                .filter(field -> ReflectionUtils.getFieldValue(field, entity) != null && !table.getPrimaryKeyColumn().equals(TableInfo.columnName(field)))
                .toArray(Field[]::new);

        return new SQL()
                .INSERT_INTO(table.getTableName())
                .INTO_COLUMNS(TableInfo.columns(notNullFields))
                .INTO_VALUES(Stream.of(notNullFields).map(TableInfo::bindParameter).toArray(String[]::new))
                .toString();

    }
}