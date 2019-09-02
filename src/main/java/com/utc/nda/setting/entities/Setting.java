package com.utc.nda.setting.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "setting")
public class Setting {

	@Id
	@Size(max = 36)
	private String guid;

	@Size(max = 255)
	@NotNull
	private String ndaApprover;

	@Size(max = 255)
	@NotNull
	private String ndaMailbox;

	@Max(10000) // approx 30 years
	@Min(1)
	@NotNull
	private String ndaExpiryCheckTimeInDays;

	@Max(86400) // 1 day
	@Min(300)
	@NotNull
	private String forgotPasswordLinkExpiryTimeInSeconds;

	@Max(1000)
	@Min(1)
	@NotNull
	private String recentRequestCheckTimeInDays;

	@Size(max = 255)
	@NotNull
	private String uiViewRequestUrl;

	@Size(max = 255)
	@NotNull
	private String uiForgotPasswordUpdateUrl;

	@Size(max = 255)
	@NotNull
	private String uiVerifyAccountEmailUrl;

	@Size(max = 1000)
	@NotNull
	private String validEmailDomains;

	@Size(max = 255)
	@NotNull
	private String ipAttorneyTitle;

	@Size(max = 255)
	@NotNull
	private String ipAttorneyName;

	@Size(max = 255)
	@NotNull
	private String ipAttorneyEmail;

	@Size(max = 255)
	@NotNull
	private String mailServerHost;

	@Max(65535)
	@Min(1)
	@NotNull
	private String mailServerPort;

	@Size(max = 255)
	@NotNull
	private String mailServerAuth;

	@Size(max = 255)
	@NotNull
	private String mailServerEnableTls;

	@Size(max = 255)
	private String mailServerUsername;

	@Size(max = 255)
	private String mailServerPassword;

	@Size(max = 255)
	@NotNull
	private String mailTransportProtocol;

	@Size(max = 255)
	@NotNull
	private String docusignOAuthBaseUrl;

	@Size(max = 255)
	@NotNull
	private String docusignBaseUrl;

	@Size(max = 255)
	@NotNull
	private String docusignIntegratorKey;

	@Size(max = 255)
	@NotNull
	private String docusignUserId;

	@NotNull
	@Size(max = 5000)
	private String docusignPrivateKey;

	@NotNull
	@Size(max = 5000)
	private String docusignPublicKey;

	@Max(2592000) // 30 days
	@Min(1)
	@NotNull
	private String docusignTokenExpiryTimeInSeconds;

	@NotNull
	@Size(max = 100)
	private String docusignEnvelopeEmailSubject;

	@NotNull
	@Size(max = 100)
	private String docusignEmailSubjectOtherParty;

	@NotNull
	@Size(max = 2000)
	private String docusignEmailContentOtherParty;

	@NotNull
	@Size(max = 100)
	private String docusignEmailSubjectIpAttorney;

	@Size(max = 2000)
	@NotNull
	private String docusignEmailContentIpAttorney;

	@NotNull
	@Size(max = 100)
	private String docusignCustomerNDAEmailSubjectOtherParty;

	@NotNull
	@Size(max = 2000)
	private String docusignCustomerNDAEmailContentOtherParty;

	@NotNull
	@Size(max = 100)
	private String docusignCustomerNDAEmailSubjectIpAttorney;

	@NotNull
	@Size(max = 2000)
	private String docusignCustomerNDAEmailContentIpAttorney;

	@NotNull
	@Size(max = 100)
	private String docusignCarbonCopyEmailSubjectRequestor;

	@NotNull
	@Size(max = 2000)
	private String docusignCarbonCopyEmailContentRequestor;

	@NotNull
	@Size(max = 100)
	private String docusignCarbonCopyEmailSubjectCertifier;

	@NotNull
	@Size(max = 2000)
	private String docusignCarbonCopyEmailContentCertifier;

