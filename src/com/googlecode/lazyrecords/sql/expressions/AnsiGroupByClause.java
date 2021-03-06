package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.functions.Function1;
import com.googlecode.totallylazy.Sequence;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;
import static com.googlecode.totallylazy.Sequences.cons;

public class AnsiGroupByClause extends CompoundExpression implements GroupByClause {
    private final Sequence<DerivedColumn> groups;

    private AnsiGroupByClause(Sequence<DerivedColumn> groups) {
        super(cons(groupBy, parts(groups)));
        this.groups = groups;
    }

    private static Sequence<? extends Expression> parts(Sequence<? extends Expression> groups) {
        return groups.safeCast(Expression.class).intersperse(textOnly(", "));
    }

    public static AnsiGroupByClause groupByClause(Sequence<DerivedColumn> groups) {
        return new AnsiGroupByClause(groups);
    }

    @Override
    public Sequence<DerivedColumn> groups() {
        return groups;
    }

    public static class functions{
        public static Function1<Sequence<DerivedColumn>, GroupByClause> groupByClause = groups1 -> AnsiGroupByClause.groupByClause(groups1);
    }
}
