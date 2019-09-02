package com.utc.nda.template.services;

import java.util.List;
import java.util.UUID;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.utc.nda.storage.dtos.Document;
import com.utc.nda.storage.services.DocumentNotFoundException;
import com.utc.nda.storage.services.DocumentProcessingException;
import com.utc.nda.storage.services.StorageService;
import com.utc.nda.template.entities.Template;
import com.utc.nda.template.repositories.TemplateRepository;

@Service
public class TemplateServiceImpl implements TemplateService {

	private static final Logger logger = LoggerFactory.getLogger(TemplateServiceImpl.class);

	@Autowired
	private StorageService storageService;

	@Autowired
	private TemplateRepository templateRepository;

	@Override
	@Transactional
	public void createTemplate(String templateName, String type, Document document, byte[] content)
			throws TemplateProcessingException, DocumentProcessingException {
		Assert.hasLength(templateName, "Template name cannot be empty!");
		Assert.hasLength(type, "Template name cannot be empty!");
		Assert.notNull(content, "Content cannot be null!");
		Assert.isTrue(content.length > 0, "Content cannot be empty!");
		Assert.notNull(document, "Document cannot be null!");

		long count = templateRepository.countByNameAndType(templateName, type);

		if (type.equalsIgnoreCase("Specific-Purpose") || type.equalsIgnoreCase("Standard-Request-Form")
				|| type.equalsIgnoreCase("Signature-Page")) {
			if (templateRepository.countByType(type) == 1) {
				throw new TemplateProcessingException("Only single template of given type is allowed!");
			}
		}
		if (count == 0) {
			document.setCategory("TEMPLATE");
			document.setContentType("text/markdown");
			String documentGuid = storageService.createDocument(document, content);
			Template template = new Template();
			template.setGuid(UUID.randomUUID().toString());
			template.setName(templateName);
			template.setType(type);
			template.setDocumentGuid(documentGuid);
			templateRepository.save(template);
		} else {
			throw new DuplicateKeyException("Template with same name and type already exists!");
		}
	}

	@Override
	@Transactional
	public void updateTemplate(String templateId, Document document, byte[] content)
			throws DocumentProcessingException, DocumentNotFoundException, TemplateNotFoundException {
		Assert.hasLength(templateId, "Template Id cannot be empty!");
		Assert.notNull(document, "Document cannot be null!");
		Assert.notNull(content, "Content cannot be null!");
		Assert.isTrue(content.length > 0, "Content cannot be empty!");

		// check template and document both exist.
		Template template = templateRepository.findById(templateId)
				.orElseThrow(() -> new TemplateNotFoundException("Template not found."));
		if (template != null) {
			document.setId(template.getDocumentGuid());
			document.setCategory("TEMPLATE");
			document.setContentType("text/markdown");
			storageService.updateDocument(document, content);
		}
	}

	@Override
	@Transactional
	public Template getTemplateByNameAndType(String templateName, String type) {
		Assert.hasLength(templateName, "Template name cannot be empty!");
		Assert.hasLength(type, "Template type cannot be empty!");

		// ideally should be one template each template name and type pair.
		List<Template> templates = templateRepository.findByNameAndType(templateName, type);
		Assert.isTrue(templates.size() > 0, "No template found for given name: " + templateName + " and type: " + type);
		Assert.isTrue(templates.size() == 1,
				"More than 1 template found for given name: " + templateName + " and type: " + type);
		return templates.iterator().next();
	}

	@Override
	@Transactional
	public Template getRequestFormTemplate() {
		// ideally should be one template for request form.
		List<Template> templates = templateRepository.findByType("Standard-Request-Form");
		Assert.isTrue(templates.size() > 0, "No template found for type: Standard-Request-Form");
		Assert.isTrue(templates.size() == 1, "More than 1 template found for type: Standard-Request-Form");
		return templates.iterator().next();
	}

