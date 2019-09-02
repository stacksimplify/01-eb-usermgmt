package com.utc.nda.storage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.utc.nda.storage.entities.FileStore;
import com.utc.nda.storage.entities.FileStoreId;

public interface FileStoreRepository extends JpaRepository<FileStore, FileStoreId> {
	public void deleteByIdId(String id);
}