	@NotNull
	@Size(max = 100)
	private String notificationTitleRequestApproved;

	@NotNull
	@Size(max = 2000)
	private String requestorNotificationContentRequestApproved;

	@NotNull
	@Size(max = 2000)
	private String adminNotificationContentRequestApproved;

	@NotNull
	@Size(max = 100)
	private String emailSubjectRequestApproved;

	@NotNull
	@Size(max = 2000)
	private String requestorEmailContentRequestApproved;

	@NotNull
	@Size(max = 2000)
	private String adminEmailContentRequestApproved;

	@NotNull
	@Size(max = 100)
	private String notificationTitleAdminApproval;

	@NotNull
	@Size(max = 2000)
	private String requestorNotificationContentAdminApproval;

	@NotNull
	@Size(max = 2000)
	private String adminNotificationContentAdminApproval;

	@NotNull
	@Size(max = 100)
	private String emailSubjectAdminApproval;

	@NotNull
	@Size(max = 2000)
	private String requestorEmailContentAdminApproval;

	@NotNull
	@Size(max = 2000)
	private String adminEmailContentAdminApproval;

	@NotNull
	@Size(max = 100)
	private String adminNotificationTitleUserRegistered;

	@NotNull
	@Size(max = 2000)
	private String adminNotificationContentUserRegistered;

	@NotNull
	@Size(max = 100)
	private String adminEmailSubjectUserRegistered;

	@NotNull
	@Size(max = 2000)
	private String adminEmailContentUserRegistered;

	@NotNull
	@Size(max = 100)
	private String userEmailSubjectUserRegistered;

	@NotNull
	@Size(max = 2000)
	private String userEmailContentUserRegistered;

	@NotNull
	@Size(max = 100)
	private String notificationTitleUserActivated;

	@NotNull
	@Size(max = 2000)
	private String adminNotificationContentUserActivated;

	@NotNull
	@Size(max = 100)
	private String adminEmailSubjectUserActivated;

	@NotNull
	@Size(max = 2000)
	private String adminEmailContentUserActivated;

	@NotNull
	@Size(max = 100)
	private String userEmailSubjectUserActivated;

	@NotNull
	@Size(max = 2000)
	private String userEmailContentUserActivated;

	@NotNull
	@Size(max = 100)
	private String notificationTitleCustomerNdaAdminApproval;

	@NotNull
	@Size(max = 2000)
	private String requestorNotificationContentCustomerNdaAdminApproval;

	@NotNull
	@Size(max = 2000)
	private String adminNotificationContentCustomerNdaAdminApproval;

	@NotNull
	@Size(max = 100)
	private String emailSubjectCustomerNdaAdminApproval;

	@NotNull
	@Size(max = 2000)
	private String requestorEmailContentCustomerNdaAdminApproval;

	@NotNull
	@Size(max = 2000)
	private String adminEmailContentCustomerNdaAdminApproval;

	@NotNull
	@Size(max = 100)
	private String emailSubjectAccountEmailVerification;

	@NotNull
	@Size(max = 2000)
	private String emailContentAccountEmailVerification;

	@NotNull
	@Size(max = 100)
	private String notificationTitleForgotPasswordInitiation;

	@NotNull
	@Size(max = 2000)
	private String notificationContentForgotPasswordInitiation;

	@NotNull
	@Size(max = 100)
	private String emailSubjectForgotPasswordInitiation;

	@NotNull
	@Size(max = 2000)
	private String emailContentForgotPasswordInitiation;

	@NotNull
	@Size(max = 100)
	private String notificationTitleForgotPasswordCompletion;

	@NotNull
	@Size(max = 2000)
	private String notificationContentForgotPasswordCompletion;

	@NotNull
	@Size(max = 100)
	private String emailSubjectForgotPasswordCompletion;

	@NotNull
	@Size(max = 2000)
	private String emailContentForgotPasswordCompletion;

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getNdaApprover() {
		return ndaApprover;
	}

