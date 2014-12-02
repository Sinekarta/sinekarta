package org.sinekartads.dto.jcl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import org.sinekartads.util.HexUtils;

public class HexOutputStream extends OutputStream implements Serializable {

	private static final long serialVersionUID = 327182581224029989L;
	
	
	public HexOutputStream ( ) {
		baos = new ByteArrayOutputStream ( );
	}
	
	private ByteArrayOutputStream baos;
	private String hex;
	
	public void close() {
		try {
			baos.close();
		} catch (IOException e) {
			// never thrown, byte array operation
			throw new RuntimeException(e);
		}
	}
	
	public void flush() {
		try {
			baos.flush();
		} catch (IOException e) {
			// never thrown, byte array operation
			throw new RuntimeException(e);
		}
	}
	
	public void write(byte[] b) {
		try {
			baos.write(b);
		} catch(IOException e) {
			// never thrown, byte array operation
			throw new RuntimeException(e);
		} finally {
			byte[] buf = baos.toByteArray();
			hex = HexUtils.encodeHex(buf);
		}
	}
	
	public void write(int b) {
		baos.write(b);
		byte[] buf = baos.toByteArray();
		hex = HexUtils.encodeHex(buf);
	}
	
	public void write(byte[] b, int off, int len) {
		baos.write(b, off, len);
		byte[] buf = baos.toByteArray();
		hex = HexUtils.encodeHex(buf);
	}
	
	public String getHex() {
		return hex;
	}
}