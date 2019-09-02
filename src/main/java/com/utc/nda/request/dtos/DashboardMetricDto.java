package com.utc.nda.request.dtos;

import java.util.List;

import com.utc.nda.request.entities.Request;

public class DashboardMetricDto {

	long ndaCount = 0;
	long pendingNdaCount = 0;
	long oneWayNdaCount = 0;
	long mutualNdaCount = 0;
	long customerNdaCount = 0;
	long amendmentNdaCount = 0;
	long expiringNdaCount = 0;
	List<Request> recentRequests;

	public long getNdaCount() {
		return ndaCount;
	}

	public void setNdaCount(long ndaCount) {
		this.ndaCount = ndaCount;
	}

	public long getPendingNdaCount() {
		return pendingNdaCount;
	}

	public void setPendingNdaCount(long pendingNdaCount) {
		this.pendingNdaCount = pendingNdaCount;
	}

	public long getOneWayNdaCount() {
		return oneWayNdaCount;
	}

	public void setOneWayNdaCount(long oneWayNdaCount) {
		this.oneWayNdaCount = oneWayNdaCount;
	}

	public long getMutualNdaCount() {
		return mutualNdaCount;
	}

	public void setMutualNdaCount(long mutualNdaCount) {
		this.mutualNdaCount = mutualNdaCount;
	}

	public long getCustomerNdaCount() {
		return customerNdaCount;
	}

	public void setCustomerNdaCount(long customerNdaCount) {
		this.customerNdaCount = customerNdaCount;
	}

	public long getAmendmentNdaCount() {
		return amendmentNdaCount;
	}

	public void setAmendmentNdaCount(long amendmentNdaCount) {
		this.amendmentNdaCount = amendmentNdaCount;
	}

	public long getExpiringNdaCount() {
		return expiringNdaCount;
	}

	public void setExpiringNdaCount(long expiringNdaCount) {
		this.expiringNdaCount = expiringNdaCount;
	}

	public List<Request> getRecentRequests() {
		return recentRequests;
	}

	public void setRecentRequests(List<Request> recentRequests) {
		this.recentRequests = recentRequests;
	}

}
