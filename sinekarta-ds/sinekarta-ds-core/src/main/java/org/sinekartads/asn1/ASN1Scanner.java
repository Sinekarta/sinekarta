package org.sinekartads.asn1;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.ASN1UTCTime;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DEREncodable;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DERUTF8String;

public abstract class ASN1Scanner {
	
	public static final String ARG_VALUE = "value";
	public static final String ARG_LEVEL = "level";
	public static final String ARG_CONTROLLER = "controller";
	
	public synchronized void scan(DEREncodable root) {
		scan(root, null);
	}
	
	public synchronized void scan(DEREncodable root, Map<String, Object> args) {
		if(args == null) {
			args = new HashMap<String, Object>(); 
		}
		ScanController controller = new ScanController();
		args.put(ARG_CONTROLLER, controller);
		if(args.containsKey(ARG_LEVEL) == false) {
			args.put(ARG_LEVEL, 0);
		}
		scan(root, null, args);
	}
	
	@SuppressWarnings("unchecked")
	protected void scan(DEREncodable object, DEREncodable parent, Map<String, Object> args) {
		ScanController controller = (ScanController)args.get(ARG_CONTROLLER);
		if(controller.isHalted()) return; 
		
		try {
			begin(object, parent, args);
			analyze(object, parent, args);
			if(controller.isHalted()) return; 
			
			if(object instanceof ASN1TaggedObject) {
				scan(((ASN1TaggedObject)object).getObject(), object, nextCallArgs(args));
			} else if(object instanceof DERSequence) {
				DERSequence seq = (DERSequence)object;
				Enumeration<DEREncodable> objects = seq.getObjects();
				while(objects.hasMoreElements()) {
					scan(objects.nextElement(), object, nextCallArgs(args));
				}
			} else if(object instanceof DERSet) {
				DERSet set = (DERSet)object;
				Enumeration<DEREncodable> objects = set.getObjects();
				while(objects.hasMoreElements()) {
					scan(objects.nextElement(), object, nextCallArgs(args));
				}
			}
		}
		finally {
			end(object, parent, args);
		}
	}
	
	private Map<String, Object> nextCallArgs(Map<String, Object> args) {
		Map<String, Object> nextArgs = new HashMap<String, Object>(args);
		int level = (Integer)args.get(ARG_LEVEL);
		nextArgs.put(ARG_LEVEL, level+1);
		return nextArgs;
	}
	
	protected void analyze(DEREncodable object, DEREncodable parent, Map<String, Object> args) {		
		Object value = null;
		if(object instanceof ASN1Integer) {
			value = ((ASN1Integer)object).getValue();
		} else if(object instanceof ASN1ObjectIdentifier) {
			value = ((ASN1ObjectIdentifier)object).getId();
		} else if(object instanceof DEROctetString) {
			value = ((DEROctetString)object).getOctets();
		} else if(object instanceof DERPrintableString) {
			value = ((DERPrintableString)object).getString();				
		} else if(object instanceof DERUTF8String) {
			value = ((DERUTF8String)object).getString();
		} else if(object instanceof DERBitString) {
			value = ((DERBitString)object).getString();
		} else if(object instanceof DERIA5String) {
			value = ((DERIA5String)object).getString();			
		} else if(object instanceof ASN1Boolean) {
			value = ((ASN1Boolean)object).isTrue();
		} else if(object instanceof ASN1UTCTime) {
			try {
				value = ((ASN1UTCTime)object).getDate();
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		} else if(object instanceof DERTaggedObject) {
			value = "tagNo: " + ((DERTaggedObject)object).getTagNo();
		} else if(object instanceof DERSequence) {
			value = "elems: " + ((DERSequence)object).size();
		} else if(object instanceof DERSet) {
			value = "elems: " + ((DERSet)object).size();			
		}
		args.put(ARG_VALUE, value);
		process(object, parent, args);
		
	}
	protected abstract void process(DEREncodable object, DEREncodable parent, Map<String, Object> args);
	protected void begin(DEREncodable object, DEREncodable parent, Map<String, Object> args) {
		// override if necessary
	}
	protected void end(DEREncodable object, DEREncodable parent, Map<String, Object> args) {
		// override if necessary
	}
	
	
	
	protected class ScanController {
		private boolean halted;
		
		private ScanController() {
			this(false);
		}
		
		private ScanController(boolean halted) {
			this.halted = halted;
		}
		
		private boolean isHalted() {
			return halted;
		}
		
		public void halt() {
			halted = true;
		}
	}
	
	protected void closeBranch(Map<String, Object> args) {
		args.put(ARG_CONTROLLER, new ScanController(true));
	}
	
	protected ScanController getScanController(Map<String, Object> args) {
		return (ScanController)args.get(ARG_CONTROLLER);
	}
	
	protected int getLevel(Map<String, Object> args) {
		return (Integer)args.get(ARG_LEVEL);
	}
	
	protected Object getValue(Map<String, Object> args) {
		return args.get(ARG_VALUE);
	}
}