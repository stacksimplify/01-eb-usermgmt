package com.utc.nda.request.repositories;

import java.time.LocalDateTime;
import java.util.Date;

import org.springframework.data.jpa.domain.Specification;

import com.utc.nda.request.entities.Request;

public class RequestSpecifications {
	public static Specification<Request> hasOwner(String owner) {
		return (request, cq, cb) -> cb.equal(request.get("owner"), owner);
	}

	public static Specification<Request> hasStatus(String status) {
		return (request, cq, cb) -> cb.equal(request.get("status"), status);
	}

	public static Specification<Request> hasGuid(String guid) {
		return (request, cq, cb) -> cb.equal(request.get("guid"), guid);
	}

	public static Specification<Request> hasNdaType(String ndaType) {
		return (request, cq, cb) -> cb.equal(request.get("ndaType"), ndaType);
	}

	public static Specification<Request> hasInformationSharingPeriod(int informationSharingPeriod) {
		return (request, cq, cb) -> cb.equal(request.get("informationSharingPeriod"), informationSharingPeriod);
	}
	
	public static Specification<Request> hasIsAmendment(boolean isAmendment) {
		return (request, cq, cb) -> cb.equal(request.get("isAmendment"), isAmendment);
	}

	public static Specification<Request> hasBusinessUnit(String businessUnit) {
		return (request, cq, cb) -> cb.equal(request.get("businessUnit"), businessUnit);
	}

	public static Specification<Request> hasDocusignGuid(String docusignGuid) {
		return (request, cq, cb) -> cb.equal(request.get("docusignGuid"), docusignGuid);
	}

	public static Specification<Request> hasStartDate(Date startDate) {
		return (request, cq, cb) -> cb.equal(request.get("startDate"), startDate);
	}

	public static Specification<Request> hasExpiryDate(Date expiryDate) {
		return (request, cq, cb) -> cb.equal(request.get("expiryDate"), expiryDate);
	}
	
	public static Specification<Request> hasCreationDateGreaterThanOrEqualTo(Date date) {
		return (request, cq, cb) -> cb.greaterThan(request.get("creationTime"), date);
		//return (request, cq, cb) -> cb.between(request.get("creationTime"), onStartTime, onEndTime);
	}
	
	public static Specification<Request> hasCreationDateLessThan(Date date) {
		return (request, cq, cb) -> cb.lessThan(request.get("creationTime"), date);
		//return (request, cq, cb) -> cb.between(request.get("creationTime"), onStartTime, onEndTime);
	}

	public static Specification<Request> requestorNameContains(String requestorName) {
		return (request, cq, cb) -> cb.like(request.get("requestorName"), "%" + requestorName + "%");
	}

	public static Specification<Request> certifierNameContains(String certifierName) {
		return (request, cq, cb) -> cb.like(request.get("certifierName"), "%" + certifierName + "%");
	}

	public static Specification<Request> otherPartyNameContains(String otherPartyName) {
		return (request, cq, cb) -> cb.like(request.get("otherPartyName"), "%" + otherPartyName + "%");
	}

}
