package com.utc.nda.authorizationserver.users.controllers;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.utc.nda.authorizationserver.users.dtos.UserModel;
import com.utc.nda.authorizationserver.users.entities.User;
import com.utc.nda.authorizationserver.users.entities.Verification;
import com.utc.nda.authorizationserver.users.repositories.UserRepository;
import com.utc.nda.authorizationserver.users.repositories.VerificationRepository;
import com.utc.nda.authorizationserver.users.services.UserService;
import com.utc.nda.notification.dtos.EmailMessage;
import com.utc.nda.notification.dtos.NotificationMessage;
import com.utc.nda.notification.services.NotificationService;
import com.utc.nda.setting.services.SettingService;

@RestController
public class UserController {

	private static final Log logger = LogFactory.getLog(UserController.class);

	@Autowired
	private SettingService settingService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserService userService;

	@Autowired
	private VerificationRepository verificationRepository;

	//@PreAuthorize("hasAnyRole('ROLE_MODERATOR','ROLE_ADMIN')")
	@GetMapping("/users")
	@ResponseStatus(HttpStatus.OK)
	public List<UserModel> listAllUsers() {
		List<User> users = userRepository.findAll();
		List<UserModel> filteredUsers = users.stream().map(u -> {
			UserModel user = new UserModel();
			user.setUsername(u.getUsername());
			user.setEmail(u.getEmail());
			user.setRole(u.getAuthorities().stream().findFirst().get().getAuthority());
			user.setEnabled(u.isEnabled());
			return user;
		}).collect(Collectors.toList());
		return filteredUsers;
	}

