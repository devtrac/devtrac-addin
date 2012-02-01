package com.fairview5.keepassbb2.common.crypto;

import java.io.IOException;

public class HashMismatchException extends IOException {
	public HashMismatchException(String message) {
		super(message);
	}

	public HashMismatchException() {
		super();
	}
}
