package com.utc.nda.docusign.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.docusign.esign.api.EnvelopesApi;
import com.docusign.esign.api.EnvelopesApi.ListStatusChangesOptions;
import com.docusign.esign.client.ApiClient;
import com.docusign.esign.client.ApiException;
import com.docusign.esign.client.Configuration;
import com.docusign.esign.client.auth.OAuth;
import com.docusign.esign.client.auth.OAuth.UserInfo;
import com.docusign.esign.model.CarbonCopy;
import com.docusign.esign.model.Document;
import com.docusign.esign.model.Envelope;
import com.docusign.esign.model.EnvelopeDefinition;
import com.docusign.esign.model.EnvelopeDocumentsResult;
import com.docusign.esign.model.EnvelopeSummary;
import com.docusign.esign.model.EnvelopesInformation;
import com.docusign.esign.model.FullName;
import com.docusign.esign.model.RecipientEmailNotification;
import com.docusign.esign.model.Recipients;
import com.docusign.esign.model.SignHere;
import com.docusign.esign.model.Signer;
import com.docusign.esign.model.Tabs;
import com.docusign.esign.model.Title;
import com.utc.nda.docusign.entities.DocumentStatus;
import com.utc.nda.request.entities.Request;
import com.utc.nda.request.repositories.RequestRepository;
import com.utc.nda.request.services.RequestService;
import com.utc.nda.setting.services.SettingService;

@Service("signatureServiceImpl")
public class SignatureServiceImpl implements SignatureService {

	private static final Logger logger = LoggerFactory.getLogger(SignatureServiceImpl.class);

	@Autowired
	private SettingService settingService;

	@Autowired
	private RequestRepository requestRepository;

	@Autowired
	private RequestService requestService;

	private long TOKEN_EXPIRATION_IN_SECONDS = 3600;
	private String privateKey = "";
	private String publicKey = "";
	private String oAuthBaseUrl = "";
	private String baseUrl = "";
	private String integratorKey = "";
	private String userId = "";

	private static long expiresIn;
	private static String accessToken = null;
	private ApiClient apiClient = null;
	private UserInfo userInfo = null;
	private String accountId = null;

	public String getToken() {
		return accessToken;
	}

	public ApiClient getApiClient() {
		return this.apiClient;
	}

	public String getAccountId() {
		return this.accountId;
	}

	public void checkToken() throws IOException, ApiException {
		logger.info("Checking if docusign token valid.");
		if (accessToken == null || (System.currentTimeMillis()) > expiresIn) {
			updateToken();
		}
	}

	private void updateToken() throws IOException, ApiException {
		logger.info("Generating new docusign token.");
		// using updated settings when getting token.
		this.oAuthBaseUrl = settingService.getSetting().getDocusignOAuthBaseUrl();
		this.baseUrl = settingService.getSetting().getDocusignBaseUrl();
		this.integratorKey = settingService.getSetting().getDocusignIntegratorKey();
		this.userId = settingService.getSetting().getDocusignUserId();
		this.TOKEN_EXPIRATION_IN_SECONDS = Long
				.valueOf(settingService.getSetting().getDocusignTokenExpiryTimeInSeconds());
		this.privateKey = settingService.getSetting().getDocusignPrivateKey();
		this.publicKey = settingService.getSetting().getDocusignPublicKey();
		this.apiClient = new ApiClient(this.baseUrl);

		java.util.List<String> scopes = new java.util.ArrayList<String>();
		scopes.add(OAuth.Scope_SIGNATURE);
		scopes.add(OAuth.Scope_IMPERSONATION);

		Path privateKeyPath = null;
		privateKeyPath = Files.createTempFile("docusign-private-key", ".txt");
		Files.write(privateKeyPath, this.privateKey.getBytes());

		Path publicKeyPath = null;
		publicKeyPath = Files.createTempFile("docusign-public-key", ".txt");
		Files.write(publicKeyPath, this.publicKey.getBytes());

		this.apiClient.configureJWTAuthorizationFlow(publicKeyPath.toString(), privateKeyPath.toString(),
				this.oAuthBaseUrl, this.integratorKey, this.userId, this.TOKEN_EXPIRATION_IN_SECONDS);
		logger.info("Docusign token generated successfully.");
		accessToken = this.apiClient.getAccessToken();
		expiresIn = System.currentTimeMillis() + (this.TOKEN_EXPIRATION_IN_SECONDS * 1000);
		logger.debug("Docusign access token: " + accessToken);

		this.userInfo = this.apiClient.getUserInfo(accessToken);
		this.accountId = this.userInfo.getAccounts().get(0).getAccountId();
		this.apiClient.setBasePath(this.userInfo.getAccounts().get(0).getBaseUri() + "/restapi");
		Configuration.setDefaultApiClient(this.apiClient);
		this.apiClient.addDefaultHeader("Authorization", "Bearer " + accessToken);
	}

