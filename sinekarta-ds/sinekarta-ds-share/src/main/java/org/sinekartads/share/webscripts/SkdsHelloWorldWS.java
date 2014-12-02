package org.sinekartads.share.webscripts;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.sinekartads.dto.ResultCode;
import org.sinekartads.dto.request.SkdsHelloWorldRequest;
import org.sinekartads.dto.response.SkdsHelloWorldResponse;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class SkdsHelloWorldWS extends BaseWS {

	static final Logger tracer = Logger.getLogger(SkdsHelloWorldWS.class);
			
	public static final String RESULT = "result";
	public static final String RESULT_CODE = "resultCode";
	public static final String ERROR_MESSAGE = "errorMessage";
	public static final String DEFAULT_FORMAT = "Hello %s!!";
	public static final String DEFAULT_NAME = "World";
	
	public Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		String messageFormat = (String)getRequestParameter(req, "messageFormat");
		String name = (String)getRequestParameter(req, "name");
		String message;

		Map<String, Object> result = new HashMap<String, Object>();
		try {
	//		if ( StringUtils.isBlank(name) ) {
	//			name = DEFAULT_NAME;
	//		}
	//		if ( StringUtils.isBlank(messageFormat) ) {
	//			messageFormat = DEFAULT_FORMAT;
	//		}
	//		message = String.format(messageFormat, name);
			SkdsHelloWorldRequest hwreq = new SkdsHelloWorldRequest();
			hwreq.setMessageFormat(messageFormat);
			hwreq.setName(name);
			SkdsHelloWorldResponse hwresp = postJsonRequest ( hwreq, SkdsHelloWorldResponse.class);
			message = hwresp.getMessage();
			result.put(RESULT, message);
			result.put(RESULT_CODE, ResultCode.SUCCESS.getCode());
		} catch(Exception e) {
			result.put(RESULT_CODE, ResultCode.INTERNAL_SERVER_ERROR.getCode());
			result.put(ERROR_MESSAGE, e.getMessage());
			tracer.error(e.getMessage(), e);
		}
		
		return result;
	}
	
}
