package com.citi.isg.notification.soi;

import java.util.ArrayList;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.citi.isg.notifications.lifecycle.MongoConnector;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

/**
 * 
 * This component makes search on mongodb
 * 
 * @author lb12728
 * 
 */
public class SubscriptionDao {

	private final Logger logger = LoggerFactory.getLogger(SubscriptionDao.class);

	private MongoConnector mongoConnector;

	public MongoConnector getMongoConnector() {
		return mongoConnector;
	}

	public void setMongoConnector(MongoConnector mongoConnector) {
		this.mongoConnector = mongoConnector;
	}

	public BasicDBList findAllSubscription() {

		DB db = getMongoConnector().getSubscriptionMongoClient().getDB(
				getMongoConnector().getSubscriptionDatabaseName());
		db.requestStart();

		BasicDBList listOfSubscriptions = new BasicDBList();
		try {
			db.requestEnsureConnection();
			DBCollection collection = db.getCollection(getMongoConnector().getSubscriptionCollectionName());

			DBCursor result = collection.find();
			while (result.hasNext()) {
				BasicDBObject subscriptionFound = (BasicDBObject) result.next();
				listOfSubscriptions.add(subscriptionFound);
			}
		} finally {
			db.requestDone();
		}

		return listOfSubscriptions;
	}

	public BasicDBObject findSubscription(BasicDBObject query) {

		DB db = getMongoConnector().getSubscriptionMongoClient().getDB(
				getMongoConnector().getSubscriptionDatabaseName());
		db.requestStart();
		BasicDBObject res = null;
		try {
			db.requestEnsureConnection();
			DBCollection collection = db.getCollection(getMongoConnector().getSubscriptionCollectionName());

			DBCursor result = collection.find(query);
			if (result.hasNext()) {
				res = (BasicDBObject) result.next();
			}

		} finally {
			db.requestDone();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("*!*" + getClass().getSimpleName() + " findAllSubscription : "
					+ (res != null ? res.toString() : null));
		}
		return res;
	}

	public void updateTheSubscriptionMapInCollection(DBCollection collection, String oneSOIValueAs_Id,
			String transformedSOIPathAsCollection, String name) {

		BasicDBObject wantedSubscriptionMapObjectBasedOnId = new BasicDBObject("_id", oneSOIValueAs_Id);
		if (logger.isDebugEnabled()) {
			logger.debug("*!*SubscriptionDao : adding " + name + " in the " + oneSOIValueAs_Id
					+ " object's SubscriptionMap list in collection : " + transformedSOIPathAsCollection);
		}

		BasicDBObject addThisSubscriptionMapRecord = new BasicDBObject("$addToSet", new BasicDBObject(
				"SubscriptionMap", name));

		collection.update(wantedSubscriptionMapObjectBasedOnId, addThisSubscriptionMapRecord, true, false);

	}

	public void upsertSubscription(BasicDBObject query, BasicDBObject subscription) {
		DB db = getMongoConnector().getSubscriptionMongoClient().getDB(
				getMongoConnector().getSubscriptionDatabaseName());
		db.requestStart();

		try {
			db.requestEnsureConnection();
			DBCollection collection = db.getCollection(mongoConnector.getSubscriptionCollectionName());

			if (logger.isDebugEnabled()) {
				logger.debug("*!*" + getClass().getSimpleName() + "updating this Subscription " + query.toString());
			}
			collection.update(query, subscription, true, false);
		} finally {
			db.requestDone();
		}

	}

