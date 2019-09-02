package com.utc.nda.template.controllers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.utc.nda.storage.dtos.Document;
import com.utc.nda.storage.services.DocumentNotFoundException;
import com.utc.nda.storage.services.DocumentProcessingException;
import com.utc.nda.template.entities.Template;
import com.utc.nda.template.repositories.TemplateRepository;
import com.utc.nda.template.services.TemplateNotFoundException;
import com.utc.nda.template.services.TemplateProcessingException;
import com.utc.nda.template.services.TemplateService;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.options.MutableDataSet;

@Controller
public class TemplateController {
	private static final Logger logger = LoggerFactory.getLogger(TemplateController.class);

	@Autowired
	private TemplateService templateService;

	@Autowired
	private TemplateRepository templateRepository;

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@RequestMapping(value = "/template", method = RequestMethod.POST)
	public void createTemplate(@RequestParam("templateName") String templateName,
			@RequestParam("templateType") String type, @RequestParam("file") MultipartFile file,
			HttpServletResponse response) throws IOException, DocumentProcessingException, TemplateProcessingException {

		if (file.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Invalid Template, template doesn't have any content!");
		}

		byte[] content = file.getBytes();
		String fileName = "Template.md";
		fileName = file.getOriginalFilename();
		Document document = new Document();
		document.setName(fileName);

		templateService.createTemplate(templateName, type, document, content);
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@RequestMapping(value = "/template/version/import", method = RequestMethod.POST)
	public @ResponseBody void updateTemplate(@RequestParam("templateId") String templateId,
			@RequestParam("file") MultipartFile file)
			throws IOException, DocumentProcessingException, DocumentNotFoundException, TemplateNotFoundException {

		if (file.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Invalid Template, template doesn't have any content!");
		}

		byte[] content = file.getBytes();
		String fileName = "Template.md";
		fileName = file.getOriginalFilename();

		Document document = new Document();
		document.setName(fileName);
		templateService.updateTemplate(templateId, document, content);
	}

	@PreAuthorize("hasAnyRole('ROLE_MODERATOR','ROLE_ADMIN')")
	@RequestMapping(value = "/template/{templateId}/download", method = RequestMethod.GET)
	public void downloadTemplate(@PathVariable("templateId") String templateId, HttpServletResponse response)
			throws SQLException, IOException, DocumentProcessingException, TemplateNotFoundException,
			DocumentNotFoundException {

		ByteArrayInputStream is = new ByteArrayInputStream(templateService.getTemplateContent(templateId));
		response.setContentType("text/markdown");
		String fileName = "Template-" + templateId + ".md";
		response.setHeader("Content-disposition", "attachment; filename=" + fileName);
		org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
		response.flushBuffer();
	}

	@PreAuthorize("hasAnyRole('ROLE_MODERATOR','ROLE_ADMIN')")
	@RequestMapping(value = "/template/{templateId}/version/{versionId}/download", method = RequestMethod.GET)
	public void downloadTemplateVersion(@PathVariable("templateId") String templateId,
			@PathVariable("versionId") String versionId, HttpServletResponse response) throws SQLException, IOException,
			DocumentProcessingException, DocumentNotFoundException, TemplateNotFoundException {

		ByteArrayInputStream is = new ByteArrayInputStream(
				templateService.getTemplateVersionContent(templateId, versionId));
		response.setContentType("text/markdown");
		String fileName = "Template-" + templateId + "-" + versionId + ".md";
		response.setHeader("Content-disposition", "attachment; filename=" + fileName);
		org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
		response.flushBuffer();
	}

	@PreAuthorize("hasAnyRole('ROLE_MODERATOR','ROLE_ADMIN')")
	@RequestMapping(value = "/template/{templateId}/view", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<?> getTemplateContent(@PathVariable("templateId") String templateId)
			throws SQLException, IOException, DocumentProcessingException, TemplateNotFoundException,
			DocumentNotFoundException {

		String inputMarkdown = new String(templateService.getTemplateContent(templateId));

		MutableDataSet options = new MutableDataSet().set(TablesExtension.COLUMN_SPANS, true)
				.set(TablesExtension.APPEND_MISSING_COLUMNS, true).set(TablesExtension.DISCARD_EXTRA_COLUMNS, true)
				.set(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, false)
				.set(TablesExtension.FORMAT_TABLE_TRIM_CELL_WHITESPACE, false)
				.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create()));
		Parser parser = Parser.builder(options).build();
		// options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create()));

		// uncomment to convert soft-breaks to hard breaks
		options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");

		HtmlRenderer renderer = HtmlRenderer.builder(options).build();

		Node document = parser.parse(inputMarkdown);
		String html = renderer.render(document);
		html = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">\n" + "<style>\n" + "table {\n" + "    width:100%;\n"
				+ "}\n" + "</style>" + "</head><body>" + html + "\n" + "</body></html>";
		return ResponseEntity.status(200).body(html);
	}

