package com.zarbosoft.rendaw.common;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.NoSuchFileException;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Common {
	public static String byteFormat(final byte b) {
		if (b == (byte) '\n')
			return "\\n";
		if (b == (byte) '\r')
			return "\\r";
		if (b == (byte) '\t')
			return "\\t";
		if ((b < 32) || (b >= 127))
			return String.format("\\x%02x", b);
		return Character.toString((char) (byte) b);
	}

	public static String byteFormat(final List<Byte> bytes) {
		return bytes.stream().map(b -> byteFormat(b)).collect(Collectors.joining());
	}

	public static String byteFormat(final byte[] bytes) {
		final StringBuilder out = new StringBuilder();
		for (final byte b : bytes)
			out.append(byteFormat(b));
		return out.toString();
	}

	public static <T> T uncheck(final Thrower1<T> code) {
		try {
			return code.get();
		} catch (final NoSuchFileException e) {
			throw new UncheckedNoSuchFileException(e);
		} catch (final FileNotFoundException e) {
			throw new UncheckedFileNotFoundException(e);
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		} catch (final RuntimeException e) {
			throw e;
		} catch (final Throwable e) {
			throw new UncheckedException(e);
		}
	}

	public static void uncheck(final Thrower2 code) {
		try {
			code.get();
		} catch (final NoSuchFileException e) {
			throw new UncheckedNoSuchFileException(e);
		} catch (final FileNotFoundException e) {
			throw new UncheckedFileNotFoundException(e);
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		} catch (final RuntimeException e) {
			throw e;
		} catch (final Throwable e) {
			throw new UncheckedException(e);
		}
	}

	public static <T> T last(final List<T> values) {
		return values.get(values.size() - 1);
	}

	public static <T> T last(final T[] values) {
		return values[values.length - 1];
	}

	public static <T> Stream<T> stream(final Iterator<T> iterator) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
	}

	public static <T> Stream<Pair<Integer, T>> enumerate(final Stream<T> stream) {
		return enumerate(stream, 0);
	}

	public static <T> Stream<Pair<Integer, T>> enumerate(final Stream<T> stream, final int start) {
		final Mutable<Integer> count = new Mutable<>(start);
		return stream.map(e -> new Pair<>(count.value++, e));
	}

	public static class Enumerator<T> implements Function<T, Pair<Integer, T>> {
		private int count = 0;

		@Override
		public Pair<Integer, T> apply(final T t) {
			return new Pair<>(count++, t);
		}
	}

	public static <T> Stream<T> stream(final Iterable<T> source) {
		return StreamSupport.stream(source.spliterator(), false);
	}

	@FunctionalInterface
	public interface Thrower1<T> {
		T get() throws Throwable;
	}

	@FunctionalInterface
	public interface Thrower2 {
		void get() throws Throwable;
	}

	public static class UncheckedException extends RuntimeException {
		public UncheckedException(final Throwable e) {
			super(e);
		}
	}

	public static class UncheckedFileNotFoundException extends RuntimeException {
		public UncheckedFileNotFoundException(final Throwable e) {
			super(e);
		}
	}

	public static class UncheckedNoSuchFileException extends RuntimeException {
		public UncheckedNoSuchFileException(final Throwable e) {
			super(e);
		}
	}

	public static class Mutable<T> {
		public T value;

		public Mutable(final T value) {
			this.value = value;
		}

		public Mutable() {
		}
	}
}
