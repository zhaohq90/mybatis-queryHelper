package in.aprilfish.mybatis.mapper;

import in.aprilfish.mybatis.provider.*;
import org.apache.ibatis.annotations.*;

import java.util.List;

//todo version enable mybatis 自动重试  通用mapper实现
//todo  UPDATE tb_account SET `username` = ?, `id` = ? WHERE (id = ?)   updateByPrimaryKeySelective id set取消

/**
 * 通用Mapper，实现基本功能
 *
 * @author meilin.huang
 * @param <I>  主键类型
 * @param <E>  实体类型
 */
public interface BaseMapper<I, E> {

    /**
     * 插入新对象,并返回主键id值(id通过实体获取)
     *
     * @param entity 实体对象
     * @return  影响条数
     */
    @InsertProvider(type = InsertSqlProvider.class, method = "sql")
    @Options(useGeneratedKeys = true, keyColumn = TableInfo.DEFAULT_PRIMARY_KEY)
    int insert(E entity);

    /**
     * 插入新对象（只设置非空字段）,并返回主键id值(id通过实体获取)
     *
     * @param entity 实体对象
     * @return  影响条数
     */
    @InsertProvider(type = InsertSelectiveSqlProvider.class, method = "sql")
    @Options(useGeneratedKeys = true, keyColumn = TableInfo.DEFAULT_PRIMARY_KEY)
    int insertSelective(E entity);

    /**
     * 批量插入实体
     *
     * @param entities  实体列表
     * @return          影响条数
     */
    @InsertProvider(type = BatchInsertSqlProvider.class, method = "sql")
    int batchInsert(@Param("entities") List<E> entities);

    /**
     * 根据主键id更新实体，若实体field为null，则对应数据库的字段也更新为null
     *
     * @param entity  实体对象
     * @return         影响条数
     */
    @UpdateProvider(type = UpdateSqlProvider.class, method = "sql")
    int updateByPrimaryKey(E entity);

    /**
     * 根据主键id更新实体，若实体field为null，则对应数据库的字段不更新
     *
     * @param entity  实体对象
     * @return        影响条数
     */
    @UpdateProvider(type = UpdateSelectiveSqlProvider.class, method = "sql")
    int updateByPrimaryKeySelective(E entity);

    /**
     * 根据主键id删除
     *
     * @param id  id
     * @return  影响条数
     */
    @DeleteProvider(type = DeleteSqlProvider.class, method = "sql")
    int deleteByPrimaryKey(I id);

    /**
     * 伪删除，即将is_deleted字段更新为1
     *
     * @param id id
     * @return  影响条数
     */
    @UpdateProvider(type = FakeDeleteSqlProvider.class, method = "sql")
    int fakeDeleteByPrimaryKey(I id);

    /**
     * 根据实体条件删除
     *
     * @param criteria  实体
     * @return  影响条数
     */
    @DeleteProvider(type = DeleteByCriteriaSqlProvider.class, method = "sql")
    int deleteByCriteria(E criteria);

    /**
     * 根据id查询实体
     *
     * @param id  id
     * @return    实体
     */
    @SelectProvider(type = SelectOneSqlProvider.class, method = "sql")
    E selectByPrimaryKey(I id);

    /**
     * 查询所有实体
     *
     * @param orderBy  排序
     * @return   实体list
     */
    @SelectProvider(type = SelectAllSqlProvider.class, method = "sql")
    List<E> selectAll(String orderBy);

    /**
     * 根据id列表查询实体列表
     * @param ids  id列表
     * @return  list
     */
    @SelectProvider(type = SelectByPrimaryKeyInSqlProvider.class, method = "sql")
    List<E> selectByPrimaryKeyIn(@Param("ids") List<I> ids);

    /**
     * 根据实体条件查询符合条件的实体list
     * @param criteria  条件实体
     * @return          list
     */
    @SelectProvider(type = SelectByCriteriaSqlProvider.class, method = "sql")
    List<E> selectByCriteria(E criteria);

    /**
     * 根据条件查询单个数据
     *
     * @param criteria  实体条件
     * @return          实体对象
     */
    @SelectProvider(type = SelectByCriteriaSqlProvider.class, method = "sql")
    E selectOneByCriteria(E criteria);

    /**
     * 返回实体总数
     *
     * @return  总数
     */
    @SelectProvider(type = CountSqlProvider.class, method = "sql")
    long count();

    /**
     * 根据条件查询符合条件的实体总数
     *
     * @param criteria  实体条件
     * @return    数量
     */
    @SelectProvider(type = CountByCriteriaSqlProvider.class, method = "sql")
    long countByCriteria(E criteria);

}
