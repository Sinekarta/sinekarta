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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.SynchronousQueue;

import org.apache.log4j.Logger;

/**
 * il metodo java lanciato dentro una applet da un javascript non viene eseguito con 
 * gli stessi criteri di sicurezza della applet.
 * per questo alla init della applet viene istanziato un worker (questa classe).
 * 
 * Un worker non e' altro che un thread (partito dalla applet) in attesa di eseguire qualcosa
 * Il qualcosa da eseguire e' un'istanza di Runnable
 * 
 * @author andrea.tessaro
 *
 */
public class AppletWorker {

	private static final Logger tracer = Logger.getLogger(AppletWorker.class);
	
	private JobExecutor jobExecutor = null;	

	/**
	 * alla costruzione del worker il thread deve partire e mettersi in attesa di qualcosa da fare.
	 */
	public AppletWorker(JobExecutorListener jobExecutorListener) {
		super();
		tracer.debug("creating AppletWorker");
		jobExecutor = new JobExecutor(jobExecutorListener);
		tracer.debug("AppletWorker created");
	}

	public void execute(Runnable job) {
		tracer.debug("AppletWorker executing job...");
		try {
			jobExecutor.execute(job);
		} finally {
			tracer.debug("AppletWorker job executed!");
		}
	}

	public void stop() {
		tracer.debug("AppletWorker stopping...");
		if (jobExecutor.isRunning()) {
			try {
				jobExecutor.execute(new Runnable() {
					@Override
					public void run() {
						tracer.debug("set running false...");
						jobExecutor.setRunning(false);
					}
				});
			} finally {
				tracer.debug("AppletWorker stopped!");
			}
		} else {
			tracer.debug("AppletWorker already stoppped...");
		}
	}

}

class JobExecutor extends Thread {
	
	private static final Logger tracer = Logger.getLogger(JobExecutor.class);
	
	private SynchronousQueue<Runnable> jobs;
	
	private boolean running;
	
	private JobExecutorListener jobExecutorListener; 

	public JobExecutor(JobExecutorListener jobExecutorListener) {
		super();
		tracer.debug("creating JobExecutor...");
		jobs = new SynchronousQueue<Runnable>();
		running=true;
		this.jobExecutorListener = jobExecutorListener;
		start();
		tracer.debug("JobExecutor created!");
	}

	@Override
	public void run() {
		while (isRunning()) {
			try {
				tracer.debug("waiting for job to be executed...");
				Runnable toRun = jobs.take();
				tracer.debug("i have a job, running in priviledged mode ");
				class MyPrivilegedAction implements PrivilegedAction<Void> {
					private Runnable toRun;

					public MyPrivilegedAction(Runnable toRun) {
						super();
						this.toRun = toRun;
					}

					public Void run() {
						tracer.debug("running job in priviledged mode...");
						toRun.run();
						tracer.debug("job done...");
						return null;
					}
				}
				MyPrivilegedAction action = new MyPrivilegedAction(toRun);
				AccessController.doPrivileged(action);
				jobExecutorListener.jobDone();
				tracer.debug("priviledged job done");
			} catch (InterruptedException e) {
				tracer.debug("wait for job interrupted");
				return;
			}
		}
	}
	
	public void execute(Runnable work) {
		if (isRunning()) {
			try {
				tracer.debug("submitting a new job");
				jobs.put(work);
				tracer.debug("job submitted");
			} catch (InterruptedException e) {
				tracer.debug("can not submit a new job :-(");
			}
		}
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

}