	@PreAuthorize("hasAnyRole('ROLE_MODERATOR','ROLE_ADMIN')")
	@RequestMapping(value = "/templates", method = RequestMethod.GET)
	public @ResponseBody List<Template> getAllTemplates() {
		return templateService.listAllTemplates();
	}

	@PreAuthorize("hasAnyRole('ROLE_MODERATOR','ROLE_ADMIN')")
	@RequestMapping(value = "/template/{templateId}/versions", method = RequestMethod.GET)
	public @ResponseBody List<Document> getAllTemplateVersions(@PathVariable("templateId") String templateId)
			throws DocumentNotFoundException, TemplateNotFoundException {
		return templateService.listAllTemplateVersions(templateId);
	}

	@PreAuthorize("hasAnyRole('ROLE_MODERATOR','ROLE_ADMIN')")
	@RequestMapping(value = "/template/{templateId}/version/{versionId}/view", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<?> getTemplateVersionContent(@PathVariable("templateId") String templateId,
			@PathVariable("versionId") String versionId)
			throws DocumentProcessingException, DocumentNotFoundException, TemplateNotFoundException {

		String inputMarkdown = new String(templateService.getTemplateVersionContent(templateId, versionId));
		MutableDataSet options = new MutableDataSet().set(TablesExtension.COLUMN_SPANS, true)
				.set(TablesExtension.APPEND_MISSING_COLUMNS, true).set(TablesExtension.DISCARD_EXTRA_COLUMNS, true)
				.set(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, false)
				.set(TablesExtension.FORMAT_TABLE_TRIM_CELL_WHITESPACE, false)
				.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create()));
		Parser parser = Parser.builder(options).build();
		// options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create()));

		// uncomment to convert soft-breaks to hard breaks
		options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");

		HtmlRenderer renderer = HtmlRenderer.builder(options).build();

		Node document = parser.parse(inputMarkdown);
		String html = renderer.render(document);
		html = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">\n" + "<style>\n" + "table {\n" + "    width:100%;\n"
				+ "}\n" + "</style>" + "</head><body>" + html + "\n" + "</body></html>";
		return ResponseEntity.status(200).body(html);
	}

	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MODERATOR','ROLE_ADMIN')")
	@RequestMapping(value = "/business-units", method = RequestMethod.GET)
	public @ResponseBody List<String> getAllBusinessUnits() {
		return templateRepository.findDistinctNames();
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@RequestMapping(value = "/template/{templateId}/version/{versionId}/mark-active", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<?> setTemplateVersionAsActive(@PathVariable("templateId") String templateId,
			@PathVariable("versionId") String versionId) throws DocumentNotFoundException, TemplateNotFoundException {

		templateService.setTemplateVersionAsActive(templateId, versionId);
		HashMap<String, String> response = new HashMap<>();
		response.put("message", "Operation successful.");
		return ResponseEntity.status(200).body(response);
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@RequestMapping(value = "/template/{templateId}/delete-permanently", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<?> deleteTemplatePermanently(@PathVariable("templateId") String templateId)
			throws TemplateNotFoundException {

		templateService.deleteTemplatePermanently(templateId);
		HashMap<String, String> response = new HashMap<>();
		response.put("message", "Template deleted successfully.");
		return ResponseEntity.status(200).body(response);
	}

}
