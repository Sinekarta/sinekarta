package org.sinekartads.alfresco.webscripts;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.sinekartads.dto.ResultCode;
import org.sinekartads.dto.request.SkdsHelloWorldRequest;
import org.sinekartads.dto.response.SkdsHelloWorldResponse;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;

public class SkdsHelloWorldWS extends BaseAlfrescoWS<SkdsHelloWorldRequest, SkdsHelloWorldResponse> {

	static final Logger tracer = Logger.getLogger(SkdsHelloWorldWS.class);
			
	public static final String DEFAULT_FORMAT = "Hello %s!!";
	public static final String DEFAULT_NAME = "World";
	
	@Override
	protected SkdsHelloWorldResponse executeImpl(SkdsHelloWorldRequest req,	Status status, Cache cache) {
		String messageFormat = req.getMessageFormat();
		String name = req.getName();
		
		if ( StringUtils.isBlank(messageFormat) ) {
			messageFormat = "Hello %s!!";
		}
		if ( StringUtils.isBlank(name) ) {
			name = "World";
		}
		String message = String.format(messageFormat, name);
		
		SkdsHelloWorldResponse resp = new SkdsHelloWorldResponse();
		resp.setMessage(message);
		resp.resultCodeToString(ResultCode.SUCCESS);
		return resp;
	}
	
}
