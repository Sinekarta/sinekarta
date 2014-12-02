package org.sinekartads.util.controller;

public interface ControllerState {
	
	public String name();
		
	public boolean isUnitialized();
	
	public boolean isInitializationState();
	
	public int initializationStep();
	
	public boolean isReady();
		
	public boolean isClosed();
	
	public boolean isFinalized();

	public boolean isDisabled();
}
