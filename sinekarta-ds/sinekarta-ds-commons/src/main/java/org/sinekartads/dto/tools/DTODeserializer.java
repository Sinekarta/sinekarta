package org.sinekartads.dto.tools;

import javax.xml.bind.JAXBException;

import org.sinekartads.dto.BaseDTO;
import org.sinekartads.util.EntityTransformer;
import org.sinekartads.util.TemplateUtils;

public abstract class DTODeserializer<D extends BaseDTO> extends EntityTransformer<String, D> {
	
	public DTODeserializer ( Class<D> dtoClass ) {
		this.dtoClass = dtoClass;
	}
	
	Class<D> dtoClass;
	
	
	
	public static class HexDeserializer<DTO extends BaseDTO> extends DTODeserializer<DTO> {
		
		public HexDeserializer(Class<DTO> dtoClass) {
			super(dtoClass);
		}

		@Override
		protected DTO doTransform(String hex) {
			return (DTO)TemplateUtils.Encoding.deserializeHex ( dtoClass, hex );
		}
	}
	
	public static class Base64Deserializer<DTO extends BaseDTO> extends DTODeserializer<DTO> {
		
		public Base64Deserializer(Class<DTO> dtoClass) {
			super(dtoClass);
		}

		@Override
		protected DTO doTransform(String base64) {
			return (DTO)TemplateUtils.Encoding.deserializeBase64 ( dtoClass, base64 );
		}
	}
	
	public static class JSONDeserializer<DTO extends BaseDTO> extends DTODeserializer<DTO> {
		
		public JSONDeserializer(Class<DTO> dtoClass) {
			super(dtoClass);
		}
		
		@Override
		protected DTO doTransform(String json) {
			return (DTO)TemplateUtils.Encoding.deserializeJSON(dtoClass, json);
		}
	}
	
	public static class XMLDeserializer<DTO extends BaseDTO> extends DTODeserializer<DTO> {
		
		public XMLDeserializer(Class<DTO> dtoClass) {
			super(dtoClass);
		}
		
		@Override
		protected DTO doTransform(String xml) {
			try {
				return (DTO)BaseDTO.fromXML(xml, dtoClass);
			} catch (JAXBException e) {
				throw new RuntimeException(e);
			}
		}
	}
}

