package in.aprilfish.mybatis.provider;

import in.aprilfish.mybatis.util.ReflectionUtils;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.jdbc.SQL;

import java.util.stream.Stream;

public class SelectByCriteriaSqlProvider extends BaseSqlProviderSupport {
    /**
     * sql
     * @param criteria  entity 条件
     * @param context context
     * @return  sql
     */
    public String sql(Object criteria, ProviderContext context) {
        TableInfo table = tableInfo(context);
        return new SQL()
                .SELECT(table.getSelectColumns())
                .FROM(table.getTableName())
                .WHERE(Stream.of(table.getFields())
                        .filter(field -> ReflectionUtils.getFieldValue(field, criteria) != null)
                        .map(TableInfo::assignParameter)
                        .toArray(String[]::new)).ORDER_BY(table.getPrimaryKeyColumn() + " DESC").toString();
    }
}