	@Override
	@Transactional
	public Template getSignaturePageTemplate() {
		// ideally should be one template for request form.
		List<Template> templates = templateRepository.findByType("Signature-Page");
		Assert.isTrue(templates.size() > 0, "No template found for type: Signature-Page");
		Assert.isTrue(templates.size() == 1, "More than 1 template found for type: Signature-Page");
		return templates.iterator().next();
	}

	@Override
	@Transactional
	public Template getSpecificPurposeTemplate() {
		// ideally should be one template for request form.
		List<Template> templates = templateRepository.findByType("Specific-Purpose");
		Assert.isTrue(templates.size() > 0, "No template found for type: Specific-Purpose");
		Assert.isTrue(templates.size() == 1, "More than 1 template found for type: Specific-Purpose");
		return templates.iterator().next();
	}

	@Override
	@Transactional
	public byte[] getTemplateContent(String templateId)
			throws DocumentProcessingException, DocumentNotFoundException, TemplateNotFoundException {
		Assert.hasLength(templateId, "Template Id cannot be empty!");

		Template template = templateRepository.findById(templateId)
				.orElseThrow(() -> new TemplateNotFoundException("Template not found, ID: " + templateId));
		String documentGuid = template.getDocumentGuid();
		return storageService.getDocumentContentById(documentGuid);
	}

	@Override
	@Transactional
	public byte[] getTemplateVersionContent(String templateId, String versionId)
			throws DocumentProcessingException, DocumentNotFoundException, TemplateNotFoundException {
		Assert.hasLength(templateId, "Template Id cannot be empty!");
		Assert.hasLength(versionId, "Template Version Id cannot be empty!");

		Template template = templateRepository.findById(templateId)
				.orElseThrow(() -> new TemplateNotFoundException("Template not found, ID: " + templateId));
		String documentGuid = template.getDocumentGuid();
		return storageService.getContentByVersionId(documentGuid, versionId);
	}

	@Override
	@Transactional
	public void setTemplateVersionAsActive(String templateId, String versionId)
			throws DocumentNotFoundException, TemplateNotFoundException {
		Assert.hasLength(templateId, "Template Id cannot be empty!");
		Assert.hasLength(versionId, "Template Version Id cannot be empty!");

		Template template = templateRepository.findById(templateId)
				.orElseThrow(() -> new TemplateNotFoundException("Template not found, ID: " + templateId));
		String documentGuid = template.getDocumentGuid();
		storageService.setActiveVersion(documentGuid, versionId);
	}

	@Override
	@Transactional
	public List<Template> listAllTemplates() {
		return templateRepository.findAll();
	}

	@Override
	@Transactional
	public List<Document> listAllTemplateVersions(String templateId)
			throws DocumentNotFoundException, TemplateNotFoundException {
		Assert.hasLength(templateId, "Template Id cannot be empty!");

		Template template = templateRepository.findById(templateId)
				.orElseThrow(() -> new TemplateNotFoundException("Template not found, ID: " + templateId));
		String documentGuid = template.getDocumentGuid();
		return storageService.listAllDocumentVersions(documentGuid);
	}

	@Override
	@Transactional
	public Template getTemplateById(String templateId) throws TemplateNotFoundException {
		Assert.hasLength(templateId, "Template Id cannot be empty!");
		return templateRepository.findById(templateId)
				.orElseThrow(() -> new TemplateNotFoundException("Template not found, ID: " + templateId));
	}

	@Override
	@Transactional
	public void deleteTemplatePermanently(String templateId) throws TemplateNotFoundException {
		Assert.hasLength(templateId, "Template Id cannot be empty!");

		Template template = templateRepository.findById(templateId)
				.orElseThrow(() -> new TemplateNotFoundException("Template not found, ID: " + templateId));
		String documentGuid = template.getDocumentGuid();
		storageService.deleteDocument(documentGuid);
		templateRepository.deleteById(templateId);
	}

}
