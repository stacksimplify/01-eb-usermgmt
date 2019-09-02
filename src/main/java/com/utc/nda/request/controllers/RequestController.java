package com.utc.nda.request.controllers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.docusign.esign.client.ApiException;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.itextpdf.text.DocumentException;
import com.opencsv.CSVWriter;
import com.utc.nda.docusign.services.SignatureService;
import com.utc.nda.notification.dtos.EmailAttachment;
import com.utc.nda.notification.dtos.EmailMessage;
import com.utc.nda.notification.dtos.NotificationMessage;
import com.utc.nda.notification.services.NotificationService;
import com.utc.nda.request.dtos.DashboardMetricDto;
import com.utc.nda.request.dtos.RequestSearchDto;
import com.utc.nda.request.dtos.SearchData;
import com.utc.nda.request.entities.Request;
import com.utc.nda.request.repositories.RequestRepository;
import com.utc.nda.request.services.RequestService;
import com.utc.nda.setting.services.SettingService;
import com.utc.nda.storage.dtos.Document;
import com.utc.nda.storage.services.DocumentNotFoundException;
import com.utc.nda.storage.services.DocumentProcessingException;
import com.utc.nda.storage.services.StorageService;
import com.utc.nda.template.services.TemplateService;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.pdf.converter.PdfConverterExtension;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.options.MutableDataSet;

@RestController
public class RequestController {

	private static final Logger logger = LoggerFactory.getLogger(RequestController.class);

	@Autowired
	private SettingService settingService;

	@Autowired
	private RequestRepository requestRepository;

	@Autowired
	private RequestService requestService;

	@Autowired
	private StorageService storageService;

	@Autowired
	private TemplateService templateService;

	@Autowired
	@Qualifier("signatureServiceImpl")
	private SignatureService signatureService;

	@Autowired
	private NotificationService notificationService;

