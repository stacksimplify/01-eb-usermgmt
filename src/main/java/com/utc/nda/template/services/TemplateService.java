package com.utc.nda.template.services;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import com.utc.nda.storage.dtos.Document;
import com.utc.nda.storage.services.DocumentNotFoundException;
import com.utc.nda.storage.services.DocumentProcessingException;
import com.utc.nda.template.entities.Template;

public interface TemplateService {

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	public void createTemplate(String templateName, String type, Document document, byte[] content)
			throws TemplateProcessingException, DocumentProcessingException;

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	public void updateTemplate(String templateId, Document document, byte[] content)
			throws DocumentProcessingException, DocumentNotFoundException, TemplateNotFoundException;

	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MODERATOR','ROLE_ADMIN')")
	public Template getTemplateById(String id) throws TemplateNotFoundException;

	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MODERATOR','ROLE_ADMIN')")
	public Template getTemplateByNameAndType(String templateName, String type);

	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MODERATOR','ROLE_ADMIN')")
	public byte[] getTemplateContent(String documentGuid)
			throws DocumentProcessingException, TemplateNotFoundException, DocumentNotFoundException;

	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MODERATOR','ROLE_ADMIN')")
	public byte[] getTemplateVersionContent(String templateId, String versionId)
			throws DocumentProcessingException, DocumentNotFoundException, TemplateNotFoundException;

	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MODERATOR','ROLE_ADMIN')")
	public Template getRequestFormTemplate();

	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MODERATOR','ROLE_ADMIN')")
	public Template getSpecificPurposeTemplate();

	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MODERATOR','ROLE_ADMIN')")
	public Template getSignaturePageTemplate();

	@PreAuthorize("hasAnyRole('ROLE_MODERATOR','ROLE_ADMIN')")
	public List<Template> listAllTemplates();

	@PreAuthorize("hasAnyRole('ROLE_MODERATOR','ROLE_ADMIN')")
	public List<Document> listAllTemplateVersions(String id)
			throws DocumentNotFoundException, TemplateNotFoundException;

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	public void setTemplateVersionAsActive(String templateId, String versionId)
			throws DocumentNotFoundException, TemplateNotFoundException;

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	public void deleteTemplatePermanently(String templateId) throws TemplateNotFoundException;

}
