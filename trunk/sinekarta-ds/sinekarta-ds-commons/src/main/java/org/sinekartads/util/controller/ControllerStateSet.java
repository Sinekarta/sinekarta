package org.sinekartads.util.controller;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.sinekartads.util.AbstractObjectFormatter;
import org.sinekartads.util.ObjectFormatter;
import org.sinekartads.util.TextUtils;




public abstract class ControllerStateSet extends TreeSet<ControllerState> { 

	private static final long serialVersionUID = -2984821581456592651L;

	public enum DefaultControllerStateSet implements ControllerState {
		UNITIALIZED,
		INIT_STEP_01,
		INIT_STEP_02,
		INIT_STEP_03,
		INIT_STEP_04,
		INIT_STEP_05,
		INIT_STEP_06,
		INIT_STEP_07,
		INIT_STEP_08,
		INIT_STEP_09,
		READY,
		CLOSED,
		DISABED,	
		DELETED;
		
		@Override
		public boolean isUnitialized() {
			return this == DefaultControllerStateSet.UNITIALIZED;
		}
		
		@Override
		public boolean isInitializationState() {
			Integer ordinal = this.ordinal();
			return ordinal >= DefaultControllerStateSet.INIT_STEP_01.ordinal() 
					&& ordinal <= DefaultControllerStateSet.INIT_STEP_09.ordinal();
		}
				
		@Override
		public boolean isReady() {
			return this == DefaultControllerStateSet.READY;
		}
		
		@Override
		public boolean isClosed() {
			return this == DefaultControllerStateSet.CLOSED;
		}
		
		@Override
		public boolean isFinalized() {
			return this == DefaultControllerStateSet.DELETED;
		}
	
		@Override
		public boolean isDisabled() {
			return this == DefaultControllerStateSet.DISABED;
		}
	
		@Override
		public int initializationStep() {
			if(ordinal() < INIT_STEP_01.ordinal() || ordinal() > INIT_STEP_09.ordinal()) {
				return -1;
			}
			return ordinal() - INIT_STEP_01.ordinal();
		}
	
	}
	
