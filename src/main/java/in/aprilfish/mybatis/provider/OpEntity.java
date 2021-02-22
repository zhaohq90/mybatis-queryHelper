package in.aprilfish.mybatis.provider;

public interface OpEntity<T> {

    //void setEntity(Object obj);

    boolean isNotNull(T entity, String property);

}
