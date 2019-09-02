package com.utc.nda.storage.services;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import com.utc.nda.storage.dtos.Document;

public interface StorageService {

	// @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MODERATOR','ROLE_ADMIN')")
	public String createDocument(Document document, byte[] content) throws DocumentProcessingException;

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	public void updateDocument(Document document, byte[] content)
			throws DocumentProcessingException, DocumentNotFoundException;

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	public void renameDocument(String id, String name) throws DocumentNotFoundException;

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	public void deleteDocument(String id);

	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MODERATOR','ROLE_ADMIN')")
	public Document getDocumentMetadataById(String id) throws DocumentNotFoundException;

	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MODERATOR','ROLE_ADMIN')")
	public byte[] getDocumentContentById(String id) throws DocumentProcessingException, DocumentNotFoundException;

	@PreAuthorize("hasAnyRole('ROLE_MODERATOR','ROLE_ADMIN')")
	public List<Document> listAllDocumentsByCategory(String category);

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	public List<Document> listAllDocuments();

	@PreAuthorize("hasAnyRole('ROLE_MODERATOR','ROLE_ADMIN')")
	public Document getMetadataByVersionId(String id, String versionId) throws DocumentNotFoundException;

	@PreAuthorize("hasAnyRole('ROLE_MODERATOR','ROLE_ADMIN')")
	public byte[] getContentByVersionId(String id, String versionId)
			throws DocumentProcessingException, DocumentNotFoundException;

	@PreAuthorize("hasAnyRole('ROLE_MODERATOR','ROLE_ADMIN')")
	public List<Document> listAllDocumentVersions(String id) throws DocumentNotFoundException;

	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MODERATOR','ROLE_ADMIN')")
	public Document getDocumentById(String id) throws DocumentNotFoundException;

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	public void setActiveVersion(String id, String versionId) throws DocumentNotFoundException;

}
