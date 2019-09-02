package com.utc.nda.notification.services;

import com.utc.nda.notification.dtos.EmailMessage;
import com.utc.nda.notification.dtos.NotificationMessage;

public interface NotificationService {

	public void sendNotification(NotificationMessage notificationMessage);

	public void sendEmail(EmailMessage emailMessage);

}
