package com.citi.isg.notification.soi;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.citi.isg.notifications.lifecycle.MongoConnector;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class SOIListMaker implements Callable {

	private final Logger logger = LoggerFactory.getLogger(SOIListMaker.class.toString());
	private FileCreator fileCreator = new FileCreator();

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {

		String Name = eventContext.getMessage().getProperty("Name", PropertyScope.INVOCATION);
		String _SOIPath = eventContext.getMessage().getProperty("_SOIPath", PropertyScope.INVOCATION);
		String returnEverything = eventContext.getMessage().getProperty("returnEverything", PropertyScope.INVOCATION);
		if (logger.isDebugEnabled()) {
			logger.debug("*!*" + getClass().getSimpleName() + ": Name : " + Name);
			logger.debug("*!*" + getClass().getSimpleName() + ": _SOIPath :" + _SOIPath);
			logger.debug("*!*" + getClass().getSimpleName() + ": returnEverything :" + returnEverything);

		}
		BasicDBList SOIvalues = fetchSOIValues(Name, _SOIPath, null);

		BasicDBObject payloadobj = new BasicDBObject();
		payloadobj.put("Name", Name);
		payloadobj.put("SOIPath", _SOIPath);

		payloadobj.put("SOIValues", SOIvalues);

		if (returnEverything != null && returnEverything.equalsIgnoreCase("yes")) {
			// call from API
			// don't do anything

		} else {
			// call from UI
			String fileName = fileCreator.createTheFile(payloadobj.toString());
			payloadobj.put("fileName", fileName);

			if (SOIvalues.size() > 500) {
				payloadobj.put("BigSOIList", "YES");
				SOIvalues = fetchSOIValues(Name, _SOIPath, 500);
			} else {
				payloadobj.put("BigSOIList", "NO");
			}
		}
		payloadobj.put("SOIValues", SOIvalues);

		return payloadobj.toString();
	}

	private BasicDBList fetchSOIValues(String Name, String _SOIPath, Integer limit) {
		BasicDBList SOIvalues = new com.mongodb.BasicDBList();

		DBObject query = new BasicDBObject();
		BasicDBObject inObject = new BasicDBObject();
		BasicDBList listOnNames = new BasicDBList();
		listOnNames.add(Name);
		inObject.put("$in", listOnNames);
		query.put("SubscriptionMap", inObject);

		if (logger.isDebugEnabled()) {
			logger.debug("SOIListMaker is fetching the SOI values.please wait..." + "query is " + query.toString());
		}

		DB db = mongoConnector.getSubscriptionMongoClient().getDB(mongoConnector.getSubscriptionDatabaseName());

		DBCollection collection = db.getCollection(_SOIPath);
		DBCursor cursor = null;

		if (logger.isDebugEnabled()) {
			logger.debug("SOIListMaker is going to fetch all the SOI values based on this query " + query.toString());
		}
		cursor = collection.find(query);
		if (logger.isDebugEnabled()) {
			logger.debug("SOIListMaker fetched " + (cursor != null ? cursor.size() : " [ 0 ]") + " SOI Values.");
		}
		try {
			int i = 0;
			while (cursor.hasNext()) {

				BasicDBObject result = (BasicDBObject) cursor.next();
				i++;
				SOIvalues.add(result.get("_id"));
				if (limit != null && i >= limit) {
					break;
				}
			}
		} finally {
			cursor.close();
		}

		return SOIvalues;
	}

	public void setMongoConnector(MongoConnector mongoConnector) {
		this.mongoConnector = mongoConnector;
	}

	public MongoConnector getMongoConnector() {
		return mongoConnector;
	}

	private MongoConnector mongoConnector;

}
