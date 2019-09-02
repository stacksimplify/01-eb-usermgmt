package com.utc.nda.storage.services;

public class DocumentProcessingException extends Exception {
	public DocumentProcessingException(String message, Exception exception) {
		super(message, exception);
	}

	public DocumentProcessingException(String message) {
		super(message);
	}
}
