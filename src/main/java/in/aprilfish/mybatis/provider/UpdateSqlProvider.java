package in.aprilfish.mybatis.provider;

import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.jdbc.SQL;

import java.util.stream.Stream;

public  class UpdateSqlProvider extends BaseSqlProviderSupport {
    /**
     * sql
     * @param context context
     * @return  sql
     */
    public String sql(ProviderContext context) {
        TableInfo table = tableInfo(context);

        return new SQL()
                .UPDATE(table.getTableName())
                .SET(Stream.of(table.getFields())
                        .filter(field -> !table.getPrimaryKeyColumn().equals(TableInfo.columnName(field)))
                        .map(TableInfo::assignParameter).toArray(String[]::new))
                .WHERE(table.getPrimaryKeyWhere())
                .toString();
    }
}
