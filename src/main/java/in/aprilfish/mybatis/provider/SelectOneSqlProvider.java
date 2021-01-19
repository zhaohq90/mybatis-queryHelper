package in.aprilfish.mybatis.provider;

import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.jdbc.SQL;

public class SelectOneSqlProvider extends BaseSqlProviderSupport {
    /**
     * sql
     * @param context context
     * @return  sql
     */
    public String sql(ProviderContext context) {
        TableInfo table = tableInfo(context);

        return new SQL()
                .SELECT(table.getSelectColumns())
                .FROM(table.getTableName())
                .WHERE(table.getPrimaryKeyWhere())
                .toString();
    }
}