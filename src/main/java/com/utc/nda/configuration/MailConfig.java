package com.utc.nda.configuration;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.utc.nda.setting.services.SettingService;

@Configuration
public class MailConfig {

	@Autowired
	private SettingService settingService;

	@Bean
	public JavaMailSender getMailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

		if (settingService.getSetting().getMailServerAuth().equalsIgnoreCase("true")) {
			mailSender.setUsername(settingService.getSetting().getMailServerUsername());
			mailSender.setPassword(settingService.getSetting().getMailServerPassword());
		}

		mailSender.setHost(settingService.getSetting().getMailServerHost());
		mailSender.setPort(Integer.parseInt(settingService.getSetting().getMailServerPort()));

		Properties javaMailProperties = new Properties();
		javaMailProperties.put("mail.smtp.starttls.enable", settingService.getSetting().getMailServerEnableTls());
		javaMailProperties.put("mail.smtp.auth", settingService.getSetting().getMailServerAuth());
		javaMailProperties.put("mail.transport.protocol", settingService.getSetting().getMailTransportProtocol());
		javaMailProperties.put("mail.debug", "false");// Prints out everything on screen

		mailSender.setJavaMailProperties(javaMailProperties);
		return mailSender;
	}
}