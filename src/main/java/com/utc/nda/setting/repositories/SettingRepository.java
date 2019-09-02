package com.utc.nda.setting.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.utc.nda.setting.entities.Setting;

public interface SettingRepository extends JpaRepository<Setting, String>{
	
}