	public void setNdaApprover(String ndaApprover) {
		this.ndaApprover = ndaApprover;
	}

	public String getNdaMailbox() {
		return ndaMailbox;
	}

	public void setNdaMailbox(String ndaMailbox) {
		this.ndaMailbox = ndaMailbox;
	}

	public String getNdaExpiryCheckTimeInDays() {
		return ndaExpiryCheckTimeInDays;
	}

	public void setNdaExpiryCheckTimeInDays(String ndaExpiryCheckTimeInDays) {
		this.ndaExpiryCheckTimeInDays = ndaExpiryCheckTimeInDays;
	}

	public String getForgotPasswordLinkExpiryTimeInSeconds() {
		return forgotPasswordLinkExpiryTimeInSeconds;
	}

	public void setForgotPasswordLinkExpiryTimeInSeconds(String forgotPasswordLinkExpiryTimeInSeconds) {
		this.forgotPasswordLinkExpiryTimeInSeconds = forgotPasswordLinkExpiryTimeInSeconds;
	}

	public String getRecentRequestCheckTimeInDays() {
		return recentRequestCheckTimeInDays;
	}

	public void setRecentRequestCheckTimeInDays(String recentRequestCheckTimeInDays) {
		this.recentRequestCheckTimeInDays = recentRequestCheckTimeInDays;
	}

	public String getUiViewRequestUrl() {
		return uiViewRequestUrl;
	}

	public void setUiViewRequestUrl(String uiViewRequestUrl) {
		this.uiViewRequestUrl = uiViewRequestUrl;
	}

	public String getUiForgotPasswordUpdateUrl() {
		return uiForgotPasswordUpdateUrl;
	}

	public void setUiForgotPasswordUpdateUrl(String uiForgotPasswordUpdateUrl) {
		this.uiForgotPasswordUpdateUrl = uiForgotPasswordUpdateUrl;
	}

	public String getUiVerifyAccountEmailUrl() {
		return uiVerifyAccountEmailUrl;
	}

	public void setUiVerifyAccountEmailUrl(String uiVerifyAccountEmailUrl) {
		this.uiVerifyAccountEmailUrl = uiVerifyAccountEmailUrl;
	}

	public String getValidEmailDomains() {
		return validEmailDomains;
	}

	public void setValidEmailDomains(String validEmailDomains) {
		this.validEmailDomains = validEmailDomains;
	}

	public String getIpAttorneyTitle() {
		return ipAttorneyTitle;
	}

	public void setIpAttorneyTitle(String ipAttorneyTitle) {
		this.ipAttorneyTitle = ipAttorneyTitle;
	}

	public String getIpAttorneyName() {
		return ipAttorneyName;
	}

	public void setIpAttorneyName(String ipAttorneyName) {
		this.ipAttorneyName = ipAttorneyName;
	}

	public String getIpAttorneyEmail() {
		return ipAttorneyEmail;
	}

	public void setIpAttorneyEmail(String ipAttorneyEmail) {
		this.ipAttorneyEmail = ipAttorneyEmail;
	}

	public String getMailServerHost() {
		return mailServerHost;
	}

	public void setMailServerHost(String mailServerHost) {
		this.mailServerHost = mailServerHost;
	}

	public String getMailServerPort() {
		return mailServerPort;
	}

	public void setMailServerPort(String mailServerPort) {
		this.mailServerPort = mailServerPort;
	}

	public String getMailServerAuth() {
		return mailServerAuth;
	}

	public void setMailServerAuth(String mailServerAuth) {
		this.mailServerAuth = mailServerAuth;
	}

	public String getMailServerEnableTls() {
		return mailServerEnableTls;
	}

	public void setMailServerEnableTls(String mailServerEnableTls) {
		this.mailServerEnableTls = mailServerEnableTls;
	}

	public String getMailServerUsername() {
		return mailServerUsername;
	}

