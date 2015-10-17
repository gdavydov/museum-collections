package org.alfresco.web.scripts.forms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.alfresco.web.config.forms.FormConfigElement;
import org.alfresco.web.config.forms.Mode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.ServletUtil;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.surf.util.StringBuilderWriter;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.ConnectorContext;
import org.springframework.extensions.webscripts.connector.ConnectorService;
import org.springframework.extensions.webscripts.connector.HttpMethod;
import org.springframework.extensions.webscripts.connector.Response;
import org.springframework.extensions.webscripts.json.JSONWriter;

/**
 * This is Java-controller of ucm-form.get webscript. It is used to redirect
 * requests to "ucm-formdefinitions" webscript instead of "formdefinitions".
 */
public class UCMFormUIGet extends FormUIGet {
	private static final Log logger = LogFactory.getLog(UCMFormUIGet.class);

	protected static final String PARAM_INHERIT = "inherit";
	
	private ConnectorService connectorService;

	/**
	 * Duplicate of FormUIGet.generateModel except for connector url difference
	 * and "inherit" param.
	 */
	protected Map<String, Object> generateModel(String itemKind, String itemId, WebScriptRequest request,
			Status status, Cache cache) {
		Map<String, Object> model = null;

		// get mode and optional formId
		String modeParam = getParameter(request, MODEL_MODE, DEFAULT_MODE);
		String formId = getParameter(request, PARAM_FORM_ID);
		String inherit = getParameter(request, PARAM_INHERIT);
		Mode mode = Mode.modeFromString(modeParam);

		if (logger.isDebugEnabled())
			logger.debug("Showing " + mode + " form (id=" + formId + ") for item: [" + itemKind + "]" + itemId);

		// get the form configuration and list of fields that are visible (if
		// any)
		FormConfigElement formConfig = getFormConfig(itemId, formId);
		List<String> visibleFields = getVisibleFields(mode, formConfig);

		// get the form definition from the form service
		Response formSvcResponse = retrieveFormDefinition(itemKind, itemId, inherit, visibleFields, formConfig);
		if (formSvcResponse.getStatus().getCode() == Status.STATUS_OK) {
			model = generateFormModel(request, mode, formSvcResponse, formConfig);
		} else if (formSvcResponse.getStatus().getCode() == Status.STATUS_UNAUTHORIZED) {
			// set status to 401 and return null model
			status.setCode(Status.STATUS_UNAUTHORIZED);
			status.setRedirect(true);
		} else {
			String errorKey = getParameter(request, PARAM_ERROR_KEY);
			model = generateErrorModel(formSvcResponse, errorKey);
		}

		return model;
	}

	/**
	 * Duplicate of FormUIGet.retrieveFormDefinition except for connector url
	 * difference and "inherit" param.
	 */
	protected Response retrieveFormDefinition(String itemKind, String itemId, String inherit,
			List<String> visibleFields, FormConfigElement formConfig) {
		Response response = null;

		try {
			// setup the connection
			RequestContext requestContext = ThreadLocalRequestContext.getRequestContext();
			String currentUserId = requestContext.getUserId();
			HttpSession currentSession = ServletUtil.getSession(true);
			Connector connector = this.getConnectorService().getConnector(ENDPOINT_ID, currentUserId, currentSession);
			ConnectorContext context = new ConnectorContext(HttpMethod.POST, null, buildDefaultHeaders());
			context.setContentType("application/json");

			// call the form service
			response = connector.call("/api/ucm-formdefinitions", context,
					generateFormDefPostBody(itemKind, itemId, inherit, visibleFields, formConfig));

			if (logger.isDebugEnabled())
				logger.debug("Response status: " + response.getStatus().getCode());
		} catch (Exception e) {
			if (logger.isErrorEnabled())
				logger.error("Failed to get form definition: ", e);
		}

		return response;
	}

	/**
	 * Duplicate of parent generateFormDefPostBody implementation with "inherit"
	 * parameter added.
	 */
	protected ByteArrayInputStream generateFormDefPostBody(String itemKind, String itemId, String inherit,
			List<String> visibleFields, FormConfigElement formConfig) throws IOException {
		StringBuilderWriter buf = new StringBuilderWriter(512);
		JSONWriter writer = new JSONWriter(buf);

		writer.startObject();
		writer.writeValue(PARAM_ITEM_KIND, itemKind);
		// URLEncode/URLDecode would do better, but Alfresco approach is to trim :/
		writer.writeValue(PARAM_ITEM_ID, itemId.replace(":/", ""));
		writer.writeValue(PARAM_INHERIT, inherit.replace(":/", ""));

		List<String> forcedFields = null;
		if (visibleFields != null && visibleFields.size() > 0) {
			// list the requested fields
			writer.startValue(MODEL_FIELDS);
			writer.startArray();

			forcedFields = new ArrayList<String>(visibleFields.size());
			for (String fieldId : visibleFields) {
				// write out the fieldId
				writer.writeValue(fieldId);

				// determine which fields need to be forced
				if (formConfig.isFieldForced(fieldId)) {
					forcedFields.add(fieldId);
				}
			}

			// close the fields array
			writer.endArray();
		}

		// list the forced fields, if present
		if (forcedFields != null && forcedFields.size() > 0) {
			writer.startValue(MODEL_FORCE);
			writer.startArray();

			for (String fieldId : forcedFields) {
				writer.writeValue(fieldId);
			}

			writer.endArray();
		}

		// end the JSON object
		writer.endObject();

		if (logger.isDebugEnabled())
			logger.debug("Generated JSON POST body: " + buf.toString());

		// return the JSON body as a stream
		return new ByteArrayInputStream(buf.toString().getBytes());
	}

	/**
	 * Copy of private method from parent class.
	 */
	private static Map<String, String> buildDefaultHeaders() {
		Map<String, String> headers = new HashMap<String, String>(1, 1.0f);
		headers.put("Accept-Language", I18NUtil.getLocale().toString().replace('_', '-'));
		return headers;
	}

	public ConnectorService getConnectorService() {
		return connectorService;
	}

	public void setConnectorService(ConnectorService connectorService) {
		this.connectorService = connectorService;
	}
}
