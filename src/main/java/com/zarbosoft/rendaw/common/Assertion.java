package com.zarbosoft.rendaw.common;

public class Assertion extends RuntimeException {

	public Assertion(final String message) {
		super(message);
	}

	public Assertion() {

	}

	public static Assertion format(final String template, final Object... args) {
		return new Assertion(String.format(template, args));
	}
}
