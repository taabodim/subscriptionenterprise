package com.citi.isg.notification.subscriptions.tests;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class SimpleTestsIT extends BaseTests {

	@Test
	public void testGetAllSubscriptions() throws Exception {
		for (int i = 0; i < 10000; i++) {

			URL url = new URL(serverUrl + "getAllSubscriptions");
			HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("POST");
			OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());

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
			BasicDBList dblist = (BasicDBList) JSON.parse(response.toString());
			Iterator<Object> it = dblist.iterator();
			while (it.hasNext()) {
				BasicDBObject sub = (BasicDBObject) it.next();

				System.out.println("mySubscription " + sub.getString("Name"));
				// +": \n" + sub.toString());
			}

			String responseMessage = httpCon.getResponseMessage();

			BasicDBList jsonResponse = (BasicDBList) JSON.parse(response.toString());

			out.close();

			System.out.println("Output from Server .... \n");
			System.out.println(response.toString());

		}

		// Assert.assertNotNull(response.toString());

	}

	@Test
	public void testGetSubscriptionByName() throws Exception {
		String name = "myTest";
		saveSubscription(name);
		String response = getSubscriptionByName(name);
		System.out.println("Output from Server .... \n");
		System.out.println(response);
		Assert.assertNotNull(response);
		deleteSubscription(name);
	}

	@Test
	public void testGetSubscriptionSOI() throws Exception {
		String name = "myTest";
		saveSubscription(name);
		String SOIPath = "full___Xref___FII";
		saveSubscriptionSOI(name, SOIPath);

		URL url = new URL(serverUrl + "getSubscriptionSOI");
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setRequestMethod("POST");
		OutputStream out = httpCon.getOutputStream();

		BasicDBObject bsonObject = new BasicDBObject("Name", name);
		bsonObject.put("SOIPath", SOIPath);

		out.write(bsonObject.toString().getBytes());
		out.flush();

		int responseCode = httpCon.getResponseCode();

		if (responseCode != 200) {
			System.out.println("httpCon.getResponseMessage() is " + httpCon.getResponseMessage()
					+ " \nFailed : HTTP error code : " + responseCode);
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

		deleteSubscription(name);

	}

	@Test
	public void testSaveSubscriptionSOI() throws Exception {
	
		for (int i = 0; i < 10000; i++) {
			System.out.println("testing " + i);
			String name = "Test40";
			String SOIPath = "Xref.ISN";
			saveSubscriptionSOI(name, SOIPath);
			deleteSubscription(name);

		}
	}

	@Test
	public void testSaveSubscriptionSOIWhenTheSubscriptionDoesntExist() throws Exception {
		String subName = "MySubscriptionTestFromJapanDoesntExisasdlkj123098213t";
		URL url = new URL(serverUrl + "saveSubscriptionSOI");
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);

		httpCon.setRequestMethod("POST");
		httpCon.setRequestProperty("Content-Type", "application/json");

		BasicDBObject bsonObject = new BasicDBObject("Name", subName);
		bsonObject.put("SOIPath", "Xref.ISN");

		BasicDBList soiValuesList = new BasicDBList();
		soiValuesList.add("BADSPIR00002");
		soiValuesList.add("123123");
		soiValuesList.add("129999SASD");

		bsonObject.put("SOIValues", soiValuesList);

		OutputStream out = httpCon.getOutputStream();

		System.out.println("bsonObject is " + bsonObject.toString());
		out.write(bsonObject.toString().getBytes());

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

		System.out.println("Output from Server .... \n");
		System.out.println(response.toString());
		Assert.assertNotNull(response.toString());

	}

	@Test
	public void testRemoveSOICodesFromAnExistingSOICollection() throws Exception {
		// this test will remove whatever SOI codes that are passed
		String name = "MyTest";
		saveSubscription(name);
		String SOIPath = "Xref.ISN2";
		saveSubscriptionSOI(name, SOIPath);

		URL url = new URL(serverUrl + "removeSOICodesFromAnExistingSOICollection");
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);

		httpCon.setRequestMethod("POST");
		BasicDBObject bsonObject = new BasicDBObject("Name", "MyTest");
		bsonObject.put("SOIPath", "Xref.ISN2");
		BasicDBList dbList = new BasicDBList();
		dbList.add("BADSPIR00002");
		bsonObject.put("SOIValues", dbList);

		OutputStream out = httpCon.getOutputStream();

		out.write(bsonObject.toString().getBytes());

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

		System.out.println("Output from Server .... \n");
		System.out.println(response.toString());
		Assert.assertNotNull(response.toString());
		deleteSubscription(name);
	}

	@Test
	public void testDeleteSubscriptionSOI() throws Exception {
		String name = "MyTest";
		saveSubscription(name);
		String SOIPath = "Xref.ISN2";
		saveSubscriptionSOI(name, SOIPath);
		URL url = new URL(serverUrl + "deleteSubscriptionSOI");
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setRequestMethod("POST");
		OutputStream out = httpCon.getOutputStream();

		BasicDBObject bsonObject = new BasicDBObject("Name", name);
		bsonObject.put("SOIPath", SOIPath);

		out.write(bsonObject.toString().getBytes());
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

		deleteSubscription(name);

	}

	@Test
	public void testDeleteSubscription() throws Exception {
		String name = "MyTest";
		saveSubscription(name);
		String response = deleteSubscription(name);
		System.out.println("Output from Server .... \n");
		System.out.println(response);
		Assert.assertNotNull(response);
		Assert.assertEquals(((DBObject) JSON.parse(response)).get("Status"), "Success");

	}

}
