package com.utc.nda.template.services;

public class TemplateNotFoundException extends Exception {
	public TemplateNotFoundException(String message, Exception exception) {
		super(message, exception);
	}

	public TemplateNotFoundException(String message) {
		super(message);
	}
}
