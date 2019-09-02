package com.utc.nda.storage.entities;


import java.sql.Blob;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "file_store")
public class FileStore {

	@NotNull
	@EmbeddedId
	private FileStoreId id;
	
	@NotNull
	@Lob
	private Blob content;

	public FileStoreId getId() {
		return id;
	}

	public void setId(FileStoreId id) {
		this.id = id;
	}

	public Blob getContent() {
		return content;
	}

	public void setContent(Blob content) {
		this.content = content;
	}
	
	
	
}  

