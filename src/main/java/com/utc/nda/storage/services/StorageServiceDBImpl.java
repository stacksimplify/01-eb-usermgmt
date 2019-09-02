package com.utc.nda.storage.services;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.sql.rowset.serial.SerialBlob;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.utc.nda.storage.dtos.Document;
import com.utc.nda.storage.entities.File;
import com.utc.nda.storage.entities.FileMetadataStore;
import com.utc.nda.storage.entities.FileStore;
import com.utc.nda.storage.entities.FileStoreId;
import com.utc.nda.storage.repositories.FileMetadataStoreRepository;
import com.utc.nda.storage.repositories.FileRepository;
import com.utc.nda.storage.repositories.FileStoreRepository;

@Service
public class StorageServiceDBImpl implements StorageService {

	private static final Logger logger = LoggerFactory.getLogger(StorageServiceDBImpl.class);

	@Autowired
	private FileStoreRepository fileStoreRepository;

	@Autowired
	private FileMetadataStoreRepository fileMetadataStoreRepository;

	@Autowired
	private FileRepository fileRepository;

	@Override
	@Transactional
	public String createDocument(Document document, byte[] content) throws DocumentProcessingException {
		logger.info("Creating new document.");
		Assert.notNull(document, "Document cannot be null!");
		Assert.hasLength(document.getName(), "Document name cannot be empty!");
		Assert.hasLength(document.getContentType(), "Document content type cannot be empty!");
		Assert.notNull(content, "Content cannot be null!");
		Assert.isTrue(content.length > 0, "Content cannot be empty!");

		String id = UUID.randomUUID().toString();
		String versionId = UUID.randomUUID().toString();

		FileStoreId storeId = new FileStoreId(id, versionId);
		FileStore fileStore = new FileStore();
		fileStore.setId(storeId);

		try {
			fileStore.setContent(new SerialBlob(content));
		} catch (SQLException e) {
			throw new DocumentProcessingException("Error encountered in creating new document, ", e);
		}

		Date creationTime = new Date();
		Date modificationTime = creationTime;

		FileMetadataStore fileMetadataStore = new FileMetadataStore();
		fileMetadataStore.setId(storeId);
		fileMetadataStore.setName(document.getName());
		fileMetadataStore.setContentType(document.getContentType());
		fileMetadataStore.setSize(content.length);
		fileMetadataStore.setCreationTime(creationTime);
		fileMetadataStore.setModificationTime(modificationTime);
		fileMetadataStore.setFileStore(fileStore);

		File file = new File();
		file.setId(id);
		file.setActiveVersion(versionId);
		file.setCategory(document.getCategory());// category to identity document types like templates, signed-docs etc
		file.setName(document.getName()); // set document name as version 1 name.
		file.setSize(content.length);
		file.setContentType(document.getContentType());
		file.setCreationTime(creationTime);
		file.setModificationTime(modificationTime);

		fileStoreRepository.save(fileStore);
		fileMetadataStoreRepository.save(fileMetadataStore);
		fileRepository.save(file);
		logger.info("Document created successfully. ID: " + id + " , version ID: " + versionId);
		return id;
	}

	@Override
	@Transactional
	public void updateDocument(Document document, byte[] content)
			throws DocumentProcessingException, DocumentNotFoundException {
		logger.info("Updating document.");
		// new version is created when document is updated.
		// document name remains same as earlier, version will have new name provided.
		// to update document name use rename api.
		Assert.notNull(document, "Document cannot be null!");
		Assert.hasLength(document.getId(), "Document Id cannot be empty!");
		Assert.hasLength(document.getName(), "Document name cannot be empty!");
		Assert.hasLength(document.getContentType(), "Document content type cannot be empty!");
		Assert.notNull(content, "Content cannot be null!");
		Assert.isTrue(content.length > 0, "Content cannot be empty!");

		String versionId = UUID.randomUUID().toString();

		FileStoreId storeId = new FileStoreId(document.getId(), versionId);
		FileStore fileStore = new FileStore();
		fileStore.setId(storeId);

		try {
			fileStore.setContent(new SerialBlob(content));
		} catch (SQLException e) {
			throw new DocumentProcessingException("Error encountered in updating document, ", e);
		}

		Date creationTime = new Date();
		Date modificationTime = creationTime;

		FileMetadataStore fileMetadataStore = new FileMetadataStore();
		fileMetadataStore.setId(storeId);
		fileMetadataStore.setName(document.getName()); // version can have its own name.
		fileMetadataStore.setContentType(document.getContentType());
		fileMetadataStore.setSize(content.length);
		fileMetadataStore.setCreationTime(creationTime);
		fileMetadataStore.setModificationTime(modificationTime);
		fileMetadataStore.setFileStore(fileStore);

		// file name and creation time remains as is when version changes
		File file = fileRepository.findById(document.getId())
				.orElseThrow(() -> new DocumentNotFoundException("Document not found. ID: " + document.getId()));
		file.setActiveVersion(versionId);
		file.setContentType(document.getContentType());
		file.setSize(content.length);
		file.setModificationTime(modificationTime);

		fileStoreRepository.save(fileStore);
		fileMetadataStoreRepository.save(fileMetadataStore);
		fileRepository.save(file);
		logger.info("Document updated successfully. ID: " + storeId.getId() + " , version ID: " + storeId.getVersion());
	}

