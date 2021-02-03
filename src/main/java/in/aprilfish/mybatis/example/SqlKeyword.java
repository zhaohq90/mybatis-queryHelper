package in.aprilfish.mybatis.example;

/**
 * SQL 保留关键字枚举
 *
 * @author hubin
 * @since 2018-05-28
 */
public enum SqlKeyword {
	AND("AND"),
	OR("OR"),
	IN("IN"),
	NOT_IN("NOT_IN"),
	NOT("NOT"),
	LIKE("LIKE"),
	NOT_LIKE("NOT_LIKE"),
	EQ("="),
	NE("<>"),
	GT(">"),
	GE(">="),
	LT("<"),
	LE("<="),
	IS_NULL("IS NULL"),
	IS_NOT_NULL("IS NOT NULL"),
	GROUP_BY("GROUP BY"),
	HAVING("HAVING"),
	ORDER_BY("ORDER BY"),
	EXISTS("EXISTS"),
	BETWEEN("BETWEEN"),
	NOT_BETWEEN("NOT_BETWEEN"),
	ASC("ASC"),
	DESC("DESC");

	private final String keyword;

	SqlKeyword(final String keyword) {
		this.keyword = keyword;
	}

	public String getSqlSegment() {
		return this.keyword;
	}

}
