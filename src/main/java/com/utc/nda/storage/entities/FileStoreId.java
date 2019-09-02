package com.utc.nda.storage.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Embeddable
public class FileStoreId implements Serializable {

	private static final long serialVersionUID = -5345781812995418575L;

	@Size(min = 1, max = 36)
	@NotNull
	@Column(name = "id", length = 36)
	private String id;

	@Size(min = 1, max = 36)
	@NotNull
	@Column(name = "version", length = 36)
	private String version;

	public FileStoreId() {

	}

	public FileStoreId(String id, String version) {
		super();

		this.id = id;
		this.version = version;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileStoreId other = (FileStoreId) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FileStoreId [id=" + id + ", version=" + version + "]";
	}

}
