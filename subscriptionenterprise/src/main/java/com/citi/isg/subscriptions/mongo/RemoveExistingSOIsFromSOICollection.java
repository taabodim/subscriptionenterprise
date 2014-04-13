package com.citi.isg.subscriptions.mongo;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.citi.isg.notification.soi.SubscriptionDao;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.util.JSON;

public class RemoveExistingSOIsFromSOICollection implements Callable {

	final Logger logger = LoggerFactory.getLogger(SaveSubscriptionInMongo.class);

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {

		BasicDBObject payloadObj = (BasicDBObject) JSON.parse(eventContext.getMessage().getPayload().toString());
		String Name = payloadObj.getString("Name");
		String SOIPath = payloadObj.getString("SOIPath");
		BasicDBList SOIValues = (BasicDBList) payloadObj.get("SOIValues");
		String _SOIPathAsCollectionName = SOIPath.replace(".", "___");
		if (logger.isDebugEnabled()) {
			logger.debug("*!*" + getClass().getSimpleName() + ": payload is "
					+ eventContext.getMessage().getPayload().toString() + " payload type is "
					+ eventContext.getMessage().getPayload().getClass());

			logger.debug("*!*" + getClass().getSimpleName() + ": Name is " + Name);
			logger.debug("*!*" + getClass().getSimpleName() + ": _SOIPathAsCollectionName is "
					+ _SOIPathAsCollectionName);
			logger.debug("*!*" + getClass().getSimpleName() + ": SOIValues is " + SOIValues.toString());

		}

		// subscription sois are deleted in 3 stages
		// 1. removing the subscription name from subscription maps in the right
		// collection for every SOI Value
		// 2.removing empty subscription maps

		DB db = null;
		try {
			
			db = subscriptionDao.getMongoConnector().getSubscriptionMongoClient()
					.getDB(subscriptionDao.getMongoConnector().getSubscriptionDatabaseName());

			db.requestStart();
			db.requestEnsureConnection();
			
			DBCollection collectionDB = db.getCollection(_SOIPathAsCollectionName);
			if (logger.isDebugEnabled()) {
				logger.debug("*!*" + getClass().getSimpleName() + ": collectionDB is " + collectionDB.getName());
			}

			for (int i = 0; i < SOIValues.size(); i++) {

				String payloadAsSOIValueInSubscriptionMap = (String) SOIValues.get(i);
				if (logger.isDebugEnabled()) {
					logger.debug("*!*" + getClass().getSimpleName() + ": payloadAsSOIValueInSubscriptionMap is " + payloadAsSOIValueInSubscriptionMap);
				}
				subscriptionDao.removeAllSSOICodesFromAnExistingSOICollections(collectionDB, _SOIPathAsCollectionName,
						Name, payloadAsSOIValueInSubscriptionMap);

			}

		} finally {
			db.requestDone();
		}

		BasicDBObject queryTodeleteEmptySOIPath = (BasicDBObject) JSON
				.parse("{ \"SubscriptionMap\" : { $size  : 0 } }");
		subscriptionDao.removeAllEmptySubscriptionMap(_SOIPathAsCollectionName, queryTodeleteEmptySOIPath);

		return eventContext.getMessage();
	}

	private SubscriptionDao subscriptionDao;

	public SubscriptionDao getSubscriptionDao() {
		return subscriptionDao;
	}

	public void setSubscriptionDao(SubscriptionDao subscriptionDao) {
		this.subscriptionDao = subscriptionDao;
	}

}
