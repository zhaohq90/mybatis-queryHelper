package in.aprilfish.mybatis.query;

import in.aprilfish.mybatis.utils.LambdaUtils;
import in.aprilfish.mybatis.utils.SFunction;

public class LambdaQuery extends Example {

	private Criteria criteria = super.createCriteria();

	public <T, R> LambdaQuery eq(SFunction<T, R> func, Object val) {
		return eq(true, func, val);
	}

	public <T, R> LambdaQuery eq(boolean condition, SFunction<T, R> func, Object val) {
		return addCondition(condition, func, SqlKeyword.EQ, val);
	}

	public <T, R> LambdaQuery like(SFunction<T, R> func, Object val) {
		return like(true, func, val);
	}

	public <T, R> LambdaQuery like(boolean condition, SFunction<T, R> func, Object val) {
		return addCondition(condition, func, SqlKeyword.LIKE, val);
	}

	public <T, R> LambdaQuery addCondition(boolean condition, SFunction<T, R> func, SqlKeyword keyword, Object val) {
		if (!condition) {
			return this;
		}

		String property = LambdaUtils.getProperty(func);
		//todo
		String column = property;

		FieldPath<LambdaQuery> fieldPath = new FieldPath<>(this, criteria, property, column);

		if (keyword.equals(SqlKeyword.EQ)) {
			fieldPath.eq(val);
		} else if (keyword.equals(SqlKeyword.LIKE)) {
			fieldPath.like(val);
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
