package com.zarbosoft.rendaw.common;

public class Pair<T1, T2> {
	@FunctionalInterface
	public interface Consumer<T1, T2> {
		void accept(T1 a, T2 b);
	}

	@FunctionalInterface
	public interface Function<R, T1, T2> {
		R accept(T1 a, T2 b);
	}

	public T1 first;
	public T2 second;

	public Pair(final T1 first, final T2 second) {
		super();
		this.first = first;
		this.second = second;
	}

	@FunctionalInterface
	public interface MapOperator<A, B, C> {
		C apply(A a, B b);
	}

	public <T> T map(final MapOperator<T1, T2, T> operator) {
		return operator.apply(first, second);
	}
}
