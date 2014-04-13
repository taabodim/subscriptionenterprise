package com.citi.isg.notifications.lifecycle;

import org.apache.log4j.Logger;
import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.report.ProcessingMessage;
import com.github.fge.jsonschema.report.ProcessingReport;
import com.github.fge.jsonschema.util.JsonLoader;

public class SubscriptionValidator implements Callable {

	private static final Logger logger = Logger.getLogger(SubscriptionValidator.class.getName());

	public Object onCall(MuleEventContext eventContext) throws Exception {

		if (logger.isInfoEnabled()) {
			logger.info("*!*" + getClass().getSimpleName() + ": validating message - muleMsg type is "
					+ eventContext.getMessage().getClass());
		}

		String json = eventContext.getMessage().getPayload().toString();

		JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
		JsonNode schemaNode = null;

		schemaNode = JsonLoader.fromPath(Start.getAppHome() + "/classes/subscription-schema.json");

		JsonSchema schema = null;

		schema = factory.getJsonSchema(schemaNode);

		ProcessingReport report = null;

		JsonNode theJsonToValidate = JsonLoader.fromString(json);
		JsonNode result = null;
		
		report = schema.validate(theJsonToValidate);
		if (!report.isSuccess()) {
			StringBuffer messages = new StringBuffer();
			int counter = 0;
			for (final ProcessingMessage message : report) {
				if (counter++ > 0) {
					messages.append(",");
				}
				messages.append(message.asJson());
			}


			String success = "{\"Status\":\"Error\",\"Errors\": ["
				+ messages.toString() + "], \"originalMessage\":"
				+ json + "}";
			
			 result = JsonLoader.fromString(success);
			
			eventContext.getMessage().setProperty("Status", "Error", PropertyScope.OUTBOUND);
			eventContext.getMessage().setProperty("Errors", messages.toString(), PropertyScope.OUTBOUND);
			eventContext.getMessage().setPayload(result.toString());
			
			
			
			if (logger.isInfoEnabled()) {
				logger.info("*!*" + getClass().getSimpleName() + " : Json was NOT validated , error is !" + messages.toString());
				logger.info("*!*" + getClass().getSimpleName() + ":  payload is set to  " +eventContext.getMessage().getPayload().toString());
				
			}

		} else {

			if (logger.isInfoEnabled()) {
				logger.info("*!*" + getClass().getSimpleName() + ":  Json was validated subccessfully!");
			}
			result = JsonLoader
			.fromString("{\"Status\":\"Success\",\"originalMessage\":"
					+ json + "}");
			eventContext.getMessage().setProperty("Status", "Success", PropertyScope.OUTBOUND);
		}

		return eventContext.getMessage();
	}

}
