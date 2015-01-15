package org.sinekartads.core.cms;

import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.cms.Time;
import org.bouncycastle.asn1.esf.CommitmentTypeIndication;
import org.bouncycastle.asn1.esf.CommitmentTypeQualifier;
import org.bouncycastle.asn1.x500.DirectoryString;
import org.bouncycastle.cms.CMSAttributeTableGenerator;

/**
 * Default signed attributes generator.
 */
public class ExtSignedAttributeTableGenerator
    implements CMSAttributeTableGenerator
{
    private final Hashtable table;

    /**
     * Initialise to use all defaults
     */
    public ExtSignedAttributeTableGenerator()
    {
        table = new Hashtable();
    }

    /**
     * Initialise with some extra attributes or overrides.
     *
     * @param attributeTable initial attribute table to use.
     */
    public ExtSignedAttributeTableGenerator(
        AttributeTable attributeTable)
    {
        if (attributeTable != null)
        {
            table = attributeTable.toHashtable();
        }
        else
        {
            table = new Hashtable();
        }
    }

    /**
     * Create a standard attribute table from the passed in parameters - this will
     * normally include contentType, signingTime, and messageDigest. If the constructor
     * using an AttributeTable was used, entries in it for contentType, signingTime, and
     * messageDigest will override the generated ones.
     *
     * @param parameters source parameters for table generation.
     *
     * @return a filled in Hashtable of attributes.
     */
    protected Hashtable createStandardAttributeTable(
        Map parameters)
    {
        Hashtable std = (Hashtable)table.clone();

        if (!std.containsKey(CMSAttributes.contentType))
        {
            DERObjectIdentifier contentType = (DERObjectIdentifier)
                parameters.get(CMSAttributeTableGenerator.CONTENT_TYPE);

            // contentType will be null if we're trying to generate a counter signature.
            if (contentType != null)
            {
                Attribute attr = new Attribute(CMSAttributes.contentType,
                    new DERSet(contentType));
                std.put(attr.getAttrType(), attr);
            }
        }

        if (!std.containsKey(CMSAttributes.signingTime))
        {
        	if ( signingTime == null ) {
        		signingTime = new Date();
        	}
            Attribute attr = new Attribute(CMSAttributes.signingTime,
                new DERSet(new Time(signingTime)));
            std.put(attr.getAttrType(), attr);
        }

        if (!std.containsKey(CMSAttributes.messageDigest))
        {
            byte[] messageDigest = (byte[])parameters.get(
                CMSAttributeTableGenerator.DIGEST);
            Attribute attr = new Attribute(CMSAttributes.messageDigest,
                new DERSet(new DEROctetString(messageDigest)));
            std.put(attr.getAttrType(), attr);
        }
        
        if (StringUtils.isNotBlank(location)) {		// id-aa-ets-signerLocation
        	ASN1EncodableVector dev = new ASN1EncodableVector();
        	dev.add(new ASN1ObjectIdentifier("2.5.4.7"));
        	dev.add(new DirectoryString(location));
            Attribute attr = new Attribute(new ASN1ObjectIdentifier("1.2.840.113549.1.9.16.2.17"),
                new DERSet(new DERSequence(new DERSet(new DERSequence(dev)))));
            std.put(attr.getAttrType(), attr);
        }
        
        if (StringUtils.isNotBlank(reason)) {		// id-aa-ets-commitmentType
        	ASN1ObjectIdentifier proofOfOrigin = new ASN1ObjectIdentifier("1.2.840.113549.1.9.16.6.1");
        	ASN1EncodableVector dev = new ASN1EncodableVector();
        	dev.add(new CommitmentTypeQualifier(proofOfOrigin, new DERUTF8String(reason)));
        	CommitmentTypeIndication commitment = new CommitmentTypeIndication(proofOfOrigin, new DERSequence(dev));
        	Attribute attr = new Attribute(new ASN1ObjectIdentifier("1.2.840.113549.1.9.16.2.16"), new DERSet(commitment));
            std.put(attr.getAttrType(), attr);
        }

        return std;
    }

    /**
     * @param parameters source parameters
     * @return the populated attribute table
     */
    public AttributeTable getAttributes(Map parameters)
    {
        return new AttributeTable(createStandardAttributeTable(parameters));
    }
    
    
    
    private Date signingTime;
    private String reason;
    private String location;
    
    public Date getSigningTime() {
		return signingTime;
	}

	public void setSigningTime(Date signingTime) {
		this.signingTime = signingTime;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

}
