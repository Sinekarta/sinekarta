package org.sinekartads.dto.tools;

import java.lang.annotation.Annotation;

import org.sinekartads.dto.formats.DateDTOProperty;
import org.sinekartads.dto.formats.DateTimeDTOProperty;
import org.sinekartads.dto.formats.DecimalDTOProperty;
import org.sinekartads.dto.formats.FlagDTOProperty;
import org.sinekartads.dto.formats.IntegerDTOProperty;
import org.sinekartads.dto.formats.PercentageDTOProperty;
import org.sinekartads.dto.formats.PriceDTOProperty;
import org.sinekartads.dto.formats.TimeDTOProperty;

public enum DTOPropertyType {
	
	Flag(FlagDTOProperty.class),
	DateTime(DateTimeDTOProperty.class),
	Date(DateDTOProperty.class),
	Time(TimeDTOProperty.class),
	Integer(IntegerDTOProperty.class),
	Decimal(DecimalDTOProperty.class),
	Price(PriceDTOProperty.class),
	Percentage(PercentageDTOProperty.class);
	
	Class<? extends Annotation> annot;
	
	DTOPropertyType(Class<? extends Annotation> annot) {
		this.annot = annot;
	}
	
	public Class<? extends Annotation> getAnnot() {
		return annot;
	}
	
}
