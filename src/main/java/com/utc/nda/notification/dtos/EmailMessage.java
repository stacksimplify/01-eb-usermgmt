package com.utc.nda.notification.dtos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmailMessage {

	private String subject;
	private String content;
	private String from;
	private List<String> toUsers;
	private List<String> toRoles;
	private List<String> toEmails;
	private List<String> ccUsers;
	private List<String> ccRoles;
	private List<String> ccEmails;
	private List<String> bccUsers;
	private List<String> bccRoles;
	private List<String> bccEmails;
	private Map<String, EmailAttachment> attachments = new HashMap<String, EmailAttachment>();

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public List<String> getToUsers() {
		return toUsers;
	}

	public void setToUsers(List<String> toUsers) {
		this.toUsers = toUsers;
	}

	public List<String> getToRoles() {
		return toRoles;
	}

	public void setToRoles(List<String> toRoles) {
		this.toRoles = toRoles;
	}

	public List<String> getToEmails() {
		return toEmails;
	}

	public void setToEmails(List<String> toEmails) {
		this.toEmails = toEmails;
	}

	public List<String> getCcUsers() {
		return ccUsers;
	}

	public void setCcUsers(List<String> ccUsers) {
		this.ccUsers = ccUsers;
	}

	public List<String> getCcRoles() {
		return ccRoles;
	}

	public void setCcRoles(List<String> ccRoles) {
		this.ccRoles = ccRoles;
	}

	public List<String> getCcEmails() {
		return ccEmails;
	}

	public void setCcEmails(List<String> ccEmails) {
		this.ccEmails = ccEmails;
	}

	public List<String> getBccUsers() {
		return bccUsers;
	}

	public void setBccUsers(List<String> bccUsers) {
		this.bccUsers = bccUsers;
	}

	public List<String> getBccRoles() {
		return bccRoles;
	}

	public void setBccRoles(List<String> bccRoles) {
		this.bccRoles = bccRoles;
	}

	public List<String> getBccEmails() {
		return bccEmails;
	}

	public void setBccEmails(List<String> bccEmails) {
		this.bccEmails = bccEmails;
	}

	public Map<String, EmailAttachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(Map<String, EmailAttachment> attachments) {
		this.attachments = attachments;
	}

}
