package com.utc.nda.setting.controllers;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.utc.nda.setting.entities.Setting;
import com.utc.nda.setting.services.SettingService;

@RestController
public class SettingController {

	@Autowired
	private SettingService settingService;

	private static final Logger logger = LoggerFactory.getLogger(SettingController.class);

	@RequestMapping(value = "/setting", method = RequestMethod.POST)
	public void addSetting(@RequestBody @Valid Setting setting) {
		settingService.saveSetting(setting);
	}

	@RequestMapping(value = "/setting", method = RequestMethod.GET)
	public Setting getSetting() {
		return settingService.getSetting();
	}
}
