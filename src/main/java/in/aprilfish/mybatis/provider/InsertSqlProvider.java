package in.aprilfish.mybatis.provider;

import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.jdbc.SQL;

import java.util.stream.Stream;

public
class InsertSqlProvider extends BaseSqlProviderSupport {
    /**
     * sql
     * @param context context
     * @return  sql
     */
    public String sql(ProviderContext context) {
        TableInfo table = tableInfo(context);

        return new SQL()
                .INSERT_INTO(table.getTableName())
                .INTO_COLUMNS(table.getColumns())
                .INTO_VALUES(Stream.of(table.getFields()).map(TableInfo::bindParameter).toArray(String[]::new))
                .toString();

    }

}