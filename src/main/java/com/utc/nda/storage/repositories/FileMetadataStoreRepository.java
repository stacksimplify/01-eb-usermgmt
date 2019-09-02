package com.utc.nda.storage.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.utc.nda.storage.entities.FileMetadataStore;
import com.utc.nda.storage.entities.FileStoreId;

public interface FileMetadataStoreRepository extends JpaRepository<FileMetadataStore, FileStoreId> {
	
	public List<FileMetadataStore> findByFileStoreIdId(String id); 
	public void deleteByIdId(String id);
}