	public void setMailServerUsername(String mailServerUsername) {
		this.mailServerUsername = mailServerUsername;
	}

	public String getMailServerPassword() {
		return mailServerPassword;
	}

	public void setMailServerPassword(String mailServerPassword) {
		this.mailServerPassword = mailServerPassword;
	}

	public String getMailTransportProtocol() {
		return mailTransportProtocol;
	}

	public void setMailTransportProtocol(String mailTransportProtocol) {
		this.mailTransportProtocol = mailTransportProtocol;
	}

	public String getDocusignOAuthBaseUrl() {
		return docusignOAuthBaseUrl;
	}

	public void setDocusignOAuthBaseUrl(String docusignOAuthBaseUrl) {
		this.docusignOAuthBaseUrl = docusignOAuthBaseUrl;
	}

	public String getDocusignBaseUrl() {
		return docusignBaseUrl;
	}

	public void setDocusignBaseUrl(String docusignBaseUrl) {
		this.docusignBaseUrl = docusignBaseUrl;
	}

	public String getDocusignIntegratorKey() {
		return docusignIntegratorKey;
	}

	public void setDocusignIntegratorKey(String docusignIntegratorKey) {
		this.docusignIntegratorKey = docusignIntegratorKey;
	}

	public String getDocusignUserId() {
		return docusignUserId;
	}

	public void setDocusignUserId(String docusignUserId) {
		this.docusignUserId = docusignUserId;
	}

	public String getDocusignPrivateKey() {
		return docusignPrivateKey;
	}

	public void setDocusignPrivateKey(String docusignPrivateKey) {
		this.docusignPrivateKey = docusignPrivateKey;
	}

	public String getDocusignPublicKey() {
		return docusignPublicKey;
	}

	public void setDocusignPublicKey(String docusignPublicKey) {
		this.docusignPublicKey = docusignPublicKey;
	}

	public String getDocusignTokenExpiryTimeInSeconds() {
		return docusignTokenExpiryTimeInSeconds;
	}

	public void setDocusignTokenExpiryTimeInSeconds(String docusignTokenExpiryTimeInSeconds) {
		this.docusignTokenExpiryTimeInSeconds = docusignTokenExpiryTimeInSeconds;
	}

	public String getDocusignEnvelopeEmailSubject() {
		return docusignEnvelopeEmailSubject;
	}

	public void setDocusignEnvelopeEmailSubject(String docusignEnvelopeEmailSubject) {
		this.docusignEnvelopeEmailSubject = docusignEnvelopeEmailSubject;
	}

	public String getDocusignEmailSubjectOtherParty() {
		return docusignEmailSubjectOtherParty;
	}

	public void setDocusignEmailSubjectOtherParty(String docusignEmailSubjectOtherParty) {
		this.docusignEmailSubjectOtherParty = docusignEmailSubjectOtherParty;
	}

	public String getDocusignEmailContentOtherParty() {
		return docusignEmailContentOtherParty;
	}

	public void setDocusignEmailContentOtherParty(String docusignEmailContentOtherParty) {
		this.docusignEmailContentOtherParty = docusignEmailContentOtherParty;
	}

	public String getDocusignEmailSubjectIpAttorney() {
		return docusignEmailSubjectIpAttorney;
	}

	public void setDocusignEmailSubjectIpAttorney(String docusignEmailSubjectIpAttorney) {
		this.docusignEmailSubjectIpAttorney = docusignEmailSubjectIpAttorney;
	}

	public String getDocusignEmailContentIpAttorney() {
		return docusignEmailContentIpAttorney;
	}

	public void setDocusignEmailContentIpAttorney(String docusignEmailContentIpAttorney) {
		this.docusignEmailContentIpAttorney = docusignEmailContentIpAttorney;
	}

	public String getDocusignCustomerNDAEmailSubjectOtherParty() {
		return docusignCustomerNDAEmailSubjectOtherParty;
	}

