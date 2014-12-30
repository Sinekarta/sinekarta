/*
 * Copyright (C) 2010 - 2012 Jenia Software.
 *
 * This file is part of Sinekarta
 *
 * Sinekarta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Sinekarta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 */
package org.sinekartads.integration.cms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;
import org.sinekartads.applet.AppletResponseDTO;
import org.sinekartads.applet.AppletResponseDTO.ActionErrorDTO;
import org.sinekartads.applet.AppletResponseDTO.FieldErrorDTO;
import org.sinekartads.applet.SignApplet;
import org.sinekartads.dto.BaseDTO;
import org.sinekartads.dto.ResultCode;
import org.sinekartads.dto.domain.DocumentDTO;
import org.sinekartads.dto.domain.NodeDTO;
import org.sinekartads.dto.domain.SignatureDTO;
import org.sinekartads.dto.domain.TimeStampRequestDTO;
import org.sinekartads.dto.domain.VerifyDTO;
import org.sinekartads.dto.jcl.JclResponseDTO;
import org.sinekartads.dto.request.BaseRequest;
import org.sinekartads.dto.request.SkdsDocumentDetailsRequest;
import org.sinekartads.dto.request.SkdsSignRequest.SkdsPostSignRequest;
import org.sinekartads.dto.request.SkdsSignRequest.SkdsPreSignRequest;
import org.sinekartads.dto.response.BaseResponse;
import org.sinekartads.dto.response.SkdsDocumentDetailsResponse;
import org.sinekartads.dto.response.SkdsSignResponse.SkdsPostSignResponse;
import org.sinekartads.dto.response.SkdsSignResponse.SkdsPreSignResponse;
import org.sinekartads.integration.BaseIntegrationTC;
import org.sinekartads.model.domain.SignDisposition;
import org.sinekartads.model.domain.SignatureType.SignCategory;
import org.sinekartads.model.oid.DigestAlgorithm;
import org.sinekartads.model.oid.SinekartaDsObjectIdentifiers;
import org.sinekartads.util.DNParser;
import org.sinekartads.util.HexUtils;
import org.sinekartads.util.TemplateUtils;
import org.sinekartads.util.x509.X509Utils;
import org.sinekartads.utils.JSONUtils;


public class SignCMSonAlfresco extends BaseIntegrationTC {
	
	static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	
	static Logger tracer = Logger.getLogger(SignCMSonAlfresco.class);
	
	private static final String SOURCE_REF 		= "workspace://SpacesStore/82f59afc-9deb-4fc6-b1b6-78da98bbbf20"; 
	
