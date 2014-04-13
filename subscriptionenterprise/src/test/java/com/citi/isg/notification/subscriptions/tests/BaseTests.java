package com.citi.isg.notification.subscriptions.tests;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.Assert;

import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

public class BaseTests {

	protected static String serverUrl = "http://localhost:8081/subscription/";
//	protected static String serverUrl = "http://lab7-dl380-2:8081/subscription/";
	// protected static String serverUrl =
	// "http://isgswntu6n1:8081/subscription/";
	// protected static String serverUrl =
	// "http://datacloudselfserviceapi.uat.icg.citigroup.net/subscription/getSubscriptionSOI";
	// protected static String serverUrl =
	// "http://lab7-dl380-2:8181/subscription/";
//	 protected static String serverUrl =
//	 "http://isgmwntp6n1:8081/subscription/";

	protected String getSubscriptionByName(String name) throws Exception {
		URL url = new URL(serverUrl + "getSubscriptionByName");
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setRequestMethod("POST");
		OutputStream out = httpCon.getOutputStream();
		
		String subscriptionStr ="{\"Name\":\"" + name + "\"}";
		BasicDBObject mySubscriptionObject = (BasicDBObject) JSON.parse(subscriptionStr);
		System.out.println("bsonObject is " + mySubscriptionObject.toString());
		
		out.write(mySubscriptionObject.toString().getBytes());
		out.flush();

		int responseCode = httpCon.getResponseCode();

		if (responseCode != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + responseCode);
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			System.out.println(inputLine);
			if (inputLine != null) {
				response.append(inputLine);
			}
		}
		in.close();

		out.close();

		return response.length() == 0 ? null : response.toString();

	}

	

	protected void saveSubscription(String name) throws Exception {
		System.out.println(" serverl Url is " + serverUrl);
		URL url = new URL(serverUrl + "saveSubscription");
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setRequestMethod("POST");
		OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());

		String subscriptionStr = "{ \"Name\" : \""
			+ name
			+ "\", \"IsActive\" : false, \"DestinationType\" : \"EMS\", "
			+ "\"EMS_Details\" : { \"TopicName\" : \"com.citi.isg.jp.product\", \"ConnectorName\" : \"TibcoEMSLabSit\" }, "
			+ "\"MessageFilter\" : { \"FilterQuery\" : \"{'Product.Country': 'JP'}\", \"FieldSelection\" : [ ], \"DataSource\" : \"amc\", \"DataType\" : [\"account\"], \"FilterQueryDelta\" :  \"{}\" }, "
			+ "\"MessageFormatter\" : { \"ContentType\":\"FullConcepts\",\"TypeName\" : \"json\", \"ContentOptions\" : \"FullConcepts\" },\"ContactEmails\" : [],\"SOICollections\" : [] }";

		BasicDBObject mySubscriptionObject = (BasicDBObject) JSON.parse(subscriptionStr);
		System.out.println("bsonObject is " + mySubscriptionObject.toString());
		out.write(mySubscriptionObject.toString());

		out.close();

		int responseCode = httpCon.getResponseCode();

		if (responseCode != 200) {
			System.out.println("httpCon.getResponseMessage() is "+httpCon.getResponseMessage()+" \nFailed : HTTP error code : "
					+ responseCode);
			throw new RuntimeException("Failed : HTTP error code : " + responseCode);
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			System.out.println(inputLine);
			response.append(inputLine);
		}
		in.close();

		String responseMessage = httpCon.getResponseMessage();

		out.close();

		System.out.println("Output from Server .... \n");
		System.out.println(response.toString());
		Assert.assertNotNull(response.toString());

	}

	protected void saveSubscriptionSOI(String name, String SOIPath) throws Exception {
		saveSubscription(name);
		URL url = new URL(serverUrl + "saveSubscriptionSOI");
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);

		httpCon.setRequestMethod("POST");
		httpCon.setRequestProperty("Content-Type", "application/json");

		String myNewSoiWithRightFormat = "{" + "\"Name\": \"" + name + "\"," + "\"SOIPath\": \"" + SOIPath + "\","
				+ "\"SOIValues\":[\"BADSPIR00002\",\"01010101\",\"789456\",\"3432324423\",\"asd\"]" + "}";

		OutputStream out = httpCon.getOutputStream();

		System.out.println("bsonObject is " + myNewSoiWithRightFormat.toString());
		out.write(myNewSoiWithRightFormat.toString().getBytes());

		out.close();

		int responseCode = httpCon.getResponseCode();

		if (responseCode != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + responseCode);
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			System.out.println(inputLine);
			response.append(inputLine);
		}
		in.close();

		String responseMessage = httpCon.getResponseMessage();

		out.close();

		System.out.println("saveSubscriptionSOI :Output from Server .... \n");
		System.out.println(response.toString());
		Assert.assertNotNull(response.toString());

	}

	protected String deleteSubscription(String name) throws Exception {
		URL url = new URL(serverUrl + "deleteSubscription");
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setRequestMethod("POST");
		OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());

		BasicDBObject bsonObject = new BasicDBObject("Name", name);

		out.write(bsonObject.toString());
		out.flush();

		int responseCode = httpCon.getResponseCode();

		if (responseCode != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + responseCode);
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			System.out.println(inputLine);
			response.append(inputLine);
		}
		in.close();

		String responseMessage = httpCon.getResponseMessage();

		out.close();

		System.out.println("Output from Server .... \n");
		System.out.println(response.toString());
		Assert.assertNotNull(response.toString());

		return response.toString();
	}

}