	public void setDocusignCustomerNDAEmailSubjectOtherParty(String docusignCustomerNDAEmailSubjectOtherParty) {
		this.docusignCustomerNDAEmailSubjectOtherParty = docusignCustomerNDAEmailSubjectOtherParty;
	}

	public String getDocusignCustomerNDAEmailContentOtherParty() {
		return docusignCustomerNDAEmailContentOtherParty;
	}

	public void setDocusignCustomerNDAEmailContentOtherParty(String docusignCustomerNDAEmailContentOtherParty) {
		this.docusignCustomerNDAEmailContentOtherParty = docusignCustomerNDAEmailContentOtherParty;
	}

	public String getDocusignCustomerNDAEmailSubjectIpAttorney() {
		return docusignCustomerNDAEmailSubjectIpAttorney;
	}

	public void setDocusignCustomerNDAEmailSubjectIpAttorney(String docusignCustomerNDAEmailSubjectIpAttorney) {
		this.docusignCustomerNDAEmailSubjectIpAttorney = docusignCustomerNDAEmailSubjectIpAttorney;
	}

	public String getDocusignCustomerNDAEmailContentIpAttorney() {
		return docusignCustomerNDAEmailContentIpAttorney;
	}

	public void setDocusignCustomerNDAEmailContentIpAttorney(String docusignCustomerNDAEmailContentIpAttorney) {
		this.docusignCustomerNDAEmailContentIpAttorney = docusignCustomerNDAEmailContentIpAttorney;
	}

	public String getDocusignCarbonCopyEmailSubjectRequestor() {
		return docusignCarbonCopyEmailSubjectRequestor;
	}

	public void setDocusignCarbonCopyEmailSubjectRequestor(String docusignCarbonCopyEmailSubjectRequestor) {
		this.docusignCarbonCopyEmailSubjectRequestor = docusignCarbonCopyEmailSubjectRequestor;
	}

	public String getDocusignCarbonCopyEmailContentRequestor() {
		return docusignCarbonCopyEmailContentRequestor;
	}

	public void setDocusignCarbonCopyEmailContentRequestor(String docusignCarbonCopyEmailContentRequestor) {
		this.docusignCarbonCopyEmailContentRequestor = docusignCarbonCopyEmailContentRequestor;
	}

	public String getDocusignCarbonCopyEmailSubjectCertifier() {
		return docusignCarbonCopyEmailSubjectCertifier;
	}

	public void setDocusignCarbonCopyEmailSubjectCertifier(String docusignCarbonCopyEmailSubjectCertifier) {
		this.docusignCarbonCopyEmailSubjectCertifier = docusignCarbonCopyEmailSubjectCertifier;
	}

	public String getDocusignCarbonCopyEmailContentCertifier() {
		return docusignCarbonCopyEmailContentCertifier;
	}

	public void setDocusignCarbonCopyEmailContentCertifier(String docusignCarbonCopyEmailContentCertifier) {
		this.docusignCarbonCopyEmailContentCertifier = docusignCarbonCopyEmailContentCertifier;
	}

	public String getNotificationTitleRequestApproved() {
		return notificationTitleRequestApproved;
	}

	public void setNotificationTitleRequestApproved(String notificationTitleRequestApproved) {
		this.notificationTitleRequestApproved = notificationTitleRequestApproved;
	}

	public String getRequestorNotificationContentRequestApproved() {
		return requestorNotificationContentRequestApproved;
	}

	public void setRequestorNotificationContentRequestApproved(String requestorNotificationContentRequestApproved) {
		this.requestorNotificationContentRequestApproved = requestorNotificationContentRequestApproved;
	}

	public String getAdminNotificationContentRequestApproved() {
		return adminNotificationContentRequestApproved;
	}

	public void setAdminNotificationContentRequestApproved(String adminNotificationContentRequestApproved) {
		this.adminNotificationContentRequestApproved = adminNotificationContentRequestApproved;
	}

