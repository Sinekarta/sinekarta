package org.sinekartads.dto.request;

public class SkdsFindRefByNameRequest extends BaseRequest {

	private static final long serialVersionUID = -2418000620322902273L;
	private String name;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

}