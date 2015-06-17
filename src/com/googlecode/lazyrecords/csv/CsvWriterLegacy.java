package com.googlecode.lazyrecords.csv;

import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.RecordTo;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Function;
import com.googlecode.totallylazy.Curried2;
import com.googlecode.totallylazy.Iterators;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;

import java.io.Writer;
import java.util.Iterator;

import static com.googlecode.totallylazy.Callables.toString;

public class CsvWriterLegacy {
    private static final String FIELD_SEPARATOR = ",";
    private static final char ROW_SEPARATOR = '\n';

    public static void writeTo(Iterator<Record> records, Writer writer, Sequence<? extends Keyword<?>> fields) {
        Iterators.fold(Iterators.cons(headers(fields), Iterators.map(records, rowToString(fields))), writer, writeLine());
    }

    private static Curried2<Writer, String, Writer> writeLine() {
        return new Curried2<Writer, String, Writer>() {
            @Override
            public Writer call(Writer writer, String line) throws Exception {
                return writer.append(line).append(ROW_SEPARATOR);
            }
        };
    }

    private static RecordTo<String> rowToString(final Sequence<? extends Keyword<?>> fields) {
        return new RecordTo<String>() {
            @Override
            public String call(final Record record) throws Exception {
                return fields.map(fieldToString(record)).map(escapeSpecialCharacters()).toString(FIELD_SEPARATOR);
            }
        };
    }

    private static Function1<Keyword<?>, String> fieldToString(final Record record) {
        return new Function1<Keyword<?>, String>() {
            @Override
            public String call(Keyword<?> keyword) throws Exception {
                return Option.option(record.get(keyword)).map(toString).getOrElse("");
            }
        };
    }

    private static Function<String, String> escapeSpecialCharacters() {
        return new Function<String, String>() {
            @Override
            public String call(String recordValue) throws Exception {
                recordValue = recordValue.replace('\n', ' ');
                if (recordValue.contains(",")) {
                    return '"' + recordValue + '"';
                }
                return recordValue;
            }
        };
    }

    private static String headers(Sequence<? extends Keyword<?>> fields) {
        return fields.map(Keyword.functions.name).toString(FIELD_SEPARATOR);
    }
}