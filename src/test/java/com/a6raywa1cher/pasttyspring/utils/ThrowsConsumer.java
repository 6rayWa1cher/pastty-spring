package com.a6raywa1cher.pasttyspring.utils;

@FunctionalInterface
public interface ThrowsConsumer<T> {
	void accept(T t) throws Exception;
}
