package org.sinekartads.dto.domain;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.sinekartads.dto.BaseDTO;
import org.sinekartads.dto.formats.DateDTOProperty;

public class NodeDTO extends BaseDTO {

	private static final long serialVersionUID = 5082558771871459894L;

	private String parentRef;
	private String nodeRef;
	private String fileName;
	private String filePath;
	private String mimetype;
	private String description;
	@DateDTOProperty
	private String creationDate;
	@DateDTOProperty
	private String lastUpdate;
	
    public boolean isEmpty ( ) {
    	return StringUtils.isBlank ( nodeRef );
    }
	
	
	
	// -----
	// --- Simple properties
	// -
	
	public String getParentRef() {
		return parentRef;
	}

	public void setParentRef(String parentRef) {
		this.parentRef = parentRef;
	}
	
	public String getNodeRef() {
		return nodeRef;
	}

	public void setNodeRef(String nodeRef) {
		this.nodeRef = nodeRef;
	}

	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getMimetype() {
		return mimetype;
	}

	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	
	
	// -----
	// --- Formatted properties
	// -
	
	public Date creationDateFromString() {
		Date creationDate = null;
		if ( StringUtils.isNotBlank(this.creationDate) ) {
			try {
				creationDate = timeFormat.parse ( this.creationDate );
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
		return creationDate;
	}
	
	public void creationDateToString(Date creationDate) {
		if ( creationDate == null) {
			this.creationDate = null;
		} else {
			this.creationDate = timeFormat.format(creationDate);
		}
	}
	
	public Date lastUpdateFromString() {
		Date lastUpdate = null;
		if ( StringUtils.isNotBlank(this.lastUpdate) ) {
			try {
				lastUpdate = timeFormat.parse ( this.lastUpdate );
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
		return lastUpdate;
	}
	
	public void lastUpdateToString(Date lastUpdate) {
		if ( lastUpdate == null) {
			this.lastUpdate = null;
		} else {
			this.lastUpdate = timeFormat.format(lastUpdate);
		}
	}
	
	
	
	// -----
	// --- Direct access to formatted properties
	// -

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}
	public String getCreationDate() {
		return creationDate;
	}

	public void setLastUpdate(String lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	public String getLastUpdate() {
		return lastUpdate;
	}
	
}
