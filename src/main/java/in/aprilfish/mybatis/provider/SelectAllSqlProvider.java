package in.aprilfish.mybatis.provider;

import in.aprilfish.mybatis.util.StringUtils;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.jdbc.SQL;

public class SelectAllSqlProvider extends BaseSqlProviderSupport {
    /**
     * sql
     * @param orderBy  排序字段
     * @param context context
     * @return  sql
     */
    public String sql(String orderBy, ProviderContext context) {
        TableInfo table = tableInfo(context);
        SQL sql = new SQL()
                .SELECT(table.getSelectColumns())
                .FROM(table.getTableName());
        if (StringUtils.isEmpty(orderBy)) {
            orderBy = table.getPrimaryKeyColumn() + " DESC";
        }
        return sql.ORDER_BY(orderBy).toString();
    }
}