	private static final int 	PORT = 8080;
	private static final String HOST_NAME = "localhost";
	private static final String USER = "admin";
	private static final String PWD = "admin";
	

	
	@Test
	public void test() throws Exception {
		if ( Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null ) {
			Security.addProvider(new BouncyCastleProvider());
		}
		
		SignApplet applet = new SignApplet();
		try {
			
			// Main options
			boolean applyMark = true;
			boolean useFakeSmartCard = false;
			String driver;
			String scPin;
			if ( useFakeSmartCard ) {
				driver = "fake";
				scPin = "123";
			} else {
				driver = "libbit4ipki.so";
				scPin = "18071971";
			}
			
			// Test products
			String[] aliases;
			String alias;
			X509Certificate certificate;
			X509Certificate[] certificateChain;
			byte[] fingerPrint;
			byte[] digitalSignature;
			
			// Communication unities
			DocumentDTO[] documents;
			String jsonResp;
			SkdsDocumentDetailsResponse detailsResp;
			SkdsPreSignResponse preSignResp;
			SkdsPostSignResponse postSignResp;
			AppletResponseDTO appletResponse;
			SignatureDTO emptySignatureDTO;
			SignatureDTO chainSignatureDTO;
			SignatureDTO digestSignatureDTO;
			SignatureDTO signedSignatureDTO;
			SignatureDTO finalizedSignatureDTO;
			VerifyDTO verifyDTO;
			
			// Init the applet
			try {
				applet.init();
				jsonResp = applet.selectDriver ( driver );
				appletResponse = (AppletResponseDTO) JSONUtils.deserializeJSON(AppletResponseDTO.class, jsonResp);
			} catch(Exception e) {
				tracer.error("error during the applet initialization", e);
				throw e;
			}
			
			// Login with the smartCard
			try {
				jsonResp = applet.login ( scPin );
				appletResponse = (AppletResponseDTO) JSONUtils.deserializeJSON(AppletResponseDTO.class, jsonResp);
				aliases = (String[]) JSONUtils.deserializeJSON ( String[].class, extractJSON(appletResponse) );
			} catch(Exception e) {
				tracer.error("error during the applet login", e);
				throw e;
			}
			
			// Choose the signing alias
			StringBuilder buf = new StringBuilder();
			for ( String a : aliases ) {
				buf.append(a).append(" ");
			}
			alias = aliases[0];
			tracer.info(String.format ( "available aliases:   %s", buf ));
			tracer.info(String.format ( "signing alias:       %s", alias ));
			
			// Load the certificate chain from the applet
			try {
				jsonResp = applet.selectCertificate ( alias );
				appletResponse = (AppletResponseDTO) JSONUtils.deserializeJSON(AppletResponseDTO.class, jsonResp);
				certificate = (X509Certificate) X509Utils.rawX509CertificateFromHex( extractJSON(appletResponse) );
				tracer.info(String.format ( "certificate:         %s", certificate ));
				certificateChain = new X509Certificate[] { certificate };
			} catch(Exception e) {
				tracer.error("error during the certificate selection", e);
				throw e;
			}
			
			// DocumentDetails
			try {
				SkdsDocumentDetailsRequest req = new SkdsDocumentDetailsRequest();
				req.setNodeRefs ( new String[] {SOURCE_REF} );
				detailsResp = postJsonRequest ( req, SkdsDocumentDetailsResponse.class );
				if ( ResultCode.valueOf(detailsResp.getResultCode()) == ResultCode.SUCCESS ) {
					documents = detailsResp.documentsFromBase64();
				} else {
					throw new Exception(detailsResp.getMessage());
				}
				String fileName = documents[0].getBaseDocument().getFileName();
				if ( applyMark ) {
					documents[0].setDestName(fileName + ".m7m");
				} else {
					documents[0].setDestName(fileName + ".p7m");
				}
			} catch(Exception e) {
				tracer.error("error during the pre sign phase", e);
				throw e;
			}
			
			// empty signature - initialized with the SHA256withRSA and RSA algorithms
			emptySignatureDTO = new SignatureDTO ( );
			emptySignatureDTO.setSignAlgorithm(conf.getSignatureAlgorithm().getName());
			emptySignatureDTO.setDigestAlgorithm(conf.getDigestAlgorithm().getName());
			emptySignatureDTO.signCategoryToString(SignCategory.CMS);
			
			// Add to the empty signature the timeStamp request if needed
			TimeStampRequestDTO tsRequestDTO = new TimeStampRequestDTO ();
			if ( applyMark ) {
				tsRequestDTO.timestampDispositionToString(SignDisposition.TimeStamp.ENVELOPING);
				tsRequestDTO.messageImprintAlgorithmToString(DigestAlgorithm.SHA256);
				tsRequestDTO.nounceToString(BigInteger.TEN);
				tsRequestDTO.setTsUrl("http://ca.signfiles.com/TSAServer.aspx");
			}
			emptySignatureDTO.setTimeStampRequest(tsRequestDTO);
			
			// chain signature - contains the certificate chain
			chainSignatureDTO = TemplateUtils.Instantiation.clone(emptySignatureDTO);
			chainSignatureDTO.certificateChainToHex(certificateChain);
			documents[0].setSignatures(new SignatureDTO[] {chainSignatureDTO});
			
			// PreSign phase - join the content with the certificate chain and evaluate the digest
			try {
				SkdsPreSignRequest req = new SkdsPreSignRequest();
				req.documentsToBase64(documents);
				preSignResp = postJsonRequest ( req, SkdsPreSignResponse.class );
				if ( ResultCode.valueOf(preSignResp.getResultCode()) == ResultCode.SUCCESS ) {
					documents = preSignResp.documentsFromBase64();
					digestSignatureDTO = documents[0].getSignatures()[0];
				} else {
					throw new Exception(preSignResp.getMessage());
				}
			} catch(Exception e) {
				tracer.error("error during the pre sign phase", e);
				throw e;
			}
			
			// signed signature - sign the digest with the smartCard to obtain the digitalSignature
			try {
				fingerPrint = digestSignatureDTO.getDigest().fingerPrintFromHex();
				tracer.info(String.format ( "fingerPrint:         %s", HexUtils.encodeHex(fingerPrint) ));
				jsonResp = applet.signDigest( HexUtils.encodeHex(fingerPrint) );
				appletResponse = (AppletResponseDTO) JSONUtils.deserializeJSON(AppletResponseDTO.class, jsonResp);
				digitalSignature = HexUtils.decodeHex ( (String) extractJSON(appletResponse) );
				tracer.info(String.format ( "digitalSignature:    %s", HexUtils.encodeHex(digitalSignature) ));
				signedSignatureDTO = TemplateUtils.Instantiation.clone(digestSignatureDTO); 
				signedSignatureDTO.digitalSignatureToHex(digitalSignature);
				documents[0].getSignatures()[0] = signedSignatureDTO;
			} catch(Exception e) {
				tracer.error("error during the digital signature evaluation", e);
				throw e;
			} 
			
			// PostSign phase - add the digitalSignature to the envelope and store the result into the JCLResultDTO
			try {
				SkdsPostSignRequest req = new SkdsPostSignRequest();
				req.documentsToBase64(documents);
				postSignResp = postJsonRequest ( req, SkdsPostSignResponse.class );
				if ( ResultCode.valueOf(postSignResp.getResultCode()) == ResultCode.SUCCESS ) {
					documents = postSignResp.documentsFromBase64();
					finalizedSignatureDTO = documents[0].getSignatures()[0];
				} else {
					throw new Exception(postSignResp.getMessage());
				}
			} catch(Exception e) {
				tracer.error("error during the envelope generation", e);
				throw e;
			}

//			// Verify phase - load the envelope content and verify the nested signature 
//			try {
//				jsonResp = signatureService.verify ( envelopeHex, null, null, VerifyResult.VALID.name() );
//				verifyDTO = extractResult ( VerifyDTO.class, jsonResp );
//			} catch(Exception e) {
//				tracer.error("error during the envelope verification", e);
//				throw e;
//			}
//			
//			// finalized signature - enveloped signed and eventually marked, not modifiable anymore
//			try {
//				verifyResult = (VerifyInfo) converter.toVerifyInfo( verifyDTO );
//			} catch(Exception e) {
//				tracer.error("unable to obtain the verifyInfo from the DTO", e);
//				throw e;
//			}
//			
//			try {
//				for(VerifiedSignature < ?, ?, VerifyResult, ?> verifiedSignature : verifyResult.getSignatures() ) {
//					tracer.info(String.format ( "signature validity:  %s", verifiedSignature.getVerifyResult().name() ));
//					tracer.info(String.format ( "signature type:      %s", verifiedSignature.getSignType().name() ));
//					tracer.info(String.format ( "disposition:         %s", verifiedSignature.getDisposition().name() ));
//					tracer.info(String.format ( "digest algorithm:    %s", verifiedSignature.getDigest().getAlgorithm().name() ));
//					tracer.info(String.format ( "finger print:        %s", HexUtils.encodeHex(verifiedSignature.getDigest().getFingerPrint()) ));
//					tracer.info(String.format ( "counter signature:   %s", verifiedSignature.isCounterSignature() ));
//					tracer.info(String.format ( "signature algorithm: %s", verifiedSignature.getSignAlgorithm().name() ));
//					tracer.info(String.format ( "digital signature:   %s", HexUtils.encodeHex(verifiedSignature.getDigitalSignature()) ));
//					tracer.info(String.format ( "reason:              %s", verifiedSignature.getReason() ));
//					tracer.info(String.format ( "signing location:    %s", verifiedSignature.getLocation() ));
//					tracer.info(String.format ( "signing time:        %s", formatDate(verifiedSignature.getSigningTime()) ));
//					tracer.info(String.format ( "\n "));
//					tracer.info(String.format ( "signing certificate chain: "));
//					for ( X509Certificate cert : verifiedSignature.getRawX509Certificates() ) {
//						showCertificate(cert);
//					}
//					if ( verifiedSignature.getTimeStamps() != null ) {
//						tracer.info(String.format ( "\n "));
//						tracer.info(String.format ( "timestamps: "));
//						for ( TimeStampInfo mark : verifiedSignature.getTimeStamps() ) {
//							tracer.info(String.format ( "timestamp validity:  %s", mark.getVerifyResult().name() ));
//							tracer.info(String.format ( "timestamp authority: %s", mark.getTsaName() ));
//							tracer.info(String.format ( "timestamp authority: %s", mark.getTsaName() ));
//							tracer.info(String.format ( "message imprint alg: %s", mark.getMessageInprintInfo().getAlgorithm().name() ));
//							tracer.info(String.format ( "message imprint:     %s", HexUtils.encodeHex(mark.getMessageInprintInfo().getFingerPrint()) ));
//							tracer.info(String.format ( "digest algorithm:    %s", mark.getDigestAlgorithm().name() ));
//							tracer.info(String.format ( "digital signature:   %s", HexUtils.encodeHex(mark.getDigitalSignature()) ));
//							tracer.info(String.format ( "signature algorithm: %s", mark.getSignAlgorithm().name() ));
//							tracer.info(String.format ( "timestamp certificate: "));
//							for ( X509Certificate cert : mark.getRawX509Certificates() ) {
//								showCertificate(cert);
//							}
//						}
//					}
//				}
//			} catch(Exception e) {
//				tracer.error("unable to print the verify results", e);
//				throw e;
//			}
			
		} finally {
			applet.destroy();
		}
	}
	
