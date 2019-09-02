package com.utc.nda.template.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class Template {

	@Id
	@Size(min = 1, max = 36)
	@NotNull
	private String guid;

	@Size(min = 1, max = 255)
	@NotNull
	private String name;

	@Size(min = 1)
	@NotNull
	private String type;

	@Size(min = 1, max = 255)
	@NotNull
	private String documentGuid;

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDocumentGuid() {
		return documentGuid;
	}

	public void setDocumentGuid(String documentGuid) {
		this.documentGuid = documentGuid;
	}

}
