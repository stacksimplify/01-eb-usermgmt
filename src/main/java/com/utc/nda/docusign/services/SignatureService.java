package com.utc.nda.docusign.services;

import java.io.IOException;

import com.docusign.esign.client.ApiClient;
import com.docusign.esign.client.ApiException;

public interface SignatureService {

	public String getToken();

	public ApiClient getApiClient();

	public String getAccountId();

	public void checkToken() throws IOException, ApiException;

	public String docuSign(String documentName, byte[] documentToSign, String requestorName, String requestorEmail,
			String firstSignerName, String firstSignerEmail, String secondSignerName, String secondSignerEmail,
			String certifierName, String certifierEmail, String ndaType) throws IOException, ApiException;

	public Object getAllEnvelope() throws IOException, ApiException;

	public Object getEnvelopeStatus(String envelopeId) throws IOException, ApiException;

	public Object getEnvelopeRecipients(String envelopeId) throws IOException, ApiException;

	public byte[] getEnvelopeDocument(String envelopeId) throws IOException, ApiException;

	public String getEnvelopeDocumentName(String envelopeId) throws IOException, ApiException;

}
