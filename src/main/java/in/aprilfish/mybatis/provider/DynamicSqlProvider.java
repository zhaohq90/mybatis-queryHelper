package in.aprilfish.mybatis.provider;

import in.aprilfish.mybatis.example.Example;
import in.aprilfish.mybatis.util.PlaceholderResolver;
import in.aprilfish.mybatis.util.ReflectionUtils;
import in.aprilfish.mybatis.util.StrKit;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.jdbc.SQL;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class DynamicSqlProvider {
    /**
     * key -> mapper class   value -> tableInfo
     */
    private static Map<Class<?>, TableInfo> tableCache = new ConcurrentHashMap<>(128);

    /**
     * 获取表信息结构
     *
     * @param context provider context
     * @return 表基本信息
     */
    private TableInfo tableInfo(ProviderContext context) {
        // 如果不存在则创建
        return tableCache.computeIfAbsent(context.getMapperType(), TableInfo::of);
    }

    public String insert(ProviderContext context) {
        TableInfo table = tableInfo(context);

        return new SQL()
                .INSERT_INTO(table.getTableName())
                .INTO_COLUMNS(table.getColumns())
                .INTO_VALUES(Stream.of(table.getFields()).map(TableInfo::bindParameter).toArray(String[]::new))
                .toString();
    }

    public String insertSelective(Object entity, ProviderContext context) {
        TableInfo table = tableInfo(context);

        Field[] notNullFields = Stream.of(table.getFields())
                .filter(field -> ReflectionUtils.getFieldValue(field, entity) != null && !table.getPrimaryKeyColumn().equals(TableInfo.columnName(field)))
                .toArray(Field[]::new);

        return new SQL()
                .INSERT_INTO(table.getTableName())
                .INTO_COLUMNS(TableInfo.columns(notNullFields))
                .INTO_VALUES(Stream.of(notNullFields).map(TableInfo::bindParameter).toArray(String[]::new))
                .toString();
    }

    public String batchInsert(Map<String, Object> param, ProviderContext context) {
        TableInfo table = tableInfo(context);
        @SuppressWarnings("unchecked")
        int size = ((List<Object>) param.get("entities")).size();
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

    public String selectByPrimaryKey(ProviderContext context) {
        TableInfo table = tableInfo(context);

        return new SQL()
                .SELECT(table.getSelectColumns())
                .FROM(table.getTableName())
                .WHERE(table.getPrimaryKeyWhere())
                .toString();
    }

    public String selectByPrimaryKeyIn(Map<String, Object> params, ProviderContext context) {
        @SuppressWarnings("unchecked")
        List<Object> ids = (List<Object>) params.get("ids");
        TableInfo table = tableInfo(context);
        return new SQL()
                .SELECT(table.getSelectColumns())
                .FROM(table.getTableName())
                .WHERE(table.getPrimaryKeyColumn()
                        + " IN (" + String.join(",", ids.stream().map(String::valueOf).toArray(String[]::new)) + ")")
                .toString();
    }

    /**
     * @param orderBy 排序字段
     * @param context context
     * @return sql
     */
    public String selectAll(String orderBy, ProviderContext context) {
        TableInfo table = tableInfo(context);
        SQL sql = new SQL()
                .SELECT(table.getSelectColumns())
                .FROM(table.getTableName());
        if (StrKit.isEmpty(orderBy)) {
            orderBy = table.getPrimaryKeyColumn() + " DESC";
        }
        return sql.ORDER_BY(orderBy).toString();
    }

    /**
     * @param criteria entity 条件
     * @param context  context
     * @return sql
     */
    public String selectByCriteria(Object criteria, ProviderContext context) {
        TableInfo table = tableInfo(context);
        return new SQL()
                .SELECT(table.getSelectColumns())
                .FROM(table.getTableName())
                .WHERE(Stream.of(table.getFields())
                        .filter(field -> ReflectionUtils.getFieldValue(field, criteria) != null)
                        .map(TableInfo::assignParameter)
                        .toArray(String[]::new)).ORDER_BY(table.getPrimaryKeyColumn() + " DESC").toString();
    }

    /**
     * @param criteria entity 条件
     * @param context  context
     * @return sql
     */
    public String count(Object criteria, ProviderContext context) {
        TableInfo table = tableInfo(context);
        return new SQL()
                .SELECT("COUNT(*)")
                .FROM(table.getTableName())
                .toString();
    }

    /**
     * @param criteria entity 条件
     * @param context  context
     * @return sql
     */
    public String countByCriteria(Object criteria, ProviderContext context) {
        TableInfo table = tableInfo(context);
        return new SQL()
                .SELECT("COUNT(*)")
                .FROM(table.getTableName())
                .WHERE(Stream.of(table.getFields())
                        .filter(field -> ReflectionUtils.getFieldValue(field, criteria) != null)
                        .map(TableInfo::assignParameter).toArray(String[]::new))
                .toString();
    }

    public String updateByPrimaryKey(ProviderContext context) {
        TableInfo table = tableInfo(context);

        return new SQL()
                .UPDATE(table.getTableName())
                .SET(Stream.of(table.getFields())
                        .filter(field -> !table.getPrimaryKeyColumn().equals(TableInfo.columnName(field)))
                        .map(TableInfo::assignParameter).toArray(String[]::new))
                .WHERE(table.getPrimaryKeyWhere())
                .toString();
    }

    public String updateSelective(Object entity, ProviderContext context) {
        TableInfo table = tableInfo(context);

        return new SQL()
                .UPDATE(table.getTableName())
                .SET(Stream.of(table.getFields())
                        //.filter(field -> ReflectionUtils.getFieldValue(field, entity) != null && !table.getPrimaryKeyColumn().equals(TableInfo.columnName(field)))
                        .filter(field -> table.isNotNull(entity,field) && !table.isPrimaryKey(field))
                        .map(TableInfo::assignParameter).toArray(String[]::new))
                .WHERE(table.getPrimaryKeyWhere())
                .toString();
    }

    public String fakeDeleteByPrimaryKey(ProviderContext context) {
        TableInfo table = tableInfo(context);

        return new SQL()
                .UPDATE(table.getTableName())
                .SET("is_deleted = 1")
                .WHERE(table.getPrimaryKeyColumn() + " = #{id}")
                .toString();
    }

    public String deleteByPrimaryKey(ProviderContext context) {
        TableInfo table = tableInfo(context);

        return new SQL()
                .DELETE_FROM(table.getTableName())
                .WHERE(table.getPrimaryKeyColumn() + " = #{id}")
                .toString();
    }

    public String deleteByCriteria(Object criteria, ProviderContext context) {
        TableInfo table = tableInfo(context);

        return new SQL()
                .DELETE_FROM(table.getTableName())
                .WHERE(Stream.of(table.getFields())
                        .filter(field -> ReflectionUtils.getFieldValue(field, criteria) != null)
                        .map(TableInfo::assignParameter)
                        .toArray(String[]::new))
                .toString();
    }

    public String selectByExample(Object object, ProviderContext context) {
        Example example=(Example)object;
        TableInfo table = tableInfo(context);
        List<Example.Criteria> oredCriteria=example.getOredCriteria();
        List<List<String>> oredConditions=new ArrayList<>();
        for(Example.Criteria criteria:oredCriteria){
            if(!criteria.isValid()) continue;
            List<String> conditions=new ArrayList<>();
            List<Example.Criterion> criterionList=criteria.getAllCriterions();
            for(Example.Criterion criterion:criterionList){
                String condition=null;
                if(criterion.isNoValue()) condition=criterion.getCondition();
                if(criterion.isSingleValue()) condition=String.join(" ",criterion.getCondition(),criterion.getValue().toString());
                //todo 时间类型格式化 DATE  /DATETIME 格式
                //in date ?
                if(criterion.isBetweenValue()) condition=String.join(" ",criterion.getCondition(),criterion.getValue().toString(),"and",criterion.getSecondValue().toString());
                if(criterion.isListValue()){
                    List<?> list=(List<?>)criterion.getValue();
                    String[] values= list.stream().map(Object::toString).toArray(String[]::new);
                    String value=String.join("","(",String.join(",",values),")");
                    condition=String.join(" ",criterion.getCondition(),value);
                }
                conditions.add(condition);
            }
            oredConditions.add(conditions);
        }

        SQL sql=new SQL().SELECT(String.join(",",table.getColumns())).FROM(table.getTableName());
        for(int i=0;i<oredConditions.size();i++){
            if(i>0) sql.OR();
            sql.WHERE(oredConditions.get(i).toArray(new String[0]));
        }
        if(example.getOrderByClause()!=null) sql.ORDER_BY(example.getOrderByClause());

        return sql.toString();
    }

}