	private String extractJSON(AppletResponseDTO resp) throws Exception {
		String resultCode = resp.getResultCode();
		String json;
		if ( StringUtils.equals(resultCode, AppletResponseDTO.SUCCESS) ) {
			json = resp.getResult();
		} else {
			StringBuilder buf = new StringBuilder();
			for ( FieldErrorDTO fieldError : resp.getFieldErrors() ) {
				for ( String errorMessage : fieldError.getErrors() ) {
					buf.append ( String.format("fieldError  - %s: %s\n", fieldError.getField(), errorMessage) );
				}
			}
			for ( ActionErrorDTO actionError : resp.getActionErrors() ) {
				buf.append ( String.format("actionError - %s\n", actionError.getErrorMessage()) );
			}
			throw new Exception ( buf.toString() );
		}
		return json;
	}

	private void showCertificate(X509Certificate certificate) {
		Map<String, String> dns = DNParser.parse ( certificate.getSubjectDN() );
		tracer.info(String.format ( "subject:             %s", dns.get(SinekartaDsObjectIdentifiers.dn_commonName) ));
		tracer.info(String.format ( "country:             %s", dns.get(SinekartaDsObjectIdentifiers.dn_countryName) ));
		tracer.info(String.format ( "organization:        %s", dns.get(SinekartaDsObjectIdentifiers.dn_organizationName) ));
		tracer.info(String.format ( "organization unit:   %s", dns.get(SinekartaDsObjectIdentifiers.dn_organizationUnitName) ));
		tracer.info(String.format ( "not before:          %s", formatDate(certificate.getNotBefore()) ));
		tracer.info(String.format ( "not after:           %s", formatDate(certificate.getNotAfter()) ));
		dns = DNParser.parse ( certificate.getIssuerDN() );
		tracer.info(String.format ( "issuer:              %s", dns.get(SinekartaDsObjectIdentifiers.dn_commonName) ));
	}
	
