package in.aprilfish.mybatis.query;

import java.util.ArrayList;
import java.util.List;

public class Example {

    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria = new ArrayList<>();

    public Example() {
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void setOredCriteria(List<Criteria> oredCriteria) {
        this.oredCriteria = oredCriteria;
    }

    /**
     * inner class Criteria
     */
    protected class Criteria {
        protected List<Criterion> criterions = new ArrayList<>();

        protected Criteria() {
        }

        public boolean isValid() {
            return criterions.size() > 0;
        }

        public List<Criterion> getAllCriterions() {
            return criterions;
        }

        public List<Criterion> getCriterions() {
            return criterions;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criterions.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criterions.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criterions.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIsNull(FieldPath field) {
            addCriterion(field.getColumn() + " is null");
            return this;
        }

        public Criteria andIsNotNull(FieldPath field) {
            addCriterion(field.getColumn() + " is not null");
            return this;
        }

        public Criteria andEqualTo(FieldPath field, Object value) {
            addCriterion(field.getColumn() + "=", value, field.getProperty());
            return this;
        }

        public Criteria andNotEqualTo(FieldPath field, Object value) {
            addCriterion(field.getColumn() + "<>", value, field.getProperty());
            return this;
        }

        public Criteria andGreaterThan(FieldPath field, Object value) {
            addCriterion(field.getColumn() + ">", value, field.getProperty());
            return this;
        }

        public Criteria andGreaterThanOrEqualTo(FieldPath field, Object value) {
            addCriterion(field.getColumn() + ">=", value, field.getProperty());
            return this;
        }

        public Criteria andLessThan(FieldPath field, Object value) {
            addCriterion(field.getColumn() + "<", value, field.getProperty());
            return this;
        }

        public Criteria andLessThanOrEqualTo(FieldPath field, Object value) {
            addCriterion(field.getColumn() + "<=", value, field.getProperty());
            return this;
        }

        public Criteria andIn(FieldPath field, Iterable values) {
            addCriterion(field.getColumn() + " in ", values, field.getProperty());
            return this;
        }

        public Criteria andNotIn(FieldPath field, Iterable values) {
            addCriterion(field.getColumn() + " not in ", values, field.getProperty());
            return this;
        }

        public Criteria andBetween(FieldPath field, Object value1, Object value2) {
            addCriterion(field.getColumn() + " between", value1, value2, field.getProperty());
            return this;
        }

        public Criteria andNotBetween(FieldPath field, Object value1, Object value2) {
            addCriterion(field.getColumn() + " not between", value1, value2, field.getProperty());
            return this;
        }

        public Criteria andLike(FieldPath field, String value) {
            addCriterion(field.getColumn() + " like", value, field.getProperty());
            return this;
        }

        public Criteria andNotLike(FieldPath field, String value) {
            addCriterion(field.getColumn() + " not like", value, field.getProperty());
            return this;
        }

    }

    /**
     * inner class Criterion
     */
    public class Criterion {

        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }

    /**
     * inner class FieldPath
     * @param <T>
     */
    public class FieldPath<T extends Example> {

        private T root;

        private Criteria criteria;

        private String property;

        private String column;

        private String javaType;

        public FieldPath(T root, Criteria criteria, String property, String column) {
            this.root = root;
            this.criteria = criteria;
            this.property = property;
            this.column = column;
        }

        public FieldPath setCriteria(Criteria criteria) {
            this.criteria = criteria;

            return this;
        }

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }

        public String getColumn() {
            return column;
        }

        public void setColumn(String column) {
            this.column = column;
        }

        public String getJavaType() {
            return javaType;
        }

        public void setJavaType(String javaType) {
            this.javaType = javaType;
        }

        public T isNull() {
            criteria.andIsNull(this);
            return this.root;
        }

        public T isNotNull() {
            criteria.andIsNotNull(this);
            return this.root;
        }

        public T eq(Object val) {
            criteria.andEqualTo(this, val);
            return this.root;
        }

        public T notEq(Object val) {
            criteria.andNotEqualTo(this, val);
            return this.root;
        }

        public T like(Object val) {
            criteria.andLike(this, "%" + val + "%");
            return this.root;
        }

        public T notLike(Object val) {
            criteria.andNotLike(this, "%" + val + "%");
            return this.root;
        }

        public T greaterThan(Object val) {
            criteria.andGreaterThan(this, val);
            return this.root;
        }

        public T greaterOrEq(Object val) {
            criteria.andGreaterThanOrEqualTo(this, val);
            return this.root;
        }

        public T lessThan(Object val) {
            criteria.andLessThan(this, val);
            return this.root;
        }

        public T lessOrEq(Object val) {
            criteria.andLessThanOrEqualTo(this, val);
            return this.root;
        }

        public T in(List values) {
            criteria.andIn(this, values);
            return this.root;
        }

        public T notIn(List<Object> values) {
            criteria.andNotIn(this, values);
            return this.root;
        }

        public T between(Object value1, Object value2) {
            criteria.andBetween(this, value1, value2);
            return this.root;
        }

        public T notBetween(Object value1, Object value2) {
            criteria.andNotBetween(this, value1, value2);
            return this.root;
        }
    }
}
