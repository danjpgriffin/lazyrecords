package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Join;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.sql.grammars.SqlGrammar;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.predicates.Predicate;
import com.googlecode.totallylazy.functions.Reducer;
import com.googlecode.totallylazy.Sequence;

import java.util.Comparator;

import static com.googlecode.lazyrecords.sql.expressions.AnsiSelectExpression.selectExpression;
import static com.googlecode.lazyrecords.sql.expressions.AnsiSetQuantifier.DISTINCT;
import static com.googlecode.lazyrecords.sql.expressions.AnsiWhereClause.whereClause;
import static com.googlecode.lazyrecords.sql.expressions.SelectBuilder.aggregates;
import static com.googlecode.lazyrecords.sql.expressions.SelectBuilder.countStar;
import static com.googlecode.lazyrecords.sql.grammars.AndExpression.andExpression;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Sequences.sequence;

public class AnsiSelectBuilder implements ExpressionBuilder {
    private final SqlGrammar grammar;
    private final SelectExpression expression;

    private AnsiSelectBuilder(final SqlGrammar grammar, final SelectExpression expression) {
        this.grammar = grammar;
        this.expression = expression;
    }

    public static AnsiSelectBuilder from(final SqlGrammar grammar, final SelectExpression expression) {
        return new AnsiSelectBuilder(grammar, expression);
    }

    public static AnsiSelectBuilder from(final SqlGrammar grammar, final Definition definition) {
        return new AnsiSelectBuilder(grammar, grammar.selectExpression(
                Option.<SetQuantifier>none(),
                definition.fields(),
                definition,
                Option.<Predicate<? super Record>>none(),
                Option.<Comparator<? super Record>>none(),
                Option.<Sequence<? extends Keyword<?>>>none(),
                none(Integer.class),
                none(Integer.class)
        ));
    }

    @Override
    public Sequence<Keyword<?>> fields() {
        return SelectList.methods.fields(expression.selectList());
    }

    @Override
    public AnsiSelectBuilder select(final Keyword<?>... columns) {
        return select(sequence(columns));
    }

    @Override
    public AnsiSelectBuilder select(final Sequence<? extends Keyword<?>> columns) {
        if(columns.isEmpty()) return this;
        return select(grammar.selectList(columns));
    }

    public AnsiSelectBuilder select(final SelectList selectList) {
        return from(grammar, selectExpression(
                expression.setQuantifier(),
                selectList,
                expression.fromClause(),
                expression.whereClause(),
                expression.orderByClause(),
                expression.groupByClause(),
                expression.offsetClause(),
                expression.fetchClause()));
    }

    @Override
    public AnsiSelectBuilder filter(final Predicate<? super Record> predicate) {
        return filter(grammar.whereClause(predicate));
    }

    public AnsiSelectBuilder filter(final WhereClause whereClause) {
        return from(grammar, selectExpression(
                expression.setQuantifier(),
                expression.selectList(),
                expression.fromClause(),
                some(combine(expression.whereClause(), whereClause)),
                expression.orderByClause(),
                expression.groupByClause(),
                expression.offsetClause(),
                expression.fetchClause()));
    }

    public static Option<WhereClause> combine(final Option<WhereClause> existing, final Option<WhereClause> additional) {
        if(additional.isEmpty()) return existing;
        return some(combine(existing, additional.get()));
    }

    public static WhereClause combine(final Option<WhereClause> existing, final WhereClause additional) {
        if (existing.isEmpty()) return additional;
        return whereClause(andExpression(existing.get().expression(), additional.expression()));
    }

    @Override
    public AnsiSelectBuilder orderBy(final Comparator<? super Record> comparator) {
        return orderBy(grammar.orderByClause(comparator));
    }

    @Override
    public AnsiSelectBuilder groupBy(Keyword<?>... columns) {
        return groupBy(sequence(columns));
    }

    @Override
    public AnsiSelectBuilder groupBy(Sequence<? extends Keyword<?>> columns) {
        return groupBy(grammar.groupByClause(columns));
    }

    @Override
    public AnsiSelectBuilder offset(int number) {
        return from(grammar, selectExpression(
                expression.setQuantifier(),
                expression.selectList(),
                expression.fromClause(),
                expression.whereClause(),
                expression.orderByClause(),
                expression.groupByClause(),
                some(grammar.offsetClause(number)),
                expression.fetchClause()));
    }

    @Override
    public AnsiSelectBuilder fetch(int number) {
        return from(grammar, selectExpression(
                expression.setQuantifier(),
                expression.selectList(),
                expression.fromClause(),
                expression.whereClause(),
                expression.orderByClause(),
                expression.groupByClause(),
                expression.offsetClause(),
                some(grammar.fetchClause(number))));
    }

    public AnsiSelectBuilder groupBy(final GroupByClause groupByClause) {
        return from(grammar, selectExpression(
                expression.setQuantifier(),
                expression.selectList(),
                expression.fromClause(),
                expression.whereClause(),
                expression.orderByClause(),
                some(groupByClause),
                expression.offsetClause(),
                expression.fetchClause()));
    }

    public AnsiSelectBuilder orderBy(final OrderByClause orderByClause) {
        return from(grammar, selectExpression(
                expression.setQuantifier(),
                expression.selectList(),
                expression.fromClause(),
                expression.whereClause(),
                some(orderByClause),
                expression.groupByClause(),
                expression.offsetClause(),
                expression.fetchClause()));
    }

    @Override
    public AnsiSelectBuilder count() {
        return from(grammar, selectExpression(
                expression.setQuantifier(),
                grammar.selectList(countStar()),
                expression.fromClause(),
                expression.whereClause(),
                Option.<OrderByClause>none(),
                expression.groupByClause(),
                expression.offsetClause(),
                expression.fetchClause()));
    }

    @Override
    public AnsiSelectBuilder distinct() {
        return from(grammar, selectExpression(
                Option.<SetQuantifier>some(DISTINCT),
                expression.selectList(),
                expression.fromClause(),
                expression.whereClause(),
                expression.orderByClause(),
                expression.groupByClause(),
                expression.offsetClause(),
                expression.fetchClause()));
    }

    @Override
    public AnsiSelectBuilder reduce(final Reducer<?, ?> reducer) {
        return select(aggregates(reducer, fields()));
    }

    @Override
    public ExpressionBuilder join(Join join) {
        return grammar.join(this, join);
    }

    @Override
    public SelectExpression build() {
        return expression;
    }

    @Override
    public String text() {
        return expression.text();
    }

    @Override
    public Sequence<Object> parameters() {
        return expression.parameters();
    }
}