	public String getEmailSubjectRequestApproved() {
		return emailSubjectRequestApproved;
	}

	public void setEmailSubjectRequestApproved(String emailSubjectRequestApproved) {
		this.emailSubjectRequestApproved = emailSubjectRequestApproved;
	}

	public String getRequestorEmailContentRequestApproved() {
		return requestorEmailContentRequestApproved;
	}

	public void setRequestorEmailContentRequestApproved(String requestorEmailContentRequestApproved) {
		this.requestorEmailContentRequestApproved = requestorEmailContentRequestApproved;
	}

	public String getAdminEmailContentRequestApproved() {
		return adminEmailContentRequestApproved;
	}

	public void setAdminEmailContentRequestApproved(String adminEmailContentRequestApproved) {
		this.adminEmailContentRequestApproved = adminEmailContentRequestApproved;
	}

	public String getNotificationTitleAdminApproval() {
		return notificationTitleAdminApproval;
	}

	public void setNotificationTitleAdminApproval(String notificationTitleAdminApproval) {
		this.notificationTitleAdminApproval = notificationTitleAdminApproval;
	}

	public String getRequestorNotificationContentAdminApproval() {
		return requestorNotificationContentAdminApproval;
	}

	public void setRequestorNotificationContentAdminApproval(String requestorNotificationContentAdminApproval) {
		this.requestorNotificationContentAdminApproval = requestorNotificationContentAdminApproval;
	}

	public String getAdminNotificationContentAdminApproval() {
		return adminNotificationContentAdminApproval;
	}

	public void setAdminNotificationContentAdminApproval(String adminNotificationContentAdminApproval) {
		this.adminNotificationContentAdminApproval = adminNotificationContentAdminApproval;
	}

	public String getEmailSubjectAdminApproval() {
		return emailSubjectAdminApproval;
	}

	public void setEmailSubjectAdminApproval(String emailSubjectAdminApproval) {
		this.emailSubjectAdminApproval = emailSubjectAdminApproval;
	}

	public String getRequestorEmailContentAdminApproval() {
		return requestorEmailContentAdminApproval;
	}

	public void setRequestorEmailContentAdminApproval(String requestorEmailContentAdminApproval) {
		this.requestorEmailContentAdminApproval = requestorEmailContentAdminApproval;
	}

	public String getAdminEmailContentAdminApproval() {
		return adminEmailContentAdminApproval;
	}

	public void setAdminEmailContentAdminApproval(String adminEmailContentAdminApproval) {
		this.adminEmailContentAdminApproval = adminEmailContentAdminApproval;
	}

	public String getAdminNotificationTitleUserRegistered() {
		return adminNotificationTitleUserRegistered;
	}

	public void setAdminNotificationTitleUserRegistered(String adminNotificationTitleUserRegistered) {
		this.adminNotificationTitleUserRegistered = adminNotificationTitleUserRegistered;
	}

	public String getAdminNotificationContentUserRegistered() {
		return adminNotificationContentUserRegistered;
	}

	public void setAdminNotificationContentUserRegistered(String adminNotificationContentUserRegistered) {
		this.adminNotificationContentUserRegistered = adminNotificationContentUserRegistered;
	}

	public String getAdminEmailSubjectUserRegistered() {
		return adminEmailSubjectUserRegistered;
	}

	public void setAdminEmailSubjectUserRegistered(String adminEmailSubjectUserRegistered) {
		this.adminEmailSubjectUserRegistered = adminEmailSubjectUserRegistered;
	}

	public String getAdminEmailContentUserRegistered() {
		return adminEmailContentUserRegistered;
	}

	public void setAdminEmailContentUserRegistered(String adminEmailContentUserRegistered) {
		this.adminEmailContentUserRegistered = adminEmailContentUserRegistered;
	}

