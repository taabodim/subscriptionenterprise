package com.citi.isg.notifications.lifecycle;

import java.lang.management.ManagementFactory;

import org.mule.api.MuleException;
import org.mule.api.lifecycle.Startable;
import org.mvel2.optimizers.OptimizerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Start implements Startable {

	@Override
	public void start() throws MuleException {
		System.out.println("ManagementFactory.getRuntimeMXBean().getName() : "+
				ManagementFactory.getRuntimeMXBean().getName());

	}

	private static String fileUploadFolder;
	private static String appHome;

	public static String getAppHome() {
		return appHome;
	}

	public void setAppHome(String appHome) {
		this.appHome = appHome;
	}

	final Logger logger = LoggerFactory.getLogger(Start.class);

	public void setSSLPassword(String sslpassword) {
		// We need this to workaround this: artf110696
		OptimizerFactory.setDefaultOptimizer("reflective");

	}

	public static String getFileUploadFolder() {

		return fileUploadFolder;
	}

	public void setFileUploadFolder(String fileUploadFolder) {
		Start.fileUploadFolder = fileUploadFolder;
	}

}
