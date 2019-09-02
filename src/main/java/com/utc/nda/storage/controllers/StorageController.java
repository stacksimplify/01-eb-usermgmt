package com.utc.nda.storage.controllers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.utc.nda.storage.dtos.Document;
import com.utc.nda.storage.services.DocumentNotFoundException;
import com.utc.nda.storage.services.DocumentProcessingException;
import com.utc.nda.storage.services.StorageService;

@Controller
@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
public class StorageController {

	@Autowired
	private StorageService storageService;

	private static final Logger logger = LoggerFactory.getLogger(StorageController.class);

	@RequestMapping(value = "/document", method = RequestMethod.POST)
	public void addDocument(@RequestParam("file") MultipartFile file) throws IOException, DocumentProcessingException {

		Assert.isTrue(!file.isEmpty(), "Invalid file, file doesn't have any content!");

		byte[] content = file.getBytes();
		String fileName = "Document";
		fileName = file.getOriginalFilename();

		Document document = new Document();
		document.setName(fileName);
		document.setCategory("DOCUMENT");
		document.setContentType("application/octet-stream");
		storageService.createDocument(document, content);
	}

	@RequestMapping(value = "/document/{id}", method = RequestMethod.GET)
	public void getDocument(@PathVariable("id") String id, HttpServletResponse response)
			throws SQLException, IOException, DocumentProcessingException, DocumentNotFoundException {

		Assert.hasLength(id, "Id cannot be empty!");
		ByteArrayInputStream is = new ByteArrayInputStream(storageService.getDocumentContentById(id));
		Document document = storageService.getDocumentMetadataById(id);
		response.setContentType(document.getContentType());
		response.setHeader("Content-disposition", "attachment; filename=" + document.getName());
		org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
		response.flushBuffer();
	}

	@RequestMapping(value = "/document/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteDocument(@PathVariable("id") String id) {
		Assert.hasLength(id, "Id cannot be empty!");
		storageService.deleteDocument(id);
		return ResponseEntity.status(200).body("Document deleted.");
	}

	@RequestMapping(value = "/documents", method = RequestMethod.GET)
	public ResponseEntity<?> getAllDocuments() {
		return ResponseEntity.status(200).body(storageService.listAllDocuments());
	}

}
