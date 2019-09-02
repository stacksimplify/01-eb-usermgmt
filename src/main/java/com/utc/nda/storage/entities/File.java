package com.utc.nda.storage.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "file")
public class File {

	@Id
	@Size(min = 1, max = 36)
	@NotNull
	@Column(name = "id", length = 36)
	private String id;

	@Size(min = 1, max = 36)
	@NotNull
	private String activeVersion;

	@Size(min = 1, max = 255)
	@NotNull
	private String category;

	@Size(min = 1, max = 255)
	@NotNull
	private String name;

	@Min(1)
	private long size;

	@Size(min = 1, max = 255)
	@NotNull
	private String contentType;

	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	private Date creationTime;

	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	private Date modificationTime;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getActiveVersion() {
		return activeVersion;
	}

	public void setActiveVersion(String activeVersion) {
		this.activeVersion = activeVersion;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	public Date getModificationTime() {
		return modificationTime;
	}

	public void setModificationTime(Date modificationTime) {
		this.modificationTime = modificationTime;
	}

}
