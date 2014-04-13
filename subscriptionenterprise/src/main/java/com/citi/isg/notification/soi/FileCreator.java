package com.citi.isg.notification.soi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.mule.api.annotations.param.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.citi.isg.notifications.lifecycle.Start;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

public class FileCreator {

	private final Logger logger = LoggerFactory.getLogger(FileCreator.class.toString());
	private String apphome;
	private String dirWay;

	public Object process(@Payload String msg) throws IOException {

		apphome = Start.getAppHome();
		if (logger.isDebugEnabled()) {
			logger.debug("*!*FileCreator : app.home is set to " + apphome);
		}

		if (apphome == null)
			dirWay = "src/main/app/docroot/SOIFolder";
		else
			dirWay = apphome + "/docroot/SOIFolder";

		if (logger.isDebugEnabled()) {
			logger.debug("*!*FileCreator : dirWay is set to " + dirWay);
		}

		deleteFilesOlderThanNdays(1);

		BasicDBObject payload = (BasicDBObject) JSON.parse(msg);

		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy-hhmmss");

		File file = new File(dirWay + "/" + payload.getString("Name") + "-" + payload.getString("SOIPath")
				+ sdf.format(new Date()) + ".txt");

		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(msg);
		bw.close();

		payload.put("fileName", file.getName());
		return payload.toString();
	}

	public String createTheFile(String msg) throws IOException {

		apphome = Start.getAppHome();

		if (apphome == null)
			dirWay = "src/main/app/docroot/SOIFolder";
		else
			dirWay = apphome + "/docroot/SOIFolder";

		if (logger.isDebugEnabled()) {
			logger.debug("*!*dirWay is set to " + dirWay);
		}

		deleteFilesOlderThanNdays(1);

		BasicDBObject payload = (BasicDBObject) JSON.parse(msg);

		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy-hhmmss");

		File file = new File(dirWay + "/" + payload.getString("Name") + "-" + payload.getString("SOIPath")
				+ sdf.format(new Date()) + ".txt");

		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(msg);
		bw.close();

		return file.getName().toString();
	}

	public void deleteFilesOlderThanNdays(int daysBack) {

		File directory = new File(dirWay);
		if (directory.exists()) {

			File[] listFiles = directory.listFiles();
			long purgeTime = System.currentTimeMillis() - (daysBack * 24 * 60 * 60 * 1000);
			for (File listFile : listFiles) {
				if (listFile.lastModified() < purgeTime) {
					if (!listFile.delete()) {
						if (logger.isDebugEnabled()) {
							logger.debug("*!*FileCreator :Unable to delete file: " + listFile);
						}
					}
				}
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("*!*FileCreator :Files were not deleted, directory " + dirWay + " does'nt exist!");
			}
		}
	}
}
