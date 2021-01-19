package in.aprilfish.mybatis.provider;

import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.jdbc.SQL;

import java.util.List;
import java.util.Map;

public class SelectByPrimaryKeyInSqlProvider extends BaseSqlProviderSupport {
    public String sql(Map<String, Object> params, ProviderContext context) {
        @SuppressWarnings("unchecked")
        List<Object> ids = (List<Object>)params.get("ids");
        TableInfo table = tableInfo(context);
        return new SQL()
                .SELECT(table.getSelectColumns())
                .FROM(table.getTableName())
                .WHERE(table.getPrimaryKeyColumn()
                        + " IN (" + String.join(",", ids.stream().map(String::valueOf).toArray(String[]::new)) +")")
                .toString();
    }
}