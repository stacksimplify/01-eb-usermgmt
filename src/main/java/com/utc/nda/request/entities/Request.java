package com.utc.nda.request.entities;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "request")
public class Request {

	@Id
	@NotNull
	@Size(min = 1, max = 36)
	private String guid;

	@NotNull
	@Size(min = 1)
	private String owner;

	@NotNull
	@Size(min = 1)
	private String status;

	@NotNull
	@Size(min = 1)
	private String requestorName;

	@NotNull
	@Size(min = 1)
	private String requestorEmail;

	@NotNull
	@Size(min = 1)
	private String certifierName;

	@NotNull
	@Size(min = 1)
	private String certifierEmail;

	@NotNull
	@Size(min = 1)
	private String otherPartyName;

	@NotNull
	@Size(min = 1)
	private String otherPartyEmail;

	@NotNull
	@Size(min = 1)
	private String businessUnit;

	@NotNull
	@Size(min = 1)
	private String ndaType;

	@NotNull
	@Min(1)
	@Max(5)
	private int informationSharingPeriod;
	private boolean isAmendment;
	private boolean is30DaysPrior;
	private boolean isSpecificPurpose;

	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	private Date creationTime;

	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	private Date startDate;

	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	private Date expiryDate;

	@JsonIgnore
	@NotNull
	@Column(length = 5000)
	private String requestData;

	private String docusignGuid;
	private String amendmentGuid;
	private String requestFormMdGuid;
	private String requestFormPdfGuid;
	private String parsedDocumentMdGuid;
	private String parsedDocumentPdfGuid;
	private String completedDocumentGuid;
	private String customerDocumentGuid;

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	public String getRequestorName() {
		return requestorName;
	}

	public void setRequestorName(String requestorName) {
		this.requestorName = requestorName;
	}

	public String getRequestorEmail() {
		return requestorEmail;
	}

	public void setRequestorEmail(String requestorEmail) {
		this.requestorEmail = requestorEmail;
	}

	public String getCertifierEmail() {
		return certifierEmail;
	}

	public void setCertifierEmail(String certifierEmail) {
		this.certifierEmail = certifierEmail;
	}

	public String getOtherPartyEmail() {
		return otherPartyEmail;
	}

	public void setOtherPartyEmail(String otherPartyEmail) {
		this.otherPartyEmail = otherPartyEmail;
	}

	public String getCertifierName() {
		return certifierName;
	}

	public void setCertifierName(String certifierName) {
		this.certifierName = certifierName;
	}

	public String getOtherPartyName() {
		return otherPartyName;
	}

	public void setOtherPartyName(String otherPartyName) {
		this.otherPartyName = otherPartyName;
	}

	public String getBusinessUnit() {
		return businessUnit;
	}

	public void setBusinessUnit(String businessUnit) {
		this.businessUnit = businessUnit;
	}

	public String getNdaType() {
		return ndaType;
	}

	public void setNdaType(String ndaType) {
		this.ndaType = ndaType;
	}

	public int getInformationSharingPeriod() {
		return informationSharingPeriod;
	}

	public void setInformationSharingPeriod(int informationSharingPeriod) {
		this.informationSharingPeriod = informationSharingPeriod;
	}

	public boolean isAmendment() {
		return isAmendment;
	}

	public void setAmendment(boolean isAmendment) {
		this.isAmendment = isAmendment;
	}

	public boolean isIs30DaysPrior() {
		return is30DaysPrior;
	}

	public void setIs30DaysPrior(boolean is30DaysPrior) {
		this.is30DaysPrior = is30DaysPrior;
	}

	public boolean isSpecificPurpose() {
		return isSpecificPurpose;
	}

	public void setSpecificPurpose(boolean isSpecificPurpose) {
		this.isSpecificPurpose = isSpecificPurpose;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDocusignGuid() {
		return docusignGuid;
	}

	public void setDocusignGuid(String docusignGuid) {
		this.docusignGuid = docusignGuid;
	}

	public String getAmendmentGuid() {
		return amendmentGuid;
	}

	public void setAmendmentGuid(String amendmentGuid) {
		this.amendmentGuid = amendmentGuid;
	}

	public String getRequestFormMdGuid() {
		return requestFormMdGuid;
	}

	public void setRequestFormMdGuid(String requestFormMdGuid) {
		this.requestFormMdGuid = requestFormMdGuid;
	}

	public String getRequestFormPdfGuid() {
		return requestFormPdfGuid;
	}

	public void setRequestFormPdfGuid(String requestFormPdfGuid) {
		this.requestFormPdfGuid = requestFormPdfGuid;
	}

	public String getParsedDocumentMdGuid() {
		return parsedDocumentMdGuid;
	}

	public void setParsedDocumentMdGuid(String parsedDocumentMdGuid) {
		this.parsedDocumentMdGuid = parsedDocumentMdGuid;
	}

	public String getParsedDocumentPdfGuid() {
		return parsedDocumentPdfGuid;
	}

	public void setParsedDocumentPdfGuid(String parsedDocumentPdfGuid) {
		this.parsedDocumentPdfGuid = parsedDocumentPdfGuid;
	}

	public String getCompletedDocumentGuid() {
		return completedDocumentGuid;
	}

	public void setCompletedDocumentGuid(String completedDocumentGuid) {
		this.completedDocumentGuid = completedDocumentGuid;
	}

	public String getRequestData() {
		return requestData;
	}

	public void setRequestData(String requestData) {
		this.requestData = requestData;
	}

	public String getCustomerDocumentGuid() {
		return customerDocumentGuid;
	}

	public void setCustomerDocumentGuid(String customerDocumentGuid) {
		this.customerDocumentGuid = customerDocumentGuid;
	}

	@Override
	public String toString() {
		return "Request [guid=" + guid + ", owner=" + owner + ", status=" + status + ", requestorName=" + requestorName
				+ ", requestorEmail=" + requestorEmail + ", certifierName=" + certifierName + ", certifierEmail="
				+ certifierEmail + ", otherPartyName=" + otherPartyName + ", otherPartyEmail=" + otherPartyEmail
				+ ", businessUnit=" + businessUnit + ", ndaType=" + ndaType + ", informationSharingPeriod="
				+ informationSharingPeriod + ", isAmendment=" + isAmendment + ", is30DaysPrior=" + is30DaysPrior
				+ ", isSpecificPurpose=" + isSpecificPurpose + ", creationTime=" + creationTime + ", startDate="
				+ startDate + ", expiryDate=" + expiryDate + ", docusignGuid=" + docusignGuid + ", amendmentGuid="
				+ amendmentGuid + ", requestFormMdGuid=" + requestFormMdGuid + ", requestFormPdfGuid="
				+ requestFormPdfGuid + ", parsedDocumentMdGuid=" + parsedDocumentMdGuid + ", parsedDocumentPdfGuid="
				+ parsedDocumentPdfGuid + ", completedDocumentGuid=" + completedDocumentGuid + ", customerDocumentGuid="
				+ customerDocumentGuid + "]";
	}

	public Date calculateExpiryDate() {
		Calendar c = Calendar.getInstance();
		c.setTime(this.getStartDate());
		c.add(Calendar.YEAR, this.getInformationSharingPeriod());
		return c.getTime();
	}

}
