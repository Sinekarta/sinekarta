package org.sinekartads.dto.tools;

import javax.xml.bind.JAXBException;

import org.sinekartads.dto.BaseDTO;
import org.sinekartads.util.EntityTransformer;

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
		protected DTO doTransform(String s) {
			return (DTO)BaseDTO.fromHex(s, dtoClass);
		}
	}
	
	public static class JSONDeserializer<DTO extends BaseDTO> extends DTODeserializer<DTO> {
		
		public JSONDeserializer(Class<DTO> dtoClass) {
			super(dtoClass);
		}
		
		@Override
		protected DTO doTransform(String json) {
			return (DTO)BaseDTO.fromJSON(json, dtoClass);
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

