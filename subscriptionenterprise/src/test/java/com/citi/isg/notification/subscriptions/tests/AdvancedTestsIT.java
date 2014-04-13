package com.citi.isg.notification.subscriptions.tests;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

public class AdvancedTestsIT extends BaseTests {

	@Test
	public void testGetSubscriptionByNameFailingBecauseIDidNotFindAnything() throws Exception {
		String name = "MyTest";
		String result = getSubscriptionByName(name);
		BasicDBObject object = (BasicDBObject) JSON.parse(result);
		System.out.println("result is " + object.toString());
		Assert.assertEquals(object.get("Status"), "Error");
	}

	@Test
	public void testSaveSubscriptionSOIWhenTheBadSOIFormat() throws Exception {
		String name = "MyTest";
		saveSubscription(name);
		String SOIPath = "Xref.ISN";
		URL url = new URL(serverUrl + "saveSubscriptionSOI");
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);

		httpCon.setRequestMethod("POST");
		httpCon.setRequestProperty("Content-Type", "application/json");

		String myNewSoiWithBadFormat = "{" + "\"Name\": \"" + name + "\"," + "\"SOIPath\": \"" + SOIPath + "\","
				+ "\"SOIValuesBadFormat\":[\"BADSPIR00002\",\"123123\",\"789456\",\"3432324423\",\"asd\"]" + "}";

		OutputStream out = httpCon.getOutputStream();

		System.out.println("bsonObject is " + myNewSoiWithBadFormat.toString());
		out.write(myNewSoiWithBadFormat.toString().getBytes());

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
	public void testDeleteFailBecauseIDidNotFindSubscription() throws Exception {
		String name = "MyTest";
		String result = deleteSubscription(name);
		BasicDBObject object = (BasicDBObject) JSON.parse(result);
		System.out.println("result is " + object.toString());
		Assert.assertEquals(object.get("Status"), "Error");

	}

}
