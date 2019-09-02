package com.utc.nda.request.services;

import static com.utc.nda.request.repositories.RequestSpecifications.certifierNameContains;
import static com.utc.nda.request.repositories.RequestSpecifications.hasBusinessUnit;
import static com.utc.nda.request.repositories.RequestSpecifications.hasCreationDateGreaterThanOrEqualTo;
import static com.utc.nda.request.repositories.RequestSpecifications.hasCreationDateLessThan;
import static com.utc.nda.request.repositories.RequestSpecifications.hasDocusignGuid;
import static com.utc.nda.request.repositories.RequestSpecifications.hasExpiryDate;
import static com.utc.nda.request.repositories.RequestSpecifications.hasGuid;
import static com.utc.nda.request.repositories.RequestSpecifications.hasInformationSharingPeriod;
import static com.utc.nda.request.repositories.RequestSpecifications.hasIsAmendment;
import static com.utc.nda.request.repositories.RequestSpecifications.hasNdaType;
import static com.utc.nda.request.repositories.RequestSpecifications.hasOwner;
import static com.utc.nda.request.repositories.RequestSpecifications.hasStartDate;
import static com.utc.nda.request.repositories.RequestSpecifications.hasStatus;
import static com.utc.nda.request.repositories.RequestSpecifications.otherPartyNameContains;
import static com.utc.nda.request.repositories.RequestSpecifications.requestorNameContains;
import static org.springframework.data.jpa.domain.Specifications.where;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.io.IOUtils;
import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.server.ResponseStatusException;

import com.docusign.esign.client.ApiException;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import com.utc.nda.docusign.services.SignatureService;
import com.utc.nda.notification.dtos.EmailAttachment;
import com.utc.nda.notification.dtos.EmailMessage;
import com.utc.nda.notification.dtos.NotificationMessage;
import com.utc.nda.notification.services.NotificationService;
import com.utc.nda.request.dtos.RequestSearchDto;
import com.utc.nda.request.entities.Request;
import com.utc.nda.request.repositories.RequestRepository;
import com.utc.nda.setting.services.SettingService;
import com.utc.nda.storage.services.DocumentNotFoundException;
import com.utc.nda.storage.services.DocumentProcessingException;
import com.utc.nda.storage.services.StorageService;

import fr.opensagres.xdocreport.itext.extension.font.IFontProvider;
import fr.opensagres.xdocreport.itext.extension.font.ITextFontRegistry;

@Service
public class RequestService {

	private static final Logger logger = LoggerFactory.getLogger(RequestService.class);

	@Autowired
	private RequestRepository requestRepository;

	@Autowired
	private StorageService storageService;

	@Autowired
	private SettingService settingService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	@Qualifier("signatureServiceImpl")
	private SignatureService signatureService;

