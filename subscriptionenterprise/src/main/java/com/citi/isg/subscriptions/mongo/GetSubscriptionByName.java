package com.citi.isg.subscriptions.mongo;

import java.io.StringWriter;

import org.apache.commons.httpclient.ContentLengthInputStream;
import org.apache.commons.io.IOUtils;
import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.citi.isg.notification.soi.SubscriptionDao;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

public class GetSubscriptionByName implements Callable {

	final Logger logger = LoggerFactory.getLogger(SaveSubscriptionInMongo.class);

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		
		if (eventContext.getMessage().getPayload().getClass().toString().equalsIgnoreCase("class org.apache.commons.httpclient.ContentLengthInputStream")) {
			ContentLengthInputStream payLoadInputStream = (ContentLengthInputStream) eventContext.getMessage()
					.getPayload();
			StringWriter writer = new StringWriter();
			IOUtils.copy(payLoadInputStream, writer);
			String theString = writer.toString();
			if (logger.isDebugEnabled()) {
				logger.debug("*!*" + getClass().getSimpleName()
						+ ": payload was input stream , was converted to string, now set to " + theString);
			}
			eventContext.getMessage().setPayload(theString);
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("*!*" + getClass().getSimpleName() + ": payload is "
					+ eventContext.getMessage().getPayload().toString() + " payload type is "
					+ eventContext.getMessage().getPayload().getClass());
		}

		BasicDBObject query = (BasicDBObject) JSON.parse(eventContext.getMessage().getPayload().toString());

		eventContext.getMessage().setProperty("Name", query.get("Name"), PropertyScope.INVOCATION);

		if (logger.isDebugEnabled()) {
			logger.debug("*!*" + getClass().getSimpleName() + ": Name is " + query.get("Name"));
		}

		BasicDBObject foundSubscription = getTheSubscription(query);
		if (foundSubscription == null) {
			if (logger.isErrorEnabled()) {
				logger.error("I was not able to find the the subscription with the name " + query.getString("Name")
						+ "\"]");

			}

			String msg = "{\"Status\":\"Error\",\"Errors\":[\"I was not able to find the the subscription with the name\"]}";

			eventContext.getMessage().setPayload(msg);
			eventContext.getMessage().setProperty("found", "no", PropertyScope.INVOCATION);
			return eventContext.getMessage();

		} else {

			eventContext.getMessage().setPayload(foundSubscription);
			eventContext.getMessage().setProperty("found", "yes", PropertyScope.INVOCATION);

		}

		return eventContext.getMessage().getPayload().toString();

	}

	private BasicDBObject getTheSubscription(BasicDBObject query) {

		if (logger.isDebugEnabled()) {
			logger.debug("*!*" + getClass().getSimpleName() + ": query  is " + query.toString());
		}
		return subscriptionDao.findSubscription(query);

	}

	private SubscriptionDao subscriptionDao;

	public SubscriptionDao getSubscriptionDao() {
		return subscriptionDao;
	}

	public void setSubscriptionDao(SubscriptionDao subscriptionDao) {
		this.subscriptionDao = subscriptionDao;
	}

}
