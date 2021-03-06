package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.*;
import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.functions.Function1;
import com.googlecode.totallylazy.functions.Unary;
import com.googlecode.totallylazy.regex.Regex;

import static com.googlecode.lazyrecords.Keywords.qualifier;
import static com.googlecode.lazyrecords.sql.expressions.ColumnReference.columnReference;
import static com.googlecode.lazyrecords.sql.expressions.TableName.tableName;
import static com.googlecode.totallylazy.functions.Functions.identity;
import static com.googlecode.totallylazy.Sequences.sequence;
import static java.lang.String.format;

public class Expressions {
    public static Function1<? super Expression, Iterable<Object>> parameters() {
        return Expression::parameters;
    }

    public static Function1<? super Expression, String> text() {
        return Expression::text;
    }

    public static TextOnlyExpression textOnly(String expression, Object... args) {
        return textOnly(format(expression, args));
    }

    public static TextOnlyExpression textOnly(Object expression) {
        return TextOnlyExpression.textOnly(expression.toString());
    }

    public static ColumnReference columnReference(Keyword<?> keyword) {
        return ColumnReference.columnReference(keyword.name(), keyword.metadata(qualifier));
    }

    public static TableName tableName(Definition definition) {
        return TableName.tableName(definition.name(), definition.metadata(qualifier));
    }

    public static Function1<Keyword<?>, ColumnReference> columnReference() {
        return Expressions::columnReference;
    }

    public static String names(Sequence<Keyword<?>> keywords) {
        return formatList(keywords.map(columnReference()));
    }

    public static String formatList(final Sequence<?> values) {
        return values.toString("(", ",", ")");
    }

    private static final Regex legal = Regex.regex("[a-zA-Z0-9_$*#.@]+");

    public static Unary<String> quote = Expressions::quote;

    public static Function1<String, TextOnlyExpression> quotedText = Expressions::quotedText;

    public static TextOnlyExpression quotedText(String value) {
        return textOnly(quote(value));
    }

    public static String quote(String name) {
        if (Strings.isEmpty(name)) return name;
        if (legal.matches(name)) return name;
        return '"' + name + '"';
    }

    public static AbstractExpression expression(String expression, Object head, Object... tail) {
        return new TextAndParametersExpression(expression, sequence(tail).cons(head));
    }

    public static AbstractExpression expression(String expression, Sequence<Object> parameters) {
        if (parameters.isEmpty()) {
            return textOnly(expression);
        }
        return new TextAndParametersExpression(expression, parameters);
    }

    public static EmptyExpression empty() {
        return new EmptyExpression();
    }

    public static CompoundExpression join(final Expression... expressions) {
        return new CompoundExpression(expressions);
    }

    public static CompoundExpression join(final Sequence<? extends Expression> expressions) {
        return new CompoundExpression(expressions);
    }

    public static CompoundExpression join(final Sequence<? extends Expression> expressions, final String start, final String separator, final String end) {
        return new CompoundExpression(expressions, start, separator, end);
    }

    public static boolean isEmpty(Expression expression) {
        return empty().text().equals(expression.text());
    }

    public static boolean isEmpty(Sequence<? extends Expression> expressions) {
        return empty().text().equals(join(expressions).text());
    }

    public static String toString(Expression expression, Function1<Object, Object> valueConverter) {
        return format(expression.text().replace("%", "%%").replace("?", "'%s'"), expression.parameters().map(valueConverter).toArray(Object.class));
    }

    public static Expression expression(Option<? extends Expression> optionalExpression) {
        return optionalExpression.map(identity(Expression.class)).getOrElse(empty());
    }
}
