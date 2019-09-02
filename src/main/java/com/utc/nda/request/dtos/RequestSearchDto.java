package com.utc.nda.request.dtos;

import java.util.Date;

public class RequestSearchDto {

	
	private String guid;
	private String owner;
	private String status;
	private String requestorName;
	private String certifierName;
	private String otherPartyName;
	private String businessUnit;
	private String ndaType;
	private Integer informationSharingPeriod;
	private Date startDate;
	private Date expiryDate;
	private Date creationTime;
	private String docusignGuid;
	private boolean isAmendment;
	
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
	public String getRequestorName() {
		return requestorName;
	}
	public void setRequestorName(String requestorName) {
		this.requestorName = requestorName;
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
	public Integer getInformationSharingPeriod() {
		return informationSharingPeriod;
	}
	public void setInformationSharingPeriod(Integer informationSharingPeriod) {
		this.informationSharingPeriod = informationSharingPeriod;
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
	public Date getCreationTime() {
		return creationTime;
	}
	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}
	public boolean getIsAmendment() {
		return isAmendment;
	}
	public void setIsAmendment(boolean isAmendment) {
		this.isAmendment = isAmendment;
	}	
	
	@Override
	public String toString() {
		return "RequestSearchDto [guid=" + guid + ", owner=" + owner + ", status=" + status + ", requestorName="
				+ requestorName + ", certifierName=" + certifierName + ", otherPartyName=" + otherPartyName
				+ ", businessUnit=" + businessUnit + ", ndaType=" + ndaType + ", informationSharingPeriod="
				+ informationSharingPeriod.intValue() + ", startDate=" + startDate + ", expiryDate=" + expiryDate
				+ ", creationTime=" + creationTime
				+ ", isAmendment=" + isAmendment
				+ ", docusignGuid=" + docusignGuid + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((businessUnit == null) ? 0 : businessUnit.hashCode());
		result = prime * result + ((certifierName == null) ? 0 : certifierName.hashCode());
		result = prime * result + ((guid == null) ? 0 : guid.hashCode());
		result = prime * result + ((ndaType == null) ? 0 : ndaType.hashCode());
		result = prime * result + ((otherPartyName == null) ? 0 : otherPartyName.hashCode());
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		result = prime * result + ((requestorName == null) ? 0 : requestorName.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RequestSearchDto other = (RequestSearchDto) obj;
		if (businessUnit == null) {
			if (other.businessUnit != null)
				return false;
		} else if (!businessUnit.equals(other.businessUnit))
			return false;
		if (certifierName == null) {
			if (other.certifierName != null)
				return false;
		} else if (!certifierName.equals(other.certifierName))
			return false;
		if (guid == null) {
			if (other.guid != null)
				return false;
		} else if (!guid.equals(other.guid))
			return false;
		if (ndaType == null) {
			if (other.ndaType != null)
				return false;
		} else if (!ndaType.equals(other.ndaType))
			return false;
		if (otherPartyName == null) {
			if (other.otherPartyName != null)
				return false;
		} else if (!otherPartyName.equals(other.otherPartyName))
			return false;
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner))
			return false;
		if (requestorName == null) {
			if (other.requestorName != null)
				return false;
		} else if (!requestorName.equals(other.requestorName))
			return false;
		return true;
	}
}