	public String getUserEmailSubjectUserRegistered() {
		return userEmailSubjectUserRegistered;
	}

	public void setUserEmailSubjectUserRegistered(String userEmailSubjectUserRegistered) {
		this.userEmailSubjectUserRegistered = userEmailSubjectUserRegistered;
	}

	public String getUserEmailContentUserRegistered() {
		return userEmailContentUserRegistered;
	}

	public void setUserEmailContentUserRegistered(String userEmailContentUserRegistered) {
		this.userEmailContentUserRegistered = userEmailContentUserRegistered;
	}

	public String getNotificationTitleUserActivated() {
		return notificationTitleUserActivated;
	}

	public void setNotificationTitleUserActivated(String notificationTitleUserActivated) {
		this.notificationTitleUserActivated = notificationTitleUserActivated;
	}

	public String getAdminNotificationContentUserActivated() {
		return adminNotificationContentUserActivated;
	}

	public void setAdminNotificationContentUserActivated(String adminNotificationContentUserActivated) {
		this.adminNotificationContentUserActivated = adminNotificationContentUserActivated;
	}

	public String getAdminEmailSubjectUserActivated() {
		return adminEmailSubjectUserActivated;
	}

	public void setAdminEmailSubjectUserActivated(String adminEmailSubjectUserActivated) {
		this.adminEmailSubjectUserActivated = adminEmailSubjectUserActivated;
	}

	public String getAdminEmailContentUserActivated() {
		return adminEmailContentUserActivated;
	}

	public void setAdminEmailContentUserActivated(String adminEmailContentUserActivated) {
		this.adminEmailContentUserActivated = adminEmailContentUserActivated;
	}

	public String getUserEmailSubjectUserActivated() {
		return userEmailSubjectUserActivated;
	}

	public void setUserEmailSubjectUserActivated(String userEmailSubjectUserActivated) {
		this.userEmailSubjectUserActivated = userEmailSubjectUserActivated;
	}

	public String getUserEmailContentUserActivated() {
		return userEmailContentUserActivated;
	}

	public void setUserEmailContentUserActivated(String userEmailContentUserActivated) {
		this.userEmailContentUserActivated = userEmailContentUserActivated;
	}

	public String getNotificationTitleCustomerNdaAdminApproval() {
		return notificationTitleCustomerNdaAdminApproval;
	}

	public void setNotificationTitleCustomerNdaAdminApproval(String notificationTitleCustomerNdaAdminApproval) {
		this.notificationTitleCustomerNdaAdminApproval = notificationTitleCustomerNdaAdminApproval;
	}

	public String getRequestorNotificationContentCustomerNdaAdminApproval() {
		return requestorNotificationContentCustomerNdaAdminApproval;
	}

	public void setRequestorNotificationContentCustomerNdaAdminApproval(
			String requestorNotificationContentCustomerNdaAdminApproval) {
		this.requestorNotificationContentCustomerNdaAdminApproval = requestorNotificationContentCustomerNdaAdminApproval;
	}

	public String getAdminNotificationContentCustomerNdaAdminApproval() {
		return adminNotificationContentCustomerNdaAdminApproval;
	}

	public void setAdminNotificationContentCustomerNdaAdminApproval(
			String adminNotificationContentCustomerNdaAdminApproval) {
		this.adminNotificationContentCustomerNdaAdminApproval = adminNotificationContentCustomerNdaAdminApproval;
	}

	public String getEmailSubjectCustomerNdaAdminApproval() {
		return emailSubjectCustomerNdaAdminApproval;
	}

	public void setEmailSubjectCustomerNdaAdminApproval(String emailSubjectCustomerNdaAdminApproval) {
		this.emailSubjectCustomerNdaAdminApproval = emailSubjectCustomerNdaAdminApproval;
	}

	public String getRequestorEmailContentCustomerNdaAdminApproval() {
		return requestorEmailContentCustomerNdaAdminApproval;
	}

