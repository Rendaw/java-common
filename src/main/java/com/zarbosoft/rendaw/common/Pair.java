package com.zarbosoft.rendaw.common;

import java.util.Comparator;

public class Pair<T1, T2> implements Comparable<Pair> {
	public final static Comparator<Pair<? extends Comparable, ? extends Comparable>> comparator =
			new ChainComparator<Pair<? extends Comparable, ? extends Comparable>>()
					.lesserFirst(a -> a.first)
					.lesserFirst(a -> a.second)
					.build();

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

	@Override
	public String toString() {
		return String.format("Pair[%s, %s]", first, second);
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof Pair))
			return false;
		final Pair obj1 = (Pair) obj;
		if (first == null && obj1.first != null)
			return false;
		if (second == null && obj1.second != null)
			return false;
		return first.equals(obj1.first) && second.equals(obj1.second);
	}

	@Override
	public int compareTo(final Pair o) {
		return comparator.compare((Pair<? extends Comparable, ? extends Comparable>) this, o);
	}

}