	@PreAuthorize("hasAnyRole('ROLE_MODERATOR','ROLE_ADMIN')")
	//@PostFilter("filterObject.owner == authentication.name or hasAnyRole('ROLE_MODERATOR','ROLE_ADMIN')")
	@Transactional
	public List<Request> query(RequestSearchDto params) throws ParseException {
		Specifications<Request> specifications = null;
		if (params.getGuid() != null && !("".equalsIgnoreCase(params.getGuid().trim()))) {
			specifications = where(hasGuid(params.getGuid()));
		}

		if (params.getOwner() != null && !("".equalsIgnoreCase(params.getOwner().trim()))) {
			if (specifications != null) {
				specifications = specifications.and(hasOwner(params.getOwner()));
			} else {
				specifications = where(hasOwner(params.getOwner()));
			}
		}

		if (params.getStatus() != null && !("".equalsIgnoreCase(params.getStatus().trim()))) {
			if (specifications != null) {
				specifications = specifications.and(hasStatus(params.getStatus()));
			} else {
				specifications = where(hasStatus(params.getStatus()));
			}
		}

		if (params.getRequestorName() != null && !("".equalsIgnoreCase(params.getRequestorName().trim()))) {

			if (specifications != null) {
				specifications = specifications.and(requestorNameContains(params.getRequestorName()));
			} else {
				specifications = where(requestorNameContains(params.getRequestorName()));
			}
		}

		if (params.getCertifierName() != null && !("".equalsIgnoreCase(params.getCertifierName().trim()))) {

			if (specifications != null) {
				specifications = specifications.and(certifierNameContains(params.getCertifierName()));
			} else {
				specifications = where(certifierNameContains(params.getCertifierName()));
			}
		}

		if (params.getOtherPartyName() != null && !("".equalsIgnoreCase(params.getOtherPartyName().trim()))) {

			if (specifications != null) {
				specifications = specifications.and(otherPartyNameContains(params.getOtherPartyName()));
			} else {
				specifications = where(otherPartyNameContains(params.getOtherPartyName()));
			}
		}

		if (params.getBusinessUnit() != null && !("".equalsIgnoreCase(params.getBusinessUnit().trim()))) {

			if (specifications != null) {
				specifications = specifications.and(hasBusinessUnit(params.getBusinessUnit()));
			} else {
				specifications = where(hasBusinessUnit(params.getBusinessUnit()));
			}
		}

		if (params.getNdaType() != null && !("".equalsIgnoreCase(params.getNdaType().trim()))) {

			if (specifications != null) {
				specifications = specifications.and(hasNdaType(params.getNdaType()));
			} else {
				specifications = where(hasNdaType(params.getNdaType()));
			}
		}

		if (params.getInformationSharingPeriod() != null) {

			if (specifications != null) {
				specifications = specifications
						.and(hasInformationSharingPeriod(params.getInformationSharingPeriod().intValue()));
			} else {
				specifications = where(hasInformationSharingPeriod(params.getInformationSharingPeriod().intValue()));
			}
		}

		if (params.getIsAmendment() || !params.getIsAmendment()) {

			if (specifications != null) {
				specifications = specifications.and(hasIsAmendment(params.getIsAmendment()));
			} else {
				specifications = where(hasIsAmendment(params.getIsAmendment()));
			}
		}

		if (params.getStartDate() != null) {
			if (specifications != null) {
				specifications = specifications.and(hasStartDate(params.getStartDate()));
			} else {
				specifications = where(hasStartDate(params.getStartDate()));
			}
		}

		if (params.getExpiryDate() != null) {
			if (specifications != null) {
				specifications = specifications.and(hasExpiryDate(params.getExpiryDate()));
			} else {
				specifications = where(hasExpiryDate(params.getExpiryDate()));
			}
		}

		if (params.getCreationTime() != null) {

			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dddd");
			Date startDate = formatter.parse(formatter.format(params.getCreationTime()));

			Calendar calNextDayDate = Calendar.getInstance();
			calNextDayDate.setTime(startDate);
			calNextDayDate.add(Calendar.DAY_OF_MONTH, 1);
			Date nextDayDate = formatter.parse(formatter.format(calNextDayDate.getTime()));

			if (specifications != null) {
				specifications = specifications.and(hasCreationDateGreaterThanOrEqualTo(startDate))
						.and(hasCreationDateLessThan(nextDayDate));

			} else {
				specifications = where(hasCreationDateGreaterThanOrEqualTo(startDate))
						.and(hasCreationDateLessThan(nextDayDate));
			}
		}

		if (params.getDocusignGuid() != null && !("".equalsIgnoreCase(params.getDocusignGuid().trim()))) {
			if (specifications != null) {
				specifications = specifications.and(hasDocusignGuid(params.getDocusignGuid()));
			} else {
				specifications = where(hasDocusignGuid(params.getDocusignGuid()));
			}
		}

		if (specifications != null) {
			return requestRepository.findAll(specifications);
		} else {
			return requestRepository.findAll();
		}
	}

