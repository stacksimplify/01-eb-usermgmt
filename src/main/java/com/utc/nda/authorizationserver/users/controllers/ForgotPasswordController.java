package com.utc.nda.authorizationserver.users.controllers;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.utc.nda.authorizationserver.users.entities.Verification;
import com.utc.nda.authorizationserver.users.entities.User;
import com.utc.nda.authorizationserver.users.repositories.VerificationRepository;
import com.utc.nda.authorizationserver.users.repositories.UserRepository;
import com.utc.nda.authorizationserver.users.services.UserService;
import com.utc.nda.notification.dtos.EmailMessage;
import com.utc.nda.notification.dtos.NotificationMessage;
import com.utc.nda.notification.services.NotificationService;
import com.utc.nda.setting.services.SettingService;

@RestController
public class ForgotPasswordController {

	@Autowired
	private SettingService settingService;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private VerificationRepository forgotPasswordRepository;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private UserService userService;

	@PostMapping("/public/password/forgot/update/{id}")
	@ResponseStatus(HttpStatus.OK)
	public void updatePassword(@PathVariable("id") String id, @RequestBody String jsonRequest) {

		JSONObject requestJson = new JSONObject(jsonRequest);
		String password = requestJson.get("password").toString().trim();

		Verification fp = forgotPasswordRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid token."));

		if (userService.matchesPolicy(password) == false) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Invalid password, password must have atleast 1 upper case character, atleast 1 lower case character, atleast 1 digit, atleast 1 special character and minimum length 8.");
		}

		if (fp.isTokenUsed()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN,
					"Password reset token expired, valid for single use only!");
		}

		long forgotPasswordLinkExpiryTimeInSeconds = Long
				.valueOf(settingService.getSetting().getForgotPasswordLinkExpiryTimeInSeconds());
		if (((new Date()).getTime() - fp.getCreationTime().getTime()) > forgotPasswordLinkExpiryTimeInSeconds * 1000) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Password reset token expired!");
		}

		String userName = fp.getUserName();
		User user = userRepository.findById(userName)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
		user.setPassword(passwordEncoder.encode(password));
		userRepository.save(user);
		fp.setModificationTime(new Date());
		fp.setTokenUsed(true);
		forgotPasswordRepository.save(fp);

		// Send Portal Notifications
		String userNotificationContent = settingService.getSetting().getNotificationContentForgotPasswordCompletion();
		String notificationTitle = settingService.getSetting().getNotificationTitleForgotPasswordCompletion();
		String notificationType = "Password-Reset";

		// Notification to user.
		NotificationMessage userNotification = new NotificationMessage();
		userNotification.setTitle(notificationTitle);
		userNotification.setContent(userNotificationContent);
		userNotification.setType(notificationType);
		userNotification.setAttributes(null);
		userNotification.setToUsers(Arrays.asList(user.getUsername()));
		notificationService.sendNotification(userNotification);

		String content = String.format(settingService.getSetting().getEmailContentForgotPasswordCompletion(),
				user.getFirstname());

		EmailMessage userEmailMessage = new EmailMessage();
		userEmailMessage.setSubject(settingService.getSetting().getEmailSubjectForgotPasswordCompletion());
		userEmailMessage.setContent(content);
		userEmailMessage.setToEmails(Arrays.asList(user.getEmail()));
		notificationService.sendEmail(userEmailMessage);
	}

	@GetMapping("/public/password/forgot/initiate")
	@ResponseStatus(HttpStatus.OK)
	public void generateForgotPasswordToken(@RequestParam("username") String userName) {

		User user = userRepository.findById(userName)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						String.format("User with user name %s doesn't exist!", userName)));

		Verification fp = new Verification();
		fp.setId(UUID.randomUUID().toString());
		fp.setCreationTime(new Date());
		fp.setModificationTime(fp.getCreationTime());
		fp.setUserName(userName);
		fp.setTokenUsed(false);
		forgotPasswordRepository.save(fp);

		// Send Portal Notifications
		String userNotificationContent = settingService.getSetting().getNotificationContentForgotPasswordInitiation();
		String notificationTitle = settingService.getSetting().getNotificationTitleForgotPasswordInitiation();
		String notificationType = "Password-Reset";

		// Notification to user.
		NotificationMessage userNotification = new NotificationMessage();
		userNotification.setTitle(notificationTitle);
		userNotification.setContent(userNotificationContent);
		userNotification.setType(notificationType);
		userNotification.setAttributes(null);
		userNotification.setToUsers(Arrays.asList(user.getUsername()));
		notificationService.sendNotification(userNotification);

		String uiForgotPasswordUpdateUrl = String.format(settingService.getSetting().getUiForgotPasswordUpdateUrl(),
				fp.getId());
		String forgotPasswordLinkExpiryTimeInMinutes = String.valueOf(TimeUnit.SECONDS.toMinutes(
				Long.valueOf(settingService.getSetting().getForgotPasswordLinkExpiryTimeInSeconds()).longValue()));

		String content = String.format(settingService.getSetting().getEmailContentForgotPasswordInitiation(),
				user.getFirstname(), forgotPasswordLinkExpiryTimeInMinutes, uiForgotPasswordUpdateUrl);

		EmailMessage userEmailMessage = new EmailMessage();
		userEmailMessage.setSubject(settingService.getSetting().getEmailSubjectForgotPasswordInitiation());
		userEmailMessage.setContent(content);
		userEmailMessage.setToEmails(Arrays.asList(user.getEmail()));
		notificationService.sendEmail(userEmailMessage);
	}
}