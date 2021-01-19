package in.aprilfish.mybatis.provider;

import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.jdbc.SQL;

public class CountSqlProvider extends BaseSqlProviderSupport {
    /**
     * sql
     * @param criteria  entity 条件
     * @param context context
     * @return  sql
     */
    public String sql(Object criteria, ProviderContext context) {
        TableInfo table = tableInfo(context);
        return new SQL()
                .SELECT("COUNT(*)")
                .FROM(table.getTableName())
                .toString();
    }
}