	private String formatDate(Date date) {
		if ( date == null ) 											return "";
		return dateFormat.format( date );
	}
	
	public static <SkdsResponse extends BaseResponse> SkdsResponse postJsonRequest (
			BaseRequest request, 
			Class<SkdsResponse> responseClass ) throws IllegalStateException, IOException {
		
		SkdsResponse response = null;
		InputStream respIs = null;
		DefaultHttpClient httpclient = null;
		try {
			HttpHost targetHost = new HttpHost(HOST_NAME, PORT, "http");

			httpclient = new DefaultHttpClient();
			
			httpclient.getCredentialsProvider().setCredentials(
	                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
	                new UsernamePasswordCredentials(USER, PWD));
			
	        AuthCache authCache = new BasicAuthCache();

	        BasicScheme basicAuth = new BasicScheme();
	        authCache.put(targetHost, basicAuth);

	        BasicHttpContext localcontext = new BasicHttpContext();
	        localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);

	        HttpPost httppost = new HttpPost("/alfresco/service"+request.getJSONUrl()+".json?requestType=json");
	        
	        String req = request.toJSON();
			ByteArrayEntity body = new ByteArrayEntity(req.getBytes());
			httppost.setEntity(body);
			HttpResponse resp = httpclient.execute(targetHost, httppost, localcontext);
			HttpEntity entityResp = resp.getEntity();
	        respIs = entityResp.getContent();
	        
			response = TemplateUtils.Encoding.deserializeJSON(responseClass, respIs);
				
			EntityUtils.consume(entityResp);
//		} catch(Exception e) {
//			String message = e.getMessage();
//			if ( StringUtils.isBlank(message) ) {
//				message = e.toString();
//			}
//			tracer.error(message, e);
//			throw new RuntimeException(message, e);
		} finally {
			if ( httpclient != null) {
				httpclient.getConnectionManager().shutdown();
			}
			IOUtils.closeQuietly(respIs);
		}
		return response;
	}
	