	@Override
	public String docuSign(String documentName, byte[] documentToSign, String requestorName, String requestorEmail,
			String firstSignerName, String firstSignerEmail, String secondSignerName, String secondSignerEmail,
			String certifierName, String certifierEmail, String ndaType) throws IOException, ApiException {
		logger.info("Sending request to docusign.");
		checkToken();
		EnvelopesApi envelopesApi = new EnvelopesApi(this.apiClient);
		EnvelopeDefinition envDef = new EnvelopeDefinition();

		String emailSubject = String.format(settingService.getSetting().getDocusignEnvelopeEmailSubject(),
				documentName);
		if (emailSubject.length() > 100) {
			emailSubject = emailSubject.substring(0, 96).concat("...");
		}
		envDef.setEmailSubject(emailSubject);

		// add a document to the envelope
		Document doc = new Document();
		String base64Doc = Base64.getEncoder().encodeToString(documentToSign);
		doc.setDocumentBase64(base64Doc);
		doc.setName(documentName); // can be different from actual file name
		doc.setDocumentId("1");

		// create a list of docs and add it to the envelope
		List<Document> docs = new ArrayList<Document>();
		docs.add(doc);
		envDef.setDocuments(docs);

		// first signer will be other party contact (name and email)
		Signer firstSigner = new Signer();
		firstSigner.setEmail(firstSignerEmail);
		firstSigner.setName(firstSignerName);

		if ("Customer".equalsIgnoreCase(ndaType)) {
			// ip attorney will be first signer.
			// other party will be second signer.
			firstSigner.setRecipientId("2");
			firstSigner.setRoutingOrder("2");
		} else {
			// other party will be first signer.
			// ip attorney will be second signer.
			firstSigner.setRecipientId("1");
			firstSigner.setRoutingOrder("1");
		}

		RecipientEmailNotification firstSignerEmailNotification = new RecipientEmailNotification();
		String firstSignerEmailSubject = String.format(settingService.getSetting().getDocusignEmailSubjectOtherParty(),
				documentName);
		if (firstSignerEmailSubject.length() > 100) {
			firstSignerEmailSubject = firstSignerEmailSubject.substring(0, 96).concat("...");
		}
		firstSignerEmailNotification.setEmailSubject(firstSignerEmailSubject);

		if ("Customer".equalsIgnoreCase(ndaType)) {
			// second signer in case of customer nda
			// Ip attorney signs first followed by other party.
			firstSignerEmailNotification.emailBody(String.format(
					settingService.getSetting().getDocusignCustomerNDAEmailContentOtherParty(), firstSignerName));
		} else {
			// first signer if not customer nda.
			// other party signs first followed by ip attorney.
			firstSignerEmailNotification.emailBody(
					String.format(settingService.getSetting().getDocusignEmailContentOtherParty(), firstSignerName));
		}

		firstSigner.setEmailNotification(firstSignerEmailNotification);

		SignHere firstSignersSignHere = new SignHere();
		firstSignersSignHere.setAnchorString("signature#1");
		firstSignersSignHere.setAnchorIgnoreIfNotPresent("true");

		List<SignHere> firstSignersSignHereTabs = new ArrayList<SignHere>();
		firstSignersSignHereTabs.add(firstSignersSignHere);

		FullName firstSignersFullName = new FullName();
		firstSignersFullName.setAnchorString("fullname#1");
		firstSignersFullName.anchorIgnoreIfNotPresent("true");

		List<FullName> firstSignersFullNameTabs = new ArrayList<FullName>();
		firstSignersFullNameTabs.add(firstSignersFullName);

		Title firstSignersTitle = new Title();
		firstSignersTitle.setAnchorString("title#1");
		firstSignersTitle.setAnchorIgnoreIfNotPresent("true");

		List<Title> firstSignersTitleTabs = new ArrayList<Title>();
		firstSignersTitleTabs.add(firstSignersTitle);

		Tabs firstSignersTabs = new Tabs();
		firstSignersTabs.setSignHereTabs(firstSignersSignHereTabs);
		firstSignersTabs.setFullNameTabs(firstSignersFullNameTabs);
		firstSignersTabs.setTitleTabs(firstSignersTitleTabs);

		firstSigner.setTabs(firstSignersTabs);

		// second signer will be Randy H.
		Signer secondSigner = new Signer();
		secondSigner.setEmail(secondSignerEmail);
		secondSigner.setName(secondSignerName);

		if ("Customer".equalsIgnoreCase(ndaType)) {
			secondSigner.setRecipientId("1");
			secondSigner.setRoutingOrder("1");
		} else {
			secondSigner.setRecipientId("2");
			secondSigner.setRoutingOrder("2");
		}

		RecipientEmailNotification secondSignerEmailNotification = new RecipientEmailNotification();
		String secondSignerEmailSubject = String.format(settingService.getSetting().getDocusignEmailSubjectIpAttorney(),
				documentName);
		if (secondSignerEmailSubject.length() > 100) {
			secondSignerEmailSubject = secondSignerEmailSubject.substring(0, 96).concat("...");
		}
		secondSignerEmailNotification.setEmailSubject(secondSignerEmailSubject);
		if ("Customer".equalsIgnoreCase(ndaType)) {
			// first signer in case of customer nda.
			// ip attorney signs first followed by other party.
			secondSignerEmailNotification.emailBody(String.format(
					settingService.getSetting().getDocusignCustomerNDAEmailContentIpAttorney(), secondSignerName));
		} else {
			// second signer if not customer nda.
			// other party signs first followed by ip attorney.
			secondSignerEmailNotification.emailBody(
					String.format(settingService.getSetting().getDocusignEmailContentIpAttorney(), secondSignerName));
		}
		secondSigner.setEmailNotification(secondSignerEmailNotification);

		SignHere secondSignersSignHere = new SignHere();
		secondSignersSignHere.setAnchorString("signature#2");
		secondSignersSignHere.setAnchorIgnoreIfNotPresent("true");

		List<SignHere> secondSignersSignHereTabs = new ArrayList<SignHere>();
		secondSignersSignHereTabs.add(secondSignersSignHere);

		Tabs secondSignersTabs = new Tabs();
		secondSignersTabs.setSignHereTabs(secondSignersSignHereTabs);

		secondSigner.setTabs(secondSignersTabs);

		List<CarbonCopy> carbonCopies = new ArrayList<CarbonCopy>();
		CarbonCopy requestorCarbonCopy = new CarbonCopy();
		requestorCarbonCopy.setEmail(requestorEmail);
		requestorCarbonCopy.setName(requestorName);
		requestorCarbonCopy.setRecipientId("3");
		requestorCarbonCopy.setRoutingOrder("3");

		RecipientEmailNotification requestorCarbonCopyEmailNotification = new RecipientEmailNotification();
		String requestorCarbonCopyEmailSubject = String
				.format(settingService.getSetting().getDocusignCarbonCopyEmailSubjectRequestor(), documentName);
		if (requestorCarbonCopyEmailSubject.length() > 100) {
			requestorCarbonCopyEmailSubject = requestorCarbonCopyEmailSubject.substring(0, 96).concat("...");
		}
		requestorCarbonCopyEmailNotification.setEmailSubject(requestorCarbonCopyEmailSubject);
		requestorCarbonCopyEmailNotification
				.emailBody(settingService.getSetting().getDocusignCarbonCopyEmailContentRequestor());
		requestorCarbonCopy.setEmailNotification(requestorCarbonCopyEmailNotification);
		carbonCopies.add(requestorCarbonCopy);

		if (!certifierEmail.equalsIgnoreCase(requestorEmail)) {
			CarbonCopy certiferCarbonCopy = new CarbonCopy();
			certiferCarbonCopy.setEmail(certifierEmail);
			certiferCarbonCopy.setName(certifierName);
			certiferCarbonCopy.setRecipientId("4");
			certiferCarbonCopy.setRoutingOrder("3");

			RecipientEmailNotification certifierCarbonCopyEmailNotification = new RecipientEmailNotification();
			String certifierCarbonCopyEmailSubject = String
					.format(settingService.getSetting().getDocusignCarbonCopyEmailSubjectCertifier(), documentName);
			if (certifierCarbonCopyEmailSubject.length() > 100) {
				certifierCarbonCopyEmailSubject = certifierCarbonCopyEmailSubject.substring(0, 96).concat("...");
			}
			certifierCarbonCopyEmailNotification.setEmailSubject(certifierCarbonCopyEmailSubject);
			certifierCarbonCopyEmailNotification
					.emailBody(settingService.getSetting().getDocusignCarbonCopyEmailContentCertifier());
			certiferCarbonCopy.setEmailNotification(certifierCarbonCopyEmailNotification);
			carbonCopies.add(certiferCarbonCopy);
		}

		envDef.setRecipients(new Recipients());
		envDef.getRecipients().setSigners(new ArrayList<Signer>());
		envDef.getRecipients().getSigners().add(firstSigner);
		envDef.getRecipients().getSigners().add(secondSigner);
		envDef.getRecipients().carbonCopies(carbonCopies);

		envDef.setStatus("sent");

		EnvelopeSummary envelopeSummary = envelopesApi.createEnvelope(this.accountId, envDef);
		String docuSignId = envelopeSummary.getEnvelopeId().toString();
		logger.info("Docusign signature request completed. DocuSign ID: " + docuSignId);
		return docuSignId;
	}

