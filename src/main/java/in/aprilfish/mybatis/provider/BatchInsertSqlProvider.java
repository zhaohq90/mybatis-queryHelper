package in.aprilfish.mybatis.provider;

import in.aprilfish.mybatis.util.PlaceholderResolver;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.jdbc.SQL;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class BatchInsertSqlProvider extends BaseSqlProviderSupport {
    /**
     * sql
     * @param param  mybatis @Param注解绑定的param map
     * @param context context
     * @return  sql
     */
    public String sql(Map<String, Object> param, ProviderContext context) {
        TableInfo table = tableInfo(context);
        @SuppressWarnings("unchecked")
        int size = ((List<Object>)param.get("entities")).size();
        // 构造 ( #{entities[1-->数组索引].fieldName}, #{entities[1].fieldName2})
        String value = "(" + String.join(",", Stream.of(table.getFields())
                .map(field -> "#{entities[${index}]." + field.getName() + "}").toArray(String[]::new)) + ")";
        String[] values = new String[size];
        Map<String, Object> fillIndex = new HashMap<>(2);
        for (int i = 0; i < size; i++) {
            fillIndex.put("index", i);
            values[i] = PlaceholderResolver.getDefaultResolver().resolveByMap(value, fillIndex);
        }

        SQL sql = new SQL()
                .INSERT_INTO(table.getTableName())
                .INTO_COLUMNS(table.getColumns());
        return sql.toString() + " VALUES " + String.join(",", values);
    }
}