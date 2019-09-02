package com.utc.nda.notification.entities;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class Notification {

	@Id
	@NotNull
	@Size(min = 1, max = 36)
	private String id = UUID.randomUUID().toString();

	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	private Date creationDate = new java.util.Date();

	@NotNull
	@Size(min = 1, max = 100)
	@Column(length = 100)
	private String title;

	@NotNull
	@Size(min = 1, max = 2000)
	@Column(length = 2000)
	private String content;

	@NotNull
	private boolean isRead = false;

	@NotNull
	@Size(min = 1)
	private String type;

	@NotNull
	@Size(min = 1)
	private String owner;

	@ElementCollection
	@MapKeyColumn(name = "name", length = 50)
	@Column(name = "value", length = 1000)
	@CollectionTable(name = "notification_attributes", joinColumns = @JoinColumn(name = "notification_id"))
	Map<String, String> attributes = new HashMap<String, String>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

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

	public boolean isRead() {
		return isRead;
	}

	public void setRead(boolean isRead) {
		this.isRead = isRead;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

}
