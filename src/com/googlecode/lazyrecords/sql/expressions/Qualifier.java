package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.sql.grammars.OracleGrammar;
import com.googlecode.totallylazy.functions.Function1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.functions.Unary;
import com.googlecode.totallylazy.annotations.multimethod;

import static com.googlecode.lazyrecords.sql.expressions.AnsiAsClause.asClause;
import static com.googlecode.lazyrecords.sql.expressions.AsClause.functions.alias;
import static com.googlecode.lazyrecords.sql.expressions.SetFunctionType.setFunctionType;
import static com.googlecode.totallylazy.functions.Functions.constant;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.option;
import static com.googlecode.totallylazy.Option.some;

public class Qualifier extends AbstractQualifier {
    private final String tableAlias;
    private final Unary<String> qualified;

    protected Qualifier(final String tableAlias, Function1<? super String, String> qualified) {
        this.tableAlias = tableAlias;
        this.qualified = qualified::call;
    }

    public static Qualifier qualifier(final String name) {
        return qualifier(name, constant(name));
    }

    public static Qualifier qualifier(final String name, final Function1<? super String, String> callable) {
        return new Qualifier(name, callable);
    }

    @multimethod public SelectExpression qualify(SelectExpression expression) {
        return AnsiSelectExpression.selectExpression(expression.setQuantifier(), qualify(expression.selectList()),
                qualify(expression.fromClause()), qualify(expression.whereClause()), qualify(expression.orderByClause()), qualify(expression.groupByClause()),
                expression.offsetClause(),
                expression.fetchClause());
    }

    @multimethod private FromClause qualify(FromClause fromClause) {
        return AnsiFromClause.fromClause(qualify(fromClause.tableReference()));
    }

    @multimethod public TablePrimary qualify(TablePrimary tablePrimary) {
        return AnsiTablePrimary.tablePrimary(tablePrimary.tableName(), some(tablePrimary.asClause().getOrElse(asClause(tableAlias))));
    }

    @multimethod public SelectList qualify(SelectList selectList) {
        return AnsiSelectList.selectList(qualify(selectList.derivedColumns()));
    }

    @multimethod public WhereClause qualify(WhereClause whereClause) {
        return AnsiWhereClause.whereClause(qualify(whereClause.expression()));
    }

    @multimethod public PredicateExpression qualify(PredicateExpression predicateExpression) {
        return AnsiPredicateExpression.predicateExpression(qualify(predicateExpression.predicand()), qualify(predicateExpression.predicate()));
    }

    @multimethod public QualifiedJoin qualify(QualifiedJoin qualifiedJoin) {
        return qualifiedJoin;
    }

    @multimethod public CompoundExpression qualify(CompoundExpression compoundExpression) {
        return new CompoundExpression(qualify(compoundExpression.expressions()), compoundExpression.start(),
                compoundExpression.separator(), compoundExpression.end());
    }

    @multimethod public SetFunctionType qualify(SetFunctionType functionType) {
        return setFunctionType(functionType.functionName(), qualify(functionType.columnReference()));
    }

    @multimethod public OracleGrammar.OracleGroupConcatExpression qualify(OracleGrammar.OracleGroupConcatExpression expression) {
        return new OracleGrammar.OracleGroupConcatExpression(qualify(expression.columnReference()), expression.listSeparator());
    }

    @multimethod public DerivedColumn qualify(final DerivedColumn derivedColumn) {
        final Qualifier qualifier = derivedColumn.asClause().map(alias()).flatMap(columnQualifier(qualified)).getOrElse(this);
        final ValueExpression qualifiedExpression = qualifier.qualify(derivedColumn.valueExpression());
        return AnsiDerivedColumn.derivedColumn(qualifiedExpression, derivedColumn.asClause(), derivedColumn.forClass());
    }

    private Function1<String, Option<Qualifier>> columnQualifier(final Unary<String> qualified) {
        return columnAlias -> {
            final Option<String> resolvedQualifier = option(qualified.apply(columnAlias));
            return resolvedQualifier.isDefined() ? some(qualifier(resolvedQualifier.get())) : none(Qualifier.class);
        };
    }

    @multimethod public ColumnReference qualify(ColumnReference columnReference) {
        return ColumnReference.columnReference(columnReference.name(), some(columnReference.qualifier.getOrElse(qualified.apply(columnReference.name()))));
    }

    @multimethod public OrderByClause qualify(OrderByClause orderByClause){
        return AnsiOrderByClause.orderByClause(orderByClause.sortSpecifications().
                map(value -> AnsiSortSpecification.sortSpecification(qualify(value.sortKey()), value.orderingSpecification())));
    }

    @multimethod public GroupByClause qualify(GroupByClause groupByClause){
        return AnsiGroupByClause.groupByClause(groupByClause.groups().map(Qualifier.this::qualify));
    }

}
