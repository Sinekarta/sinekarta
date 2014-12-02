package org.sinekartads.smartcard;

import iaik.pkcs.pkcs11.DefaultInitializeArgs;
import iaik.pkcs.pkcs11.InitializeArgs;
import iaik.pkcs.pkcs11.Module;
import iaik.pkcs.pkcs11.Slot;
import iaik.pkcs.pkcs11.TokenException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

public abstract class SmartCardUtils {
	
	private static Logger tracer = Logger.getLogger(SmartCardUtils.class);
//	private static String defaultDriver;
//	private static String[] workingDrivers;
	
//	public static String getDefaultDriver() 
//			throws SmartCardReaderNotFoundException, 
//					SmartCardAccessException {
//		
//		if(StringUtils.isEmpty(defaultDriver)) {
//			autodetectDrivers();
//		} 
//		return defaultDriver;
//	}
//	
//	public static String[] getWorkingDrivers() 
//			throws SmartCardReaderNotFoundException,
//					SmartCardAccessException {
//		
//		if(ArrayUtils.isEmpty(workingDrivers)) {
//			autodetectDrivers();			
//		}
//		return workingDrivers;
//	}
		
	public static String[] detectDrivers(String[] allowedDrivers) 
			throws SmartCardReaderNotFoundException, DriverNotFoundException {
		
		String libraryPath = System.getProperty("java.library.path");
		String[] libraries = libraryPath.split(File.pathSeparator);
		
		Set<String> drivers = new HashSet<String>();
		for ( String driver : allowedDrivers ) {
			drivers.add ( driver );
		}
		
		// verify all the drivers into all the directories of the library path
		List<String> matchingDrivers = new ArrayList<String>();
		for ( String library : libraries ) {
			tracer.info("folder - "+library);
			File dir = new File ( library );
			if ( dir.exists() && dir.isDirectory() ) {
				for ( String driver : dir.list() ) {
					if ( drivers.contains(driver) ) {
						try {
							tracer.info("working driver - "+driver);
							verifyDriver(driver);
							matchingDrivers.add(driver);
						} catch(SmartCardReaderNotFoundException e) {
							throw e;
						} catch(SmartCardAccessException e) {
							// do nothing
							tracer.error(e.getMessage(), e);
						}
					}
				}
			}
		}
		
		if ( matchingDrivers.isEmpty() ) {
			throw new DriverNotFoundException("no matching driver has been found");
		}
		
		return matchingDrivers.toArray ( new String[matchingDrivers.size()] );
	}
	
	public static void verifyDriver(String pkcs11Driver) 
			throws SmartCardReaderNotFoundException, 
					PKCS11DriverNotFoundException, 
					InvalidPKCS11DriverException, 
					SmartCardAccessException {
		
		Module iaikPKCS11Module = null;
		try {
			// build a iaikPKCS11Module above the given driver
			try {
				iaikPKCS11Module = Module.getInstance(pkcs11Driver);
			} catch (IOException e) {
				throw new PKCS11DriverNotFoundException("Unable to find driver " + pkcs11Driver,e);
			} catch (Exception e) {
				throw new InvalidPKCS11DriverException("Unable to validate the driver " + pkcs11Driver,e);
			}
			if (iaikPKCS11Module==null) {
				throw new PKCS11DriverNotFoundException("pkcs11 driver not found");
			}
			
			// init the module
			try {
				InitializeArgs initializeArgs = new DefaultInitializeArgs();
				iaikPKCS11Module.initialize(initializeArgs);
			} catch (TokenException e) {
				if (!e.getMessage().contains("CKR_CRYPTOKI_ALREADY_INITIALIZED")) {
					throw new SmartCardAccessException("Unable to initialize pkcs11 module",e);
				}
			}
			
			// seek a smartcard reader with a smartcard to grant that the driver is working
			Slot[] iaikSmartCardReadersWithCard = null;
			try {
				iaikSmartCardReadersWithCard = iaikPKCS11Module.getSlotList(Module.SlotRequirement.TOKEN_PRESENT);
			} catch (Exception e) {
				throw new SmartCardReaderNotFoundException("Unable to find smart card reader",e);
			}
			if (ArrayUtils.isEmpty(iaikSmartCardReadersWithCard)) {
				throw new SmartCardReaderNotFoundException("No smart card reader found");
			}
		} finally {
			// finalize the module after having tested the driver
			finalizeQuietly(iaikPKCS11Module);
		}
	}
	
	public static void finalizeQuietly(SmartCardAccess sca) {
		if(sca != null) {
			try {
				sca.finalize();
			} catch(Exception e) {
				tracer.info(String.format("error detected when closing the smartCardAccess %s: %s", sca, e.getMessage()), e);
			}
		}
	}
	
	public static void finalizeQuietly(Module iaikPKCS11Module) {
		try {
			if(iaikPKCS11Module != null) {
				iaikPKCS11Module.finalize(null);
			}
		} catch (Exception e) {
			tracer.info(String.format("error detected when finalizing the iaikPKCS11Module: %s", e.getMessage()), e);
		}
	}
}
