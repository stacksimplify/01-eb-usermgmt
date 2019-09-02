package com.utc.nda.setting.services;

import com.utc.nda.setting.entities.Setting;

public interface SettingService {

	public Setting getSetting();

	public void refreshSettings();

	public void saveSetting(Setting setting);

}
