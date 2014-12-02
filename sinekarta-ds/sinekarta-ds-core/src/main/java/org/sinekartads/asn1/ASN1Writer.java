package org.sinekartads.asn1;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEREncodable;
import org.sinekartads.util.HexUtils;

public abstract class ASN1Writer extends Writer {
	
	public static class ConsoleASN1Writer extends ASN1Writer {

		public ConsoleASN1Writer() { }
		
		public ConsoleASN1Writer(String levelGap, String objectPrefix, String valuePrefix) {
			super(false, "\t", "- ", " ");
		}
		
		@Override
		public void write(char[] chars, int from, int to)
				throws IOException {
			System.out.println(new String(chars, from, to));
		}

		@Override
		public void flush() throws IOException { }

		@Override
		public void close() throws IOException { }
		
	}
	
	
	
	public static class LoggerASN1Writer extends ASN1Writer {

		private final Logger logger;
		
		public LoggerASN1Writer(Logger logger) {
			this(false, "\t", "- ", " ", logger);
		}
		
		public LoggerASN1Writer(boolean addNewLines, String levelGap, String objectPrefix, String valuePrefix, Logger logger) {
			super(addNewLines, levelGap, objectPrefix, valuePrefix);
			this.logger = logger;
		}
		
		@Override
		public void write(char[] chars, int from, int to) {
			logger.info(new String(chars, from, to));
		}

		@Override
		public void flush() { }

		@Override
		public void close() { }
		
	}
	
	
	
	public static class StringASN1Writer extends ASN1Writer {

		private boolean autoreset;
		private StringBuilder buf;
		
		public StringASN1Writer() {
			this(true, "\t", "- ", " - ", true);
		}
		
		public StringASN1Writer(boolean addNewLines, String levelGap, String objectPrefix, String valuePrefix, boolean autoreset) {
			super(addNewLines, levelGap, objectPrefix, valuePrefix);
			buf = new StringBuilder();
			this.autoreset = autoreset;
		}
		
		@Override
		public void write(char[] chars, int from, int to) {
			buf.append(new String(chars, from, to));
		}

		@Override
		public void flush() { }

		@Override
		public void close() { }
		
		public String readContent() {
			String content = buf.toString();
			if(autoreset) {
				reset();
			}
			return content;
		}
		
		public void reset() {
			buf = new StringBuilder();
		}
		
	}
	
	

	
	class ASN1ScannerImpl extends ASN1Scanner {
		@Override
		protected void process(DEREncodable current, DEREncodable parent, Map<String, Object> args) {
			Object value = args.get(ARG_VALUE);
			Integer level = getLevel(args);
			StringBuilder buf = new StringBuilder(); 
			if(addNewLines) {
				buf.append("\n");
			}
			for( ; level>0; level--) {
				buf.append(levelGap);
			}
			buf.append(objectPrefix);
			buf.append("(").append(current.getClass().getCanonicalName()).append(")");
			buf.append(valuePrefix);
			if(value != null) {
				if(value instanceof byte[]) {
					buf.append(HexUtils.encodeHex((byte[])value));
				} else {
					buf.append(value.toString()); 
				}
			} 
			try {
				ASN1Writer.this.write(buf.toString());
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		@Override
		protected void analyze(DEREncodable current, DEREncodable parent, Map<String, Object> args) {
			if(current instanceof ASN1ObjectIdentifier) {
				args.put(ARG_VALUE, ASN1Utils.friendlyASN1ObjectIdentifier((ASN1ObjectIdentifier)current));
				process(current, parent, args);
			} else {
				super.analyze(current, parent, args);
			}
		}
	}
	
	private boolean addNewLines;
	private String levelGap;
	private String objectPrefix;
	private String valuePrefix;	
	private ASN1Scanner scanner = new ASN1ScannerImpl();

	public ASN1Writer() {
		this(false, "\t", "- ", " ");
	}
	
	public ASN1Writer(boolean addNewLines, String levelGap, String objectPrefix, String valuePrefix) {
		this.addNewLines = addNewLines;
		this.levelGap = levelGap;
		this.objectPrefix = objectPrefix;
		this.valuePrefix = valuePrefix;
	}
			
	public void write(DEREncodable object) throws IOException {
		try {
			scanner.scan(object);
		} catch(RuntimeException e) {
			if(e.getCause() instanceof IOException) {
				throw (IOException)e.getCause();
			} else {
				throw e;
			}
		}
	}

	
	public void write(byte[] encoded) throws IOException {
		try {
			scanner.scan(ASN1Utils.readObject(encoded));
		} catch(RuntimeException e) {
			if(e.getCause() instanceof IOException) {
				throw (IOException)e.getCause();
			} else {
				throw e;
			}
		}
	}
}