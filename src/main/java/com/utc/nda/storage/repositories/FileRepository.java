package com.utc.nda.storage.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.utc.nda.storage.entities.File;

public interface FileRepository extends JpaRepository<File, String>{
	
	 List<File> findAllByCategory(String category);

}
