package org.sinekartads.util.controller;

public class Controller {

	protected ControllerStateSet validStates;
	protected int initSteps;
	protected ControllerState state;	
	
	public Controller() {
		this(ControllerStateSet.DEFAULT, 1);
		initController();
	}
	
	public Controller(int initSteps) {
		this(ControllerStateSet.DEFAULT, initSteps);
	}
	
	public Controller(ControllerStateSet validStates, int initSteps) {
		if(initSteps < 1 || initSteps > validStates.getInitializationSteps()) {
			throw new IllegalArgumentException(
					String.format("invalid initSteps value: expected [1, %d], found %d", validStates.getInitializationSteps(), initSteps));
		}
		this.validStates 	= validStates;
		this.initSteps 		= initSteps;
		this.state = validStates.getUnitializedState();
	}
	
	
	
	// -----
	// --- Controller initialization
	// -
	
	protected int initCheckpoint = 0;
	
	protected synchronized void goToInitStep(int step) 
			throws IllegalArgumentException, 
			InitializingControllerException, 
			DisabledControllerException, 
			FinalizedControllerException {
		goToInitStep(step, true);
	}
	
	protected synchronized void goToInitStep(int step, boolean forceInitCheckpointBack) 
			throws IllegalArgumentException, 
					InitializingControllerException, 
					DisabledControllerException, 
					FinalizedControllerException {
					
		verifyDisabledState();
//		if( step > initSteps ) {
//			throw new IllegalArgumentException("the given step exceeds the maximum step numer: " + initSteps);
//		}
//		if( step > initCheckpoint + 1 ) {
//			throw new InitializingControllerException(
//					String.format("unable to perform the init step %d since the current inizialization state arrives to %d, perform the intermediate steps first", step, initCheckpoint));
//		}
		state = validStates.getInitializationState(step);
		if( step > initCheckpoint )	{
			initCheckpoint = step;
		} else if(forceInitCheckpointBack) {
			initCheckpoint = step;
		}
		if(validStates.getInitializationStep(state) >= initSteps-1) {
			state = validStates.getReadyState();
		}
	}
	
	protected synchronized void initController() 
			throws InitializingControllerException, 
					DisabledControllerException, 
					FinalizedControllerException {
					
		if( initSteps > 1 ) {
			throw new InitializingControllerException("the direct inizialization is available only when there are up to one init steps to do, found " + initSteps);
		}
		goToInitStep(0);
	}
	
	protected synchronized int currentInitializationStep() 
			throws InitializingControllerException, 
					DisabledControllerException, 
					FinalizedControllerException {
					
		verifyDisabledState();
		return validStates.getInitializationStep(state);
	}	
	
	protected synchronized int currentInitCheckpoint() {
		return initCheckpoint;
	}
	
	protected void forceInitCheckpointBack(int newInitCheckpoint) {
		if( newInitCheckpoint > initSteps ) {
			throw new IllegalArgumentException("the given checkpoint exceeds the maximum step numer: " + initSteps);
		}
		if(newInitCheckpoint > initCheckpoint + 1) {
			throw new InitializingControllerException(
					String.format("the newCheckpoint (%d) exceeds the current one (%d), specify a smaller one", newInitCheckpoint, initCheckpoint));
		}
	}
	
	
	
	// -----
	// --- ControllerState evaluation
	// -
	
	protected boolean isUninitialized() {
		return state.isUnitialized();
	}
	
	protected boolean isBeingInitialized() {
		return state.isInitializationState();
	}
	
	protected boolean hasBeenInitialized() {
		return initCheckpoint == initSteps-1;
	}
	
	protected boolean isReady() {
		return state.equals(validStates.getReadyState());
	}
	
	protected boolean canRun() {
		return canRun(initSteps-1);
	}
	
	protected boolean canRun(int requiredInitStep) {
		return (hasBeenInitialized() || currentInitializationStep() >= requiredInitStep) 
				&& !isClosed()
				&& !isDisabled() 
				&& !isFinalized();
	}
	
	protected boolean isClosed() {
		return state.equals(validStates.getClosedState());
	}
	
