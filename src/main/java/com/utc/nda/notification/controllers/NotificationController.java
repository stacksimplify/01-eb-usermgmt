package com.utc.nda.notification.controllers;

import java.security.Principal;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.utc.nda.notification.entities.Notification;
import com.utc.nda.notification.repositories.NotificationRepository;

@RestController
public class NotificationController {

	private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

	@Autowired
	private NotificationRepository notificationRepository;

	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MODERATOR','ROLE_ADMIN')")
	@RequestMapping(value = "/notifications", method = RequestMethod.GET)
	public ResponseEntity<?> getNotifications(Principal principal, @RequestParam("page") int page) {
		HashMap<String, Object> response = new HashMap<>();
		response.put("total", String.valueOf(notificationRepository.countByOwner(principal.getName())));
		response.put("results", notificationRepository.findByOwnerOrderByCreationDateDesc(principal.getName(),
				PageRequest.of(page, 8)));
		return ResponseEntity.status(200).body(response);
	}

	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MODERATOR','ROLE_ADMIN')")
	@RequestMapping(value = "/notification/update-read", method = RequestMethod.GET)
	public void markNotificationAsRead(@RequestParam("id") String id, @RequestParam("read") boolean read,
			Principal principal) {

		// check user updating his own notification.
		Notification n = notificationRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						String.format("Notification with id %s doesn't exist!", id)));

		if (n.getOwner().equalsIgnoreCase(principal.getName())) {
			n.setRead(read);
			notificationRepository.save(n);
		} else {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN,
					"UnAuthorized, can update personal notifications only!");
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MODERATOR','ROLE_ADMIN')")
	@RequestMapping(value = "/notification", method = RequestMethod.DELETE)
	public void deleteNotification(@RequestParam("id") String id, Principal principal) {

		// check user deleting his own notification.
		Notification n = notificationRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						String.format("Notification with id %s doesn't exist!", id)));

		if (n.getOwner().equalsIgnoreCase(principal.getName())) {
			notificationRepository.deleteById(id);
		} else {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN,
					"UnAuthorized, can delete personal notifications only!");
		}
	}

}
