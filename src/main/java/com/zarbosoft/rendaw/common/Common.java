package com.zarbosoft.rendaw.common;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.NoSuchFileException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
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

	public static <T> Iterable<T> iterable(final Stream<T> stream) {
		return new Iterable<>() {
			@Override
			public Iterator<T> iterator() {
				return stream.iterator();
			}
		};
	}

	public static <T> Iterable<T> iterable(final Iterator<T> iterator) {
		return new Iterable<>() {
			@Override
			public Iterator<T> iterator() {
				return iterator;
			}
		};
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

	public static <A, B> Stream<Pair<A, B>> zip(
			final Stream<? extends A> a, final Stream<? extends B> b
	) {
		final Spliterator<? extends A> aSpliterator = Objects.requireNonNull(a).spliterator();
		final Spliterator<? extends B> bSpliterator = Objects.requireNonNull(b).spliterator();

		// Zipping looses DISTINCT and SORTED characteristics
		final int characteristics = aSpliterator.characteristics() &
				bSpliterator.characteristics() &
				~(Spliterator.DISTINCT | Spliterator.SORTED);

		final long zipSize = ((characteristics & Spliterator.SIZED) != 0) ?
				Math.min(aSpliterator.getExactSizeIfKnown(), bSpliterator.getExactSizeIfKnown()) :
				-1;

		final Iterator<A> aIterator = Spliterators.iterator(aSpliterator);
		final Iterator<B> bIterator = Spliterators.iterator(bSpliterator);
		final Iterator<Pair<A, B>> cIterator = new Iterator<>() {
			@Override
			public boolean hasNext() {
				return aIterator.hasNext() && bIterator.hasNext();
			}

			@Override
			public Pair<A, B> next() {
				return new Pair<>(aIterator.next(), bIterator.next());
			}
		};

		final Spliterator<Pair<A, B>> split = Spliterators.spliterator(cIterator, zipSize, characteristics);
		return (a.isParallel() || b.isParallel()) ?
				StreamSupport.stream(split, true) :
				StreamSupport.stream(split, false);
	}

	public static <T> Stream<T> flatStream(final Stream<T>... values) {
		return Stream.of(values).flatMap(v -> v);
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

	public static <T> Stream<Pair<Boolean, T>> streamFinality(final Iterator<T> data) {
		return stream(new Iterator<Pair<Boolean, T>>() {
			@Override
			public boolean hasNext() {
				return data.hasNext();
			}

			@Override
			public Pair<Boolean, T> next() {
				final T temp = data.next();
				return new Pair<>(!data.hasNext(), temp);
			}
		});
	}

	public static <T> Stream<T> concatNull(final Stream<T> stream) {
		return Stream.concat(stream, Stream.of((T[]) new Object[] {null}));
	}

	public static class InputStreamIterator implements Iterator<byte[]> {
		private final InputStream source;
		private byte[] bytes = null;
		private int length = -1;

		public InputStreamIterator(final InputStream source) {
			this.source = source;
		}

		@Override
		public boolean hasNext() {
			if (bytes == null)
				read();
			return length != -1;
		}

		@Override
		public byte[] next() {
			if (bytes == null)
				read();
			if (length == -1)
				throw new NoSuchElementException();
			final byte[] out = Arrays.copyOfRange(bytes, 0, length);
			read();
			return out;
		}

		private void read() {
			if (bytes == null)
				bytes = new byte[1024];
			length = uncheck(() -> source.read(bytes));
		}
	}

	public static Stream<byte[]> stream(final InputStream source) {
		return stream(new InputStreamIterator(source));
	}

	public static <T> Stream<T> drain(final Deque<T> queue) {
		class DrainIterator implements Iterator<T> {

			@Override
			public boolean hasNext() {
				return !queue.isEmpty();
			}

			@Override
			public T next() {
				return queue.pollFirst();
			}
		}
		return stream(new DrainIterator());
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

	public static class UserData {
		private Object value;

		public UserData() {
			value = null;
		}

		public UserData(final Object value) {
			this.value = value;
		}

		public <T> T get() {
			return (T) value;
		}

		public <T> T get(final Supplier<T> supplier) {
			if (value == null)
				value = supplier.get();
			return get();
		}
	}
}