	protected boolean isFinalized() {
		return state.equals(validStates.getFinalizedState());
	}

	protected boolean isDisabled() {
		return state.equals(validStates.getDisabledState());
	}
	
	protected boolean isEnabled() {
		return state.equals(validStates.getDisabledState()) == false;
	}
	
	
	
	// -----
	// --- ControllerState alteration
	// -
	
	private ControllerState prevState;
	
	protected synchronized void disableController() 
			throws FinalizedControllerException {
		
		verifyFinalizedState();
		if(isEnabled()) {
			prevState = state;
			state = validStates.getDisabledState();
		}
	}
	
	protected synchronized void enableController() 
			throws FinalizedControllerException {
		
		verifyFinalizedState();
		if(isDisabled()) {
			state = prevState;
		}
	}
	
	protected synchronized void finalizeController() {
		state = validStates.getFinalizedState();
	}
	
	protected void closeController() 
			throws DisabledControllerException, 	
					FinalizedControllerException {
		
	}
	
	protected void closeController(boolean forceClosure) 
			throws DisabledControllerException, 	
					FinalizedControllerException {
		
		verifyDisabledState();
		if( !forceClosure ) {
			if( isClosed() ) {
				throw new ClosedControllerException("unable to close a closed controller");
			}
			if( hasBeenInitialized() ) {
				throw new InitializingControllerException("the controller can't be closed since it hasn't been fully initialized");
			}
		}
		state = validStates.getClosedState();
	}
	
	
	
	// -----
	// --- ControllerState verification
	// -
	
	protected synchronized void verifyDisabledState() 
			throws DisabledControllerException {
		
		if(state.isFinalized()) {
			throw new FinalizedControllerException(deletedControllerMessage());
		}
	}
	
	protected synchronized void verifyFinalizedState() 
			throws FinalizedControllerException {
		
		if(state.isFinalized()) {
			throw new FinalizedControllerException(deletedControllerMessage());
		}
	}
	
	protected void verifyControllerState() 
			throws UninitializedControllerException, 
					InitializingControllerException, 
					ClosedControllerException, 
					DisabledControllerException, 	
					FinalizedControllerException {
		
		verifyInitializationState(initSteps - 1);
	}
	
	protected void verifyInitializationState(int requiredInitStep) 
			throws UninitializedControllerException, 
					InitializingControllerException, 
					ClosedControllerException, 
					DisabledControllerException, 	
					FinalizedControllerException {
		
		// verify if the currentInitStep would allow the operation, verify the cause if not
		if( !canRun(requiredInitStep) ) {
			// exception if disabled or deleted
			verifyFinalizedState();
			verifyDisabledState();
			if( isUninitialized() ) {
				// exception if uninitialized 
				throw new UninitializedControllerException(unitializedControllerMessage());
			} else if( isClosed() ) {
				// exception if closed
				throw new ClosedControllerException(closedControllerMessage());
			}
			// the remaining cause is a partially initialized state which doesn't allow the operation
//			throw new InitializingControllerException(
//					partiallyInitializedControllerMessage(currentInitializationStep(), initSteps));
		}
	}

	
	
	// -----
	// --- Error descriptions, override if needed  
	// -
	
	public String disabledControllerMessage() {
		return "the controller has been disabled, it needs to be re-enabled before to be used again";
	}
	
	public String deletedControllerMessage() {
		return "the controller has been deleted, you need to create another one";
	}
	
	public String unitializedControllerMessage() {
		return "the controller has not been initialized yet";
	}
	
	public String partiallyInitializedControllerMessage(int currentStep, int initSteps) {
		return String.format(
				"the controller has not been fully initialized yet (step %d/%d)", 
				currentStep, initSteps);
	}
		
	public String initializingControllerMessage(int targetStep, int biggerInitStep) {
		return String.format(
				"unable to perform the init step %d since the current inizialization state " +
				"arrives to %d, perform the intermediate steps first", 
				targetStep, biggerInitStep);
	}
	
	public String closedControllerMessage() {
		return "the controller has been closed, it needs to be re-initialized before to be used again";
	}
	
}