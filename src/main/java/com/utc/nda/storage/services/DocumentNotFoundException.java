package com.utc.nda.storage.services;

public class DocumentNotFoundException extends Exception {
	public DocumentNotFoundException(String message, Exception exception) {
		super(message, exception);
	}

	public DocumentNotFoundException(String message) {
		super(message);
	}
}