	public void sendToDocusignSigning(String requestId)
			throws DocumentProcessingException, IOException, ApiException, DocumentNotFoundException {
		// admin can send Pending Admin Approval request to docusign.
		// or user can send his own requests marked for auto approval to docusign.
		logger.info("Sending request to docusign for signatures.");
		Assert.hasLength(requestId, "Request Id cannot be empty!");

		// check status in pending auto approval / pending admin approval
		logger.info("Fetching request from db, request ID: " + requestId);
		Request r = requestRepository.findById(requestId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found."));

		logger.info("Checking user sending personal requests for signature.");
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String principal = authentication.getPrincipal().toString().trim();
		if ("Pending Auto Approval".equalsIgnoreCase(r.getStatus())) {
			if (!principal.equalsIgnoreCase(r.getOwner())) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN,
						"Approval is allowed for personal requests only.");
			}
		}

		logger.info("Checking admin sending non personal requests for signature.");
		if ("Pending Admin Approval".equalsIgnoreCase(r.getStatus())) {
			boolean hasUserRole = authentication.getAuthorities().stream()
					.anyMatch(x -> x.getAuthority().equalsIgnoreCase("ROLE_ADMIN"));
			if (hasUserRole == false) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Approval is allowed for Admin role only.");
			}
		}

		logger.info("Fetching unsigned processed document from db.");
		byte[] documentToSign = storageService.getDocumentContentById(r.getParsedDocumentPdfGuid());
		logger.info("Doc Name: " + r.getOtherPartyName() + r.getBusinessUnit());
		logger.info("Preparing metadata required for signatures.");
		String otherPartyEmail = r.getOtherPartyEmail().trim();
		String requestorEmail = r.getRequestorEmail().trim();
		String certifierEmail = r.getCertifierEmail().trim();
		JSONObject json = new JSONObject(r.getRequestData());
		String companyFullName = json.get("section_iii_fullLegalName").toString().trim();

		logger.info("Calling docusign signature api.");
		String envelopeId = "";
		envelopeId = signatureService.docuSign(r.getOtherPartyName() + "-" + r.getBusinessUnit() + ".pdf",
				documentToSign, r.getRequestorName(), requestorEmail, r.getOtherPartyName(), otherPartyEmail,
				settingService.getSetting().getIpAttorneyName(), settingService.getSetting().getIpAttorneyEmail(),
				r.getCertifierName(), certifierEmail, r.getNdaType());

		r.setDocusignGuid(envelopeId);
		r.setStatus("Sent To DocuSign");
		logger.info("Updating request status after docusign api call.");
		requestRepository.save(r);

		// Send Portal Notifications
		logger.info("Sending portal notifications.");
		String requestorNotificationContent = settingService.getSetting()
				.getRequestorNotificationContentRequestApproved();
		String adminNotificationContent = settingService.getSetting().getAdminNotificationContentRequestApproved();
		String notificationTitle = String.format(settingService.getSetting().getNotificationTitleRequestApproved(),
				companyFullName);
		String notificationType = "NDA-Request-Sent-To-DocuSign";

		// Notification to requestor.
		NotificationMessage requestorNotification = new NotificationMessage();
		requestorNotification.setTitle(notificationTitle);
		requestorNotification.setContent(requestorNotificationContent);
		requestorNotification.setToUsers(Arrays.asList(r.getOwner()));
		requestorNotification.setType(notificationType);

		HashMap<String, String> attributes = new HashMap<String, String>();
		attributes.put("request_guid", r.getGuid());
		requestorNotification.setAttributes(attributes);
		notificationService.sendNotification(requestorNotification);

		// Notification to all users with role admin and moderator.
		NotificationMessage adminNotification = new NotificationMessage();
		adminNotification.setTitle(notificationTitle);
		adminNotification.setContent(adminNotificationContent);
		adminNotification.setType(notificationType);
		adminNotification.setAttributes(attributes);
		adminNotification.setToRoles(Arrays.asList("ROLE_ADMIN", "ROLE_MODERATOR"));
		notificationService.sendNotification(adminNotification);

		// Send Email Notifications
		// prepare attachments
		logger.info("Sending email notifications.");
		EmailAttachment requestFormAttachment = new EmailAttachment();
		requestFormAttachment.setName("NDA-Request-Form.pdf");
		requestFormAttachment.setContentType("application/octet-stream");
		requestFormAttachment.setContent(storageService.getDocumentContentById(r.getRequestFormPdfGuid()));

		EmailAttachment ndaAttachment = new EmailAttachment();
		ndaAttachment.setName("NDA-Document.pdf");
		ndaAttachment.setContentType("application/octet-stream");
		ndaAttachment.setContent(storageService.getDocumentContentById(r.getParsedDocumentPdfGuid()));

		HashMap<String, EmailAttachment> attachments = new HashMap<>();
		attachments.put("1", requestFormAttachment);
		attachments.put("2", ndaAttachment);

		if (r.isAmendment()) {
			Request parentRequest = requestRepository.findById(r.getAmendmentGuid()).orElseThrow(
					() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent amendment request not found."));
			EmailAttachment parentNdaAttachment = new EmailAttachment();
			parentNdaAttachment.setName("Parent-NDA-Document.pdf");
			parentNdaAttachment.setContentType("application/octet-stream");
			parentNdaAttachment
					.setContent(storageService.getDocumentContentById(parentRequest.getParsedDocumentPdfGuid()));
			attachments.put("3", parentNdaAttachment);
		}

		// Send email to all users with role admin or moderator.
		String viewRequestUrl = String.format(settingService.getSetting().getUiViewRequestUrl(), r.getGuid());
		String adminEmailContent = String.format(settingService.getSetting().getAdminEmailContentRequestApproved(),
				viewRequestUrl);

		EmailMessage adminEmailMessage = new EmailMessage();
		adminEmailMessage.setSubject(
				String.format(settingService.getSetting().getEmailSubjectRequestApproved(), companyFullName));
		adminEmailMessage.setContent(adminEmailContent);
		adminEmailMessage.setToRoles(Arrays.asList("ROLE_ADMIN"));
		adminEmailMessage.setCcRoles(Arrays.asList("ROLE_MODERATOR"));
		adminEmailMessage.setAttachments(attachments);
		notificationService.sendEmail(adminEmailMessage);

		// requestor email notification
		String requestorEmailContent = String
				.format(settingService.getSetting().getRequestorEmailContentRequestApproved(), viewRequestUrl);

		EmailMessage requestorEmailMessage = new EmailMessage();
		requestorEmailMessage.setSubject(
				String.format(settingService.getSetting().getEmailSubjectRequestApproved(), companyFullName));
		requestorEmailMessage.setContent(requestorEmailContent);
		requestorEmailMessage.setToEmails(Arrays.asList(requestorEmail));
		requestorEmailMessage.setAttachments(attachments);
		notificationService.sendEmail(requestorEmailMessage);
		logger.info("Request to docusign for signatures successfully.");
	}

	public byte[] mergePdfs(List<byte[]> files) throws DocumentException, IOException {
		logger.info("Merging pdf documents.");
		Assert.notEmpty(files, "List of files cannot be empty!");
		com.itextpdf.text.Document document = new com.itextpdf.text.Document();
		Path mergedPdfsPath = Files.createTempFile("merged-pdf", ".pdf");
		PdfCopy copy = new PdfCopy(document, new FileOutputStream(mergedPdfsPath.toFile()));

		document.open();
		for (byte[] file : files) {
			PdfReader reader = new PdfReader(file);
			copy.addDocument(reader);
			copy.freeReader(reader);
			reader.close();
		}
		document.close();

		FileInputStream responsePdfInputStream = new FileInputStream(mergedPdfsPath.toFile());
		byte[] responsePdf = IOUtils.toByteArray(responsePdfInputStream);
		responsePdfInputStream.close();
		logger.info("Pdf documents merged successfully.");
		return responsePdf;
	}

	public byte[] convertDocxToPdf(byte[] inputFile) throws IOException {
		logger.info("Converting markdown to pdf.");
		Assert.notNull(inputFile, "File cannot be null!");
		Assert.isTrue(inputFile.length > 0, "File cannot be empty!");
		// takes docx file in bytes, and converts to pdf and returns as bytes[].

		InputStream inputStream = new ByteArrayInputStream(inputFile);

		// convert to pdf using arial font.
		XWPFDocument document = new XWPFDocument(inputStream);
		PdfOptions options = PdfOptions.create();

		OutputStream pdfOutputStream = null;
		Path pdfPath = null;
		pdfPath = Files.createTempFile("ndapdf", ".pdf");
		pdfOutputStream = new java.io.FileOutputStream(pdfPath.toString());

		logger.info("Setting up fonts for markdown to pdf conversion.");
		options.fontProvider(new IFontProvider() {
			public Font getFont(String familyName, String encoding, float size, int style, Color color) {
				try {
					if (com.itextpdf.text.Font.BOLD == style) {

						BaseFont bfArialBold = BaseFont.createFont("Fonts/Arial Bold.ttf", BaseFont.IDENTITY_H,
								BaseFont.EMBEDDED);
						Font fontArialBold = new Font(bfArialBold, size, style, color);
						if (familyName != null)
							fontArialBold.setFamily(familyName);
						return fontArialBold;
					} else if (com.itextpdf.text.Font.ITALIC == style) {

						BaseFont bfArialItalic = BaseFont.createFont("Fonts/Arial Italic.ttf", BaseFont.IDENTITY_H,
								BaseFont.EMBEDDED);
						Font fontArialItalic = new Font(bfArialItalic, size, style, color);
						if (familyName != null)
							fontArialItalic.setFamily(familyName);
						return fontArialItalic;
					} else if (com.itextpdf.text.Font.BOLDITALIC == style) {
						BaseFont bfArialBoldItalic = BaseFont.createFont("Fonts/Arial Bold Italic.ttf",
								BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
						Font fontArialBoldItalic = new Font(bfArialBoldItalic, size, style, color);
						if (familyName != null)
							fontArialBoldItalic.setFamily(familyName);
						return fontArialBoldItalic;
					} else {
						BaseFont bfArial = BaseFont.createFont("Fonts/Arial.ttf", BaseFont.IDENTITY_H,
								BaseFont.EMBEDDED);
						Font fontArial = new Font(bfArial, size, style, color);
						if (familyName != null)
							fontArial.setFamily(familyName);
						return fontArial;
					}

				} catch (Throwable e) {
					logger.warn("Arial font not found or used, trying default font now.", e);
					// An error occurs, use the default font provider.
					return ITextFontRegistry.getRegistry().getFont(familyName, encoding, size, style, color);
				}
			}
		});

		logger.info("Converting document using fonts.");
		PdfConverter.getInstance().convert(document, pdfOutputStream, options);

		inputStream.close();

		pdfOutputStream.flush();
		pdfOutputStream.close();

		FileInputStream responsePdfInputStream = new FileInputStream(pdfPath.toFile());
		byte[] responsePdf = IOUtils.toByteArray(responsePdfInputStream);
		responsePdfInputStream.close();

		logger.info("Document converted from markdown to pdf successfully.");
		return responsePdf;
	}

	public void saveSignedDocument(Request r, byte[] signedDocumentContent) throws DocumentProcessingException {
		logger.info("Storing signed document locally.");
		com.utc.nda.storage.dtos.Document signedDocument = new com.utc.nda.storage.dtos.Document();
		signedDocument.setName("Signed-NDA.pdf");
		signedDocument.setCategory("SIGNED-NDA-PDF");
		signedDocument.setContentType("application/pdf");
		String signedDocumentId = storageService.createDocument(signedDocument, signedDocumentContent);
		r.setCompletedDocumentGuid(signedDocumentId);
		requestRepository.save(r);
		logger.info("Signed document successfully stored locally.");
	}

}
