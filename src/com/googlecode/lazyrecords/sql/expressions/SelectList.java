package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.ImmutableKeyword;
import com.googlecode.lazyrecords.Keywords;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.lazyrecords.Aggregate;
import com.googlecode.lazyrecords.AliasedKeyword;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.SelectCallable;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.expression;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.name;
import static com.googlecode.lazyrecords.sql.expressions.SetFunctionType.setFunctionType;

public class SelectList extends CompoundExpression{
    public SelectList(Sequence<Keyword<?>> select) {
        super(select.map(derivedColumn()));
    }

    public static SelectList selectList(final Sequence<Keyword<?>> select) {
        return new SelectList(select);
    }

    @Override
    public String text() {
        return expressions.map(Expressions.text()).toString();
    }

    public static Function1<Keyword<?>, Expression> derivedColumn() {
        return new Function1<Keyword<?>, Expression>() {
            public Expression call(Keyword<?> keyword) throws Exception {
                return derivedColumn(keyword);
            }
        };
    }

    public static <T> AbstractExpression derivedColumn(Callable1<? super Record, T> callable) {
        if(callable instanceof Aggregate){
            Aggregate aggregate = (Aggregate) callable;
            return setFunctionType(aggregate.callable(), aggregate.source()).join(asClause(aggregate));
        }
        if (callable instanceof AliasedKeyword) {
            AliasedKeyword aliasedKeyword = (AliasedKeyword) callable;
            return name(aliasedKeyword.source()).join(asClause(aliasedKeyword));
        }
        if (callable instanceof Keyword) {
            Keyword<?> keyword = (Keyword) callable;
            return name(keyword);
        }
        if (callable instanceof SelectCallable) {
            Sequence<Keyword<?>> keywords = ((SelectCallable) callable).keywords();
            return selectList(keywords);
        }
        throw new UnsupportedOperationException("Unsupported callable " + callable);
    }

    public static boolean isLongName(Keyword<?> keyword) {
        return keyword.name().contains(".");
    }

    public static Expression asClause(Keyword<?> keyword) {
        return asClause(keyword.name());
    }

    public static AbstractExpression asClause(String name) {
        return expression("as " + name);
    }

    public static Keyword<?> shortName(Keyword<?> keyword) {
        return Keywords.keyword(shortName(keyword.name()), keyword.forClass());
    }

    public static String shortName(String value) {
        String[] parts = value.split("\\.");
        return parts[parts.length - 1];
    }

    public static Function1<Keyword<?>, String> shortName() {
        return new Function1<Keyword<?>, String>() {
            @Override
            public String call(Keyword<?> keyword) throws Exception {
                if(isLongName(keyword)){
                    return shortName(keyword).name();
                }
                return keyword.name();
            }
        };
    }
}
