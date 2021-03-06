package com.googlecode.lazyrecords.simpledb;

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.BatchDeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.DeletableItem;
import com.amazonaws.services.simpledb.model.Item;
import com.googlecode.lazyrecords.*;
import com.googlecode.lazyrecords.simpledb.mappings.SimpleDBMappings;
import com.googlecode.lazyrecords.sql.grammars.AnsiSqlGrammar;
import com.googlecode.lazyrecords.sql.grammars.SqlGrammar;
import com.googlecode.totallylazy.functions.Function1;
import com.googlecode.totallylazy.predicates.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Value;
import com.googlecode.totallylazy.numbers.Numbers;
import org.junit.Ignore;

import java.util.List;

import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.lazyrecords.SelectFunction.select;
import static com.googlecode.lazyrecords.sql.expressions.AnsiSelectBuilder.from;
import static com.googlecode.totallylazy.numbers.Numbers.sum;

public class SimpleDBRecords extends AbstractRecords {
    private final AmazonSimpleDB sdb;
    private final SimpleDBMappings mappings;
    private final Logger logger;
    private final boolean consistentRead;
    private final Schema schema;
    private final SqlGrammar grammar;

    public SimpleDBRecords(final AmazonSimpleDB sdb, boolean consistentRead, final SimpleDBMappings mappings, final Logger logger, final Schema schema) {
        this.consistentRead = consistentRead;
        this.mappings = mappings;
        this.sdb = sdb;
        this.logger = logger;
        this.schema = schema;
        grammar = new AnsiSqlGrammar();
    }

    public SimpleDBRecords(final AmazonSimpleDB sdb, boolean consistentRead) {
        this(sdb, consistentRead, new SimpleDBMappings(), new IgnoreLogger(), new SimpleDBSchema(sdb));
    }

    public Sequence<Record> get(Definition definition) {
        return new SimpleDBSequence<Record>(sdb, from(grammar, definition), mappings.stringMappings(), mappings.asRecord(definition.fields()), logger, consistentRead);
    }

    public Number add(final Definition definition, Sequence<Record> records) {
        if (records.isEmpty()) {
            return 0;
        }

        return records.recursive(Sequences.<Record>splitAt(25)).
                mapConcurrently(putAttributes(definition)).
                reduce(sum());
    }

    public Number remove(Definition definition, Predicate<? super Record> predicate) {
        if (!schema.exists(definition)) {
            return 0;
        }
        Sequence<Record> items = get(definition).filter(predicate).realise();
        if (items.isEmpty()) {
            return 0;
        }

        return items.recursive(Sequences.<Record>splitAt(25)).
                mapConcurrently(deleteAttributes(definition)).
                reduce(sum());
    }

    @Override
    public Number remove(Definition definition) {
        Record head = get(definition).map(select(Aggregate.count(keyword("*", String.class)).as(""))).head();
        Number result = Numbers.valueOf(head.get(Keyword.constructors.keyword("Count", String.class))).get();
        schema.undefine(definition);
        schema.define(definition);
        return result;
    }

    private Function1<Value<Item>, DeletableItem> asItem() {
        return value -> new DeletableItem().withName(value.value().getName());
    }

    private Function1<Sequence<Record>, Number> putAttributes(final Definition definition) {
        return batch -> {
            sdb.batchPutAttributes(new BatchPutAttributesRequest(definition.name(), batch.map(mappings.toReplaceableItem()).toList()));
            return batch.size();
        };
    }

    private Function1<Sequence<Record>, Number> deleteAttributes(final Definition definition) {
        return batch -> {
            List<DeletableItem> items = batch.<Value<Item>>unsafeCast().map(asItem()).toList();
            sdb.batchDeleteAttributes(new BatchDeleteAttributesRequest(definition.name(), items));
            return batch.size();
        };
    }
}