	@Override
	@Transactional
	public void renameDocument(String id, String name) throws DocumentNotFoundException {
		Assert.hasLength(id, "Document Id cannot be empty!");
		Assert.hasLength(name, "Document name cannot be empty!");

		File file = fileRepository.findById(id)
				.orElseThrow(() -> new DocumentNotFoundException("Document not found. ID: " + id));
		;
		file.setName(name);
		file.setModificationTime(new Date());
		fileRepository.save(file);
	}

	@Override
	@Transactional
	public void deleteDocument(String id) {
		Assert.hasLength(id, "Document Id cannot be empty!");
		fileMetadataStoreRepository.deleteByIdId(id);
		fileStoreRepository.deleteByIdId(id);
		fileRepository.deleteById(id);
	}

	@Override
	@Transactional
	public void setActiveVersion(String id, String versionId) throws DocumentNotFoundException {
		Assert.hasLength(id, "Document Id cannot be empty!");
		Assert.hasLength(versionId, "Document Version Id cannot be empty!");

		// check file and version exists
		File file = fileRepository.findById(id)
				.orElseThrow(() -> new DocumentNotFoundException("Document not found. ID: " + id));
		;
		if (file.getActiveVersion().equalsIgnoreCase(versionId)) {
			return; // version already active.
		} else {
			FileStoreId fileStoreId = new FileStoreId(id, versionId);
			FileMetadataStore metadata = fileMetadataStoreRepository.findById(fileStoreId).orElseThrow(
					() -> new DocumentNotFoundException("Document metadata not found. " + fileStoreId.toString()));
			metadata.setModificationTime(new Date());
			fileMetadataStoreRepository.save(metadata);

			file.setActiveVersion(versionId);
			file.setModificationTime(new Date());
			fileRepository.save(file);
		}
	}

	@Override
	@Transactional
	public Document getDocumentMetadataById(String id) throws DocumentNotFoundException {
		Assert.hasLength(id, "Document Id cannot be empty!");
		File file = fileRepository.findById(id)
				.orElseThrow(() -> new DocumentNotFoundException("Document not found. ID: " + id));
		Document doc = new Document();
		doc.setId(file.getId());
		doc.setVersionId(file.getActiveVersion());
		doc.setCategory(file.getCategory());
		doc.setName(file.getName());
		doc.setSize(file.getSize());
		doc.setContentType(file.getContentType());
		doc.setCreationTime(file.getCreationTime());
		doc.setModificationTime(file.getModificationTime());
		return doc;
	}

	@Override
	@Transactional
	public byte[] getDocumentContentById(String id) throws DocumentProcessingException, DocumentNotFoundException {
		logger.info("Fetching document ID: " + id);
		Assert.hasLength(id, "Document Id cannot be empty!");
		File file = fileRepository.findById(id)
				.orElseThrow(() -> new DocumentNotFoundException("Document not found. ID: " + id));
		FileStoreId fileStoreId = new FileStoreId(id, file.getActiveVersion());
		FileStore fileStore = fileStoreRepository.findById(fileStoreId).orElseThrow(
				() -> new DocumentNotFoundException("Document content not found. " + fileStoreId.toString()));
		byte[] content = null;
		try {
			content = fileStore.getContent().getBytes(1l, (int) fileStore.getContent().length());
		} catch (SQLException e) {
			throw new DocumentProcessingException("Error encountered in fetching document, ", e);
		}
		return content;

	}

