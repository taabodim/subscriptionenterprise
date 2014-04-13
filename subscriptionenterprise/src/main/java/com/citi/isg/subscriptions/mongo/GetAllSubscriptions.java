package com.citi.isg.subscriptions.mongo;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.citi.isg.notification.soi.SubscriptionDao;
import com.mongodb.BasicDBList;

public class GetAllSubscriptions implements Callable {

	final Logger logger = LoggerFactory.getLogger(SaveSubscriptionInMongo.class);

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("*!*" + getClass().getSimpleName() + ": payload is "
					+ eventContext.getMessage().getPayload().toString() + " payload type is "
					+ eventContext.getMessage().getPayload().getClass());
		}
		BasicDBList listOfSubs = getAllSubscriptions();
		eventContext.getMessage().setPayload(listOfSubs);
		return eventContext.getMessage().getPayload().toString();
	}

	private BasicDBList getAllSubscriptions() {

		BasicDBList allSubscriptions = subscriptionDao.findAllSubscription();
		if (logger.isDebugEnabled()) {
			logger.debug("*!*" + getClass().getSimpleName() + ": allSubscriptions size is " + allSubscriptions.size());
		}
		return allSubscriptions;

	}

	private SubscriptionDao subscriptionDao;

	public SubscriptionDao getSubscriptionDao() {
		return subscriptionDao;
	}

	public void setSubscriptionDao(SubscriptionDao subscriptionDao) {
		this.subscriptionDao = subscriptionDao;
	}

}
