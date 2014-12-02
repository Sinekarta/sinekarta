package org.sinekartads.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.sinekartads.dto.tools.DTOPropertyType;

public class DTOFormatter {

	private final Map<DTOPropertyType, String> dtoPropertyFormats;
	
	public DTOFormatter() {
		dtoPropertyFormats = new HashMap<DTOPropertyType, String>();
	}
	
	public DTOFormatter(ResourceBundle resourceBundle) {
		dtoPropertyFormats = new HashMap<DTOPropertyType, String>();
		loadFormats(resourceBundle);
	}
	
	public BaseDTO format(BaseDTO dto) {
		dto.formatValues(dtoPropertyFormats);
		return dto;
	}
	
	public void clearFormats() {
		dtoPropertyFormats.clear();
	}
	
	public void loadFormats(ResourceBundle resourceBundle) {
		updateFormat(DTOPropertyType.Date, 		 resourceBundle, "format.date");
		updateFormat(DTOPropertyType.Time, 		 resourceBundle, "format.time");
		updateFormat(DTOPropertyType.DateTime, 	 resourceBundle, "format.dateTime");
		updateFormat(DTOPropertyType.Integer, 	 resourceBundle, "format.integer");
		updateFormat(DTOPropertyType.Decimal, 	 resourceBundle, "format.decimal");
		updateFormat(DTOPropertyType.Price, 	 resourceBundle, "format.price");
		updateFormat(DTOPropertyType.Percentage, resourceBundle, "format.percentage");
	}
	
	public void updateFormat(DTOPropertyType dtoPropertyType, ResourceBundle resourceBundle, String resourceKey) {
		try {
			updateFormat(dtoPropertyType, resourceBundle.getString(resourceKey));
		} catch(MissingResourceException e) {
			// do nothing, the missing formats can be load with another bundle 
		}
	}
	
	public void updateFormat(DTOPropertyType dtoPropertyType, String format) {
		if(StringUtils.isBlank(format)) {
			format = null;
		}
		dtoPropertyFormats.put(dtoPropertyType, format);
	}
}