	@Override
	@Transactional
	public List<Document> listAllDocumentsByCategory(String category) {
		Assert.hasLength(category, "Category cannot be empty!");
		List<Document> documents = fileRepository.findAllByCategory(category).stream().map(file -> {
			Document d = new Document();
			d.setId(file.getId());
			d.setVersionId(file.getActiveVersion());
			d.setCategory(file.getCategory());
			d.setName(file.getName());
			d.setSize(file.getSize());
			d.setContentType(file.getContentType());
			d.setCreationTime(file.getCreationTime());
			d.setModificationTime(file.getModificationTime());
			return d;
		}).collect(Collectors.toList());
		return documents;
	}

	@Override
	@Transactional
	public Document getMetadataByVersionId(String id, String versionId) throws DocumentNotFoundException {
		Assert.hasLength(id, "Document Id cannot be empty!");
		Assert.hasLength(versionId, "Document Version Id cannot be empty!");

		FileStoreId storeId = new FileStoreId(id, versionId);
		File file = fileRepository.findById(id)
				.orElseThrow(() -> new DocumentNotFoundException("Document not found. ID: " + id));
		FileMetadataStore metadata = fileMetadataStoreRepository.findById(storeId)
				.orElseThrow(() -> new DocumentNotFoundException("Document metadata not found. " + storeId.toString()));
		Document d = new Document();
		d.setId(metadata.getId().getId());
		d.setVersionId(metadata.getId().getVersion());
		d.setCategory(file.getCategory());
		d.setName(metadata.getName());
		d.setSize(metadata.getSize());
		d.setContentType(metadata.getContentType());
		d.setCreationTime(metadata.getCreationTime());
		d.setModificationTime(metadata.getModificationTime());
		return d;
	}

	@Override
	@Transactional
	public byte[] getContentByVersionId(String id, String versionId)
			throws DocumentProcessingException, DocumentNotFoundException {
		Assert.hasLength(id, "Document Id cannot be empty!");
		Assert.hasLength(versionId, "Document Version Id cannot be empty!");

		FileStoreId fileStoreId = new FileStoreId(id, versionId);
		FileStore fileStore = fileStoreRepository.findById(fileStoreId).orElseThrow(
				() -> new DocumentNotFoundException("Document content not found. " + fileStoreId.toString()));
		byte[] content = null;
		try {
			content = fileStore.getContent().getBytes(1l, (int) fileStore.getContent().length());
		} catch (SQLException e) {
			throw new DocumentProcessingException("Error encountered in fetching document version, ", e);
		}
		return content;
	}

	@Override
	@Transactional
	public List<Document> listAllDocumentVersions(String id) throws DocumentNotFoundException {
		Assert.hasLength(id, "Document Id cannot be empty!");

		File file = fileRepository.findById(id)
				.orElseThrow(() -> new DocumentNotFoundException("Document not found. ID: " + id));
		List<Document> documents = fileMetadataStoreRepository.findByFileStoreIdId(id).stream().map(fileMetaData -> {
			Document d = new Document();
			d.setId(fileMetaData.getId().getId());
			d.setVersionId(fileMetaData.getId().getVersion());
			d.setCategory(file.getCategory());
			d.setName(fileMetaData.getName());
			d.setSize(fileMetaData.getSize());
			d.setContentType(fileMetaData.getContentType());
			d.setCreationTime(fileMetaData.getCreationTime());
			d.setModificationTime(fileMetaData.getModificationTime());
			return d;
		}).collect(Collectors.toList());
		return documents;
	}

	@Override
	@Transactional
	public List<Document> listAllDocuments() {
		List<Document> documents = fileRepository.findAll().stream().map(file -> {
			Document d = new Document();
			d.setId(file.getId());
			d.setVersionId(file.getActiveVersion());
			d.setCategory(file.getCategory());
			d.setName(file.getName());
			d.setSize(file.getSize());
			d.setContentType(file.getContentType());
			d.setCreationTime(file.getCreationTime());
			d.setModificationTime(file.getModificationTime());
			return d;
		}).collect(Collectors.toList());
		return documents;
	}

	@Override
	@Transactional
	public Document getDocumentById(String id) throws DocumentNotFoundException {
		Assert.hasLength(id, "Document Id cannot be empty!");

		File file = fileRepository.findById(id)
				.orElseThrow(() -> new DocumentNotFoundException("Document not found. ID: " + id));
		Document d = new Document();
		d.setId(file.getId());
		d.setVersionId(file.getActiveVersion());
		d.setCategory(file.getCategory());
		d.setName(file.getName());
		d.setSize(file.getSize());
		d.setContentType(file.getContentType());
		d.setCreationTime(file.getCreationTime());
		d.setModificationTime(file.getModificationTime());
		return d;
	}

}
