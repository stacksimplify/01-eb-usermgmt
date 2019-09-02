package com.utc.nda.template.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.access.prepost.PreAuthorize;

import com.utc.nda.template.entities.Template;

@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MODERATOR','ROLE_ADMIN')")
public interface TemplateRepository extends JpaRepository<Template, String> {
	
	long countByNameAndType(String name, String type);
	long countByType(String type);
	long countByGuid(String id);
	List<Template> findByNameAndType(String name, String type);
	List<Template> findByType(String type);
	
	@Query("select Distinct t.name from Template t where t.type='One-Way' or t.type='Mutual'")
	List<String> findDistinctNames();
}
