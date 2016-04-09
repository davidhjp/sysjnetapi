package com.systemj.util;

public class Tuple<T1,T2> {
	private final T1 first;
	private final T2 second;
	
	public Tuple(T1 arg1, T2 arg2){
		first = arg1;
		second = arg2;
	}

	public T1 getFirst() {
		return first;
	}

	public T2 getSecond() {
		return second;
	}
}
