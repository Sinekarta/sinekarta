package org.sinekartads.asn1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DEREncodable;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
//import org.bouncycastle.asn1.ASN1ObjectIdentifier;
//import org.bouncycastle.asn1.ASN1TaggedObject;
//import org.bouncycastle.asn1.DEREncodable;
//import org.bouncycastle.asn1.DERNull;
//import org.bouncycastle.asn1.DERSequence;
//import org.bouncycastle.asn1.DERSet;

public abstract class ASN1Selector<Constraint> {
	
	public static class ASN1OidLabelSelector extends ASN1Selector<ASN1ObjectIdentifier> {
		@Override
		protected boolean check(DEREncodable object, DEREncodable parent, Map<String, Object> args) {
			return object.equals(args.get(ARG_CONSTRAINT));
		}
		@Override
		protected DEREncodable choose(DEREncodable object, DEREncodable parent, Map<String, Object> args) {
			return parent;
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static class ASN1PathSelector extends ASN1Selector<DEREncodable[]> {
		@Override
		protected boolean check(DEREncodable object, DEREncodable parent, Map<String, Object> args) {
			DEREncodable[] path = (DEREncodable[]) args.get(ARG_CONSTRAINT);
			Class topClazz = path[0].getClass();
			if(topClazz.equals(object.getClass())) {
				path = (DEREncodable[])ArrayUtils.remove(path, 0);
				args.put(ARG_CONSTRAINT, path);
				return ArrayUtils.isEmpty(path);
			} else {
				scanner.closeBranch(args);
				return false;
			}
		}
		@Override
		protected DEREncodable choose(DEREncodable object, DEREncodable parent, Map<String, Object> args) {
			return object;
		}
	}
	
	public static class ASN1StructureSelector extends ASN1Selector<DEREncodable> {
		@Override
		protected boolean check(DEREncodable object, DEREncodable parent, Map<String, Object> args) {
			DEREncodable template = (DEREncodable) args.get(ARG_CONSTRAINT);
			return sameStructure(object, template);
		}
		@Override
		protected DEREncodable choose(DEREncodable object, DEREncodable parent, Map<String, Object> args) {
			return object;
		}
		@SuppressWarnings({ "unchecked" })
		private boolean sameStructure(DEREncodable current, DEREncodable template) {
			// an empty template means to ignore the branch nested into current and accept it 
			if(template.getClass() == DERNull.class) {
				return true;
			}
			// the two items must have the same type
			if(current.getClass() != template.getClass()) {
				return false;
			}
			
			// take the children from both the items
			List<DEREncodable> currChildren = new ArrayList<DEREncodable>();
			List<DEREncodable> tempChildren = new ArrayList<DEREncodable>();
			if(current instanceof ASN1TaggedObject) {
				currChildren.add( ((ASN1TaggedObject)current).getObject() );
				tempChildren.add( ((ASN1TaggedObject)template).getObject() );
			} else if(current instanceof DERSequence) {
				currChildren = Collections.list(((DERSequence)current).getObjects());
				tempChildren = Collections.list(((DERSequence)template).getObjects());
			} else if(current instanceof DERSet) {
				currChildren = Collections.list(((DERSet)current).getObjects());
				tempChildren = Collections.list(((DERSet)template).getObjects());
			}
			
			// expected the same number of children
			if(currChildren.size() != tempChildren.size()) {
				return false;
			}
			// recursion on the children, they must all be successful
			for(int i=0; i<currChildren.size(); i++) {
				if( sameStructure( currChildren.get(i), tempChildren.get(i) ) == false ) {
					return false;
				}
			}
			
			// the branch nested into the current node is isomorph to the template 
			return true;
		}
	}
	
	class ASN1ScannerImpl extends ASN1Scanner {
		@Override
		protected void process(DEREncodable object, DEREncodable parent, Map<String, Object> args) {
			boolean multiple = (Boolean)args.get(ARG_MULTIPLE);
			if(ASN1Selector.this.check(object, parent, args)) {
				@SuppressWarnings("unchecked")
				List<DEREncodable> matches = (List<DEREncodable>)args.get(ARG_RESULT);
				matches.add(ASN1Selector.this.choose(object, parent, args));
				if(multiple == false) {
					getScanController(args).halt();
				}
			}
		}
	}	

	public static final String ARG_CONSTRAINT = "constraint";
	public static final String ARG_MULTIPLE = "multiple"; 
	public static final String ARG_RESULT = "matches";
	
	public static DEREncodable getByOidLabel(DEREncodable rootEnc, ASN1ObjectIdentifier oid) {
		return new ASN1OidLabelSelector().get(rootEnc, oid);
	}
	public static DEREncodable getByPath(DEREncodable rootEnc, DEREncodable[] path) {
		return new ASN1PathSelector().get(rootEnc, path);
	}
	public static DEREncodable[] findByStructure(DEREncodable rootEnc, DEREncodable template) {
		return new ASN1StructureSelector().find(rootEnc, template);
	}
	
	ASN1ScannerImpl scanner;
	
	protected ASN1Selector() {
		scanner = new ASN1ScannerImpl();
	}

	public synchronized DEREncodable get(DEREncodable root, Constraint constraint) {
		List<DEREncodable> matches = search(root, constraint, false);
		DEREncodable result = null; 
		if(matches.size() > 0) {
			result = matches.get(0);
		}
		return result;
	}
	
	public DEREncodable[] find(DEREncodable root, Constraint constraint) {
		List<DEREncodable> matches = search(root, constraint, true); 
		return matches.toArray(new DEREncodable[matches.size()]);
	}
	
	private List<DEREncodable> search(DEREncodable root, Constraint constraint, boolean multiple) {
		Map<String, Object> args = new HashMap<String, Object>();
		List<DEREncodable> matches = new ArrayList<DEREncodable>();
		args.put(ARG_CONSTRAINT, constraint);
		args.put(ARG_RESULT, matches);
		args.put(ARG_MULTIPLE, multiple);
		scanner.scan(root, args);
		return matches;
	}
	
	protected abstract boolean check(DEREncodable object, DEREncodable parent, Map<String, Object> args);
	protected abstract DEREncodable choose(DEREncodable object, DEREncodable parent, Map<String, Object> args);
	
	
}