package com.utc.nda.notification.dtos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NotificationMessage {

	private String title;
	private String content;
	private String type;
	private Map<String, String> attributes = new HashMap<String, String>();
	private List<String> toUsers;
	private List<String> toRoles;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
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

}
