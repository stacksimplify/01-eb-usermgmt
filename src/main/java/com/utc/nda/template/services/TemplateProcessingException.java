package com.utc.nda.template.services;

public class TemplateProcessingException extends Exception {
	public TemplateProcessingException(String message, Exception exception) {
		super(message, exception);
	}

	public TemplateProcessingException(String message) {
		super(message);
	}
}