	public void setRequestorEmailContentCustomerNdaAdminApproval(String requestorEmailContentCustomerNdaAdminApproval) {
		this.requestorEmailContentCustomerNdaAdminApproval = requestorEmailContentCustomerNdaAdminApproval;
	}

	public String getAdminEmailContentCustomerNdaAdminApproval() {
		return adminEmailContentCustomerNdaAdminApproval;
	}

	public void setAdminEmailContentCustomerNdaAdminApproval(String adminEmailContentCustomerNdaAdminApproval) {
		this.adminEmailContentCustomerNdaAdminApproval = adminEmailContentCustomerNdaAdminApproval;
	}

	public String getEmailSubjectAccountEmailVerification() {
		return emailSubjectAccountEmailVerification;
	}

	public void setEmailSubjectAccountEmailVerification(String emailSubjectAccountEmailVerification) {
		this.emailSubjectAccountEmailVerification = emailSubjectAccountEmailVerification;
	}

	public String getEmailContentAccountEmailVerification() {
		return emailContentAccountEmailVerification;
	}

	public void setEmailContentAccountEmailVerification(String emailContentAccountEmailVerification) {
		this.emailContentAccountEmailVerification = emailContentAccountEmailVerification;
	}

	public String getNotificationTitleForgotPasswordInitiation() {
		return notificationTitleForgotPasswordInitiation;
	}

	public void setNotificationTitleForgotPasswordInitiation(String notificationTitleForgotPasswordInitiation) {
		this.notificationTitleForgotPasswordInitiation = notificationTitleForgotPasswordInitiation;
	}

	public String getNotificationContentForgotPasswordInitiation() {
		return notificationContentForgotPasswordInitiation;
	}

	public void setNotificationContentForgotPasswordInitiation(String notificationContentForgotPasswordInitiation) {
		this.notificationContentForgotPasswordInitiation = notificationContentForgotPasswordInitiation;
	}

	public String getEmailSubjectForgotPasswordInitiation() {
		return emailSubjectForgotPasswordInitiation;
	}

	public void setEmailSubjectForgotPasswordInitiation(String emailSubjectForgotPasswordInitiation) {
		this.emailSubjectForgotPasswordInitiation = emailSubjectForgotPasswordInitiation;
	}

	public String getEmailContentForgotPasswordInitiation() {
		return emailContentForgotPasswordInitiation;
	}

	public void setEmailContentForgotPasswordInitiation(String emailContentForgotPasswordInitiation) {
		this.emailContentForgotPasswordInitiation = emailContentForgotPasswordInitiation;
	}

	public String getNotificationTitleForgotPasswordCompletion() {
		return notificationTitleForgotPasswordCompletion;
	}

	public void setNotificationTitleForgotPasswordCompletion(String notificationTitleForgotPasswordCompletion) {
		this.notificationTitleForgotPasswordCompletion = notificationTitleForgotPasswordCompletion;
	}

	public String getNotificationContentForgotPasswordCompletion() {
		return notificationContentForgotPasswordCompletion;
	}

	public void setNotificationContentForgotPasswordCompletion(String notificationContentForgotPasswordCompletion) {
		this.notificationContentForgotPasswordCompletion = notificationContentForgotPasswordCompletion;
	}

	public String getEmailSubjectForgotPasswordCompletion() {
		return emailSubjectForgotPasswordCompletion;
	}

	public void setEmailSubjectForgotPasswordCompletion(String emailSubjectForgotPasswordCompletion) {
		this.emailSubjectForgotPasswordCompletion = emailSubjectForgotPasswordCompletion;
	}

	public String getEmailContentForgotPasswordCompletion() {
		return emailContentForgotPasswordCompletion;
	}

	public void setEmailContentForgotPasswordCompletion(String emailContentForgotPasswordCompletion) {
		this.emailContentForgotPasswordCompletion = emailContentForgotPasswordCompletion;
	}

}
