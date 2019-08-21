package com.a6raywa1cher.pasttyspring.utils;

@FunctionalInterface
public interface ThrowsFunction<T, R> {
	R apply(T t) throws Exception;
}
