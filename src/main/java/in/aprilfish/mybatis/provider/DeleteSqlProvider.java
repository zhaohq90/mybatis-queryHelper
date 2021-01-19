package in.aprilfish.mybatis.provider;

import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.jdbc.SQL;

public class DeleteSqlProvider extends BaseSqlProviderSupport {
    public String sql(ProviderContext context) {
        TableInfo table = tableInfo(context);

        return new SQL()
                .DELETE_FROM(table.getTableName())
                .WHERE(table.getPrimaryKeyColumn() + " = #{id}")
                .toString();
    }
}