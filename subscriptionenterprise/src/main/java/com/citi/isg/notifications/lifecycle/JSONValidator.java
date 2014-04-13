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

public class JSONValidator implements Callable {

	private static final Logger logger = Logger.getLogger(JSONValidator.class.getName());

	public Object onCall(MuleEventContext eventContext) throws Exception {

		if (logger.isInfoEnabled()) {
			logger.info("*!*validating the message of type muleMsg type is "
					+ eventContext.getMessage().getClass());
		}

		String json = eventContext.getMessage().getPayload().toString();

		JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
		JsonNode schemaNode = null;

		schemaNode = JsonLoader.fromPath(Start.getAppHome() + "/classes/subscriptionSOI-schema.json");

		JsonSchema schema = null;

		schema = factory.getJsonSchema(schemaNode);

		ProcessingReport report = null;

		JsonNode theJsonToValidate = JsonLoader.fromString(json);

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

			eventContext.getMessage().setProperty("Status", "Error", PropertyScope.OUTBOUND);
			eventContext.getMessage().setProperty("Errors", messages.toString(), PropertyScope.OUTBOUND);
			if (logger.isInfoEnabled()) {
				logger.info("*!*JSONValidator : Json was NOT validated , error is !" + messages.toString());
			}

		} else {

			if (logger.isInfoEnabled()) {
				logger.info("*!*JSONValidator : Json was validated subccessfully!");
			}

			eventContext.getMessage().setProperty("Status", "Success", PropertyScope.OUTBOUND);
		}

		return eventContext.getMessage();
	}

}
