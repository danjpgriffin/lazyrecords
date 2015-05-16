package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.*;

import static com.googlecode.totallylazy.Functions.returns1;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Strings.equalIgnoringCase;

public interface Keyword<T> extends Named, Metadata<Keyword<T>>, Callable1<Record, T>, GenericType<T>, Comparable<Keyword<T>> {
    Record metadata();

    public String toString();

    class constructors {
        public static ImmutableKeyword<Object> keyword(String value) {
            return new ImmutableKeyword<Object>(value, Object.class);
        }

        public static <T> ImmutableKeyword<T> keyword(String value, Class<? extends T> aClass) {
            return new ImmutableKeyword<T>(value, aClass);
        }

        public static <T> ImmutableKeyword<T> keyword(Keyword<? extends T> keyword) {
            return keyword(keyword.name(), keyword.forClass()).metadata(keyword.metadata());
        }

        public static <T> CompositeKeyword<T> compose(Binary<T> combiner, Sequence<? extends Keyword<T>> keywords) {
            return CompositeKeyword.compose(combiner, keywords);
        }
    }

    class methods {
        public static Keyword<Object> matchKeyword(String name, Sequence<? extends Keyword<?>> definitions) {
            return matchKeyword(name, definitions, Keyword.functions.name());
        }

        public static Keyword<Object> matchKeyword(String shortName, Sequence<? extends Keyword<?>> definitions, Function1<Keyword<?>, String> extractor) {
            return definitions.<Keyword<Object>>unsafeCast().find(where(extractor, equalIgnoringCase(shortName))).getOrElse(Keyword.constructors.keyword(shortName));
        }

        public static boolean equalTo(Keyword<?> keyword, Keyword<?> other) {
            return keyword.name().equalsIgnoreCase(other.name());
        }

        public static Sequence<Keyword<?>> keywords(Sequence<Record> results) {
            return results.flatMap(Record.functions.keywords).unique().realise();
        }
    }

    class functions {
        public static Mapper<Keyword<?>, String> name = new Mapper<Keyword<?>, String>() {
            @Override
            public String call(Keyword<?> keyword) throws Exception {
                return keyword.name();
            }
        };

        public static Mapper<Keyword<?>, String> name() { return name; }

        public static <T> Mapper<Keyword<?>, T> metadata(final Keyword<T> metadataKey) {
            return new Mapper<Keyword<?>, T>() {
                public T call(Keyword<?> keyword) throws Exception {
                    return keyword.metadata().get(metadataKey);
                }
            };
        }

        public static <T> UnaryFunction<Keyword<T>> metadata(final Record metadata) {
            return new UnaryFunction<Keyword<T>>() {
                public Keyword<T> call(Keyword<T> keyword) throws Exception {
                    return keyword.metadata(metadata);
                }
            };
        }

        public static <T, M> UnaryFunction<Keyword<T>> metadata(final Keyword<M> name, final M value) {
            return new UnaryFunction<Keyword<T>>() {
                public Keyword<T> call(Keyword<T> keyword) throws Exception {
                    return keyword.metadata(name, value);
                }
            };
        }

        public static UnaryFunction<Keyword<?>> replace(Keyword<?> from, Keyword<?> to) {
            return Callables.<Keyword<?>>replace(Predicates.<Keyword<?>>is(from), returns1(to));
        }
    }
}