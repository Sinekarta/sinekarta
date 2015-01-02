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
package org.sinekartads.applet;

import java.applet.Applet;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;

import netscape.javascript.JSObject;

import org.apache.log4j.Logger;

public abstract class BaseApplet extends Applet implements JobExecutorListener {

	private static final int TIME_TO_SLEEP = 30;
	private static final long serialVersionUID = -2886113966359858032L;
	private static final Logger tracer = Logger.getLogger(BaseApplet.class);

	public JSObject window;
	
	private MediaTracker tr;

	private Image currentImage;
	private Image busy; 
	private Image ready; 

	private Thread displayController;
	
	private AppletWorker worker;

	private boolean running = true;
	
	public abstract String execFunction(String function, String param);

	@Override
	public void init ( ) {
		super.init();
		tracer.debug("Initializing the applet.");
		window = JSObject.getWindow(this);
		// caricamento immagini busy e ready
		tr = new MediaTracker(this);
		tracer.debug("loading busy image");
	    busy = getImage(getCodeBase(), "busy2.gif");
	    tr.addImage(busy,0);
	    tracer.debug("loading ready image");
	    ready = getImage(getCodeBase(), "ready2.gif");
	    tr.addImage(ready,1);
	    tracer.debug("starting worker");
	    worker = new AppletWorker(this);
		// esecuzione (in asincrono) del lavoro dell'applet corrente
	    tracer.debug("executing worker load");
		worker.execute(new Runnable() {
			    public void run() {
			    	tracer.debug("displaying busy image");
			    	busy();
					tracer.debug("displaying ready image");
			    	ready();
			    }
			});

	    tracer.debug("creating display controller");
		displayController = new Thread(new Runnable() {
		    @SuppressWarnings("static-access")
			public void run() {
		    	tracer.debug("display controller start");
				while (running) {
					try {
						Thread.currentThread().sleep(TIME_TO_SLEEP);
						if (isReady()) {
							checkExecutionRequest();
						}
					} catch (InterruptedException e) {
						break;
					}
					repaint();
				}
				tracer.debug("display controller end");
		    }
		});
	    tracer.debug("starting display controller");
	    displayController.start();
	    tracer.debug("init done");
	}

	@Override
	public void destroy() {
		tracer.info("destroy request");
		running=false;
		worker.stop();
		super.destroy();
		tracer.info("destroy done");
	}

	/**
	 * ridisegna la applet sulla pagina html 
	 * utility method per la applet
	 */
	@Override
	public final void paint(Graphics g) {
		if (currentImage != null)
			g.drawImage(currentImage, 0, 0, this);
	}
	
	@Override
	public void stop() {
		tracer.info("stop requested");
		running=false;
		worker.stop();
		super.stop();
		tracer.info("stop done");
	}

	public final boolean checkExecutionRequest() {
		String execField = (String)executeJS("skds_checkDo", new Object[0]);
		if (execField.equalsIgnoreCase("do")) {
			tracer.debug("request for executing function, set busy");
			busy();
			tracer.debug("getting function");
			final String function = (String)executeJS("skds_getFunction", new Object[0]);
			tracer.debug("getting parameters");
			final String parms = (String)executeJS("skds_getParms", new Object[0]);
			tracer.debug("setting doing");
			executeJS("skds_setDoing", new Object[0]);
			tracer.debug("starting worker");
			worker.execute(new Runnable() {
				@Override
				public void run() {
					tracer.debug("executing function");
					String resp = execFunction(function, parms);
					tracer.debug("function return value : " + resp);
					tracer.debug("sending resp to html page");
					executeJS("skds_setResp", new Object[]{resp});
					tracer.debug("function executed");
				}
			});
			tracer.debug("worker started");
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * metodo generico per l'esecuzione di un metodo javascript nella pagina html
	 * @param jsMethod il nome del metodo
	 * @param jsParam l'unico parametro che il metodo riceve
	 */
	public final Object executeJS(String jsMethod, Object[] jsParam) {
//		tracer.info("calling js " + jsMethod + " using " + jsParam);
		try {
			return window.call(jsMethod, jsParam);
		} catch (Throwable e) {
			tracer.error("Exception invoking js method",e);
			return null;
		}
	}
	
	protected boolean isBusy() {
		return currentImage==busy;
	}
	
	/**
	 * metodo di utility per visualizzare l'immagine di busy
	 */
	private void busy() {
		tracer.debug("set busy image");
		currentImage=busy;
		repaint();
	}

	protected boolean isReady() {
		return currentImage==ready;
	}
	
	/**
	 * metodo di utility per visualizzare l'immagine di ready
	 */
	private void ready() {
		tracer.debug("set ready image");
		currentImage=ready;
		repaint();
	}

	@Override
	public void jobDone() {
		tracer.debug("job finished, setting ready and done function");
		ready();
		executeJS("skds_setDone", new Object[0]);
	}

}
