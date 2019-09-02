package com.utc.nda.notification.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.utc.nda.authorizationserver.users.repositories.UserRepository;
import com.utc.nda.notification.dtos.EmailAttachment;
import com.utc.nda.notification.dtos.EmailMessage;
import com.utc.nda.notification.dtos.NotificationMessage;
import com.utc.nda.notification.entities.Notification;
import com.utc.nda.notification.repositories.NotificationRepository;
import com.utc.nda.setting.services.SettingService;

@Service
public class NotificationServiceImpl implements NotificationService {

	private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private SettingService settingService;

	@Autowired
	JavaMailSender mailSender;

	@Override
	@Async("threadPoolExecutor")
	public void sendNotification(NotificationMessage notificationMessage) {
		logger.info("Preparing to send notifications");
		Set<String> owners = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		if (notificationMessage.getToUsers() != null) {
			owners.addAll(notificationMessage.getToUsers());
		}

		if (notificationMessage.getToRoles() != null) {
			for (String role : notificationMessage.getToRoles()) {
				owners.addAll(userRepository.findUsernamesByRole(role));
			}
		}
		logger.info("Unique owners list created.");

		Iterator itr = owners.iterator();
		while (itr.hasNext()) {
			Notification n = new Notification();
			String owner = itr.next().toString();
			n.setOwner(owner);
			n.setTitle(notificationMessage.getTitle());
			n.setContent(notificationMessage.getContent());
			n.setType(notificationMessage.getType());
			n.setAttributes(notificationMessage.getAttributes());
			notificationRepository.save(n);

		}
		logger.info("Notification sent successfully.");
	}

	@Override
	@Async("threadPoolExecutor")
	public void sendEmail(EmailMessage emailMessage) {
		logger.info("Preparing to send email.");
		ArrayList<InternetAddress> toEmails = prepareUniqueEmailList(emailMessage.getToEmails(),
				emailMessage.getToUsers(), emailMessage.getToRoles());
		ArrayList<InternetAddress> ccEmails = prepareUniqueEmailList(emailMessage.getCcEmails(),
				emailMessage.getCcUsers(), emailMessage.getCcRoles());
		ArrayList<InternetAddress> bccEmails = prepareUniqueEmailList(emailMessage.getBccEmails(),
				emailMessage.getBccUsers(), emailMessage.getBccRoles());
		logger.info("Unique emails list for to, cc, bcc created.");

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper messageHelper = null;
		try {
			messageHelper = new MimeMessageHelper(message, true);
			messageHelper.setFrom(settingService.getSetting().getNdaMailbox());
			messageHelper.setTo(toEmails.toArray(new InternetAddress[toEmails.size()]));
			messageHelper.setCc(ccEmails.toArray(new InternetAddress[ccEmails.size()]));
			messageHelper.setBcc(bccEmails.toArray(new InternetAddress[bccEmails.size()]));
			messageHelper.setSubject(emailMessage.getSubject());
			messageHelper.setText(emailMessage.getContent());
			logger.info("Email prepared.");

			Iterator attachments = emailMessage.getAttachments().entrySet().iterator();
			while (attachments.hasNext()) {
				EmailAttachment attachment = ((Entry<String, EmailAttachment>) attachments.next()).getValue();
				messageHelper.addAttachment(attachment.getName(),
						new ByteArrayDataSource(attachment.getContent(), attachment.getContentType()));
			}
			logger.info("Email attachments processed.");

			mailSender.send(message);
			logger.info("Email sent successfully.");
		} catch (MessagingException e) {
			logger.error("Error encountered in sending email, ", e);
		}

	}

	public ArrayList<InternetAddress> prepareUniqueEmailList(List<String> emails, List<String> users,
			List<String> roles) {
		Set<String> uniqueEmails = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		if (emails != null) {
			uniqueEmails.addAll(emails);
		}

		if (users != null) {
			for (String userName : users) {
				uniqueEmails.add(userRepository.findEmailByUsername(userName));
			}
		}

		if (roles != null) {
			for (String role : roles) {
				uniqueEmails.addAll(userRepository.findEmailsByRole(role));
			}
		}

		ArrayList<InternetAddress> uniqueEmailList = new ArrayList<>();
		for (String email : uniqueEmails) {
			logger.info(email);
			InternetAddress i = new InternetAddress();
			i.setAddress(email);
			uniqueEmailList.add(i);
		}
		logger.info("Unique email list prepared successfully.");
		return uniqueEmailList;
	}

}
