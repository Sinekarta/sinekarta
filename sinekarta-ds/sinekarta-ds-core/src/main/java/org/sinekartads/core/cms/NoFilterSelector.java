package org.sinekartads.core.cms;

import java.io.Serializable;
import java.security.cert.CertSelector;
import java.security.cert.Certificate;

import org.bouncycastle.util.Selector;
import org.sinekartads.util.TemplateUtils;

public class NoFilterSelector implements CertSelector, Selector, Serializable {

	private static final long serialVersionUID = 6283627707844158782L;

	@Override
	public boolean match(Certificate cert) {
		return true;
	}
	
	@Override
	public boolean match(Object obj) {
		return true;
	}

	public Selector clone() {
		return TemplateUtils.Instantiation.clone(this);
	}
}
