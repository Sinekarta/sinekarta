package org.sinekartads.integration;

import org.junit.Test;
import org.sinekartads.dto.domain.SignatureDTO;
import org.sinekartads.util.TemplateUtils;
import org.springframework.util.Assert;

public class DTOConverterTC extends BaseIntegrationTC {

	@Test
	public void test() {
		SignatureDTO clone;
		
		// Verify the JSON serialization
		String json = TemplateUtils.Encoding.serializeJSON ( cmsSignature, true );
		tracer.info ( String.format("cmsSignature as json: \n%s", json) );
		clone = TemplateUtils.Encoding.deserializeJSON ( SignatureDTO.class, json );
		Assert.isTrue ( clone.equals(cmsSignature) );

		// Verify the JSON serialization
		String base64 = TemplateUtils.Encoding.serializeBase64 ( cmsSignature );
		tracer.info ( String.format("cmsSignature as base64: \n%s", base64) );
		clone = TemplateUtils.Encoding.deserializeBase64 ( SignatureDTO.class, base64 );
		Assert.isTrue ( clone.equals(cmsSignature) );
		
		// Verify the hex serialization
		String hex = TemplateUtils.Encoding.serializeHex ( pdfSignature );
		tracer.info ( String.format("cmsSignature as hex: \n%s", hex) );
		clone = TemplateUtils.Encoding.deserializeHex ( SignatureDTO.class, hex );
		Assert.isTrue ( clone.equals(pdfSignature) );
		
		// TODO implement and verify the XML serialization
	}

}
