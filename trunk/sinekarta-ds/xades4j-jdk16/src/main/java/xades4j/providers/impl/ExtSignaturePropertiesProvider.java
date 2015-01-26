/*
 * XAdES4j - A Java library for generation and verification of XAdES signatures.
 * Copyright (C) 2010 Luis Goncalves.
 *
 * XAdES4j is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or any later version.
 *
 * XAdES4j is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with XAdES4j. If not, see <http://www.gnu.org/licenses/>.
 */
package xades4j.providers.impl;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import xades4j.properties.SignatureProductionPlaceProperty;
import xades4j.properties.SigningTimeProperty;
import xades4j.providers.SignaturePropertiesCollector;
import xades4j.providers.SignaturePropertiesProvider;

/**
 * *** SKDSFIX ************************************************************
 * <p>This class extends SignerExtBES instead of SignerBES, there are no
 * other difference with the original one.
 * ************************************************************************
 * 
 * An implementation of {@link SignaturePropertiesProvider} which can generate 
 * and return the SigningTime when not externally set. It can receive
 * even the Location  externally.
 * @author Luís
 */
public class ExtSignaturePropertiesProvider implements SignaturePropertiesProvider
{
	private Date signingTime;
	private String location;
	
    @Override
    public void provideProperties(SignaturePropertiesCollector signaturePropsCol)
    {
    	Calendar cal = Calendar.getInstance();
    	if ( signingTime == null ) {
    		signingTime = cal.getTime();
    	} else {
    		cal.setTime(signingTime);
    	}
    	signaturePropsCol.setSigningTime(new SigningTimeProperty(cal));
    	if ( StringUtils.isNotBlank(location) ) {
    		signaturePropsCol.setSignatureProductionPlace(new SignatureProductionPlaceProperty(location, null));
    	}
    }

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Date getSigningTime() {
		return signingTime;
	}

	public void setSigningTime(Date signingTime) {
		this.signingTime = signingTime;
	}
}