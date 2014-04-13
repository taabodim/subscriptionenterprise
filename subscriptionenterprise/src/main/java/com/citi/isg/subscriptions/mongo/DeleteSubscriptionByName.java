package com.citi.isg.subscriptions.mongo;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.citi.isg.notification.soi.SubscriptionDao;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

public class DeleteSubscriptionByName implements Callable {

	final Logger logger = LoggerFactory.getLogger(SaveSubscriptionInMongo.class);

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("*!*" + getClass().getSimpleName() + ": payload is "
					+ eventContext.getMessage().getPayload().toString() + " payload type is "
					+ eventContext.getMessage().getPayload().getClass());
		}
		BasicDBObject payloadObj = (BasicDBObject) JSON.parse(eventContext.getMessage().getPayload().toString());
		String name = (String) payloadObj.get("Name");

		BasicDBObject query = new BasicDBObject("Name", name);

		return deleteTheSubscription(query);
		
	}

	private String deleteTheSubscription(BasicDBObject query) {
		String msg="";
		if (logger.isDebugEnabled()) {
			logger.debug("*!*" + getClass().getSimpleName() + ":query  is " + query.toString());
		}
		if (subscriptionDao.removeSubscription(query) == true) {
			msg = "{\"Status\":\"Success\",\"Errors\":[\"No error, subscription was deleted successfully\"]}";
			if (logger.isDebugEnabled()) {
				logger.debug("*!*" + getClass().getSimpleName() + ": subscription for this query was removed query is "
						+ query.toString());
			}

			subscriptionDao.removeAllSubscriptionNameOccurrencesFromAllSubscriptionMapsInAllCollections(query);

		} else {
			msg = "{\"Status\":\"Error\",\"Errors\":[\"I was not able to find the the subscription with the name\"]}";

		}

		return msg;

	}

	private SubscriptionDao subscriptionDao;

	public SubscriptionDao getSubscriptionDao() {
		return subscriptionDao;
	}

	public void setSubscriptionDao(SubscriptionDao subscriptionDao) {
		this.subscriptionDao = subscriptionDao;
	}

}
