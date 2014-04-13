package com.citi.isg.subscriptions.mongo;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.citi.isg.notification.soi.SubscriptionDao;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

public class SaveSubscriptionInMongo implements Callable {

	final Logger logger = LoggerFactory.getLogger(SaveSubscriptionInMongo.class);

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {

		if (logger.isDebugEnabled()) {
			logger.debug("*!*" + getClass().getSimpleName() + ": payload is "
					+ eventContext.getMessage().getPayload().toString() + " payload type is "
					+ eventContext.getMessage().getPayload().getClass());
		}

		if (logger.isDebugEnabled()) {
			logger.debug("*!*" + getClass().getSimpleName() + ": about to call saveTheSubscriptionInDB  ");
		}

		String status = eventContext.getMessage().getProperty("Status", PropertyScope.OUTBOUND);

		if (status.equalsIgnoreCase("Success")) {

			BasicDBObject subcsription = (BasicDBObject) JSON.parse(eventContext.getMessage().getPayload().toString());
			saveTheSubscriptionInDB(subcsription);
			return (subcsription.get("Name") + toString() + " was saved successfully.");

		} else {

			if (logger.isErrorEnabled()) {
				String errorMsg = "*!*" + getClass().getSimpleName()
						+ ": Error happened on subscription document validation: = " + " - Original Message was "
						+ eventContext.getMessage().getPayload();
				logger.error(errorMsg);

			}
		}

		return eventContext.getMessage().getPayload().toString();
	}

	private void saveTheSubscriptionInDB(BasicDBObject subscription) {
		if (logger.isDebugEnabled()) {
			logger.debug("*!*SaveSOIComponent : saveTheSubscriptionInDB : subscription  is " + subscription.toString());
		}
		BasicDBObject query = new BasicDBObject();
		query.put("Name", subscription.get("Name"));
		subscriptionDao.upsertSubscription(query, subscription);

	}

	private SubscriptionDao subscriptionDao;

	public SubscriptionDao getSubscriptionDao() {
		return subscriptionDao;
	}

	public void setSubscriptionDao(SubscriptionDao subscriptionDao) {
		this.subscriptionDao = subscriptionDao;
	}

}
