/*
 * Copyright (C) 2014 - 2015 Jenia Software.
 *
 * This file is part of Sinekarta-ds
 *
 * Sinekarta-ds is Open SOurce Software: you can redistribute it and/or modify
 * it under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sinekartads.smartcard.applet;

import org.apache.log4j.Logger;
import org.sinekartads.applet.BaseApplet;

public class TestApplet extends BaseApplet {

	private static final long serialVersionUID = -2886113966359858032L;
	private static final Logger tracer = Logger.getLogger(TestApplet.class);
	
	@Override
	public void init() {
		tracer.debug("TestApplet init.");
		super.init();
		executeJS("skds_test_alert", new Object[]{"Hello world"});
	}

	@Override
	public void destroy() {
		super.destroy();
		tracer.debug("TestApplet destroy.");
	}

	@Override
	public String execFunction(String function, String param) {
		tracer.debug("request for executing function : " + function + ", param : " + param);
		return "test return";
	}
	
}