//	@Test public void test1() throws Exception {
//		
//		String parentNodeRefId = "workspace://SpacesStore/87350b02-32ab-4787-98d0-3daa1c0b7b01"; // il node ref della dir che contiene i docs
//		
//		String nodeRefId = "workspace://SpacesStore/82f31432-162f-40b2-aece-09f18566ab6a"; // il noderef del documento da firmare
//		
//		String descrizioneMotivoFirma = "questo testo � la descrizione del motivo di firma che viene inserito nel documento PDF";
//		
//		String localitaFirma = "Italy"; // localit� in cui � apposta la firma (anche questo dato viene inserito nel documento PDF
//		
//		String digitalSignatureArea = null; // l' handle
//		
//		{ // step 1 : conversione in PDF/A
//		  // operazione lato server
//			HttpHost targetHost = new HttpHost(HOST_NAME, PORT, "http");
//	
//			DefaultHttpClient httpclient = new DefaultHttpClient();
//			
//			httpclient.getCredentialsProvider().setCredentials(
//	                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
//	                new UsernamePasswordCredentials(USER, PWD));
//			
//	        AuthCache authCache = new BasicAuthCache();
//	
//	        BasicScheme basicAuth = new BasicScheme();
//	        authCache.put(targetHost, basicAuth);
//	
//	        BasicHttpContext localcontext = new BasicHttpContext();
//	        localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);
//	
//	        HttpPost httppost = new HttpPost(DOCUMENT_TO_PDFA);
//	
//	        // prima di tutto, il documento va convertito in PDF/A
//	        // la action lavora sul documento stesso, quindi se non si vuole perdere l'originale � opportuno lavorare su una copia
//	        String req = 	"{\"nodeRefs\":\""+nodeRefId+"\"}";
//			ByteArrayEntity body = new ByteArrayEntity(req.getBytes());
//			httppost.setEntity(body);
//			HttpResponse response = httpclient.execute(targetHost, httppost, localcontext);
//			
//			HttpEntity entityResp = response.getEntity();
//	
//	        InputStream is = entityResp.getContent();
//	        
//			JSONObject inputJson=null;
//			try {
//				inputJson = JSONObject.fromObject(new String(loadResponse(is), ENCODING));
//			} catch (UnsupportedEncodingException e) {
//				// not possible
//			}
//			System.out.println(inputJson);
//	        
//	        EntityUtils.consume(entityResp);
//	        
//	        httpclient.getConnectionManager().shutdown();
//		}
//		// inizio della vera e propria procedura di firma
//		// se il documento fosse gi� stato un PDF/A, non sarebbe stato necessario convertirlo
//		{ // step 2 : inizializzazione procedura di firma
//		  // operazione lato server
//			HttpHost targetHost = new HttpHost(HOST_NAME, PORT, "http");
//	
//			DefaultHttpClient httpclient = new DefaultHttpClient();
//			
//			httpclient.getCredentialsProvider().setCredentials(
//	                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
//	                new UsernamePasswordCredentials(USER, PWD));
//			
//	        AuthCache authCache = new BasicAuthCache();
//	
//	        BasicScheme basicAuth = new BasicScheme();
//	        authCache.put(targetHost, basicAuth);
//	
//	        BasicHttpContext localcontext = new BasicHttpContext();
//	        localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);
//	
//	        HttpPost httppost = new HttpPost(DIGITAL_SIGNATURE_INIT);
//	
//	        // bisogna ora inizializzare la procedura di firma digitale per ottenere l'handle
//	        // l'handle ricevuto va poi passato alle chiamate successive
//	        // nessun documento viene modificato in questa fase
//	        // devo passare l'handle di una directory parent comune di tutti i documenti che andr� a firmare
//	        String req = 	"{\"nodeRef\":\""+parentNodeRefId+"\"}";
//			ByteArrayEntity body = new ByteArrayEntity(req.getBytes());
//			httppost.setEntity(body);
//			HttpResponse response = httpclient.execute(targetHost, httppost, localcontext);
//			
//			HttpEntity entityResp = response.getEntity();
//	
//	        InputStream is = entityResp.getContent();
//	        
//			JSONObject inputJson=null;
//			try {
//				inputJson = JSONObject.fromObject(new String(loadResponse(is), ENCODING));
//			} catch (UnsupportedEncodingException e) {
//				// not possible
//			}
//			System.out.println(inputJson);
//			
//			// imposto l'handle per la prima volta, viene sempre ripassato in giro
//			digitalSignatureArea = inputJson.getString("digitalSignatureArea");
//	        
//	        EntityUtils.consume(entityResp);
//	        
//	        httpclient.getConnectionManager().shutdown();
//		}
//		{ // step 3 : l'handle va passato al client di firma (che parla con il lettore di smart card) per eseguire la scelta del certificato
//		  // operazione lato client
//		  // il DRIVER ed il PIN dipendono dall'utente, li dovrebbe digitare lui
//		  // sinekarta espone un webscript che lista i driver definiti lato server
//		  // parto dal presupposto che ci sia solo un certificato caricato nella smart card, uso il primo per la firma
//			SinekartaDigitalSignatureClient client = new SinekartaDigitalSignatureClient(digitalSignatureArea); // importante : passare l'handle ricevuto in init
//			client.setDriver(DRIVER);
//			client.setPin(PIN);
//			client.start();
//			// contatta il lettore di smart card per ottenere la lista dei certificati
//			Map<X509Certificate, String> list = client.certificateList();
//			// importante, aggiornare l'handle per ripassarlo al server per la vase successiva
//			digitalSignatureArea = (String)list.values().toArray()[0];
//			client.close();
//		}
//		{ // step 4 : preparo i dati di firma per il/i documento/i da firmare
//		  // questo step pu� essere ripetuto tante volte, quanti sono i diversi motivi di firma
//		  // ogni chiamata pu� contenere diversi documenti, verranno tutti firmati con lo stesso motivo (descrizione) di firma 
//		  // operazione lato server
//			HttpHost targetHost = new HttpHost(HOST_NAME, PORT, "http");
//	
//			DefaultHttpClient httpclient = new DefaultHttpClient();
//			
//			httpclient.getCredentialsProvider().setCredentials(
//	                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
//	                new UsernamePasswordCredentials(USER, PWD));
//			
//	        AuthCache authCache = new BasicAuthCache();
//	
//	        BasicScheme basicAuth = new BasicScheme();
//	        authCache.put(targetHost, basicAuth);
//	
//	        BasicHttpContext localcontext = new BasicHttpContext();
//	        localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);
//	
//	        HttpPost httppost = new HttpPost(DIGITAL_SIGNATURE_PREPARE_AND_ADD);
//	
//	        // per ciascun documento (o elenco di documenti a parit� di descrizione) devono essere calcolati i dati di firma
//	        // importante : l'handle restituito dal client di firma va ripassato
//	        // non vengono modificati documenti in questa fase
//	        String req = "{" +
//	        				"\"digitalSignatureArea\" : \""+digitalSignatureArea+"\", " +
//	        				"\"signDescription\" : \""+descrizioneMotivoFirma+"\", " +
//	        				"\"signLocation\" : \""+localitaFirma+"\", " +
//	        				"\"nodeRefs\":\""+nodeRefId+"\"" +
//	        			"}";
//			ByteArrayEntity body = new ByteArrayEntity(req.getBytes());
//			httppost.setEntity(body);
//			HttpResponse response = httpclient.execute(targetHost, httppost, localcontext);
//			
//			HttpEntity entityResp = response.getEntity();
//	
//	        InputStream is = entityResp.getContent();
//	        
//			JSONObject inputJson=null;
//			try {
//				inputJson = JSONObject.fromObject(new String(loadResponse(is), ENCODING));
//			} catch (UnsupportedEncodingException e) {
//				// not possible
//			}
//			System.out.println(inputJson);
//			
//			// il server ha processato i dati di firma, recupero il nuvo handle da passare al client
//			digitalSignatureArea = inputJson.getString("digitalSignatureArea");
//	        
//	        EntityUtils.consume(entityResp);
//	        
//	        httpclient.getConnectionManager().shutdown();
//		}
//		{ // step 5 : l'handle va passato al client di firma (che parla con il lettore di smart card) per eseguire la vera e propria firma
//		  // operazione lato client
//			SinekartaDigitalSignatureClient client = new SinekartaDigitalSignatureClient(digitalSignatureArea); // attenzione : gli passo l'handle ricevuto dalla fase 4
//			client.start();
//			// � importante aggiornare l'handle con i dati ricevuti dal client
//			digitalSignatureArea = client.executeDigitalSignature(null); // potrei passargli un listener che mi avvisa dell'avanzamento, ma per questo test � inutile 
//			client.close();
//		}
//		{ // step 6 : chiudo e applico la firma a tutti i documenti; da eseguire una sola volta alla fine del processo
//		  // operazione lato server
//			HttpHost targetHost = new HttpHost(HOST_NAME, PORT, "http");
//	
//			DefaultHttpClient httpclient = new DefaultHttpClient();
//			
//			httpclient.getCredentialsProvider().setCredentials(
//	                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
//	                new UsernamePasswordCredentials(USER, PWD));
//			
//	        AuthCache authCache = new BasicAuthCache();
//	
//	        BasicScheme basicAuth = new BasicScheme();
//	        authCache.put(targetHost, basicAuth);
//	
//	        BasicHttpContext localcontext = new BasicHttpContext();
//	        localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);
//	
//	        HttpPost httppost = new HttpPost(DIGITAL_SIGNATURE_APPLY);
//	
//	        // devo passare l'handle ricevuto dal client
//	        // devo passare l'handle di una directory parent comune di tutti i documenti che andr� a firmare
//	        // i documenti vengono aggiornati solo in questa fase.
//	        // la procedura lasciata a met� non modifica i documenti PDF precedentemente creati
//	        String req = "{" +
//	        				"\"digitalSignatureArea\" : \""+digitalSignatureArea+"\", " +
//	        				"\"nodeRef\":\""+parentNodeRefId+"\"" +
//	        			"}";
//			ByteArrayEntity body = new ByteArrayEntity(req.getBytes());
//			httppost.setEntity(body);
//			HttpResponse response = httpclient.execute(targetHost, httppost, localcontext);
//			
//			HttpEntity entityResp = response.getEntity();
//	
//	        InputStream is = entityResp.getContent();
//	        
//			JSONObject inputJson=null;
//			try {
//				inputJson = JSONObject.fromObject(new String(loadResponse(is), ENCODING));
//			} catch (UnsupportedEncodingException e) {
//				// not possible
//			}
//			System.out.println(inputJson);
//			
//	        EntityUtils.consume(entityResp);
//	        
//	        httpclient.getConnectionManager().shutdown();
//		}
//	}
//	
	
	private byte[] loadResponse(InputStream is) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[8192];
		int len = is.read(buf);
		while (len!=-1) {
			baos.write(buf,0,len);
			len = is.read(buf);
		}
		byte[] ret = baos.toByteArray();
		is.close();
		baos.close();
		return ret;
	}
}