	public static ControllerStateSet DEFAULT = new ControllerStateSet(
			new TreeSet<ControllerState>(Arrays.asList(DefaultControllerStateSet.values()))) {

		private static final long serialVersionUID = -164554615710211310L;

		@Override
		public ControllerState[] values() {
			return DefaultControllerStateSet.values();
		}

		@Override
		public ControllerState valueOf(String name) {
			return DefaultControllerStateSet.valueOf(name);
		}
	};
	
	
	public ControllerStateSet(SortedSet<? extends ControllerState> validStates) {
		addAll(validStates);
		
		// stateDetectionMethods <- all the boolean methods starting with 'is'
		//							the dsMethods will be matched with the states to detect duplications and omissions  
		List<Method> stateDetectionMethods = new ArrayList<Method>();
		Class<ControllerState> clazz = ControllerState.class;
		for(Method method : clazz.getDeclaredMethods()) {
			if(method.getReturnType().isAssignableFrom(boolean.class) 
					&& method.getName().startsWith("is")) {
				stateDetectionMethods.add(method);
			}
		}
		
		// missingSdm <- starting from stateDetectionMethods, every sdMethod will be removed when matching with a state
		//				 at the end the set must be empty, missing state detection method otherwise
		List<Method> missingSdm = new ArrayList<Method>(stateDetectionMethods);
		
		// states <- starting from validStates with, every state will be removed when matching with a sdMethod
		// 			 at the end the set must be empty, the remaining state will match with no method otherwise
		List<ControllerState> states = new ArrayList<ControllerState>(validStates);
		
		// matchingSdMethods <- for each sdMethod, the list of matching states
		//				 		at the end the map must contain exactly one match for each non-init sdMethod, duplicated otherwise
		Map<Method, List<ControllerState>> matchingSdMethods = new HashMap<Method, List<ControllerState>>();
		
		// duplicatedStates <- the states matching with more than one state detection method
		//					   at the end the map must be empty, it will be impossible to identify the state otherwise  
		Map<ControllerState, List<Method>> duplicatedStates = new HashMap<ControllerState, List<Method>>();
		
		// initializationStates <- states that returned true on the isBeingInitialized test
		//						   the init states will maintain the immission order
		List<ControllerState> initializationStates = new ArrayList<ControllerState>();
		
		
		// detect duplicated states, removing the items from 'missing' lists
		try {
			Iterator<ControllerState> statesIt;
			ControllerState state;
			List<Method> stateMatches;
			List<ControllerState> sdMethodMatches;
			// loop over the states
			statesIt = states.iterator();
			while (statesIt.hasNext()) {
				state = statesIt.next();
				stateMatches = new ArrayList<Method>();
				// loop over the sdMethods
				for (Method sdMethod : stateDetectionMethods) {
					// positive match: collect it and remove the method from the missing ones 
					if ((Boolean) sdMethod.invoke(state)) {
						sdMethodMatches = matchingSdMethods.get(sdMethod);
						if(sdMethodMatches == null) {
							sdMethodMatches = new ArrayList<ControllerState>();
							matchingSdMethods.put(sdMethod, sdMethodMatches);
						}
						sdMethodMatches.add(state);
						missingSdm.remove(sdMethod);
						stateMatches.add(sdMethod);
					}
				}
				// match found: remove the method from the missing ones
				if(stateMatches.size() >= 1) {
					statesIt.remove();
					// multiple match, add the state to the duplicatedStates
					if(stateMatches.size() > 1) {
						duplicatedStates.put(state, stateMatches);
					}
				} 
			}
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(String.format("error during the validStates validation: %s", e.getMessage()), e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(String.format("error during the validStates validation: %s", e.getMessage()), e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(String.format("error during the validStates validation: %s", e.getMessage()), e);
		}
		
		// detect the duplicated sdMethods and populate the state retrieving properties
		StringBuilder duplications = new StringBuilder();
		String duplication;
		List<ControllerState> sdMethodMatches;
		ControllerState state;
		for(Map.Entry<Method, List<ControllerState>> matchingSdm : matchingSdMethods.entrySet()) {
			sdMethodMatches = matchingSdm.getValue();
			// populate the state retrieving properties
			if(sdMethodMatches.size() == 1) {
				state = sdMethodMatches.get(0);
				if(state.isUnitialized()) 			unitializedState = state;			else 
				if(state.isInitializationState())  	initializationStates.add(state);	else 
				if(state.isReady())  				readyState = state;					else 
				if(state.isClosed())  				closedState = state;				else 
				if(state.isFinalized())  				deletedState = state;				else 
				if(state.isDisabled())  			disabledState = state;
			} else {
				// detect the initializationStates  
				boolean initializationState = false;
				for(ControllerState s : sdMethodMatches) {
					if(s.isInitializationState()) {
						initializationStates.add(s);
						initializationState = true;
					}
				}
				// only the initializationStates can have a multiple match, duplication otherwise
				if( !initializationState ) {
					duplication = String.format(
							" - %s: %s", 
							matchingSdm.getKey().getName(), 
							TextUtils.fromCollection(matchingSdm.getValue()));
					TextUtils.appendToken(duplications, duplication, "\n");
				}
			}
		}
		this.initializationStates = initializationStates.toArray(new ControllerState[initializationStates.size()]); 
		
		// define a method formatter for an user friendly method description
		ObjectFormatter<Method> methodFormatter = new AbstractObjectFormatter<Method>() {
			@Override
			public String format(Method method) {
				return method.getName();
			}
		};
		
		// verify that each sdMethod has not multiple matching states
		if(duplications.length() > 0) {
			String.format("the following methods match with multiple states: \n%s", duplications.toString());
		}
		// verify that each state has not multiple matching sdMethods
		if(duplicatedStates.size() > 0) {
			duplications = new StringBuilder();
			for(Map.Entry<ControllerState, List<Method>> duplicatedState : duplicatedStates.entrySet()) {
				duplication = String.format(
						" - %s: %s", 
						duplicatedState.getKey().name(), 
						TextUtils.fromCollection(duplicatedStates.values(), methodFormatter));
				TextUtils.appendToken(duplications, duplication, "\n");
			}
			throw new IllegalArgumentException(
					String.format("the following states match with multiple methods: \n%s", 
								duplications.toString()));
		}
		// verify that each sdMethod has at least a matching state
		if(missingSdm.size() > 0) {
			throw new IllegalArgumentException(
					String.format("the following methods don't match with any state: %s", 
								TextUtils.fromCollection(missingSdm, methodFormatter, ", ", "", "")));
		}
		// verify that each state has at least a matching sdMethod
		if(states.size() > 0) {
			throw new IllegalArgumentException(
					String.format("the following methods dont'm match with any state: %s", 
								TextUtils.fromCollection(states, null, ", ", "", "")));
		}
		
		
	}
	
	protected abstract ControllerState[] values();
	
	protected abstract ControllerState valueOf(String name);
	
	
	
	// -----
	// --- State retrieving
	// -
	
	ControllerState unitializedState;
	ControllerState[] initializationStates;
	ControllerState readyState;
	ControllerState closedState;
	ControllerState deletedState;
	ControllerState disabledState;
	
	protected ControllerState getUnitializedState() {
		return unitializedState;
	}
	
	protected ControllerState getInitializationState(int step) throws IllegalArgumentException {
		if(step < 0 || step >= initializationStates.length) {
			throw new IllegalArgumentException(String.format("invalid step %d, expected [0, %d]", step, initializationStates.length));
		}
		return initializationStates[step];
	}
	
	protected int getInitializationStep(ControllerState state) throws IllegalArgumentException {
		if( state.isInitializationState() ) {
			for(int i=0; i<initializationStates.length; i++) {
				if(state.equals(initializationStates[i])) {
					return i;
				}
			}
			throw new IllegalArgumentException(String.format("the given state (%s) has not been found", state));
		} 
		return -1;
	}
	
	protected int getInitializationSteps() {
		return initializationStates.length;
	}
	
	protected ControllerState getReadyState() {
		return readyState;
	}
	
	protected ControllerState getClosedState() {
		return closedState;
	}
	
	protected ControllerState getFinalizedState() {
		return deletedState;
	}
	
	protected ControllerState getDisabledState() {
		return disabledState;
	}
	
}
