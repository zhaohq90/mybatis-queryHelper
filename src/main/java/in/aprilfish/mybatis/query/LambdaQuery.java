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
		String column = this.HumpToUnderline(property);

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

	private String HumpToUnderline(String para){
		StringBuilder sb=new StringBuilder(para);
		int temp=0;//定位
		if (!para.contains("_")) {
			for(int i=0;i<para.length();i++){
				if(Character.isUpperCase(para.charAt(i))){
					sb.insert(i+temp, "_");
					temp+=1;
				}
			}
		}

		return sb.toString().toLowerCase();
	}

}
