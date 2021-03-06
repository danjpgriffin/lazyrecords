package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.functions.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.predicates.Predicate;
import com.googlecode.totallylazy.Sequence;

public interface Records extends RecordsReader {

    Number add(Definition definition, Record... records);

    Number add(Definition definition, Sequence<Record> records);

    Number set(Definition definition, Pair<? extends Predicate<? super Record>, Record>... records);

    Number set(Definition definition, Sequence<? extends Pair<? extends Predicate<? super Record>, Record>> records);

    Number put(Definition definition, Pair<? extends Predicate<? super Record>, Record>... records);

    Number put(Definition definition, Sequence<? extends Pair<? extends Predicate<? super Record>, Record>> records);

    Number remove(Definition definition, Predicate<? super Record> predicate);

    Number remove(Definition definition);

    class functions {
        public static Function1<Records, Number> add(final Definition definition, final Record... records) {
            return allRecords -> allRecords.add(definition, records);
        }

        public static Function1<Records, Number> add(final Definition definition, final Sequence<Record> records) {
            return allRecords -> allRecords.add(definition, records);
        }

        public static Function1<Records, Number> set(final Definition definition, final Pair<? extends Predicate<? super Record>, Record>... records) {
            return allRecords -> allRecords.set(definition, records);
        }

        public static Function1<Records, Number> set(final Definition definition, final Sequence<? extends Pair<? extends Predicate<? super Record>, Record>> records) {
            return allRecords -> allRecords.set(definition, records);
        }

        public static Function1<Records, Number> put(final Definition definition, final Pair<? extends Predicate<? super Record>, Record>... records) {
            return allRecords -> allRecords.put(definition, records);
        }

        public static Function1<Records, Number> put(final Definition definition, final Sequence<? extends Pair<? extends Predicate<? super Record>, Record>> records) {
            return allRecords -> allRecords.put(definition, records);
        }

        public static Function1<Records, Number> remove(final Definition definition, final Predicate<? super Record> predicate) {
            return allRecords -> allRecords.remove(definition, predicate);
        }

        public static Function1<Records, Number> remove(final Definition definition) {
            return allRecords -> allRecords.remove(definition);
        }
    }
}