	@Override
	public Object getAllEnvelope() throws IOException, ApiException {
		checkToken();
		EnvelopesApi envelopesApi = new EnvelopesApi(this.apiClient);
		ListStatusChangesOptions options = envelopesApi.new ListStatusChangesOptions();

		LocalDate date = LocalDate.now().minusDays(30);
		options.setFromDate(date.toString("yyyy/MM/dd"));
		return envelopesApi.listStatusChanges(this.accountId, options);
	}

	@Override
	public Object getEnvelopeStatus(String envelopeId) throws IOException, ApiException {
		checkToken();
		EnvelopesApi envelopesApi = new EnvelopesApi(this.apiClient);
		Recipients recipient = envelopesApi.listRecipients(this.accountId, envelopeId);

		List<DocumentStatus> documentStatus = new ArrayList<DocumentStatus>();
		for (CarbonCopy cc : recipient.getCarbonCopies()) {
			DocumentStatus ds = new DocumentStatus();
			ds.setType("CarbonCopy");
			ds.setEmail(cc.getEmail());
			ds.setRoutingOrder(cc.getRoutingOrder());
			ds.setStatus(cc.getStatus());
			documentStatus.add(ds);
		}

		for (Signer signer : recipient.getSigners()) {
			DocumentStatus ds = new DocumentStatus();
			ds.setType("Signer");
			ds.setEmail(signer.getEmail());
			ds.setRoutingOrder(signer.getRoutingOrder());
			ds.setStatus(signer.getStatus());
			documentStatus.add(ds);
		}
		return documentStatus;
	}

