package com.zarbosoft.rendaw.common;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Comparable if all elements are comparable.
 *
 * @param <T>
 */
public class Tuple<T> implements Comparable<Tuple<? extends Comparable>> {
	public T[] value;

	public Tuple(final T... value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return String.format(
				"Tuple<%s>",
				Arrays.asList(value).stream().map(v -> String.valueOf(v)).collect(Collectors.joining(", "))
		);
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof Tuple))
			return false;
		final Tuple obj1 = (Tuple) obj;
		if (value.length != obj1.value.length)
			return false;
		for (int i = 0; i < value.length; ++i) {
			final Object v1 = value[i];
			final Object v2 = obj1.value[i];
			if (v1 == null && v2 != null)
				return false;
			if (!v1.equals(v2))
				return false;
		}
		return true;
	}

	@Override
	public int compareTo(final Tuple<? extends Comparable> o) {
		int i = 0;
		for (; i < value.length; ++i) {
			if (i >= o.value.length)
				return 1;
			final Comparable v1 = (Comparable) value[i];
			final Comparable v2 = o.value[i];
			if (v1 == null) {
				if (v2 == null)
					continue;
				return -1;
			}
			if (v2 == null)
				return 1;
			if (v1 == null && v2 != null)
				continue;
			final int ret = v1.compareTo(v2);
			if (ret != 0)
				return ret;
		}
		if (i < o.value.length)
			return -1;
		return 0;
	}
}
