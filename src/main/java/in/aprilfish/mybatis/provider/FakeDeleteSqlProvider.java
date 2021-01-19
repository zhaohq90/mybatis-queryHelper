package in.aprilfish.mybatis.provider;

import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.jdbc.SQL;

public class FakeDeleteSqlProvider extends BaseSqlProviderSupport {
    public String sql(ProviderContext context) {
        TableInfo table = tableInfo(context);

        return new SQL()
                .UPDATE(table.getTableName())
                .SET("is_deleted = 1")
                .WHERE(table.getPrimaryKeyColumn() + " = #{id}")
                .toString();
    }
}