	public boolean isRequestSentToDocuSign(Request r) {
		Assert.notNull(r, "Request cannot be null!");
		return r.getDocusignGuid() != null && "".equalsIgnoreCase(r.getDocusignGuid().trim()) == false;
	}

	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MODERATOR','ROLE_ADMIN')")
	@RequestMapping(value = "/docusign/{requestId}/status/all", method = RequestMethod.GET)
	public ResponseEntity<?> getDocuSignStatusAndSigners(@PathVariable("requestId") String requestId,
			HttpServletRequest request, Principal principal) throws IOException, ApiException {

		Request r = requestRepository.findById(requestId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found."));
		if (request.isUserInRole("ROLE_USER")) {
			if (!r.getOwner().equalsIgnoreCase(principal.getName())) {
				return ResponseEntity.status(403).body("UnAuthorized!");
			}
		}
		if (isRequestSentToDocuSign(r)) {
			HashMap<String, Object> response = new HashMap<>();
			response.put("signers", signatureService.getEnvelopeRecipients(r.getDocusignGuid()));
			response.put("status", signatureService.getEnvelopeStatus(r.getDocusignGuid()));
			return ResponseEntity.status(200).body(response);
		} else {
			return ResponseEntity.status(412).body("Document not yet sent to DocuSign.");
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MODERATOR','ROLE_ADMIN')")
	@RequestMapping(value = "/docusign/{requestId}/status", method = RequestMethod.GET)
	public ResponseEntity<?> getDocuSignStatus(@PathVariable("requestId") String requestId, HttpServletRequest request,
			Principal principal) throws IOException, ApiException {

//		Assert.hasLength(requestId, "Request Id cannot be empty!");
		Request r = requestRepository.findById(requestId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found."));
		if (request.isUserInRole("ROLE_USER")) {
			if (!r.getOwner().equalsIgnoreCase(principal.getName())) {
				return ResponseEntity.status(403).body("UnAuthorized!");
			}
		}
		if (isRequestSentToDocuSign(r)) {
			return ResponseEntity.status(200).body(signatureService.getEnvelopeStatus(r.getDocusignGuid()));
		} else {
			return ResponseEntity.status(412).body("Document not yet sent to DocuSign.");
		}

	}

	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MODERATOR','ROLE_ADMIN')")
	@RequestMapping(value = "/docusign/{requestId}/signers", method = RequestMethod.GET)
	public ResponseEntity<?> getEnvelopeById(@PathVariable("requestId") String requestId, HttpServletRequest request,
			Principal principal) throws IOException, ApiException {

//		Assert.hasLength(requestId, "Request Id cannot be empty!");
		Request r = requestRepository.findById(requestId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found."));
		if (request.isUserInRole("ROLE_USER")) {
			if (!r.getOwner().equalsIgnoreCase(principal.getName())) {
				return ResponseEntity.status(403).body("UnAuthorized!");
			}
		}
		if (isRequestSentToDocuSign(r)) {
			return ResponseEntity.status(200).body(signatureService.getEnvelopeRecipients(r.getDocusignGuid()));
		} else {
			return ResponseEntity.status(412).body("Document not yet sent to DocuSign.");
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MODERATOR','ROLE_ADMIN')")
	@RequestMapping(value = "/document/signed/docusign/{requestId}", method = RequestMethod.GET)
	public void getEnvelopeDocumentById(@RequestParam("action") String action,
			@PathVariable("requestId") String requestId, HttpServletResponse response, HttpServletRequest request,
			Principal principal) throws IOException, ApiException {

//		Assert.hasLength(requestId, "Request Id cannot be empty!");
		Request r = requestRepository.findById(requestId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found."));
		if (request.isUserInRole("ROLE_USER")) {
			if (!r.getOwner().equalsIgnoreCase(principal.getName())) {
				throw new AccessDeniedException("Access denied, insufficient privileges!");
			}
		}
		if ("Completed".equalsIgnoreCase(r.getStatus())) {
			ByteArrayInputStream is = new ByteArrayInputStream(
					signatureService.getEnvelopeDocument(r.getDocusignGuid()));
			String fileName = "Signed-NDA-" + requestId + ".pdf";
			response.setContentType("application/pdf");
			if ("view".equalsIgnoreCase(action)) {
				response.setHeader("Content-disposition", "inline; filename=" + fileName);
			} else {
				response.setHeader("Content-disposition", "attachment; filename=" + fileName);
			}
			org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
			response.flushBuffer();
		} else {
			return; // invalid status 412.
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MODERATOR','ROLE_ADMIN')")
	@RequestMapping(value = "/document/signed/bamboo/{requestId}", method = RequestMethod.GET)
	public void getSignedDocumentById(@RequestParam("action") String action,
			@PathVariable("requestId") String requestId, HttpServletResponse response, HttpServletRequest request,
			Principal principal) throws IOException, DocumentProcessingException, DocumentNotFoundException {

//		Assert.hasLength(requestId, "Request Id cannot be empty!");
		Request r = requestRepository.findById(requestId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found."));
		if (request.isUserInRole("ROLE_USER")) {
			if (!r.getOwner().equalsIgnoreCase(principal.getName())) {
				throw new AccessDeniedException("Access denied, insufficient privileges!");
			}
		}
		logger.info("Request ID: " + r.getGuid());
		logger.info("Completed Document ID: " + r.getCompletedDocumentGuid());
		if ("Completed".equalsIgnoreCase(r.getStatus())) {
			ByteArrayInputStream is = new ByteArrayInputStream(
					storageService.getDocumentContentById(r.getCompletedDocumentGuid()));
			String fileName = "Signed-NDA-" + requestId + ".pdf";
			response.setContentType("application/pdf");
			if ("view".equalsIgnoreCase(action)) {
				response.setHeader("Content-disposition", "inline; filename=" + fileName);
			} else {
				response.setHeader("Content-disposition", "attachment; filename=" + fileName);
			}
			org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
			response.flushBuffer();
		} else {
			return; // invalid status 412.
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
	@RequestMapping(value = "/docusign/sign", method = RequestMethod.GET)
	public ResponseEntity<?> sendToDocusignSigning(@RequestParam("requestId") String requestId)
			throws DocumentProcessingException, IOException, ApiException, DocumentNotFoundException {

		requestService.sendToDocusignSigning(requestId);

		HashMap<String, String> response = new HashMap<>();
		response.put("message", "Request approved and sent to DocuSign.");
		return ResponseEntity.status(200).body(response);
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	@RequestMapping(value = "/customer-nda/upload", method = RequestMethod.POST)
	public void uploadCustomerNdaDocument(@RequestParam("file") MultipartFile file, HttpServletResponse response)
			throws IOException, DocumentProcessingException {
		if (file.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Invalid customer document, document doesn't have any content!");
		}

		byte[] content = file.getBytes();
		String fileName = "Customer-NDA-Document.docx";
		fileName = file.getOriginalFilename();
		logger.info("customer document filename: " + fileName);

		Document document = new Document();
		document.setName(fileName);
		document.setCategory("CUSTOMER-NDA-DOCUMENT");
		document.setContentType("application/octet-stream");
		String id = storageService.createDocument(document, content);
		response.getWriter().println(id);

	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value = "/customer-nda/processed/upload", method = RequestMethod.POST)
	public void uploadProcessedCustomerNdaDocument(@RequestParam("requestId") String requestId,
			@RequestParam("file") MultipartFile file) throws DocumentException, IOException, JAXBException,
			SQLException, DocumentProcessingException, DocumentNotFoundException {

		logger.info("Creating nda document using customer nda.");
		if (file.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Invalid file, document doesn't have any content!");
		}

		// check admin is doing this operation
		// check request is of type customer nda
		// check document format is .docx

		byte[] content = file.getBytes();
		logger.info("Fetching request from db, request ID: " + requestId);
		Request r = requestRepository.findById(requestId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found."));
		logger.info("Preparing variables for signature template.");
		JSONObject requestJson = new JSONObject(r.getRequestData());
		String otherCompanyName = requestJson.get("section_iii_fullLegalName").toString().trim();
		String businessUnitName = requestJson.get("section_ii_ccsBusinessUnit").toString().trim();
		String ipAttorneyName = settingService.getSetting().getIpAttorneyName();
		String ipAttorneyTitle = settingService.getSetting().getIpAttorneyTitle();

		HashMap<String, String> mappings = new HashMap<String, String>();
		mappings.put("other-company-name", StringEscapeUtils.escapeXml11(otherCompanyName));
		mappings.put("ccs-business-unit", StringEscapeUtils.escapeXml11(businessUnitName));
		mappings.put("second-signer-name", StringEscapeUtils.escapeXml11(ipAttorneyName));
		mappings.put("second-signer-title", StringEscapeUtils.escapeXml11(ipAttorneyTitle));

		// populate markdown template for signature page, convert it to pdf.
		logger.info("Fetching signature template metadata.");
		String signatureTemplateId = "st-1";
		com.utc.nda.template.entities.Template signatureTemplate = templateService.getSignaturePageTemplate();
		signatureTemplateId = signatureTemplate.getDocumentGuid();

		logger.info("Fetching signature template content.");
		byte[] signatureTemplateContent = storageService.getDocumentContentById(signatureTemplateId);
		logger.info("Processing signature template.");
		String processedSignatureTemplate = processTemplate(mappings, signatureTemplateContent);
		logger.info("Converting signature template from markdown to pdf.");
		byte[] pdfSignatureTemplateContent = convertMdtoPdf(processedSignatureTemplate);

		// convert processed customer nda from docx to pdf.
		logger.info("Converting customer nda from docx to pdf.");
		byte[] pdfContent = requestService.convertDocxToPdf(content);

		// merge both pdf.
		List<byte[]> files = new ArrayList<>();
		files.add(pdfContent);
		files.add(pdfSignatureTemplateContent);

		logger.info("Merging signature template pdf and customer document pdf.");
		byte[] mergedPdfContent = requestService.mergePdfs(files);

		// store merged pdf.
		String fileName = "Processed-Customer-NDA-Document.pdf";

		Document document = new Document();
		document.setName(fileName);
		document.setCategory("PROCESSED-TEMPLATE");
		document.setContentType("application/pdf");
		logger.info("Storing final unsigned nda in db.");
		String documentId = storageService.createDocument(document, mergedPdfContent);

		logger.info("Updating request status.");
		r.setParsedDocumentPdfGuid(documentId);
		r.setStatus("Pending Admin Approval");
		requestRepository.save(r);
		logger.info("Cusotmer nda processing completed successfully.");
	}

	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MODERATOR','ROLE_ADMIN')")
	@RequestMapping(value = "/customer-nda/download/pdf/{requestId}", method = RequestMethod.GET)
	public void getCustomerNDADocument(@PathVariable("requestId") String requestId, HttpServletResponse response,
			HttpServletRequest request, Principal principal)
			throws SQLException, IOException, DocumentProcessingException, DocumentNotFoundException {

//		Assert.hasLength(requestId, "Request Id cannot be empty!");
		Request r = requestRepository.findById(requestId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found."));
		if (!(request.isUserInRole("ROLE_ADMIN") || request.isUserInRole("ROLE_MODERATOR"))) {
			if (!r.getOwner().equalsIgnoreCase(principal.getName())) {
				throw new AccessDeniedException("Access denied, insufficient privileges!");
			}
		}
		String documentId = r.getCustomerDocumentGuid();
		ByteArrayInputStream is = new ByteArrayInputStream(storageService.getDocumentContentById(documentId));
		Document document = storageService.getDocumentMetadataById(documentId);
		response.setContentType(document.getContentType());
		String fileName = "Customer-NDA-" + requestId + ".docx";
		response.setHeader("Content-disposition", "attachment; filename=" + fileName);
		org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
		response.flushBuffer();
	}

	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MODERATOR','ROLE_ADMIN')")
	@RequestMapping(value = "/request-form/download/pdf/{requestId}", method = RequestMethod.GET)
	public void getRequestFormDocument(@PathVariable("requestId") String requestId, HttpServletResponse response,
			HttpServletRequest request, Principal principal)
			throws SQLException, IOException, DocumentProcessingException, DocumentNotFoundException {

//		Assert.hasLength(requestId, "Request Id cannot be empty!");
		Request r = requestRepository.findById(requestId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found."));
		if (!(request.isUserInRole("ROLE_ADMIN") || request.isUserInRole("ROLE_MODERATOR"))) {
			if (!r.getOwner().equalsIgnoreCase(principal.getName())) {
				throw new AccessDeniedException("Access denied, insufficient privileges!");
			}
		}
		String documentId = r.getRequestFormPdfGuid();
		ByteArrayInputStream is = new ByteArrayInputStream(storageService.getDocumentContentById(documentId));
		Document document = storageService.getDocumentMetadataById(documentId);
		response.setContentType(document.getContentType());
		String fileName = "Request-Form-" + requestId + ".pdf";
		response.setHeader("Content-disposition", "attachment; filename=" + fileName);
		org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
		response.flushBuffer();
	}

	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MODERATOR','ROLE_ADMIN')")
	@RequestMapping(value = "/request-form/view/pdf/{requestId}", method = RequestMethod.GET)
	public void viewRequestFormDocument(@PathVariable("requestId") String requestId, HttpServletResponse response,
			HttpServletRequest request, Principal principal)
			throws SQLException, IOException, DocumentProcessingException, DocumentNotFoundException {

//		Assert.hasLength(requestId, "Request Id cannot be empty!");
		Request r = requestRepository.findById(requestId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found."));
		if (!(request.isUserInRole("ROLE_ADMIN") || request.isUserInRole("ROLE_MODERATOR"))) {
			if (!r.getOwner().equalsIgnoreCase(principal.getName())) {
				throw new AccessDeniedException("Access denied, insufficient privileges!");
			}
		}
		String documentId = r.getRequestFormPdfGuid();

		ByteArrayInputStream is = new ByteArrayInputStream(storageService.getDocumentContentById(documentId));
		Document document = storageService.getDocumentMetadataById(documentId);
		response.setContentType(document.getContentType());
		String fileName = "Request-Form-" + requestId + ".pdf";
		response.setHeader("Content-disposition", "inline; filename=" + fileName);
		org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
		response.flushBuffer();
	}

	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MODERATOR','ROLE_ADMIN')")
	@RequestMapping(value = "/document/unsigned/download/pdf/{requestId}", method = RequestMethod.GET)
	public void downloadUnSignedDocument(@PathVariable("requestId") String requestId, HttpServletResponse response,
			HttpServletRequest request, Principal principal)
			throws SQLException, IOException, DocumentProcessingException, DocumentNotFoundException {

//		Assert.hasLength(requestId, "Request Id cannot be empty!");
		Request r = requestRepository.findById(requestId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found."));
		if (!(request.isUserInRole("ROLE_ADMIN") || request.isUserInRole("ROLE_MODERATOR"))) {
			if (!r.getOwner().equalsIgnoreCase(principal.getName())) {
				throw new AccessDeniedException("Access denied, insufficient privileges!");
			}
		}
		String documentId = "";
		String fileName = "";
		if ("Customer".equalsIgnoreCase(r.getNdaType())) {
			documentId = r.getCustomerDocumentGuid();
			fileName = "Customer-NDA-" + requestId + ".docx";
		} else {
			documentId = r.getParsedDocumentPdfGuid();
			fileName = "NDA-UnSigned-" + requestId + ".pdf";
		}

		ByteArrayInputStream is = new ByteArrayInputStream(storageService.getDocumentContentById(documentId));
		Document document = storageService.getDocumentMetadataById(documentId);
		response.setContentType(document.getContentType());
		response.setHeader("Content-disposition", "attachment; filename=" + fileName);
		org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
		response.flushBuffer();
	}

	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MODERATOR','ROLE_ADMIN')")
	@RequestMapping(value = "/document/unsigned/view/pdf/{requestId}", method = RequestMethod.GET)
	public void getUnSignedDocument(@PathVariable("requestId") String requestId, HttpServletResponse response,
			HttpServletRequest request, Principal principal)
			throws SQLException, IOException, DocumentProcessingException, DocumentNotFoundException {

//		Assert.hasLength(requestId, "Request Id cannot be empty!");
		Request r = requestRepository.findById(requestId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found."));
		if (!(request.isUserInRole("ROLE_ADMIN") || request.isUserInRole("ROLE_MODERATOR"))) {
			if (!r.getOwner().equalsIgnoreCase(principal.getName())) {
				throw new AccessDeniedException("Access denied, insufficient privileges!");
			}
		}
		String documentId = r.getParsedDocumentPdfGuid();

		ByteArrayInputStream is = new ByteArrayInputStream(storageService.getDocumentContentById(documentId));
		Document document = storageService.getDocumentMetadataById(documentId);
		response.setContentType(document.getContentType());
		String fileName = "NDA-UnSigned-" + requestId + ".pdf";
		response.setHeader("Content-disposition", "inline; filename=" + fileName);
		org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
		response.flushBuffer();
	}

	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MODERATOR','ROLE_ADMIN')")
	@RequestMapping(value = "/dashboard/metrics", method = RequestMethod.GET)
	public DashboardMetricDto getDashboardMetrics(HttpServletRequest request, Principal principal)
			throws ParseException {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dddd");
		Calendar calExpiryDate = Calendar.getInstance();
		calExpiryDate.add(Calendar.DAY_OF_MONTH,
				Integer.valueOf(settingService.getSetting().getNdaExpiryCheckTimeInDays()));
		Date nDaysLater = null;
		nDaysLater = sdf.parse(sdf.format(calExpiryDate.getTime()));

		Calendar calStartDate = Calendar.getInstance();
		calStartDate.add(Calendar.DAY_OF_MONTH,
				-(Integer.valueOf(settingService.getSetting().getRecentRequestCheckTimeInDays())));
		Date nDaysBefore = null;
		nDaysBefore = sdf.parse(sdf.format(calStartDate.getTime()));

		DashboardMetricDto response = new DashboardMetricDto();
		if (request.isUserInRole("ROLE_ADMIN") || request.isUserInRole("ROLE_MODERATOR")) {
			response.setNdaCount(requestRepository.count());
			response.setPendingNdaCount(
					requestRepository.countByStatusIn(Arrays.asList("Sent To DocuSign", "Pending Signatures")));
			response.setOneWayNdaCount(requestRepository.countByNdaType("One-Way"));
			response.setMutualNdaCount(requestRepository.countByNdaType("Mutual"));
			response.setCustomerNdaCount(requestRepository.countByNdaType("Customer"));
			response.setAmendmentNdaCount(requestRepository.countByIsAmendment(true));
			response.setExpiringNdaCount(requestRepository.countByExpiryDateBetween(new Date(), nDaysLater));
			response.setRecentRequests(
					requestRepository.findTop3ByCreationTimeBetweenOrderByCreationTimeDesc(nDaysBefore, new Date()));
		} else {
			response.setNdaCount(requestRepository.countByOwner(principal.getName()));
			response.setPendingNdaCount(requestRepository.countByOwnerAndStatusIn(principal.getName(),
					Arrays.asList("Sent To DocuSign", "Pending Signatures")));
			response.setOneWayNdaCount(requestRepository.countByOwnerAndNdaType(principal.getName(), "One-Way"));
			response.setMutualNdaCount(requestRepository.countByOwnerAndNdaType(principal.getName(), "Mutual"));
			response.setCustomerNdaCount(requestRepository.countByOwnerAndNdaType(principal.getName(), "Customer"));
			response.setAmendmentNdaCount(requestRepository.countByOwnerAndIsAmendment(principal.getName(), true));
			response.setExpiringNdaCount(
					requestRepository.countByOwnerAndExpiryDateBetween(principal.getName(), new Date(), nDaysLater));
			response.setRecentRequests(requestRepository.findTop3ByOwnerAndCreationTimeBetweenOrderByCreationTimeDesc(
					principal.getName(), nDaysBefore, new Date()));
		}
		return response;
	}

	@PreAuthorize("hasAnyRole('ROLE_MODERATOR','ROLE_ADMIN')")
	@RequestMapping(value = "/requests", method = RequestMethod.GET)
	public ResponseEntity<?> listAllRequests(@RequestParam("page") int page) {
		HashMap<String, Object> response = new HashMap<>();
		response.put("total", String.valueOf(requestRepository.count()));
		response.put("results", requestRepository.findByOrderByCreationTimeDesc(PageRequest.of(page, 8)));
		return ResponseEntity.status(200).body(response);
	}

	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MODERATOR','ROLE_ADMIN')")
	@RequestMapping(value = "/user-requests", method = RequestMethod.GET)
	public ResponseEntity<?> listAllUserRequests(Principal principal, @RequestParam("page") int page) {
		HashMap<String, Object> response = new HashMap<>();
		response.put("total", String.valueOf(requestRepository.countByOwner(principal.getName())));
		response.put("results",
				requestRepository.findAllByOwnerOrderByCreationTimeDesc(principal.getName(), PageRequest.of(page, 8)));
		return ResponseEntity.status(200).body(response);
	}

	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MODERATOR','ROLE_ADMIN')")
	@PostAuthorize("returnObject.owner == authentication.name or hasAnyRole('ROLE_MODERATOR','ROLE_ADMIN')")
	@RequestMapping(value = "/request/{requestId}", method = RequestMethod.GET)
	public Request requestDetails(@PathVariable("requestId") String requestId, HttpServletRequest request) {
//		Assert.hasLength(requestId, "Request Id cannot be empty!");
		return requestRepository.findById(requestId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found."));
	}

	@PreAuthorize("hasAnyRole('ROLE_USER')")
	@RequestMapping(value = "/request-form/process", method = RequestMethod.POST)
	public ResponseEntity<?> processRequestForm(@RequestBody String body, Principal principal)
			throws JSONException, ParseException, JAXBException, IOException, SQLException, DocumentProcessingException,
			ApiException, DocumentNotFoundException {

		logger.info("Processing request form to create nda.");
		// save request in db.
		JSONObject requestJson = new JSONObject(body);

		Request r = new Request();
		String requestGuid = UUID.randomUUID().toString();
		r.setGuid(requestGuid);
		r.setOwner(principal.getName());
		r.setStatus("Created");
		r.setRequestorName(requestJson.get("section_ii_requestorName").toString().trim());
		r.setCertifierName(requestJson.get("section_ii_2_certifierName").toString().trim());
		r.setOtherPartyName(requestJson.get("section_iii_fullLegalName").toString().trim());
		r.setRequestorEmail(requestJson.get("section_ii_email").toString().trim());
		r.setCertifierEmail(requestJson.get("section_ii_2_email").toString().trim());
		r.setOtherPartyEmail(requestJson.get("section_iii_contactEmail").toString().trim());
		r.setBusinessUnit(requestJson.get("section_ii_ccsBusinessUnit").toString().trim());
		r.setNdaType(requestJson.get("section_iv_1").toString().trim());
		r.setInformationSharingPeriod(Integer.valueOf(requestJson.get("section_iv_4").toString().trim()));

		// request creation time
		r.setCreationTime(new Date());

		SimpleDateFormat dateFormatter = new SimpleDateFormat("MMMM dd, yyyy");
		Date startDate = dateFormatter.parse(requestJson.get("section_iv_5").toString().trim());
		// nda start/effective date
		r.setStartDate(startDate);

		// nda expiry date
		r.setExpiryDate(r.calculateExpiryDate());
		logger.info("Primary request details initialized.");

		// check and set amendment flag
		boolean isAmendment = false;
		if (requestJson.get("isAmendment") != null) {
			isAmendment = Boolean.parseBoolean(requestJson.get("isAmendment").toString());
			String amendmentGuid = requestJson.get("amendmentGuid").toString().trim();
			r.setAmendment(isAmendment);
			r.setAmendmentGuid(amendmentGuid);
			logger.info("Request is of amendment type.");
		}

		boolean isInformationShared = false;
		if (requestJson.get("isInformationShared") != null) {
			isInformationShared = Boolean.parseBoolean(requestJson.get("isInformationShared").toString());
		}

		String effectiveDate = requestJson.get("section_iv_5").toString().trim();
		boolean isStartDate30DaysPrior = false;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
		LocalDate aDate = LocalDate.parse(effectiveDate, formatter);

		if (aDate.isBefore(LocalDate.now().minusMonths(1)) && isInformationShared) {
			isStartDate30DaysPrior = true;
			r.setIs30DaysPrior(true);
			logger.info("Request is 30 days prior.");
		}

		boolean isNdaSpecificPurpose = false;
		if ("Yes".equalsIgnoreCase(requestJson.get("section_iv_3").toString().trim())) {
			isNdaSpecificPurpose = true;
			r.setSpecificPurpose(true);
			logger.info("Request is specific purpose.");
		}

		// complete request json
		r.setRequestData(body);

		// requestRepository.save(r);

		String ndaPeriodInYears = requestJson.get("section_iv_4").toString().trim();
		if (Integer.parseInt(ndaPeriodInYears) < 1 && Integer.parseInt(ndaPeriodInYears) > 5) {
			logger.error("Error encountered, information sharing period invalid.");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid information sharing period!");
		}

		// standard nda are those for which we have a nda template.
		// non standadrd are those for which we dont have a nda template.

		// process and create request form pdf.
		logger.info("Fetching request form template metadata from db.");
		String requestFormTemplateId = "rf-1";
		com.utc.nda.template.entities.Template requestFormTemplate = templateService.getRequestFormTemplate();
		requestFormTemplateId = requestFormTemplate.getDocumentGuid();

		logger.info("Fetching request form template content from db.");
		byte[] requestFormTemplateContent = storageService.getDocumentContentById(requestFormTemplateId);
		logger.info("Processing request form template.");
		String processedRequestFormTemplate = processTemplate(this.parseContent(requestJson),
				requestFormTemplateContent);
		logger.info("Converting request form template from markdown to pdf.");
		byte[] pdfRequestFormContent = convertMdtoPdf(processedRequestFormTemplate);

		Document requestFormDoc = new Document();
		requestFormDoc.setName("Request-Form.pdf");
		requestFormDoc.setCategory("REQUEST-FORM-PDF");
		requestFormDoc.setContentType("application/pdf");
		logger.info("Storing processed request form document pdf.");
		String requestFormDocId = storageService.createDocument(requestFormDoc, pdfRequestFormContent);
		r.setRequestFormPdfGuid(requestFormDocId);

		HashMap<String, String> response = new HashMap<>();
		response.put("message", "Thank you for submitting your bamboo request.  "
				+ "Please allow up to 24 hours for review of your request and up to 5 business days to complete.  "
				+ "If this is an urgent request or if you have any questions, " + "please contact the NDA mailbox at "
				+ settingService.getSetting().getNdaMailbox() + " or refer to the NDA website.");
		response.put("request_id", r.getGuid());
		response.put("requestor_approval", String.valueOf(false));

		String ndaType = requestJson.get("section_iv_1").toString().trim();
		if (isAmendment == true && false) { // amendment request ,blocking amendments for now.
			// check amendment id exists and owner is same as principal.
			Request amendmentRequest = requestRepository.findById(r.getAmendmentGuid()).orElseThrow(
					() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent amendment request not found."));
			if (r.getOwner().equalsIgnoreCase(amendmentRequest.getOwner()) == false) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN,
						"Authorized!, Can amend personal NDA's only. If you have any questions, "
								+ "please contact the NDA mailbox at " + settingService.getSetting().getNdaMailbox()
								+ " or refer to the NDA website.");
			}
			// NDA can be amended if its completed, and is not already amended or in process
			// of amendment.
			if ("Completed".equalsIgnoreCase(amendmentRequest.getStatus()) == false) {
				if ("Amendment In Progress".equalsIgnoreCase(amendmentRequest.getStatus())
						|| "Amended".equalsIgnoreCase(amendmentRequest.getStatus())) {
					throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
							"NDA cannot be amendeded again, Kindly select the latest NDA to amend. "
									+ "If you have any questions, please contact the NDA mailbox at "
									+ settingService.getSetting().getNdaMailbox() + " or refer to the NDA website.");
				} else {
					throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
							"NDA can be amended only after current signing process in complete. "
									+ "If you have any questions, please contact the NDA mailbox at "
									+ settingService.getSetting().getNdaMailbox() + " or refer to the NDA website.");
				}
			}

			// change status of old request to Amendment In Progress.
			amendmentRequest = requestRepository.findById(r.getAmendmentGuid()).orElseThrow(
					() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent amendment request not found."));
			amendmentRequest.setStatus("Amendment In Progress");
			requestRepository.save(amendmentRequest);

			// order of processing matters.
			if ("Customer".equalsIgnoreCase(ndaType)) {
				processAmendmentRequestCustomerNDA(r);
				return ResponseEntity.status(200).body(response);
			} else if ("One-Way".equalsIgnoreCase(ndaType) || "Mutual".equalsIgnoreCase(ndaType)) {
				if (isNdaSpecificPurpose) {
					processAmendmentRequestSpecificPurposeNDA(r);
					return ResponseEntity.status(200).body(response);
				}
				if (isStartDate30DaysPrior) {
					processAmendmentRequest30DaysPriorNDA(r);
					return ResponseEntity.status(200).body(response);
				}
				processAmendmentRequestStandardNDA(r);
				return ResponseEntity.status(200).body(response);
			} else {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid NDA Type!");
			}
		} else {
			// new request
			// order of processing matters.
			if ("Customer".equalsIgnoreCase(ndaType)) {
				processNewRequestCustomerNDA(r);
				return ResponseEntity.status(200).body(response);

			} else if ("One-Way".equalsIgnoreCase(ndaType) || "Mutual".equalsIgnoreCase(ndaType)) {
				if (isNdaSpecificPurpose) {
					processNewRequestSpecificPurposeNDA(r);
					return ResponseEntity.status(200).body(response);
				}
				if (isStartDate30DaysPrior) {
					processNewRequest30DaysPriorNDA(r);
					return ResponseEntity.status(200).body(response);
				}
				processNewRequestStandardNDA(r);
				logger.info("Request form processing completed successfully.");
				response.put("message", "Thank you for submitting your bamboo request.  "
						+ "Your request is approved and sent to DocuSign for signatures.");
				response.put("request_id", r.getGuid());
				return ResponseEntity.status(200).body(response);
			} else {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid NDA Type!");
			}
		}
	}

	public void processNewRequestCustomerNDA(Request r) throws DocumentProcessingException, DocumentNotFoundException {
		logger.info("Processing new request for customer nda.");
		Assert.notNull(r, "Request cannot be null!");
		JSONObject requestJson = new JSONObject(r.getRequestData());
		String companyFullName = requestJson.get("section_iii_fullLegalName").toString().trim();
		String requestorEmail = requestJson.get("section_ii_email").toString().trim();
		String customerNdaDocumentId = requestJson.get("customer_nda_id").toString().trim();

		logger.info("Updating request status.");
		r.setCustomerDocumentGuid(customerNdaDocumentId);
		r.setStatus("Pending Admin Review");
		requestRepository.save(r);

		// Send Portal Notifications
		logger.info("Sending portal notifications.");
		String ndaTypeDetails = "customer NDA";
		String newRequestIdentifier = ""; // Its blank for new request and amendment for amendments.

		String requestorNotificationContent = String.format(
				settingService.getSetting().getRequestorNotificationContentCustomerNdaAdminApproval(),
				newRequestIdentifier, ndaTypeDetails);
		String adminNotificationContent = String.format(
				settingService.getSetting().getAdminNotificationContentCustomerNdaAdminApproval(), newRequestIdentifier,
				ndaTypeDetails);
		String notificationTitle = String.format(
				settingService.getSetting().getNotificationTitleCustomerNdaAdminApproval(), companyFullName,
				newRequestIdentifier, ndaTypeDetails);
		String notificationType = "NDA-New-Request-Accepted";

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
		logger.info("Sending email notifications.");
		// prepare attachments
		EmailAttachment requestFormAttachment = new EmailAttachment();
		requestFormAttachment.setName("NDA-Request-Form.pdf");
		requestFormAttachment.setContentType("application/octet-stream");
		requestFormAttachment.setContent(storageService.getDocumentContentById(r.getRequestFormPdfGuid()));

		EmailAttachment customerNdaAttachment = new EmailAttachment();
		customerNdaAttachment.setName("Customer-NDA-Document.docx");
		customerNdaAttachment.setContentType("application/octet-stream");
		customerNdaAttachment.setContent(storageService.getDocumentContentById(r.getCustomerDocumentGuid()));

		HashMap<String, EmailAttachment> attachments = new HashMap<>();
		attachments.put("1", requestFormAttachment);
		attachments.put("2", customerNdaAttachment);

		// Send email to all users with role admin or moderator.
		String viewRequestUrl = String.format(settingService.getSetting().getUiViewRequestUrl(), r.getGuid());
		String adminEmailContent = String.format(
				settingService.getSetting().getAdminEmailContentCustomerNdaAdminApproval(), newRequestIdentifier,
				ndaTypeDetails, viewRequestUrl);

		EmailMessage adminEmailMessage = new EmailMessage();
		adminEmailMessage
				.setSubject(String.format(settingService.getSetting().getEmailSubjectCustomerNdaAdminApproval(),
						companyFullName, newRequestIdentifier, ndaTypeDetails));
		adminEmailMessage.setContent(adminEmailContent);
		adminEmailMessage.setToRoles(Arrays.asList("ROLE_ADMIN"));
		adminEmailMessage.setCcRoles(Arrays.asList("ROLE_MODERATOR"));
		adminEmailMessage.setAttachments(attachments);
		notificationService.sendEmail(adminEmailMessage);

		// requestor email notification
		String requestorEmailContent = String.format(
				settingService.getSetting().getRequestorEmailContentCustomerNdaAdminApproval(), newRequestIdentifier,
				ndaTypeDetails, viewRequestUrl);

		EmailMessage requestorEmailMessage = new EmailMessage();
		requestorEmailMessage.setSubject(String.format(settingService.getSetting().getEmailSubjectAdminApproval(),
				companyFullName, newRequestIdentifier, ndaTypeDetails));
		requestorEmailMessage.setContent(requestorEmailContent);
		requestorEmailMessage.setToEmails(Arrays.asList(requestorEmail));
		requestorEmailMessage.setAttachments(attachments);
		notificationService.sendEmail(requestorEmailMessage);
		logger.info("New request for customer nda completed successfully.");
	}

	public void processNewRequestSpecificPurposeNDA(Request r)
			throws JAXBException, IOException, SQLException, DocumentProcessingException, DocumentNotFoundException {
		logger.info("Processing new request for specific purpose nda.");
		Assert.notNull(r, "Request cannot be null!");
		JSONObject requestJson = new JSONObject(r.getRequestData());
		String companyFullName = requestJson.get("section_iii_fullLegalName").toString().trim();
		String requestorEmail = requestJson.get("section_ii_email").toString().trim();

		logger.info("Fetching template metadata from db.");
		com.utc.nda.template.entities.Template specificPurposeTemplate = templateService.getSpecificPurposeTemplate();
		String templateId = specificPurposeTemplate.getDocumentGuid();

		logger.info("Fetching template content from db.");
		byte[] templateContent = storageService.getDocumentContentById(templateId);
		String processedTemplate = processTemplate(this.parseContent(requestJson), templateContent);
		byte[] pdfTemplateContent = convertMdtoPdf(processedTemplate);

		Document document = new Document();
		document.setName("Processed-Document.pdf");
		document.setCategory("PROCESSED-TEMPLATE");
		document.setContentType("application/pdf");
		logger.info("Storing processed unsigned pdf document.");
		String id = storageService.createDocument(document, pdfTemplateContent);

		r.setParsedDocumentPdfGuid(id);
		r.setStatus("Pending Admin Approval");
		logger.info("Updating request status.");
		requestRepository.save(r);

		// Send Portal Notifications
		logger.info("Sending portal notifications.");
		String ndaTypeDetails = "specific purpose";
		String newRequestIdentifier = ""; // Its blank for new request and amendment for amendments.

		String requestorNotificationContent = String.format(
				settingService.getSetting().getRequestorNotificationContentAdminApproval(), newRequestIdentifier,
				ndaTypeDetails);
		String adminNotificationContent = String.format(
				settingService.getSetting().getAdminNotificationContentAdminApproval(), newRequestIdentifier,
				ndaTypeDetails);
		String notificationTitle = String.format(settingService.getSetting().getNotificationTitleAdminApproval(),
				companyFullName, newRequestIdentifier, ndaTypeDetails);
		String notificationType = "NDA-New-Request-Accepted";

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

		// Send email to all users with role admin or moderator.
		String viewRequestUrl = String.format(settingService.getSetting().getUiViewRequestUrl(), r.getGuid());
		String adminEmailContent = String.format(settingService.getSetting().getAdminEmailContentAdminApproval(),
				newRequestIdentifier, ndaTypeDetails, viewRequestUrl);

		EmailMessage adminEmailMessage = new EmailMessage();
		adminEmailMessage.setSubject(String.format(settingService.getSetting().getEmailSubjectAdminApproval(),
				companyFullName, newRequestIdentifier, ndaTypeDetails));
		adminEmailMessage.setContent(adminEmailContent);
		adminEmailMessage.setToRoles(Arrays.asList("ROLE_ADMIN"));
		adminEmailMessage.setCcRoles(Arrays.asList("ROLE_MODERATOR"));
		adminEmailMessage.setAttachments(attachments);
		notificationService.sendEmail(adminEmailMessage);

		// requestor email notification
		String requestorEmailContent = String.format(
				settingService.getSetting().getRequestorEmailContentAdminApproval(), newRequestIdentifier,
				ndaTypeDetails, viewRequestUrl);

		EmailMessage requestorEmailMessage = new EmailMessage();
		requestorEmailMessage.setSubject(String.format(settingService.getSetting().getEmailSubjectAdminApproval(),
				companyFullName, newRequestIdentifier, ndaTypeDetails));
		requestorEmailMessage.setContent(requestorEmailContent);
		requestorEmailMessage.setToEmails(Arrays.asList(requestorEmail));
		requestorEmailMessage.setAttachments(attachments);
		notificationService.sendEmail(requestorEmailMessage);
		logger.info("New request for specific purpose nda completed successfully.");
	}

	public void processNewRequest30DaysPriorNDA(Request r)
			throws JAXBException, IOException, SQLException, DocumentProcessingException, DocumentNotFoundException {
		logger.info("Processing new request for 30 days prior nda.");
		Assert.notNull(r, "Request cannot be null!");
		JSONObject requestJson = new JSONObject(r.getRequestData());
		String ndaType = requestJson.get("section_iv_1").toString().trim();
		String entityName = requestJson.get("section_ii_ccsBusinessUnit").toString().trim();
		String companyFullName = requestJson.get("section_iii_fullLegalName").toString().trim();
		String requestorEmail = requestJson.get("section_ii_email").toString().trim();

		logger.info("Fetching template metadata from db.");
		com.utc.nda.template.entities.Template standardTemplate = templateService.getTemplateByNameAndType(entityName,
				ndaType);
		String templateId = standardTemplate.getDocumentGuid();

		logger.info("Fetching template content from db.");
		byte[] templateContent = storageService.getDocumentContentById(templateId);
		String processedTemplate = processTemplate(this.parseContent(requestJson), templateContent);
		byte[] pdfTemplateContent = convertMdtoPdf(processedTemplate);

		Document document = new Document();
		document.setName("Processed-Document.pdf");
		document.setCategory("PROCESSED-TEMPLATE");
		document.setContentType("application/pdf");
		logger.info("Storing processed unsigned pdf document.");
		String id = storageService.createDocument(document, pdfTemplateContent);

		r.setParsedDocumentPdfGuid(id);
		r.setStatus("Pending Admin Approval");
		logger.info("Updating request status");
		requestRepository.save(r);

		// Send Portal Notifications
		logger.info("Sending portal notifications.");
		String ndaTypeDetails = "start date 30 days prior";
		String newRequestIdentifier = ""; // Its blank for new request and amendment for amendments.

		String requestorNotificationContent = String.format(
				settingService.getSetting().getRequestorNotificationContentAdminApproval(), newRequestIdentifier,
				ndaTypeDetails);
		String adminNotificationContent = String.format(
				settingService.getSetting().getAdminNotificationContentAdminApproval(), newRequestIdentifier,
				ndaTypeDetails);
		String notificationTitle = String.format(settingService.getSetting().getNotificationTitleAdminApproval(),
				companyFullName, newRequestIdentifier, ndaTypeDetails);
		String notificationType = "NDA-New-Request-Accepted";

		// Notification to requestor.
		NotificationMessage requestorNotification = new NotificationMessage();
		requestorNotification.setTitle(notificationTitle);
		requestorNotification.setContent(requestorNotificationContent);
		requestorNotification.setType(notificationType);
		requestorNotification.setToUsers(Arrays.asList(r.getOwner()));

		HashMap<String, String> attributes = new HashMap<String, String>();
		attributes.put("request_guid", r.getGuid());
		requestorNotification.setAttributes(attributes);
		notificationService.sendNotification(requestorNotification);

		// Notification to all users with roles admin or moderator.
		NotificationMessage adminNotification = new NotificationMessage();
		adminNotification.setTitle(notificationTitle);
		adminNotification.setContent(adminNotificationContent);
		adminNotification.setType(notificationType);
		adminNotification.setAttributes(attributes);
		adminNotification.setToRoles(Arrays.asList("ROLE_ADMIN", "ROLE_MODERATOR"));
		notificationService.sendNotification(adminNotification);

		// Send Email Notifications
		// prepare attachments
		logger.info("Storing email notifications.");
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

		// Send email to all users with role admin or moderator.
		String viewRequestUrl = String.format(settingService.getSetting().getUiViewRequestUrl(), r.getGuid());
		String adminEmailContent = String.format(settingService.getSetting().getAdminEmailContentAdminApproval(),
				newRequestIdentifier, ndaTypeDetails, viewRequestUrl);

		EmailMessage adminEmailMessage = new EmailMessage();
		adminEmailMessage.setSubject(String.format(settingService.getSetting().getEmailSubjectAdminApproval(),
				companyFullName, newRequestIdentifier, ndaTypeDetails));
		adminEmailMessage.setContent(adminEmailContent);
		adminEmailMessage.setToRoles(Arrays.asList("ROLE_ADMIN"));
		adminEmailMessage.setCcRoles(Arrays.asList("ROLE_MODERATOR"));
		adminEmailMessage.setAttachments(attachments);
		notificationService.sendEmail(adminEmailMessage);

		// requestor email notification
		String requestorEmailContent = String.format(
				settingService.getSetting().getRequestorEmailContentAdminApproval(), newRequestIdentifier,
				ndaTypeDetails, viewRequestUrl);

		EmailMessage requestorEmailMessage = new EmailMessage();
		requestorEmailMessage.setSubject(String.format(settingService.getSetting().getEmailSubjectAdminApproval(),
				companyFullName, newRequestIdentifier, ndaTypeDetails));
		requestorEmailMessage.setContent(requestorEmailContent);
		requestorEmailMessage.setToEmails(Arrays.asList(requestorEmail));
		requestorEmailMessage.setAttachments(attachments);
		notificationService.sendEmail(requestorEmailMessage);
		logger.info("New request for 30 days prior nda completed successfully.");
	}

	public void processNewRequestStandardNDA(Request r) throws JAXBException, IOException, SQLException,
			DocumentProcessingException, ApiException, DocumentNotFoundException {
		logger.info("Processing new request for standard nda.");
		Assert.notNull(r, "Request cannot be null!");
		JSONObject requestJson = new JSONObject(r.getRequestData());
		String ndaType = requestJson.get("section_iv_1").toString().trim();
		String entityName = requestJson.get("section_ii_ccsBusinessUnit").toString().trim();
		String companyFullName = requestJson.get("section_iii_fullLegalName").toString().trim();
		String requestorEmail = requestJson.get("section_ii_email").toString().trim();

		logger.info("Fetching template metadata from db.");
		com.utc.nda.template.entities.Template standardTemplate = templateService.getTemplateByNameAndType(entityName,
				ndaType);
		String templateId = standardTemplate.getDocumentGuid();

		logger.info("Fetching template content from db.");
		byte[] templateContent = storageService.getDocumentContentById(templateId);
		String processedTemplate = processTemplate(this.parseContent(requestJson), templateContent);
		byte[] pdfTemplateContent = convertMdtoPdf(processedTemplate);

		Document document = new Document();
		document.setName("Processed-Document.pdf");
		document.setCategory("PROCESSED-TEMPLATE");
		document.setContentType("application/pdf");
		logger.info("Storing processed unsigned pdf document.");
		String id = storageService.createDocument(document, pdfTemplateContent);

		r.setParsedDocumentPdfGuid(id);
		r.setStatus("Pending Auto Approval");
		logger.info("Updating request status.");
		requestRepository.save(r);

		logger.info("Sending request to docusign for signature.");
		requestService.sendToDocusignSigning(r.getGuid());
		logger.info("New request for standard nda completed successfully.");
	}

	public void processAmendmentRequestCustomerNDA(Request r)
			throws DocumentProcessingException, DocumentNotFoundException {
		logger.info("Processing amendment request for customer nda.");
		Assert.notNull(r, "Request cannot be null!");
		JSONObject requestJson = new JSONObject(r.getRequestData());
		String companyFullName = requestJson.get("section_iii_fullLegalName").toString().trim();
		String requestorEmail = requestJson.get("section_ii_email").toString().trim();
		String customerNdaDocumentId = requestJson.get("customer_nda_id").toString().trim();

		logger.info("Updating request status");
		r.setCustomerDocumentGuid(customerNdaDocumentId);
		r.setStatus("Pending Admin Review");
		requestRepository.save(r);

		// Send Portal Notifications
		logger.info("Sending portal notifications.");
		String ndaTypeDetails = "customer NDA";
		String amendmentRequestIdentifier = "amendment"; // Its blank for new request and amendment for amendments.
		String requestorNotificationContent = String.format(
				settingService.getSetting().getRequestorNotificationContentCustomerNdaAdminApproval(),
				amendmentRequestIdentifier, ndaTypeDetails);
		String adminNotificationContent = String.format(
				settingService.getSetting().getAdminNotificationContentCustomerNdaAdminApproval(),
				amendmentRequestIdentifier, ndaTypeDetails);
		String notificationTitle = String.format(
				settingService.getSetting().getNotificationTitleCustomerNdaAdminApproval(), companyFullName,
				amendmentRequestIdentifier, ndaTypeDetails);
		String notificationType = "NDA-Amendment-Request-Accepted";

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

		EmailAttachment customerNdaAttachment = new EmailAttachment();
		customerNdaAttachment.setName("Customer-NDA-Document.docx");
		customerNdaAttachment.setContentType("application/octet-stream");
		customerNdaAttachment.setContent(storageService.getDocumentContentById(r.getCustomerDocumentGuid()));

		Request parentRequest = requestRepository.findById(r.getAmendmentGuid()).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent amendment request not found."));
		EmailAttachment parentNdaAttachment = new EmailAttachment();
		parentNdaAttachment.setName("Parent-NDA-Document.pdf");
		parentNdaAttachment.setContentType("application/octet-stream");
		parentNdaAttachment.setContent(storageService.getDocumentContentById(parentRequest.getParsedDocumentPdfGuid()));

		HashMap<String, EmailAttachment> attachments = new HashMap<>();
		attachments.put("1", requestFormAttachment);
		attachments.put("2", customerNdaAttachment);
		attachments.put("3", parentNdaAttachment);

		// Send email to all users with role admin or moderator.
		logger.info("preparing email content.");
		String viewRequestUrl = String.format(settingService.getSetting().getUiViewRequestUrl(), r.getGuid());
		String adminEmailContent = String.format(
				settingService.getSetting().getAdminEmailContentCustomerNdaAdminApproval(), amendmentRequestIdentifier,
				ndaTypeDetails, viewRequestUrl);

		EmailMessage adminEmailMessage = new EmailMessage();
		adminEmailMessage
				.setSubject(String.format(settingService.getSetting().getEmailSubjectCustomerNdaAdminApproval(),
						companyFullName, amendmentRequestIdentifier, ndaTypeDetails));
		adminEmailMessage.setContent(adminEmailContent);
		adminEmailMessage.setToRoles(Arrays.asList("ROLE_ADMIN"));
		adminEmailMessage.setCcRoles(Arrays.asList("ROLE_MODERATOR"));
		adminEmailMessage.setAttachments(attachments);
		notificationService.sendEmail(adminEmailMessage);

		// requestor email notification
		String requestorEmailContent = String.format(
				settingService.getSetting().getRequestorEmailContentCustomerNdaAdminApproval(),
				amendmentRequestIdentifier, ndaTypeDetails, viewRequestUrl);

		EmailMessage requestorEmailMessage = new EmailMessage();
		requestorEmailMessage
				.setSubject(String.format(settingService.getSetting().getEmailSubjectCustomerNdaAdminApproval(),
						companyFullName, amendmentRequestIdentifier, ndaTypeDetails));
		requestorEmailMessage.setContent(requestorEmailContent);
		requestorEmailMessage.setToEmails(Arrays.asList(requestorEmail));
		requestorEmailMessage.setAttachments(attachments);
		notificationService.sendEmail(requestorEmailMessage);
		logger.info("Email notifications sent.");
		logger.info("Amendment request for customer nda completed successfully.");

	}

	public void processAmendmentRequestSpecificPurposeNDA(Request r)
			throws JAXBException, IOException, SQLException, DocumentProcessingException, DocumentNotFoundException {
		logger.info("Processing amendment request for specific purpose nda.");
		Assert.notNull(r, "Request cannot be null!");
		JSONObject requestJson = new JSONObject(r.getRequestData());

		logger.info("Fetching template content from db.");
		com.utc.nda.template.entities.Template specificPurposeTemplate = templateService.getSpecificPurposeTemplate();
		String templateId = specificPurposeTemplate.getDocumentGuid();

		logger.info("Fetching template metadata from db.");
		byte[] templateContent = storageService.getDocumentContentById(templateId);
		String processedTemplate = processTemplate(this.parseContent(requestJson), templateContent);
		byte[] pdfTemplateContent = convertMdtoPdf(processedTemplate);

		Document document = new Document();
		document.setName("Processed-Document.pdf");
		document.setCategory("PROCESSED-TEMPLATE");
		document.setContentType("application/pdf");
		logger.info("Storing processed unsigned pdf document.");
		String id = storageService.createDocument(document, pdfTemplateContent);

		logger.info("Updating request status.");
		r.setParsedDocumentPdfGuid(id);
		r.setStatus("Pending Admin Approval");
		requestRepository.save(r);

		logger.info("Sending portal and email notifications.");
		sendPortalNotificationsAmendmentRequest(r, "specific purpose"); // Send Portal Notifications
		sendEmailNotificationsAmendmentRequest(r, "specific purpose"); // Send Email Notifications
		logger.info("Amendment request for specific nda completed successfully.");
	}

	public void processAmendmentRequest30DaysPriorNDA(Request r)
			throws JAXBException, IOException, SQLException, DocumentProcessingException, DocumentNotFoundException {
		logger.info("Processing amendment request for 30 days prior nda.");
		Assert.notNull(r, "Request cannot be null!");
		JSONObject requestJson = new JSONObject(r.getRequestData());
		String ndaType = requestJson.get("section_iv_1").toString().trim();
		String entityName = requestJson.get("section_ii_ccsBusinessUnit").toString().trim();

		logger.info("Fetching template metadata from db.");
		com.utc.nda.template.entities.Template standardTemplate = templateService.getTemplateByNameAndType(entityName,
				ndaType);
		String templateId = standardTemplate.getDocumentGuid();

		logger.info("Fetching template content from db.");
		byte[] templateContent = storageService.getDocumentContentById(templateId);
		String processedTemplate = processTemplate(this.parseContent(requestJson), templateContent);
		byte[] pdfTemplateContent = convertMdtoPdf(processedTemplate);

		Document document = new Document();
		document.setName("Processed-Document.pdf");
		document.setCategory("PROCESSED-TEMPLATE");
		document.setContentType("application/pdf");
		logger.info("Storing processed unsigned pdf document.");
		String id = storageService.createDocument(document, pdfTemplateContent);

		logger.info("Updating request status.");
		r.setParsedDocumentPdfGuid(id);
		r.setStatus("Pending Admin Approval");
		requestRepository.save(r);

		logger.info("Sending portal and email notifications.");
		sendPortalNotificationsAmendmentRequest(r, "start date 30 days prior"); // Send Portal Notifications
		sendEmailNotificationsAmendmentRequest(r, "start date 30 days prior"); // Send Email Notifications
		logger.info("Amendment request for 30 days prior nda completed successfully.");
	}

	public void processAmendmentRequestStandardNDA(Request r)
			throws JAXBException, IOException, SQLException, DocumentProcessingException, DocumentNotFoundException {
		logger.info("Processing amendment request for standard nda.");
		Assert.notNull(r, "Request cannot be null!");
		JSONObject requestJson = new JSONObject(r.getRequestData());
		String ndaType = requestJson.get("section_iv_1").toString().trim();
		String entityName = requestJson.get("section_ii_ccsBusinessUnit").toString().trim();

		logger.info("Fetching template metadata from db.");
		com.utc.nda.template.entities.Template standardTemplate = templateService.getTemplateByNameAndType(entityName,
				ndaType);
		String templateId = standardTemplate.getDocumentGuid();

		logger.info("Fetching template content from db.");
		byte[] templateContent = storageService.getDocumentContentById(templateId);
		String processedTemplate = processTemplate(this.parseContent(requestJson), templateContent);
		byte[] pdfTemplateContent = convertMdtoPdf(processedTemplate);

		Document document = new Document();
		document.setName("Processed-Document.pdf");
		document.setCategory("PROCESSED-TEMPLATE");
		document.setContentType("application/pdf");
		logger.info("Storing processed unsigned pdf document.");
		String id = storageService.createDocument(document, pdfTemplateContent);

		logger.info("Updating request status.");
		r.setParsedDocumentPdfGuid(id);
		r.setStatus("Pending Admin Approval");
		requestRepository.save(r);

		logger.info("Sending portal and email notifications.");
		sendPortalNotificationsAmendmentRequest(r, "standard NDA"); // Send Portal Notifications
		sendEmailNotificationsAmendmentRequest(r, "standard NDA"); // Send Email Notifications
		logger.info("Amendment request for standard nda completed successfully.");
	}

	public void sendPortalNotificationsAmendmentRequest(Request r, String ndaTypeDetails) {
		logger.info("Sending portal notifications for amendment request.");
		Assert.notNull(r, "Request cannot be null!");
		Assert.hasLength(ndaTypeDetails, "NdaTypeDetails cannot be empty!");
		JSONObject requestJson = new JSONObject(r.getRequestData());
		String companyFullName = requestJson.get("section_iii_fullLegalName").toString().trim();

		// Send Portal Notifications
		String amendmentRequestIdentifier = "amendment"; // Its blank for new request and amendment for amendments.
		String requestorNotificationContent = String.format(
				settingService.getSetting().getRequestorNotificationContentAdminApproval(), amendmentRequestIdentifier,
				ndaTypeDetails);
		String adminNotificationContent = String.format(
				settingService.getSetting().getAdminNotificationContentAdminApproval(), amendmentRequestIdentifier,
				ndaTypeDetails);
		String notificationTitle = String.format(settingService.getSetting().getNotificationTitleAdminApproval(),
				companyFullName, amendmentRequestIdentifier, ndaTypeDetails);
		String notificationType = "NDA-Amendment-Request-Accepted";

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
		logger.info("Portal notifications for amendment request sent.");
	}

	public void sendEmailNotificationsAmendmentRequest(Request r, String ndaTypeDetails)
			throws DocumentProcessingException, DocumentNotFoundException {
		logger.info("Sending amendment request email notifications.");
		Assert.notNull(r, "Request cannot be null!");
		Assert.hasLength(ndaTypeDetails, "NdaTypeDetails cannot be empty!");
		JSONObject requestJson = new JSONObject(r.getRequestData());
		String companyFullName = requestJson.get("section_iii_fullLegalName").toString().trim();
		String requestorEmail = requestJson.get("section_ii_email").toString().trim();
		String amendmentRequestIdentifier = "amendment"; // Its blank for new request and amendment for amendments.

		// Send Email Notifications
		// prepare attachments
		logger.info("Preparing email attachments.");
		EmailAttachment requestFormAttachment = new EmailAttachment();
		requestFormAttachment.setName("NDA-Request-Form.pdf");
		requestFormAttachment.setContentType("application/octet-stream");
		requestFormAttachment.setContent(storageService.getDocumentContentById(r.getRequestFormPdfGuid()));

		EmailAttachment ndaAttachment = new EmailAttachment();
		ndaAttachment.setName("NDA-Document.pdf");
		ndaAttachment.setContentType("application/octet-stream");
		ndaAttachment.setContent(storageService.getDocumentContentById(r.getParsedDocumentPdfGuid()));

		Request parentRequest = requestRepository.findById(r.getAmendmentGuid()).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent amendment request not found."));
		EmailAttachment parentNdaAttachment = new EmailAttachment();
		parentNdaAttachment.setName("Parent-NDA-Document.pdf");
		parentNdaAttachment.setContentType("application/octet-stream");
		parentNdaAttachment.setContent(storageService.getDocumentContentById(parentRequest.getParsedDocumentPdfGuid()));

		HashMap<String, EmailAttachment> attachments = new HashMap<>();
		attachments.put("1", requestFormAttachment);
		attachments.put("2", ndaAttachment);
		attachments.put("3", parentNdaAttachment);

		logger.info("Preparing email content.");
		// Send email to all users with role admin or moderator.
		String viewRequestUrl = String.format(settingService.getSetting().getUiViewRequestUrl(), r.getGuid());
		String adminEmailContent = String.format(settingService.getSetting().getAdminEmailContentAdminApproval(),
				amendmentRequestIdentifier, ndaTypeDetails, viewRequestUrl);

		EmailMessage adminEmailMessage = new EmailMessage();
		adminEmailMessage.setSubject(String.format(settingService.getSetting().getEmailSubjectAdminApproval(),
				companyFullName, amendmentRequestIdentifier, ndaTypeDetails));
		adminEmailMessage.setContent(adminEmailContent);
		adminEmailMessage.setToRoles(Arrays.asList("ROLE_ADMIN"));
		adminEmailMessage.setCcRoles(Arrays.asList("ROLE_MODERATOR"));
		adminEmailMessage.setAttachments(attachments);
		notificationService.sendEmail(adminEmailMessage);
		logger.info("Admin email sent.");

		// requestor email notification
		String requestorEmailContent = String.format(
				settingService.getSetting().getRequestorEmailContentAdminApproval(), amendmentRequestIdentifier,
				ndaTypeDetails, viewRequestUrl);

		EmailMessage requestorEmailMessage = new EmailMessage();
		requestorEmailMessage.setSubject(String.format(settingService.getSetting().getEmailSubjectAdminApproval(),
				companyFullName, amendmentRequestIdentifier, ndaTypeDetails));
		requestorEmailMessage.setContent(requestorEmailContent);
		requestorEmailMessage.setToEmails(Arrays.asList(requestorEmail));
		requestorEmailMessage.setAttachments(attachments);
		notificationService.sendEmail(requestorEmailMessage);
		logger.info("Requestor email sent.");
	}

	public String processTemplate(HashMap<String, String> variableMappings, byte[] template)
			throws JAXBException, IOException, SQLException {
		logger.info("Creating document by populating template variables.");
		Assert.notEmpty(variableMappings, "Variable Mappings cannot be empty!");
		Assert.notNull(template, "Template file cannot be null!");
		Assert.isTrue(template.length > 0, "Template file cannot be empty!");

		String templateMarkdown = new String(template);

		Handlebars handlebars = new Handlebars();
		Template handlebarTemplate;
		handlebarTemplate = handlebars.compileInline(templateMarkdown);
		String processedMarkdown = handlebarTemplate.apply(variableMappings);
		logger.info("Document creation by populating template variables completed successfully.");
		return processedMarkdown;
	}

	public byte[] convertMdtoPdf(String markdown) throws JAXBException, IOException, SQLException {
		logger.info("Converting markdown to pdf.");
		Assert.hasLength(markdown, "Markdown content cannot be empty!");

		MutableDataSet options = new MutableDataSet().set(TablesExtension.COLUMN_SPANS, false)
				.set(TablesExtension.APPEND_MISSING_COLUMNS, true).set(TablesExtension.DISCARD_EXTRA_COLUMNS, true)
				.set(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, true)
				.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create()));

		// uncomment to convert soft-breaks to hard breaks
		options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");
		Parser parser = Parser.builder(options).build();

		HtmlRenderer renderer = HtmlRenderer.builder(options).build();
		Node document = parser.parse(markdown);
		logger.debug(markdown);
		String parsedHtml = renderer.render(document);
		parsedHtml = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">\n" + "<style>\n" + "table {\n"
				+ "    width:100%;\n" + "}\n" + "</style>" + "</head><body>" + parsedHtml + "\n" + "</body></html>";
		logger.debug(parsedHtml);
		logger.info("Markdown parsed and converted to html.");
		Path path = Files.createTempFile("md-to-pdf", ".pdf");
		logger.info("Converting html to pdf.");
		PdfConverterExtension.exportToPdf(path.toString(), parsedHtml, "", options);
		byte[] content = Files.readAllBytes(path);
		path.toFile().delete();
		logger.info("Converting markdown to pdf completed successfully.");
		return content;
	}

	public HashMap<String, String> parseContent(JSONObject requestJson) throws JAXBException, IOException {
		logger.info("Creating variable map from json object.");
		Assert.notNull(requestJson, "Json object cannot be null!");

		String ndaType = requestJson.get("section_iv_1").toString().trim();
		String entityName = requestJson.get("section_ii_ccsBusinessUnit").toString().trim();
		String effectiveDate = requestJson.get("section_iv_5").toString().trim();

		String ocName = requestJson.get("section_iii_fullLegalName").toString().trim();
		String ocContactName = requestJson.get("section_iii_contactName").toString().trim();
		String ocBusinessAddress = requestJson.get("section_iii_fullBusinessAddress").toString().trim();
		String ocAddress = requestJson.get("section_iii_fullBusinessAddress").toString().trim();
		String ocCity = requestJson.get("section_iii_city").toString().trim();
		String ocState = requestJson.get("section_iii_state").toString().trim();
		String ocZip = requestJson.get("section_iii_zipCode").toString().trim();
		String ocEmail = requestJson.get("section_iii_contactEmail").toString().trim();

		ocBusinessAddress = ocBusinessAddress + ", " + ocCity + ", " + ocState + " " + ocZip;

		String requestorName = requestJson.get("section_ii_requestorName").toString().trim();
		String requestorAddress = requestJson.get("section_ii_address").toString().trim();
		String requestorCity = requestJson.get("section_ii_city").toString().trim();
		String requestorState = requestJson.get("section_ii_state").toString().trim();
		String requestorZip = requestJson.get("section_ii_zip").toString().trim();
		String requestorEmail = requestJson.get("section_ii_email").toString().trim();
		String requestorPhone = requestJson.get("section_ii_phone").toString().trim();
		String requestorJobTitle = requestJson.get("section_ii_jobTitle").toString().trim();
		String legaEntityRegion = requestJson.get("section_ii_legalEntityRegion").toString().trim();

		String isCertifierAlsoRequestor = requestJson.get("isCertifierAlsoRequestor").toString().trim();

		String certifierName = requestJson.get("section_ii_2_certifierName").toString().trim();
		String certifierAddress = requestJson.get("section_ii_2_address").toString().trim();
		String certifierCity = requestJson.get("section_ii_2_city").toString().trim();
		String certifierState = requestJson.get("section_ii_2_state").toString().trim();
		String certifierZip = requestJson.get("section_ii_2_ZipCode").toString().trim();
		String certifierEmail = requestJson.get("section_ii_2_email").toString().trim();

		String section1_2 = requestJson.get("section_i_2").toString().trim();
		String section1_3 = requestJson.get("section_i_3").toString().trim();
		String isBiddingRFPFromOtherParty = requestJson.get("isBiddingRFPFromOtherParty").toString().trim();
		String section1_4 = requestJson.get("section_i_4").toString().trim();
		String section1_5 = requestJson.get("section_i_5").toString().trim();

		String section2_1 = requestJson.get("section_ii_1").toString().trim();

		String section3_1 = requestJson.get("section_iii_1").toString().trim();
		// String section3_2 = requestJson.get("section_iii_2").toString().trim();

		String isNdaForReviewProvided = requestJson.get("isNdaForReviewProvided").toString().trim();

		String section4_2_desc = requestJson.get("section_iv_2_textarea").toString().trim();
		String section4_3 = requestJson.get("section_iv_3").toString().trim();
		String section4_3_desc = requestJson.get("section_iv_3_textarea").toString().trim();

		String ndaPeriodInYears = requestJson.get("section_iv_4").toString().trim();
		String ndaPeriodInYearsInWords = "";

		if (ndaPeriodInYears.equals("1")) {
			ndaPeriodInYearsInWords = "one ";
		} else if (ndaPeriodInYears.equals("2")) {
			ndaPeriodInYearsInWords = "two ";
		} else if (ndaPeriodInYears.equals("3")) {
			ndaPeriodInYearsInWords = "three ";
		} else if (ndaPeriodInYears.equals("4")) {
			ndaPeriodInYearsInWords = "four ";
		} else if (ndaPeriodInYears.equals("5")) {
			ndaPeriodInYearsInWords = "five ";
		}

		String ndaTimePeriod = "";
		if (Integer.parseInt(ndaPeriodInYears) > 1) {
			ndaTimePeriod = " " + ndaPeriodInYearsInWords + "(" + ndaPeriodInYears + ")" + " years ";
		} else {
			ndaTimePeriod = " " + ndaPeriodInYearsInWords + "(" + ndaPeriodInYears + ")" + " year ";
		}

		HashMap<String, String> mappings = new HashMap<String, String>();
		mappings.put("effective-date", StringEscapeUtils.escapeXml11(effectiveDate));
		mappings.put("other-company-name", StringEscapeUtils.escapeXml11(ocName));
		mappings.put("business-address-of-other-company", StringEscapeUtils.escapeXml11(ocBusinessAddress));
		mappings.put("numeric-year", StringEscapeUtils.escapeXml11(ndaPeriodInYearsInWords));
		mappings.put("year", StringEscapeUtils.escapeXml11(ndaPeriodInYears));
		mappings.put("nda-time-period", StringEscapeUtils.escapeXml11(ndaTimePeriod));
		mappings.put("certifier-address", StringEscapeUtils.escapeXml11(certifierAddress));
		mappings.put("certifier-city", StringEscapeUtils.escapeXml11(certifierCity));
		mappings.put("certifier-state", StringEscapeUtils.escapeXml11(certifierState));
		mappings.put("certifier-zip", StringEscapeUtils.escapeXml11(certifierZip));
		mappings.put("certifier-name", StringEscapeUtils.escapeXml11(certifierName));

		mappings.put("is-certifier-also-requestor", StringEscapeUtils.escapeXml11(isCertifierAlsoRequestor));

		mappings.put("certifier-email", StringEscapeUtils.escapeXml11(certifierEmail));

		mappings.put("other-company-address", StringEscapeUtils.escapeXml11(ocAddress));
		mappings.put("other-company-city", StringEscapeUtils.escapeXml11(ocCity));
		mappings.put("other-company-state", StringEscapeUtils.escapeXml11(ocState));
		mappings.put("other-company-zip", StringEscapeUtils.escapeXml11(ocZip));
		mappings.put("other-company-contact-name", StringEscapeUtils.escapeXml11(ocContactName));

		mappings.put("other-company-email", StringEscapeUtils.escapeXml11(ocEmail));

		mappings.put("requestor-address", StringEscapeUtils.escapeXml11(requestorAddress));
		mappings.put("requestor-city", StringEscapeUtils.escapeXml11(requestorCity));
		mappings.put("requestor-state", StringEscapeUtils.escapeXml11(requestorState));
		mappings.put("requestor-zip", StringEscapeUtils.escapeXml11(requestorZip));
		mappings.put("requestor-name", StringEscapeUtils.escapeXml11(requestorName));
		mappings.put("requestor-email", StringEscapeUtils.escapeXml11(requestorEmail));
		mappings.put("requestor-phone", StringEscapeUtils.escapeXml11(requestorPhone));
		mappings.put("requestor-job-title", StringEscapeUtils.escapeXml11(requestorJobTitle));

		mappings.put("ccs-business-unit", StringEscapeUtils.escapeXml11(entityName));
		mappings.put("legal-entity-region", StringEscapeUtils.escapeXml11(legaEntityRegion));
		mappings.put("nda-type", StringEscapeUtils.escapeXml11(ndaType));

		mappings.put("section1-2", StringEscapeUtils.escapeXml11(section1_2));
		mappings.put("section1-3", StringEscapeUtils.escapeXml11(section1_3));
		mappings.put("is-bidding-rfp-from-other-party", StringEscapeUtils.escapeXml11(isBiddingRFPFromOtherParty));
		mappings.put("section1-4", StringEscapeUtils.escapeXml11(section1_4));
		mappings.put("section1-5", StringEscapeUtils.escapeXml11(section1_5));
		mappings.put("section2-1", StringEscapeUtils.escapeXml11(section2_1));
		mappings.put("section3-1", StringEscapeUtils.escapeXml11(section3_1));
		// mappings.put("section3-2", StringEscapeUtils.escapeXml11(section3_2));
		mappings.put("section4-2-desc", StringEscapeUtils.escapeXml11(section4_2_desc));
		mappings.put("section4-3", StringEscapeUtils.escapeXml11(section4_3));
		mappings.put("section4-3-desc", StringEscapeUtils.escapeXml11(section4_3_desc));
		mappings.put("is-nda-for-review-provided", StringEscapeUtils.escapeXml11(isNdaForReviewProvided));

		mappings.put("second-signer-name",
				StringEscapeUtils.escapeXml11(settingService.getSetting().getIpAttorneyName()));
		mappings.put("second-signer-title",
				StringEscapeUtils.escapeXml11(settingService.getSetting().getIpAttorneyTitle()));
		logger.info("Variable map created successfully.");
		return mappings;

	}

	@PreAuthorize("hasAnyRole('ROLE_MODERATOR','ROLE_ADMIN')")
//	@PostFilter("filterObject.owner == authentication.name or hasAnyRole('ROLE_MODERATOR','ROLE_ADMIN')")
	@RequestMapping(value = "/requests/search", method = RequestMethod.GET)
	public List<Request> searchRequestByParams(@RequestParam(required = false) String guid,
			@RequestParam(required = false) String owner, @RequestParam(required = false) String status,
			@RequestParam(required = false) String requestorName, @RequestParam(required = false) String certifierName,
			@RequestParam(required = false) String otherPartyName, @RequestParam(required = false) String businessUnit,
			@RequestParam(required = false) String ndaType,
			@RequestParam(required = false) Integer informationSharingPeriod,
			@RequestParam(name = "startDate", required = false) Date startDate,
			@RequestParam(required = false) Date expiryDate,
			@RequestParam(name = "creationTime", required = false) Date creationTime,
			@RequestParam(required = false) boolean isAmendment, @RequestParam(required = false) String docusignGuid)
			throws ParseException {
		RequestSearchDto requestSearchParams = new RequestSearchDto();

		requestSearchParams.setGuid(guid);
		requestSearchParams.setOwner(owner);
		requestSearchParams.setStatus(status);
		requestSearchParams.setRequestorName(requestorName);
		requestSearchParams.setCertifierName(certifierName);
		requestSearchParams.setOtherPartyName(otherPartyName);
		requestSearchParams.setBusinessUnit(businessUnit);
		requestSearchParams.setNdaType(ndaType);
		requestSearchParams.setInformationSharingPeriod(informationSharingPeriod);
		requestSearchParams.setStartDate(startDate);
		requestSearchParams.setExpiryDate(expiryDate);
		requestSearchParams.setCreationTime(creationTime);
		if (Boolean.valueOf(isAmendment) != null) {
			requestSearchParams.setIsAmendment(isAmendment);
		}
		requestSearchParams.setDocusignGuid(docusignGuid);

		List<Request> results = requestService.query(requestSearchParams);
		logger.info("Search results count: " + results.size());
		return results;
	}

	@PreAuthorize("hasAnyRole('ROLE_MODERATOR','ROLE_ADMIN')")
	@RequestMapping(value = "/requests/reportdata", method = RequestMethod.GET)
	public List<SearchData> searchRequestByDate(@RequestParam("start") String start, @RequestParam("end") String end,
			@RequestParam("type") String type) throws ParseException {

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

		List<Request> data = null;

		List<SearchData> search = new ArrayList<SearchData>();

		Map<String, Integer> searchData = new HashMap<String, Integer>();

		data = requestRepository.findByCreationTimeBetween(new SimpleDateFormat("yyyy-MM-dd").parse(start),
				new SimpleDateFormat("yyyy-MM-dd").parse(end));

		if (type.equalsIgnoreCase("ndaCount")) {
			for (Request request : data) {

				String createDate = simpleDateFormat.format(request.getCreationTime());
				if (searchData.containsKey(createDate)) {
					int count = searchData.get(createDate) + 1;
					searchData.put(createDate, count);
				} else {
					searchData.put(createDate, 1);
				}
			}
			for (String s : searchData.keySet()) {
				search.add(new SearchData(s, searchData.get(s)));
			}
		} else if (type.equalsIgnoreCase("ndaType")) {

			for (Request request : data) {

				String ndaType = request.getNdaType();
				if (searchData.containsKey(ndaType)) {
					int count = searchData.get(ndaType) + 1;
					searchData.put(ndaType, count);
				} else {
					searchData.put(ndaType, 1);
				}
			}
			for (String s : searchData.keySet()) {
				search.add(new SearchData(s, searchData.get(s)));
			}

		}

		return search;
	}

	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MODERATOR','ROLE_ADMIN')")
	@RequestMapping(value = "/requests/export", method = RequestMethod.GET)
	public void exportRequestByParams(@RequestParam(required = false) String guid,
			@RequestParam(required = false) String owner, @RequestParam(required = false) String status,
			@RequestParam(required = false) String requestorName, @RequestParam(required = false) String certifierName,
			@RequestParam(required = false) String otherPartyName, @RequestParam(required = false) String businessUnit,
			@RequestParam(required = false) String ndaType,
			@RequestParam(required = false) Integer informationSharingPeriod,
			@RequestParam(required = false) String startDate, @RequestParam(required = false) String expiryDate,
			@RequestParam(required = false) String creationTime, @RequestParam(required = false) boolean isAmendment,
			@RequestParam(required = false) String docusignGuid, HttpServletResponse response,
			HttpServletRequest request, Principal principal) throws ParseException, IOException {

		if (!(request.isUserInRole("ROLE_ADMIN") || request.isUserInRole("ROLE_MODERATOR"))) {
			owner = principal.getName();
		}

		RequestSearchDto requestSearchParams = new RequestSearchDto();

		response.setHeader("Content-Disposition", "attachment; filename=Search-Results.csv");

		CSVWriter csvWriter = null;

		requestSearchParams.setGuid(guid);
		requestSearchParams.setOwner(owner);
		requestSearchParams.setStatus(status);
		requestSearchParams.setRequestorName(requestorName);
		requestSearchParams.setCertifierName(certifierName);
		requestSearchParams.setOtherPartyName(otherPartyName);
		requestSearchParams.setBusinessUnit(businessUnit);
		requestSearchParams.setNdaType(ndaType);
		requestSearchParams.setInformationSharingPeriod(informationSharingPeriod);
		requestSearchParams.setIsAmendment(isAmendment);

		requestSearchParams.setDocusignGuid(docusignGuid);

		if (startDate != null) {
			requestSearchParams.setStartDate(new SimpleDateFormat("yyyy-MM-dd").parse(startDate));
		}
		if (expiryDate != null) {
			requestSearchParams.setExpiryDate(new SimpleDateFormat("yyyy-MM-dd").parse(expiryDate));

		}
		if (creationTime != null) {
			requestSearchParams.setCreationTime(new SimpleDateFormat("yyyy-MM-dd").parse(creationTime));
		}

		OutputStreamWriter outputwriter = new OutputStreamWriter(response.getOutputStream());
		csvWriter = new CSVWriter(outputwriter, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);

		String[] header = { "CreationTime", "RequestorName", "CertifierName", "OtherPartyName", "BusinessUnit",
				"NDAType", "InformationSharingPeriod", "Status", "StartDate", "ExpiryDate", "IsAmendment" };
		csvWriter.writeNext(header);

		List<Request> data = requestService.query(requestSearchParams);

		List<String[]> csvData = new LinkedList<String[]>();

//			
//			csvData.add("CreationTime,RequestorName,CertifierName,OtherPartyName,BusinessUnit,NDAType,InformationSharingPeriod,"
//					+ "status,startDate,expiryDate\n");
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		for (Request req : data) {

			String[] row = new String[11];
			row[0] = simpleDateFormat.format(req.getCreationTime());
			row[1] = req.getRequestorName();
			row[2] = req.getCertifierName();
			row[3] = req.getOtherPartyName();
			row[4] = req.getBusinessUnit();
			row[5] = req.getNdaType();
			row[6] = req.getInformationSharingPeriod() + "";
			row[7] = req.getStatus();
			row[8] = simpleDateFormat.format(req.getStartDate());
			row[9] = simpleDateFormat.format(req.getExpiryDate());
			row[10] = req.isAmendment() + "";
			csvData.add(row);
		}
		csvWriter.writeAll(csvData);
		csvWriter.close();
	}

}
