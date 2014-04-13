package com.citi.isg.notifications.lifecycle;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class MongoConnector {

	final Logger logger = LoggerFactory.getLogger(MongoConnector.class);

	public MongoConnector() throws Exception {
	}

	public void init() throws Exception {

		try {
			MongoClientOptions.Builder builder = MongoClientOptions.builder();
			builder.connectionsPerHost(subscriptionsConnectionsPerHost);
			builder.autoConnectRetry(true);
			builder.description("Mongo Connector - Subscriptions");
			builder.threadsAllowedToBlockForConnectionMultiplier(subscriptionsThreadsAllowedToBlockForConnectionMultiplier);
			List<MongoCredential> credentialsList = new ArrayList<MongoCredential>();
			MongoCredential credential = MongoCredential.createMongoCRCredential(subscriptionUserName,
					subscriptionDatabaseName, subscriptionPassword.toCharArray());
			credentialsList.add(credential);
			subscriptionMongoClient = new MongoClient(new ServerAddress(subscriptionMongoHost, subscriptionMongoPort),
					credentialsList, builder.build());
		} catch (Exception e) {
			logger.error("Impossible to connect to the subscriptions mongo instance at " + subscriptionMongoHost + ":"
					+ subscriptionMongoPort);
			throw e;
		}
		if (logger.isInfoEnabled()) {
			logger.info("*!*Done with opening mongo databases.  Subscription Connection Options : "
					+ subscriptionMongoClient.getMongoOptions().toString());
			logger.info("*!*Subscription Connection Options : " + subscriptionMongoClient.getMongoOptions().toString());
		}

	}

	public Mongo getSubscriptionMongoClient() {
		return subscriptionMongoClient;
	}

	public void setSubscriptionMongoClient(Mongo subscriptionMongoClient) {
		this.subscriptionMongoClient = subscriptionMongoClient;
	}

	public String getSubscriptionMongoHost() {
		return subscriptionMongoHost;
	}

	public void setSubscriptionMongoHost(String subscriptionMongoHost) {
		this.subscriptionMongoHost = subscriptionMongoHost;
	}

	public int getSubscriptionMongoPort() {
		return subscriptionMongoPort;
	}

	public void setSubscriptionMongoPort(int subscriptionMongoPort) {
		this.subscriptionMongoPort = subscriptionMongoPort;
	}

	public String getSubscriptionDatabaseName() {
		return subscriptionDatabaseName;
	}

	public void setSubscriptionDatabaseName(String subscriptionDatabaseName) {
		this.subscriptionDatabaseName = subscriptionDatabaseName;
	}

	public String getSubscriptionCollectionName() {
		return subscriptionCollectionName;
	}

	public void setSubscriptionCollectionName(String subscriptionCollectionName) {
		this.subscriptionCollectionName = subscriptionCollectionName;
	}

	public int getSubscriptionsConnectionsPerHost() {
		return subscriptionsConnectionsPerHost;
	}

	public void setSubscriptionsConnectionsPerHost(int subscriptionsConnectionsPerHost) {
		this.subscriptionsConnectionsPerHost = subscriptionsConnectionsPerHost;
	}

	public int getSubscriptionsThreadsAllowedToBlockForConnectionMultiplier() {
		return subscriptionsThreadsAllowedToBlockForConnectionMultiplier;
	}

	public void setSubscriptionsThreadsAllowedToBlockForConnectionMultiplier(
			int subscriptionsThreadsAllowedToBlockForConnectionMultiplier) {
		this.subscriptionsThreadsAllowedToBlockForConnectionMultiplier = subscriptionsThreadsAllowedToBlockForConnectionMultiplier;
	}

	public String getSubscriptionPassword() {
		return subscriptionPassword;
	}

	public void setSubscriptionPassword(String subscriptionPassword) {
		this.subscriptionPassword = subscriptionPassword;
	}

	public String getSubscriptionUserName() {
		return subscriptionUserName;
	}

	public void setSubscriptionUserName(String subscriptionUserName) {
		this.subscriptionUserName = subscriptionUserName;
	}

	private Mongo subscriptionMongoClient;

	private String subscriptionMongoHost;
	private int subscriptionMongoPort;
	private String subscriptionDatabaseName;
	private String subscriptionCollectionName;
	private int subscriptionsConnectionsPerHost = 100;
	private int subscriptionsThreadsAllowedToBlockForConnectionMultiplier = 5;
	private String subscriptionPassword;
	private String subscriptionUserName;

	public void close() {

		if (subscriptionMongoClient != null) {
			subscriptionMongoClient.close();
		}
	}

}
