package com.citi.isg.subscriptions.mongo;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.citi.isg.notification.soi.SubscriptionDao;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

public class RemoveSubscriptionSOIs implements Callable {

	final Logger logger = LoggerFactory.getLogger(SaveSubscriptionInMongo.class);

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {

		BasicDBObject payloadObj =(BasicDBObject) JSON.parse(eventContext.getMessage().getPayload().toString());
		String Name = payloadObj.getString("Name");
		String SOIPath = payloadObj.getString("SOIPath");

		String _SOIPathAsCollectionName = SOIPath.replace(".", "___");
		if (logger.isDebugEnabled()) {
			logger.debug("*!*" + getClass().getSimpleName() + ": payload is "
					+ eventContext.getMessage().getPayload().toString() + " payload type is "
					+ eventContext.getMessage().getPayload().getClass());

			logger.debug("*!*" + getClass().getSimpleName() + ": Name is " + Name);
			logger.debug("*!*" + getClass().getSimpleName() + ": _SOIPathAsCollectionName is "
					+ _SOIPathAsCollectionName);
		}

		// subscription sois are deleted in 3 stages
		
		// 1.updating the subscription with new SOICollections (removing the soi
		// path value from it)
		// 2.removing empty subscription maps
		// 3. removing the subscription name from subscription maps in the right
		// collection

	
		String subsctionName = Name;
		String mySOIPath = _SOIPathAsCollectionName;
		subscriptionDao.removeSOIPathFromSOICollectionInSubscription(subsctionName, mySOIPath);
		
		subscriptionDao.removeAllSubscriptionNameOccurrencesFromAllSubscriptionMapsInThisCollections(
				_SOIPathAsCollectionName, Name);
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