	@Override
	public Object getEnvelopeRecipients(String envelopeId) throws IOException, ApiException {
		checkToken();
		EnvelopesApi envelopesApi = new EnvelopesApi(this.apiClient);
		return envelopesApi.getEnvelope(this.accountId, envelopeId);
	}

	@Override
	public byte[] getEnvelopeDocument(String envelopeId) throws IOException, ApiException {
		checkToken();
		EnvelopesApi envelopesApi = new EnvelopesApi(this.apiClient);
		return envelopesApi.getDocument(this.accountId, envelopeId, "1");
	}

	@Override
	public String getEnvelopeDocumentName(String envelopeId) throws IOException, ApiException {

		checkToken();
		EnvelopesApi envelopesApi = new EnvelopesApi(this.apiClient);
		EnvelopeDocumentsResult docsList = envelopesApi.listDocuments(this.accountId, envelopeId);
		return docsList.getEnvelopeDocuments().get(0).getName();
	}

	@Scheduled(cron = "${docusign.cron.schedule}")
	@Transactional
	public void updateStatusFromDocusign() {
		logger.info("Starting docusign cron.");
		long totalRequestRecieved = 0;
		long totalRequestProcessed = 0;
		long totalRequestIgnored = 0;
		long totalRequestFailed = 0;
		try {
			checkToken();
			EnvelopesApi envelopesApi = new EnvelopesApi(this.getApiClient());
			ListStatusChangesOptions options = envelopesApi.new ListStatusChangesOptions();

			LocalDate date = LocalDate.now().minusDays(3);
			options.setFromDate(date.toString("yyyy/MM/dd"));
			EnvelopesInformation information = envelopesApi.listStatusChanges(this.getAccountId(), options);

			for (Envelope e : information.getEnvelopes()) {
				try {
					totalRequestRecieved += 1;
					logger.info("Received update for envelope ID: " + e.getEnvelopeId());

					Optional<Request> optionalRequest = requestRepository.findByDocusignGuid(e.getEnvelopeId());
					if (optionalRequest.isPresent() == false) {
						totalRequestIgnored += 1;
						logger.info("Request not found for docusign ID: " + e.getEnvelopeId());
						continue;
					}

					Request request = optionalRequest.get();
					logger.info("Valid request found, request ID: " + request.getGuid());

					String docusignStatus = e.getStatus();
					String convertedStatus = "";
					if (docusignStatus.equalsIgnoreCase("completed")) {
						convertedStatus = "Completed";
						if (request.getStatus().equalsIgnoreCase(convertedStatus) == false) {
							// update status
							request.setStatus(convertedStatus);
							requestRepository.save(request);

							// update parent request in case of amendment.
							if (request.isAmendment()) {
								Optional<Request> optionalParentRequest = requestRepository
										.findById(request.getAmendmentGuid());
								if (optionalParentRequest.isPresent()) {
									Request parentRequest = optionalParentRequest.get();
									parentRequest.setStatus("Amended");
									requestRepository.save(parentRequest);
								} else {
									logger.info("Parent request not found, parent request ID: "
											+ request.getAmendmentGuid());
								}
							}
							totalRequestProcessed += 1;
							logger.info("Request processed, ID: " + request.getGuid());

							// save updated document
							byte[] signedDocument = envelopesApi.getDocument(this.accountId, e.getEnvelopeId(), "1");
							requestService.saveSignedDocument(request, signedDocument);

							// send notification and emails
						} else {
							totalRequestIgnored += 1;
							logger.info("Request ignored, already updated, ID: " + request.getGuid());
						}
					} else if (docusignStatus.equalsIgnoreCase("voided")) {
						convertedStatus = "Voided";
						if (request.getStatus().equalsIgnoreCase(convertedStatus) == false) {
							// update status
							request.setStatus(convertedStatus);
							requestRepository.save(request);
							totalRequestProcessed += 1;
							logger.info("Request processed, ID: " + request.getGuid());
						} else {
							totalRequestIgnored += 1;
							logger.info("Request ignored, already updated, ID: " + request.getGuid());
						}
					} else if (docusignStatus.equalsIgnoreCase("declined")) {
						convertedStatus = "Declined";
						if (request.getStatus().equalsIgnoreCase(convertedStatus) == false) {
							// update status
							request.setStatus(convertedStatus);
							requestRepository.save(request);
							// update parent request in case of amendment.
							if (request.isAmendment()) {
								Optional<Request> optionalParentRequest = requestRepository
										.findById(request.getAmendmentGuid());
								if (optionalParentRequest.isPresent()) {
									Request parentRequest = optionalParentRequest.get();
									parentRequest.setStatus("Completed");
									requestRepository.save(parentRequest);
								} else {
									logger.info("Parent request not found, parent request ID: "
											+ request.getAmendmentGuid());
								}
							}
							totalRequestProcessed += 1;
							logger.info("Request processed, ID: " + request.getGuid());
						} else {
							totalRequestIgnored += 1;
							logger.info("Request ignored, already updated, ID: " + request.getGuid());
						}
					} else if (docusignStatus.equalsIgnoreCase("created") || docusignStatus.equalsIgnoreCase("sent")
							|| docusignStatus.equalsIgnoreCase("delivered")
							|| docusignStatus.equalsIgnoreCase("signed")) {
						convertedStatus = "Pending Signatures";
						if (request.getStatus().equalsIgnoreCase(convertedStatus) == false) {
							// update status
							request.setStatus(convertedStatus);
							requestRepository.save(request);
							totalRequestProcessed += 1;
							logger.info("Request processed, ID: " + request.getGuid());
						} else {
							totalRequestIgnored += 1;
							logger.info("Request ignored, already updated, ID: " + request.getGuid());
						}
					} else {
						totalRequestIgnored += 1;
						logger.info("Ignored Status found, status: " + e.getStatus());
						logger.info("Request ignored, ignored status, ID: " + request.getGuid());
						continue;
					}
				} catch (Exception ex) {
					totalRequestFailed += 1;
					logger.error("Error encountered while processing request in docusign cron, ", ex);
				}
			}
		} catch (Exception e) {
			logger.error("Error encountered in docusign cron, ", e);
		} finally {
			logger.info(String.format(
					"Docusign cron finished. Stats:- Total Request Recieved: %1$d, Processed: %2$d, Failed: %3$d, Ignored: %4$d",
					totalRequestRecieved, totalRequestProcessed, totalRequestFailed, totalRequestIgnored));
		}
	}

}
