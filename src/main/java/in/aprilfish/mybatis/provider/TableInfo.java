package in.aprilfish.mybatis.provider;

import in.aprilfish.mybatis.mapper.CommonMapper;
import in.aprilfish.mybatis.util.CollectionUtils;
import in.aprilfish.mybatis.util.ReflectionUtils;
import in.aprilfish.mybatis.util.StrKit;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TableInfo {

    private static Log log=new Slf4jImpl(TableInfo.class.getName());

    /**
     * 表前缀
     */
    public static final String TABLE_PREFIX = "";

    /**
     * 主键名
     */
    public static final String DEFAULT_PRIMARY_KEY = "id";

    /**
     * 表名
     */
    private String tableName;

    /**
     * 实体类型不含@NoColunm注解的field
     */
    private Field[] fields;

    /**
     * 主键列名
     */
    private String primaryKeyColumn;

    /**
     * 所有列名
     */
    private String[] columns;

    /**
     * 所有select sql的列名，有带下划线的将其转为aa_bb AS aaBb
     */
    private String[] selectColumns;

    private OpEntity opEntity;

    private TableInfo() {}

    /**
     * 获取主键的where条件，如 id = #{id}
     *
     * @return  主键where条件
     */
    public String getPrimaryKeyWhere() {
        String pk = this.primaryKeyColumn;
        return pk + " = #{" + StrKit.removeDelimiter(pk) + "}";
    }

    /**
     * 获取TableInfo的简单工厂
     *
     * @param mapperType mapper类型
     * @return            {@link TableInfo}
     */
    public static TableInfo of(Class<?> mapperType) {
        Class<?> entityClass = entityType(mapperType);
        // 获取不含有@Transient注解的fields
        Field[] fields = excludeTransientField(entityClass);
        TableInfo tableInfo = new TableInfo();
        tableInfo.fields = fields;
        tableInfo.tableName = tableName(entityClass);
        tableInfo.primaryKeyColumn =  primaryKeyColumn(fields);
        tableInfo.columns = columns(fields);
        tableInfo.selectColumns = selectColumns(fields);
        tableInfo.opEntity = opEntity(entityClass.getPackage().getName(), entityClass.getSimpleName());
        return tableInfo;
    }

    /**
     * 获取BaseMapper接口中的泛型类型
     *
     * @param mapperType  mapper类型
     * @return       实体类型
     */
    public static Class<?> entityType(Class<?> mapperType) {
        return Stream.of(mapperType.getGenericInterfaces())
                .filter(ParameterizedType.class::isInstance)
                .map(ParameterizedType.class::cast)
                .filter(type -> type.getRawType() == CommonMapper.class)
                .findFirst()
                .map(type -> type.getActualTypeArguments()[1])
                .filter(Class.class::isInstance).map(Class.class::cast)
                .orElseThrow(() -> new IllegalStateException("未找到BaseMapper的泛型类 " + mapperType.getName() + "."));
    }


    /**
     * 获取表名
     *
     * @param entityType  实体类型
     * @return      表名
     */
    public static String tableName(Class<?> entityType) {
        Table table = entityType.getAnnotation(Table.class);
        return table == null ? TABLE_PREFIX + StrKit.camel2Underscore(entityType.getSimpleName()) : table.name();
    }

    /**
     * 过滤含有@Transient注解或者是静态的field
     *
     * @param entityClass 实体类型
     * @return 不包含@Transient注解的fields
     */
    public static Field[] excludeTransientField(Class<?> entityClass) {
        Field[] allFields = ReflectionUtils.getFields(entityClass);
        List<String> excludeColumns = getClassExcludeColumns(entityClass);
        return Stream.of(allFields)
                //过滤掉类上指定的@Transient注解的字段和字段上@Transient注解或者是静态的field
                .filter(field -> !CollectionUtils.contains(excludeColumns, field.getName())
                        && (!field.isAnnotationPresent(Transient.class) && !Modifier.isStatic(field.getModifiers())))
                .toArray(Field[]::new);
    }

    /**
     * 获取实体类上标注的不需要映射的字段名
     *
     * @param entityClass  实体类
     * @return             不需要映射的字段名
     */
    public static List<String> getClassExcludeColumns(Class<?> entityClass) {
        List<String> excludeColumns = new ArrayList<>();
        Field[] fields = entityClass.getFields();
        for(Field field : fields){
            boolean hasAnnotation = field.isAnnotationPresent(Transient.class);
            if(hasAnnotation){
                excludeColumns.add(field.getName());

                log.debug(String.format("exclude column %s",field.getName()));
            }
        }

        return excludeColumns;
    }

    /**
     * 获取查询对应的字段 (不包含pojo中含有@NoColumn主键的属性)
     *
     * @param fields p
     * @return  所有需要查询的查询字段
     */
    public static String[] selectColumns(Field[] fields) {
        return Stream.of(fields).map(TableInfo::selectColumnName).toArray(String[]::new);
    }

    public static OpEntity opEntity(String packageName,String simpleName){
        String className=packageName+".Op"+simpleName;
        try {
            System.out.println("build OpEntity");
            Class clazz= Class.forName(className);
            return (OpEntity)clazz.newInstance();
        }catch (Exception e){
            //e.printStackTrace();
            System.out.println(String.format("%s OpEntity not exist, ignore",simpleName));
        }

        return null;
    }

    /**
     * 获取所有pojo所有属性对应的数据库字段 (不包含pojo中含有@NoColumn主键的属性)
     *
     * @param fields entityClass所有fields
     * @return        所有的column名称
     */
    public static String[] columns(Field[] fields) {
        return Stream.of(fields).map(TableInfo::columnName).toArray(String[]::new);
    }

    /**
     * 如果fields中含有@Primary的字段，则返回该字段名为主键，否则默认'id'为主键名
     *
     * @param fields entityClass所有fields
     * @return 主键column(驼峰转为下划线)
     */
    public static String primaryKeyColumn(Field[] fields) {
        return Stream.of(fields).filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()    //返回第一个primaryKey的field
                .map(TableInfo::columnName)
                .orElse(DEFAULT_PRIMARY_KEY);
    }

    /**
     * 获取单个属性对应的数据库字段（带有下划线字段将其转换为"字段 AS pojo属性名"形式）
     *
     * @param field  字段
     * @return      带有下划线字段将其转换为"字段 AS pojo属性名"形式
     */
    public static String selectColumnName(Field field) {
        return columnName(field);
        //String camel = columnName(field);
        //return camel.contains("_") ? camel + " AS `" + field.getName() + "`" : camel;
    }

    /**
     * 获取单个属性对应的数据库字段
     *
     * @param field entityClass中的field
     * @return  字段对应的column
     */
    public static String columnName(Field field) {
        return "`" + StrKit.camel2Underscore(field.getName()) + "`";
    }

    /**
     * 绑定参数
     *
     * @param field  字段
     * @return        参数格式
     */
    public static String bindParameter(Field field) {
        return "#{" + field.getName() + "}";
    }

    /**
     * 获取该字段的参数赋值语句，如 user_name = #{userName}
     * @param field  字段
     * @return       参数赋值语句
     */
    public static String assignParameter(Field field) {
        return columnName(field) + " = " + bindParameter(field);
    }

    public String getTableName() {
        return tableName;
    }

    public Field[] getFields() {
        return fields;
    }

    public String getPrimaryKeyColumn() {
        return primaryKeyColumn;
    }

    public String[] getColumns() {
        return columns;
    }

    public String[] getSelectColumns() {
        return selectColumns;
    }

    public boolean isPrimaryKey(Field field){
        return this.primaryKeyColumn.equals(TableInfo.columnName(field));
    }

    public boolean isNotNull(Object entity, Field field){
        if(opEntity==null){
            return ReflectionUtils.getFieldValue(field, entity)!=null;
        }

        //opEntity.setEntity(entity);

        return opEntity.isNotNull(entity, field.getName());
    }

}