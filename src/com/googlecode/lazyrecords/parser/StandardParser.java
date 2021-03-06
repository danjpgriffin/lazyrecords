package com.googlecode.lazyrecords.parser;

import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.DateMapping;
import com.googlecode.lazyrecords.mappings.IntegerMapping;
import com.googlecode.lazyrecords.mappings.LongMapping;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.predicates.Predicate;
import com.googlecode.totallylazy.predicates.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.parser.Result;

import java.util.Date;


public class StandardParser implements PredicateParser {
    private final StringMappings mappings;

    public StandardParser() {
        this(new ParserDateConverter());
    }

    public StandardParser(ParserDateConverter dateConverter) {
        this(new StringMappings().
                add(Integer.class, new IntegerMapping()).
                add(Long.class, new LongMapping()).
                add(Date.class, new DateMapping(dateConverter)));
    }

    public StandardParser(StringMappings mappings) {
        this.mappings = mappings;
    }

    public Predicate<Record> parse(String raw, Sequence<? extends Keyword<?>> implicits) throws IllegalArgumentException {
        final String query = raw.trim();
        if (Strings.isEmpty(query)) {
            return Predicates.all();
        }
        Result<Predicate<Record>> result = Grammar.PARSER(implicits, mappings).parse(query);
        if(!result.remainder().isEmpty()) throw new IllegalArgumentException("Query did not match the whole expression");
        return result.value();
    }
}
