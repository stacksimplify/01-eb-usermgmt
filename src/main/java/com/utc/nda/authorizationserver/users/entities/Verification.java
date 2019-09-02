package com.utc.nda.authorizationserver.users.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "forgot_password")
public class Verification {

	@Id
	@Column(name = "id", length = 36)
	private String id;

	@NotNull
	@Size(min = 1)
	private String userName;

	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	private Date creationTime;

	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	private Date modificationTime;

	@NotNull
	@Column(columnDefinition = "boolean default false")
	private boolean tokenUsed;

	public boolean isTokenUsed() {
		return tokenUsed;
	}

	public void setTokenUsed(boolean status) {
		this.tokenUsed = status;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
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
