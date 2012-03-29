package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.GenericType;

public interface Keyword<T> extends Callable1<Record, T>, GenericType<T> {
    String name();
    Record metadata();
    Keyword<T> metadata(Record record);
}