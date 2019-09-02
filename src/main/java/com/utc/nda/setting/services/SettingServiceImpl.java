package com.utc.nda.setting.services;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.utc.nda.setting.entities.Setting;
import com.utc.nda.setting.repositories.SettingRepository;

@Service
@Scope("singleton")
public class SettingServiceImpl implements SettingService {

	private static final Logger logger = LoggerFactory.getLogger(SettingServiceImpl.class);

	private static Setting setting;
	private SettingRepository settingRepository;
	private final String settingId = "4908963b-dd9a-426d-b00e-9e99a80d3ca2";

	SettingServiceImpl(SettingRepository settingRepository) {
		logger.info("Initializing settings.");
		this.settingRepository = settingRepository;
		setting = settingRepository.findById(settingId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Setting not found."));
	}

	@Override
	public Setting getSetting() {
		return setting;
	}

	@Override
	@Transactional
	public void refreshSettings() {
		logger.info("Refreshing settings.");
		setting = settingRepository.findById(settingId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Setting not found."));
	}

	@Override
	@Transactional
	public void saveSetting(Setting setting1) {
		logger.info("Saving settings.");
		setting1.setGuid(settingId);
		settingRepository.save(setting1);
		refreshSettings();
	}

}
