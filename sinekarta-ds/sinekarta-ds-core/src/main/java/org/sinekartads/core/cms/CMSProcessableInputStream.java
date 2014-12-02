package org.sinekartads.core.cms;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.util.io.Streams;

//import org.bouncycastle.cms.CMSException;
//import org.bouncycastle.cms.CMSProcessable;
//import org.bouncycastle.util.io.Streams;

/**
 * Source ripped by org.bouncycastle.cms.CMSProcessableInputStream 
 * into bcprov-jdk16_146. <br/>
 * Weirdly, it had only package access.
 * @author org.bouncycastle
 */
public class CMSProcessableInputStream implements CMSProcessable {
    private InputStream input;
    private boolean used = false;

    public CMSProcessableInputStream(InputStream input) {
        this.input = input;
    }

    public InputStream getInputStream() {
        checkSingleUsage();
        return input;
    }

    public void write(OutputStream zOut) throws IOException, CMSException {
        checkSingleUsage();
        Streams.pipeAll(input, zOut);
        input.close();
    }

    public Object getContent() {
        return getInputStream();
    }

    private synchronized void checkSingleUsage() {
        if (used) {
            throw new IllegalStateException("CMSProcessableInputStream can only be used once");
        }
        used = true;
    }
}