	public void removeAllEmptySubscriptionMap(String collectionName, BasicDBObject query) {
		DB db = getMongoConnector().getSubscriptionMongoClient().getDB(
				getMongoConnector().getSubscriptionDatabaseName());
		db.requestStart();

		try {
			db.requestEnsureConnection();
			DBCollection collection = db.getCollection(collectionName);
			DBCursor result = collection.find(query);
			while (result.hasNext()) {

				BasicDBObject mySubscription = (BasicDBObject) result.next();
				if (logger.isDebugEnabled()) {
					logger.debug("*!*" + getClass().getSimpleName()
							+ "removeAllEmptySubscriptionMap: removing SubscriptionMap in this collection "
							+ collectionName + "  " + mySubscription.toString());

				}
				collection.remove(mySubscription);

			}
		} finally {
			db.requestDone();
		}

	}

	public boolean removeSubscription(BasicDBObject query) {
		DB db = getMongoConnector().getSubscriptionMongoClient().getDB(
				getMongoConnector().getSubscriptionDatabaseName());
		db.requestStart();

		try {
			db.requestEnsureConnection();
			DBCollection collection = db.getCollection(getMongoConnector().getSubscriptionCollectionName());

			BasicDBObject subscription = findSubscription(query);
			if (subscription != null) {
				collection.remove(subscription);
				if (logger.isDebugEnabled()) {
					logger.debug("*!*" + getClass().getSimpleName() + "removing subscription" + " "
							+ subscription.toString());
					return true;
				}
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("*!*" + getClass().getSimpleName() + " subscription was not found to be removed" + " "
							+ query.toString());
				}
				return false;
			}

		} finally {
			db.requestDone();
		}
		return false;
	}

	public void updateTheSubscription(BasicDBObject queryReference, BasicDBObject elementReference) {
		upsertSubscription(queryReference, elementReference);
	}

	public void removeAllSubscriptionNameOccurrencesFromAllSubscriptionMapsInAllCollections(BasicDBObject subscription) {
		DB db = getMongoConnector().getSubscriptionMongoClient().getDB(
				getMongoConnector().getSubscriptionDatabaseName());
		db.requestStart();
		BasicDBObject elementReference = (BasicDBObject) JSON.parse("{ $pull : {\"SubscriptionMap\":" + "\""
				+ subscription.get("Name") + "\"" + "} }");

		try {
			db.requestEnsureConnection();
			Set<String> collectionNames = db.getCollectionNames();
			for (String collection : collectionNames) {

				DBCollection collectionDB = db.getCollection(collection);
				DBCursor cursor = collectionDB.find();
				while (cursor.hasNext()) {
					BasicDBObject entry = (BasicDBObject) cursor.next();
					BasicDBList soiValues = (BasicDBList) entry.get("SubscriptionMap");
					if (entry.containsKey("SubscriptionMap") && soiValues.contains(subscription.get("Name"))) {
						if (logger.isDebugEnabled()) {
							logger.debug("*!*" + getClass().getSimpleName()
									+ " uptdating the subscriptionMap for this " + entry + " in this collection "
									+ collection);
						}
						collectionDB.update(entry, elementReference);
					}

				}

			}

		} finally {
			db.requestDone();
		}

	}

	public ArrayList<String> getAllCollectionsNotInTheList(BasicDBList allSoiCollections) {
		DB db = getMongoConnector().getSubscriptionMongoClient().getDB(
				getMongoConnector().getSubscriptionDatabaseName());
		db.requestStart();
		ArrayList<String> list = new ArrayList<String>();
		try {
			db.requestEnsureConnection();
			Set<String> collectionNames = db.getCollectionNames();
			for (String collection : collectionNames) {

				if (!allSoiCollections.contains(collection)
						&& !collection.equalsIgnoreCase(mongoConnector.getSubscriptionCollectionName()))
					list.add(collection);

			}

		} finally {
			db.requestDone();
		}
		return list;
	}

	public void removeAllSubscriptionNameOccurrencesFromAllSubscriptionMapsInThisCollections(String collectionName,
			String subscriptionName) {

		DB db = getMongoConnector().getSubscriptionMongoClient().getDB(
				getMongoConnector().getSubscriptionDatabaseName());
		db.requestStart();
		BasicDBObject elementReference = (BasicDBObject) JSON.parse("{ $pull : {\"SubscriptionMap\":" + "\""
				+ subscriptionName + "\"" + "} }");

		try {
			db.requestEnsureConnection();

			DBCollection collectionDB = db.getCollection(collectionName);
			DBCursor cursor = collectionDB.find();
			while (cursor.hasNext()) {
				BasicDBObject entry = (BasicDBObject) cursor.next();
				BasicDBList soiValues = (BasicDBList) entry.get("SubscriptionMap");
				if (entry.containsKey("SubscriptionMap") && soiValues.contains(subscriptionName)) {
					if (logger.isDebugEnabled()) {
						logger.debug("*!*" + getClass().getSimpleName() + " uptdating the subscriptionMap for this "
								+ entry + " in this collection " + collectionName + "with this query "
								+ elementReference.toString());
					}
					collectionDB.update(entry, elementReference);
				}

			}

		} finally {
			db.requestDone();
		}

	}

	public void removeSOIPathFromSOICollectionInSubscription(String subsctionName, String sOIPath) {
		BasicDBObject queryReference = (BasicDBObject) JSON.parse("{\"Name\":\"" + subsctionName + "\"}");
		BasicDBObject elementReference = (BasicDBObject) JSON.parse("{ $pull : {\"SOICollections\":\"" + sOIPath
				+ "\"} }");
		DB db = getMongoConnector().getSubscriptionMongoClient().getDB(
				getMongoConnector().getSubscriptionDatabaseName());
		db.requestStart();

		try {
			db.requestEnsureConnection();

			DBCollection collectionDB = db.getCollection(getMongoConnector().getSubscriptionCollectionName());
			collectionDB.update(queryReference, elementReference, false, true);
			if (logger.isDebugEnabled()) {
				logger.debug("*!*" + getClass().getSimpleName() + " : removeSOIPathFromSOICollectionInSubscription : "
						+ "uptdating the subscription  " + subsctionName + "with this query "
						+ elementReference.toString());
			}
		} finally {
			db.requestDone();
		}
	}

	public void removeAllSSOICodesFromAnExistingSOICollections(DBCollection collectionDB,
			String _SOIPathAsCollectionName, String Name, String payloadAsSOIValueInSubscriptionMap) {
		if (logger.isDebugEnabled()) {
			logger.debug("*!*" + getClass().getSimpleName() + " : removeAllSSOICodesFromAnExistingSOICollections "
					+ " _SOIPathAsCollectionName :  " + _SOIPathAsCollectionName
					+ " payloadAsSOIValueInSubscriptionMap " + payloadAsSOIValueInSubscriptionMap + " Name " + Name);
		}

		BasicDBObject queryReference = (BasicDBObject) JSON
				.parse("{ $and: [ {\"SubscriptionMap\" : { $exists : true, $in:[ \"" + Name + "\" ] } } , {\"_id\":\""
						+ payloadAsSOIValueInSubscriptionMap + "\" } ] }");
		BasicDBObject elementReference = (BasicDBObject) JSON.parse("{ $pull : {\"SubscriptionMap\":" + "\"" + Name
				+ "\"} }");

		if (logger.isDebugEnabled()) {
			logger.debug("*!*" + getClass().getSimpleName() + " : removeAllSSOICodesFromAnExistingSOICollections "
					+ "queryReference :  " + queryReference.toString() + "with this elementReference "
					+ elementReference.toString());
		}

		DBCursor cursor = collectionDB.find(queryReference);
		while (cursor.hasNext()) {
			BasicDBObject entry = (BasicDBObject) cursor.next();
			if (logger.isDebugEnabled()) {
				logger.debug("*!*" + getClass().getSimpleName() + " uptdating the subscriptionMap for this " + entry
						+ " in this collection " + _SOIPathAsCollectionName + "with this query "
						+ elementReference.toString());
			}
			collectionDB.update(entry, elementReference);

		}

	}

}
