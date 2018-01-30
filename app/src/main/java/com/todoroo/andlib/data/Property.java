/**
 * Copyright (c) 2012 Todoroo Inc
 *
 * See the file "LICENSE" for the full license governing this code.
 */
package com.todoroo.andlib.data;

import android.database.Cursor;
import android.text.TextUtils;

import com.todoroo.andlib.sql.Criterion;
import com.todoroo.andlib.sql.Field;
import com.todoroo.andlib.sql.Operator;
import com.todoroo.andlib.sql.UnaryCriterion;

import static com.todoroo.andlib.sql.SqlConstants.COMMA;
import static com.todoroo.andlib.sql.SqlConstants.LEFT_PARENTHESIS;
import static com.todoroo.andlib.sql.SqlConstants.RIGHT_PARENTHESIS;
import static com.todoroo.andlib.sql.SqlConstants.SPACE;

/**
 * Property represents a typed column in a database.
 *
 * Within a given database row, the parameter may not exist, in which case the
 * value is null, it may be of an incorrect type, in which case an exception is
 * thrown, or the correct type, in which case the value is returned.
 *
 * @author Tim Su <tim@todoroo.com>
 *
 * @param <TYPE>
 *            a database supported type, such as String or Integer
 */
public abstract class Property<TYPE> extends Field implements Cloneable {

    // --- implementation

    /** The database table name this property */
    public final Table table;

    /** The database column name for this property */
    public final String name;

    /**
     * Create a property by table and column name. Uses the default property
     * expression which is derived from default table name
     */
    Property(Table table, String columnName) {
        this(table, columnName, (table == null) ? (columnName) : (table.name() + "." + columnName));
    }

    /**
     * Create a property by table and column name, manually specifying an
     * expression to use in SQL
     */
    Property(Table table, String columnName, String expression) {
        super(expression);
        this.table = table;
        this.name = columnName;
    }

    /**
     * Accept a visitor
     */
    abstract public TYPE getValue(Cursor data);

    /**
     * Return a clone of this property
     */
    @Override
    public Property<TYPE> clone() {
        try {
            return (Property<TYPE>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return a clone of this property
     */
    Property<TYPE> cloneAs(String tableAlias, String columnAlias) {
        Table aliasedTable = this.table;
        if (!TextUtils.isEmpty(tableAlias)) {
            aliasedTable = table.as(tableAlias);
        }

        try {
            Property<TYPE> newInstance = this.getClass().getConstructor(Table.class, String.class).newInstance(aliasedTable, this.name);
            if(!TextUtils.isEmpty(columnAlias)) {
                return (Property<TYPE>) newInstance.as(columnAlias);
            }
            return newInstance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // --- children

    /**
     * Integer property type. See {@link Property}
     *
     * @author Tim Su <tim@todoroo.com>
     *
     */
    public static class IntegerProperty extends Property<Integer> {

        public IntegerProperty(Table table, String name) {
            super(table, name);
        }

        IntegerProperty(String name, String expression) {
            super(null, name, expression);
        }

        @Override
        public Integer getValue(Cursor data) {
            return data.getInt(data.getColumnIndexOrThrow(getColumnName()));
        }

        @Override
        public IntegerProperty as(String newAlias) {
            return (IntegerProperty) super.as(newAlias);
        }

        @Override
        public IntegerProperty cloneAs(String tableAlias, String columnAlias) {
            return this.cloneAs(tableAlias, columnAlias);
        }
    }

    /**
     * String property type. See {@link Property}
     *
     * @author Tim Su <tim@todoroo.com>
     *
     */
    public static class StringProperty extends Property<String> {

        public StringProperty(Table table, String name) {
            super(table, name);
        }

        @Override
        public String getValue(Cursor data) {
            return data.getString(data.getColumnIndexOrThrow(getColumnName()));
        }

        @Override
        public StringProperty as(String newAlias) {
            return (StringProperty) super.as(newAlias);
        }

        @Override
        public StringProperty cloneAs(String tableAlias, String columnAlias) {
            return (StringProperty) super.cloneAs(tableAlias, columnAlias);
        }

        public Criterion in(final String[] value) {
            final Field field = this;
            return new Criterion(Operator.in) {

                @Override
                protected void populate(StringBuilder sb) {
                    sb.append(field).append(SPACE).append(Operator.in).append(SPACE).append(LEFT_PARENTHESIS).append(SPACE);
                    for (String s : value) {
                        sb.append("'").append(UnaryCriterion.sanitize(s)).append("'").append(COMMA);
                    }
                    sb.deleteCharAt(sb.length() - 1).append(RIGHT_PARENTHESIS);
                }
            };
        }
    }

    /**
     * Long property type. See {@link Property}
     *
     * @author Tim Su <tim@todoroo.com>
     *
     */
    public static class LongProperty extends Property<Long> {

        public LongProperty(Table table, String name) {
            super(table, name);
        }

        @Override
        public Long getValue(Cursor data) {
            return data.getLong(data.getColumnIndexOrThrow(getColumnName()));
        }

        @Override
        public LongProperty as(String newAlias) {
            return (LongProperty) super.as(newAlias);
        }

        @Override
        public LongProperty cloneAs(String tableAlias, String columnAlias) {
            return (LongProperty) super.cloneAs(tableAlias, columnAlias);
        }
    }

    public String getColumnName() {
        if (hasAlias()) {
            return alias;
        }
        return name;
    }

    // --- pseudo-properties

    /** Runs a SQL function and returns the result as a string */
    public static class CountProperty extends IntegerProperty {
        public CountProperty() {
            super("count", "COUNT(1)");
            alias = "count";
        }
    }
}
