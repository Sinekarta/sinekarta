package org.sinekartads.dto.tools;

import javax.xml.bind.JAXBException;

import org.sinekartads.dto.BaseDTO;
import org.sinekartads.util.EntityTransformer;

public abstract class DTOSerializer<D extends BaseDTO> extends EntityTransformer<D, String> {
	
	
	public static class HexSerializer<DTO extends BaseDTO> extends DTOSerializer<DTO> {

		@Override
		protected String doTransform(DTO dto) {
			return dto.toHex();
		}
	}
	
	public static class JSONSerializer<DTO extends BaseDTO> extends DTOSerializer<DTO> {

		@Override
		protected String doTransform(DTO dto) {
			return dto.toJSON();
		}
	}
	
	public static class XMLSerializer<DTO extends BaseDTO> extends DTOSerializer<DTO> {

		@Override
		protected String doTransform(DTO dto) {
			try {
				return dto.toXML();
			} catch (JAXBException e) {
				throw new RuntimeException(e);
			}
		}
	}
}

