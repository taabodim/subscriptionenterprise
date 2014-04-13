package com.citi.isg.notification.soi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.IOUtils;
import org.mule.api.MuleEventContext;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileExtractor implements org.mule.api.lifecycle.Callable {

	public String hostnameHeader;

	private final Logger logger = LoggerFactory.getLogger(FileExtractor.class);

	private String boundary;

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {

		MuleMessage msg = eventContext.getMessage();

		getTheHostnameAndBoundary(msg);
		MimeMessage mimeMsg = crateTheMIMEmsg(msg);
		String contentOfFile = extractAndValidateTheContentOfFile(eventContext, mimeMsg);

		eventContext.getMessage().setProperty("hostnameHeader", hostnameHeader, PropertyScope.INVOCATION);
		eventContext.getMessage().setPayload(contentOfFile);
		return eventContext.getMessage();
	}

	private String extractAndValidateTheContentOfFile(MuleEventContext eventContext, MimeMessage message)
			throws IOException, MessagingException, MuleException {
		String content = "";

		if (message.getContent() instanceof String) {
			content = (String) message.getContent();
		} else {
			InputStream contentInputStream = (InputStream) message.getContent();
			content = IOUtils.toString(contentInputStream, "UTF-8");
		}
		if (logger.isDebugEnabled() && content != null) {
			logger.debug("*!*FileExtractor : content of MIME message recieved : " + content.substring(0, 100) + "...");
		}

		content = lookForFileDataInContent(eventContext, content);

		return content;
	}

	private void getTheHostnameAndBoundary(MuleMessage msg) {
		String contentType = msg.getInboundProperty("Content-Type");
		boundary = contentType.substring(contentType.indexOf("boundary=") + 9);
		if (logger.isDebugEnabled()) {
			logger.debug("*!*FileExtractor : contentType is " + contentType);
		}
		String Host = msg.getInboundProperty("Host");
		hostnameHeader = Host.substring(0, Host.indexOf(":"));

	}

	private MimeMessage crateTheMIMEmsg(MuleMessage msg) throws Exception {
		Session s = Session.getDefaultInstance(new Properties());
		InputStream is = new ByteArrayInputStream(msg.getPayloadAsBytes());
		MimeMessage message = new MimeMessage(s, is);
		return message;
	}

	private String lookForFileDataInContent(MuleEventContext eventContext, String content) throws MuleException {

		String fileContent = content;
		String modified = content.replaceAll(boundary, "");
		int startIndex = modified.indexOf("{");
		int endIndex = modified.lastIndexOf("}");
		if (startIndex > -1 && endIndex > -1) {
			fileContent = modified.substring(startIndex, endIndex + 1);
		} else {
			logger.error("*!*FileExtractor :  content is bad :  " + fileContent);
			return "{\"content\":\"invalid json document\"}";
		}

		return fileContent;
	}

}