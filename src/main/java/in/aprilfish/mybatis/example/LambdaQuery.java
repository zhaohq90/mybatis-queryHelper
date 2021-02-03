package in.aprilfish.mybatis.example;

import in.aprilfish.mybatis.util.LambdaUtils;
import in.aprilfish.mybatis.util.SFunction;
import in.aprilfish.mybatis.util.StrKit;

import java.util.Arrays;

public class LambdaQuery extends Example {

    private Criteria criteria = super.createCriteria();

    public <T, R> LambdaQuery eq(SFunction<T, R> func, Object val) {
        return eq(true, func, val);
    }

    public <T, R> LambdaQuery eq(boolean condition, SFunction<T, R> func, Object val) {
        return addCondition(condition, func, SqlKeyword.EQ, val);
    }

    public <T, R> LambdaQuery notEquals(SFunction<T, R> func, Object val) {
        return notEquals(true, func, val);
    }

    public <T, R> LambdaQuery notEquals(boolean condition, SFunction<T, R> func, Object val) {
        return addCondition(condition, func, SqlKeyword.NE, val);
    }

    public <T, R> LambdaQuery like(SFunction<T, R> func, Object val) {
        return like(true, func, val);
    }

    public <T, R> LambdaQuery like(boolean condition, SFunction<T, R> func, Object val) {
        return addCondition(condition, func, SqlKeyword.LIKE, val);
    }

    public <T, R> LambdaQuery notLike(SFunction<T, R> func, Object val) {
        return notLike(true, func, val);
    }

    public <T, R> LambdaQuery notLike(boolean condition, SFunction<T, R> func, Object val) {
        return addCondition(condition, func, SqlKeyword.NOT_LIKE, val);
    }

    public <T, R> LambdaQuery in(SFunction<T, R> func, Object val) {
        return in(true, func, val);
    }

    public <T, R> LambdaQuery in(boolean condition, SFunction<T, R> func, Object val) {
        return addCondition(condition, func, SqlKeyword.IN, val);
    }

    public <T, R> LambdaQuery between(SFunction<T, R> func, Object val0, Object val1) {
        return between(true, func, val0, val1);
    }

    public <T, R> LambdaQuery between(boolean condition, SFunction<T, R> func, Object val0, Object val1) {
        return addCondition(condition, func, SqlKeyword.BETWEEN, val0, val1);
    }

    public <T, R> LambdaQuery notBetween(SFunction<T, R> func, Object val0, Object val1) {
        return notBetween(true, func, val0, val1);
    }

    public <T, R> LambdaQuery notBetween(boolean condition, SFunction<T, R> func, Object val0, Object val1) {
        return addCondition(condition, func, SqlKeyword.NOT_BETWEEN, val0, val1);
    }

    public <T, R> LambdaQuery greaterThan(SFunction<T, R> func, Object val) {
        return greaterThan(true, func, val);
    }

    public <T, R> LambdaQuery greaterThan(boolean condition, SFunction<T, R> func, Object val) {
        return addCondition(condition, func, SqlKeyword.GT, val);
    }

    public <T, R> LambdaQuery greaterThanOrEquals(SFunction<T, R> func, Object val) {
        return greaterThanOrEquals(true, func, val);
    }

    public <T, R> LambdaQuery greaterThanOrEquals(boolean condition, SFunction<T, R> func, Object val) {
        return addCondition(condition, func, SqlKeyword.GE, val);
    }

    public <T, R> LambdaQuery lessThan(SFunction<T, R> func, Object val) {
        return lessThan(true, func, val);
    }

    public <T, R> LambdaQuery lessThan(boolean condition, SFunction<T, R> func, Object val) {
        return addCondition(condition, func, SqlKeyword.LT, val);
    }

    public <T, R> LambdaQuery lessThanOrEquals(SFunction<T, R> func, Object val) {
        return lessThanOrEquals(true, func, val);
    }

    public <T, R> LambdaQuery lessThanOrEquals(boolean condition, SFunction<T, R> func, Object val) {
        return addCondition(condition, func, SqlKeyword.LE, val);
    }

    public <T, R> LambdaQuery isNull(SFunction<T, R> func, Object val) {
        return isNull(true, func, val);
    }

    public <T, R> LambdaQuery isNull(boolean condition, SFunction<T, R> func, Object val) {
        return addCondition(condition, func, SqlKeyword.IS_NULL, val);
    }

    public <T, R> LambdaQuery isNotNull(SFunction<T, R> func, Object val) {
        return isNotNull(true, func, val);
    }

    public <T, R> LambdaQuery isNotNull(boolean condition, SFunction<T, R> func, Object val) {
        return addCondition(condition, func, SqlKeyword.IS_NOT_NULL, val);
    }

    private <T, R> LambdaQuery addCondition(boolean condition, SFunction<T, R> func, SqlKeyword keyword, Object... vals) {
        if (!condition) {
            return this;
        }

        String property = LambdaUtils.getProperty(func);
        String column = StrKit.humpToUnderline(property);

        FieldPath<LambdaQuery> fieldPath = new FieldPath<>(this, criteria, property, column);

        if (keyword.equals(SqlKeyword.EQ)) {
            fieldPath.eq(vals[0]);
        } else if (keyword.equals(SqlKeyword.NE)) {
            fieldPath.notEq(vals[0]);
        } else if (keyword.equals(SqlKeyword.LIKE)) {
            fieldPath.like(vals[0]);
        } else if (keyword.equals(SqlKeyword.NOT_LIKE)) {
            fieldPath.notLike(vals[0]);
        } else if (keyword.equals(SqlKeyword.GT)) {
            fieldPath.greaterThan(vals[0]);
        } else if (keyword.equals(SqlKeyword.GE)) {
            fieldPath.greaterOrEq(vals[0]);
        } else if (keyword.equals(SqlKeyword.LT)) {
            fieldPath.lessThan(vals[0]);
        } else if (keyword.equals(SqlKeyword.LE)) {
            fieldPath.lessOrEq(vals[0]);
        } else if (keyword.equals(SqlKeyword.IN)) {
            fieldPath.in(Arrays.asList(vals));
        } else if (keyword.equals(SqlKeyword.NOT_IN)) {
            fieldPath.notIn(Arrays.asList(vals));
        } else if (keyword.equals(SqlKeyword.IS_NULL)) {
            fieldPath.isNull();
        } else if (keyword.equals(SqlKeyword.IS_NOT_NULL)) {
            fieldPath.isNotNull();
        } else if (keyword.equals(SqlKeyword.BETWEEN)) {
            fieldPath.between(vals[0], vals[1]);
        } else if (keyword.equals(SqlKeyword.NOT_BETWEEN)) {
            fieldPath.notBetween(vals[0], vals[1]);
        } else {
            throw new UnsupportedOperationException();
        }

        return this;
    }

    @Override
    public Criteria or() {
        criteria = super.or();

        return criteria;
    }

}
