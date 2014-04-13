package com.citi.isg.notification.soi;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.util.JSON;

public class SaveSOIComponent implements Callable {

	private final Logger logger = LoggerFactory.getLogger(SaveSOIComponent.class);

	public Object onCall(MuleEventContext eventContext) {

		if (logger.isDebugEnabled()) {
			logger.debug("*!*SaveSOIComponent was called");
		}

		BasicDBObject payloadObj = (BasicDBObject) JSON.parse(eventContext.getMessage().getPayload().toString());
		String replyMsgContent;

		if (logger.isDebugEnabled()) {
			logger.debug("*!*SaveSOIComponent : hostnameHeader  is "
					+ eventContext.getMessage().getProperty("hostnameHeader", PropertyScope.INVOCATION));
		}
		String status = eventContext.getMessage().getProperty("Status", PropertyScope.OUTBOUND);
		String name = payloadObj.getString("Name");

		if (logger.isDebugEnabled()) {
			logger.debug("*!*SaveSOIComponent : status  is " + status);
			logger.debug("*!*SaveSOIComponent : name  is " + name);
		}
		if (status.equalsIgnoreCase("Success")) {
			if (logger.isDebugEnabled()) {
				logger.debug("*!*SaveSOIComponent : about to call insertSOI  ");
			}
			replyMsgContent = insertTheSOIValuesAndUpdateTheSOICollection(name, payloadObj);

		} else {
			replyMsgContent = "Error happened on subscription document validation";
			String errorMsg = "*!*SaveSOIComponent : Error happened on subscription document validation: = "
					+ " - Original Message was " + payloadObj;
			logger.error(errorMsg);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("*!*SaveSOIComponent : replyMsgContent  is " + replyMsgContent);
		}
		eventContext.getMessage().setProperty("replyMsg", replyMsgContent, PropertyScope.OUTBOUND);
		return eventContext.getMessage();
	}

	private String insertTheSOIValuesAndUpdateTheSOICollection(String name, BasicDBObject subscriptionSOIObj) {
		if (logger.isDebugEnabled()) {
			logger.debug("*!*SaveSOIComponent : insertTheSOIValuesAndUpdateTheSOICollection was called.");
		}
		String replyMsgContent;

		BasicDBObject query = new BasicDBObject("Name", name);

		if (logger.isDebugEnabled()) {
			logger.debug("*!*SaveSOIComponent : query  is " + query.toString());
		}
		BasicDBObject subscription = subscriptionDao.findSubscription(query);

		if (subscription != null) {

			if (logger.isDebugEnabled()) {
				logger.debug("*!*SaveSOIComponent : Filter matched. Let me check for SOI filter");
			}

			String transformedSOIPathAsCollection = subscriptionSOIObj.getString("SOIPath").replace(".", "___");

			// now upserting with new soi values
			BasicDBList allSOIValues = (BasicDBList) subscriptionSOIObj.get("SOIValues");
			if (logger.isDebugEnabled()) {
				logger.debug("*!*SaveSOIComponent : adding SOI values...");
			}

			DB db = subscriptionDao.getMongoConnector().getSubscriptionMongoClient()
					.getDB(subscriptionDao.getMongoConnector().getSubscriptionDatabaseName());
			db.requestStart();

			try {
				db.requestEnsureConnection();
				DBCollection collection = db.getCollection(transformedSOIPathAsCollection);

				for (int i = 0; i < allSOIValues.size(); i++) {
					String oneSOIValueAs_Id = (String) allSOIValues.get(i);
					subscriptionDao.updateTheSubscriptionMapInCollection(collection, oneSOIValueAs_Id,
							transformedSOIPathAsCollection, name);
				}
			} finally {
				db.requestDone();
			}

			BasicDBObject nameOfSubscription = new BasicDBObject();
			nameOfSubscription.put("Name", name);

			BasicDBObject addThisSOIValueToSoiCollection = new BasicDBObject("$addToSet", new BasicDBObject(
					"SOICollections", transformedSOIPathAsCollection));

			subscriptionDao.updateTheSubscription(nameOfSubscription, addThisSOIValueToSoiCollection);

			replyMsgContent = allSOIValues.size() + " SOI Values were updated successfully for subscription " + name;
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("*!*SaveSOIComponent : subscription  is not found");
			}
			replyMsgContent = "subscription " + name + " was not found.";

			if (logger.isInfoEnabled()) {
				logger.info("*!*SaveSOIComponent : Subscription Object was not found match. Discarting and going to the next destination");
			}

		}

		return replyMsgContent;
	}

	private SubscriptionDao subscriptionDao;

	public SubscriptionDao getSubscriptionDao() {
		return subscriptionDao;
	}

	public void setSubscriptionDao(SubscriptionDao subscriptionDao) {
		this.subscriptionDao = subscriptionDao;
	}

}