	@PreAuthorize("hasAnyRole('ROLE_MODERATOR','ROLE_ADMIN')")
	@GetMapping("/user/{username}")
	@ResponseStatus(HttpStatus.OK)
	public UserModel getUserByUsername(@PathVariable("username") String userName) {

		User user = userRepository.findById(userName)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						String.format("User with user name %s doesn't exist!", userName)));

		UserModel userModel = new UserModel();
		userModel.setUsername(user.getUsername());
		userModel.setEmail(user.getEmail());
		userModel.setFirstname(user.getFirstname());
		userModel.setLastname(user.getLastname());
		userModel.setEnabled(user.isEnabled());
		userModel.setRole(user.getAuthorities().stream().findFirst().get().getAuthority());
		return userModel;
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@PostMapping("/user")
	@ResponseStatus(HttpStatus.OK)
	public void createUser(@RequestBody @Valid User user) {

		if (userRepository.findById(user.getUsername()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("User with user name %s already exists!", user.getUsername()));
		}
		if (userRepository.countByEmail(user.getEmail()) != 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("User with email %s already exists!", user.getEmail()));
		}
		if (userService.matchesPolicy(user.getPassword()) == false) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("User Password is invalid", user.getPassword()));
		}
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		userRepository.save(user);
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@PutMapping("/user")
	@ResponseStatus(HttpStatus.OK)
	public void updateUser(@RequestBody @Valid User user) {

		User u = userRepository.findById(user.getUsername())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						String.format("User with user name %s doesn't exist!", user.getUsername())));

		// check email unique.
		if (u.getEmail().trim().equalsIgnoreCase(user.getEmail().trim()) == false
				&& userRepository.countByEmail(user.getEmail()) != 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("User with email %s already exists!", user.getEmail()));
		}

		u.setEmail(user.getEmail());
		u.setFirstname(user.getFirstname());
		u.setLastname(user.getLastname());
		userRepository.save(u);
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@DeleteMapping("/user/{username}")
	@ResponseStatus(HttpStatus.OK)
	public void deleteUser(@PathVariable("username") String userName) {

		if (userRepository.findById(userName).isPresent()) {
			userRepository.deleteById(userName);
		} else {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					String.format("User with user name %s doesn't exist!", userName));
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@GetMapping("/status/{username}")
	@ResponseStatus(HttpStatus.OK)
	public void changeUserStatus(@PathVariable("username") String userName) {

		User user = userRepository.findById(userName)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						String.format("User with user name %s doesn't exist!", userName)));

		boolean state = !user.isEnabled();
		user.setEnabled(state);
		userRepository.save(user);
	}

	@PostMapping("/public/register")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<?> registerUser(@RequestBody @Valid User user) {

		// check if user with same name or email doesn't exist.
		if (userRepository.findById(user.getUsername()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("User with user name %s already exists!", user.getUsername()));
		}
		if (userRepository.countByEmail(user.getEmail()) != 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("User with email %s already exists!", user.getEmail()));
		}
		if (userService.matchesPolicy(user.getPassword()) == false) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("User Password is invalid", user.getPassword()));
		}

		String allowedMailDomains = settingService.getSetting().getValidEmailDomains();
		boolean isEmailDomainValid = false;
		for (String allowedMailDomain : allowedMailDomains.split(",")) {
			if (user.getEmail().toLowerCase().endsWith("@" + allowedMailDomain.toLowerCase())) {
				isEmailDomainValid = true;
				break;
			}
		}

		user.setRole("ROLE_USER"); // registering users will always have role ROLE_USER.
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setEnabled(false);
		userRepository.save(user);

		if (isEmailDomainValid) {
			initiateEmailVerificationInDomainUser(user);
			HashMap<String, String> response = new HashMap<>();
			response.put("message",
					"Thank you for registering with Bamboo,  "
							+ "A email verification link is sent to your registered email address, "
							+ "kindly verify your email address to activate your account. ");
			return ResponseEntity.status(200).body(response);
		} else {
			sendOutOfDomainUserNotifications(user);
			HashMap<String, String> response = new HashMap<>();
			response.put("message",
					"Thank you for registering with Bamboo,  "
							+ "your account is created successfully and is pending activation,  "
							+ "you will be able to login once admin approve's and activates your account. "
							+ "You will recieve a confirmation email when your account is activated. ");
			return ResponseEntity.status(200).body(response);
		}

	}

	public void initiateEmailVerificationInDomainUser(User user) {
		Verification userEmailVerifier = new Verification();
		userEmailVerifier.setId(UUID.randomUUID().toString());
		userEmailVerifier.setUserName(user.getUsername());
		userEmailVerifier.setCreationTime(new Date());
		userEmailVerifier.setModificationTime(userEmailVerifier.getCreationTime());
		userEmailVerifier.setTokenUsed(false);
		verificationRepository.save(userEmailVerifier);

		// user email notification
		String emailverificationUrl = String.format(settingService.getSetting().getUiVerifyAccountEmailUrl(),
				userEmailVerifier.getId());
		String userEmailContent = String.format(settingService.getSetting().getEmailContentAccountEmailVerification(),
				user.getFirstname(), emailverificationUrl);

		EmailMessage userEmailMessage = new EmailMessage();
		userEmailMessage.setSubject(settingService.getSetting().getEmailSubjectAccountEmailVerification());
		userEmailMessage.setContent(userEmailContent);
		userEmailMessage.setToEmails(Arrays.asList(user.getEmail()));
		notificationService.sendEmail(userEmailMessage);
	}

	public void sendOutOfDomainUserNotifications(User user) {

		// Send Portal Notifications
		String adminNotificationContent = String.format(
				settingService.getSetting().getAdminNotificationContentUserRegistered(), user.getUsername(),
				user.getEmail());
		String notificationTitle = settingService.getSetting().getAdminNotificationTitleUserRegistered();
		String notificationType = "User-Account-Creation";

		HashMap<String, String> attributes = new HashMap<String, String>();
		attributes.put("user_name", user.getUsername());

		// Notification to all users with role admin or moderator.
		NotificationMessage adminNotification = new NotificationMessage();
		adminNotification.setTitle(notificationTitle);
		adminNotification.setContent(adminNotificationContent);
		adminNotification.setType(notificationType);
		adminNotification.setAttributes(attributes);
		adminNotification.setToRoles(Arrays.asList("ROLE_ADMIN", "ROLE_MODERATOR"));
		notificationService.sendNotification(adminNotification);

		// Send Email Notifications
		// Send email to all users with role admin or moderator.
		String adminEmailContent = String.format(settingService.getSetting().getAdminEmailContentUserRegistered(),
				user.getUsername(), user.getEmail());

		EmailMessage adminEmailMessage = new EmailMessage();
		adminEmailMessage.setSubject(settingService.getSetting().getAdminEmailSubjectUserRegistered());
		adminEmailMessage.setContent(adminEmailContent);
		adminEmailMessage.setToRoles(Arrays.asList("ROLE_ADMIN"));
		adminEmailMessage.setCcRoles(Arrays.asList("ROLE_MODERATOR"));
		notificationService.sendEmail(adminEmailMessage);

		// user email notification
		String userEmailContent = String.format(settingService.getSetting().getUserEmailContentUserRegistered(),
				user.getFirstname(), user.getUsername());

		EmailMessage userEmailMessage = new EmailMessage();
		userEmailMessage.setSubject(settingService.getSetting().getUserEmailSubjectUserRegistered());
		userEmailMessage.setContent(userEmailContent);
		userEmailMessage.setToEmails(Arrays.asList(user.getEmail()));
		notificationService.sendEmail(userEmailMessage);

	}

	@PostMapping("/public/account/email/verify/{id}")
	@ResponseStatus(HttpStatus.OK)
	public void verifyUserEmail(@PathVariable("id") String id) {

		Verification emailVerifier = verificationRepository.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid email verification token."));

		if (emailVerifier.isTokenUsed()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN,
					"Email verification token expired, valid for single use only!");
		}

		long activateUserLinkExpiryTimeInSeconds = Long
				.valueOf(settingService.getSetting().getForgotPasswordLinkExpiryTimeInSeconds());
		if (((new Date()).getTime() - emailVerifier.getCreationTime().getTime()) > activateUserLinkExpiryTimeInSeconds
				* 1000) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Email verification token expired!");
		}

		String userName = emailVerifier.getUserName();
		User user = userRepository.findById(userName)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
		user.setEnabled(true);
		userRepository.save(user);
		emailVerifier.setModificationTime(new Date());
		emailVerifier.setTokenUsed(true);
		verificationRepository.save(emailVerifier);

		sendAccountActivationNotifications(user);

	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@GetMapping("/user/activate/{username}")
	@ResponseStatus(HttpStatus.OK)
	public void activateUser(@PathVariable("username") String userName) {

		User user = userRepository.findById(userName)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"User with user name " + userName + " doesn't exist!"));

		// if user account is not already activated, activate user account.
		Assert.isTrue(user.isEnabled() == false, "User already activated!");

		user.setEnabled(true);
		userRepository.save(user);

		sendAccountActivationNotifications(user);

	}

	public void sendAccountActivationNotifications(User user) {
		// Send Portal Notifications
		String adminNotificationContent = String.format(
				settingService.getSetting().getAdminNotificationContentUserActivated(), user.getUsername(),
				user.getEmail());
		String notificationTitle = settingService.getSetting().getNotificationTitleUserActivated();
		String notificationType = "User-Account-Activation";

		HashMap<String, String> attributes = new HashMap<String, String>();
		attributes.put("user_name", user.getUsername());

		// Notification to all users with role admin or moderator.
		NotificationMessage adminNotification = new NotificationMessage();
		adminNotification.setTitle(notificationTitle);
		adminNotification.setContent(adminNotificationContent);
		adminNotification.setType(notificationType);
		adminNotification.setAttributes(attributes);
		adminNotification.setToRoles(Arrays.asList("ROLE_ADMIN", "ROLE_MODERATOR"));
		notificationService.sendNotification(adminNotification);

		// Send Email Notifications
		// Send email to all users with role admin or moderator.
		String adminEmailContent = String.format(settingService.getSetting().getAdminEmailContentUserActivated(),
				user.getUsername(), user.getEmail());

		EmailMessage adminEmailMessage = new EmailMessage();
		adminEmailMessage.setSubject(settingService.getSetting().getAdminEmailSubjectUserActivated());
		adminEmailMessage.setContent(adminEmailContent);
		adminEmailMessage.setToRoles(Arrays.asList("ROLE_ADMIN"));
		adminEmailMessage.setCcRoles(Arrays.asList("ROLE_MODERATOR"));
		notificationService.sendEmail(adminEmailMessage);

		// user email notification
		String userEmailContent = String.format(settingService.getSetting().getUserEmailContentUserActivated(),
				user.getFirstname());

		EmailMessage userEmailMessage = new EmailMessage();
		userEmailMessage.setSubject(settingService.getSetting().getUserEmailSubjectUserActivated());
		userEmailMessage.setContent(userEmailContent);
		userEmailMessage.setToEmails(Arrays.asList(user.getEmail()));
		notificationService.sendEmail(userEmailMessage);
	}

}
