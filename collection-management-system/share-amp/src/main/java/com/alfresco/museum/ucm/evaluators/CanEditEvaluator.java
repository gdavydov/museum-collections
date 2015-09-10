package com.alfresco.museum.ucm.evaluators;

import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.config.Config;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.ServletUtil;
import org.springframework.extensions.surf.exception.ConnectorServiceException;
import org.springframework.extensions.surf.extensibility.impl.DefaultSubComponentEvaluator;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.ConnectorService;
import org.springframework.extensions.webscripts.connector.Response;
import org.springframework.extensions.webscripts.connector.ResponseStatus;
import org.springframework.util.StringUtils;

/**
 * Web script "document-metadata" contains some logic to determine if user has
 * write permission for node.<br/>
 * Corresponding boolean value is stored in model as "allowMetaDataUpdate"
 * property.<br/>
 * This evaluator is reimplementation of same logic in java.
 */
public class CanEditEvaluator extends DefaultSubComponentEvaluator {
	private static final String ALFRESCO_ENDPOINT_ID = "alfresco";
	
	private static Log logger = LogFactory.getLog(CanEditEvaluator.class);
	
	@Override
	public boolean evaluate(RequestContext context, Map<String, String> params) {
		String nodeRef = params.get("nodeRef");
		if (!StringUtils.isEmpty(nodeRef)) {
			String url = "/slingshot/doclib2/node/" + nodeRef.replace("://", "/");
			String root = getRootNode(context);
			// Repository mode
			url += "?libraryRoot=" + URLEncoder.encode(root);
			
			try {
				// setup the connection
				ConnectorService connectorService = context.getServiceRegistry().getConnectorService();
				String currentUserId = context.getUserId();
				HttpSession currentSession = ServletUtil.getSession(true);
				Connector connector = connectorService.getConnector(ALFRESCO_ENDPOINT_ID, currentUserId, currentSession);
				
				Response response = connector.call(url);
				
				if (response.getStatus().getCode() == ResponseStatus.STATUS_OK) {
					JSONObject responseJson = new JSONObject(new JSONTokener(response.getResponse()));
					JSONObject jsonNode = responseJson.getJSONObject("item").getJSONObject("node");
					boolean isLocked = jsonNode.getBoolean("isLocked");
					boolean isWriteable = jsonNode.getJSONObject("permissions").getJSONObject("user").getBoolean("Write");
					return !isLocked && isWriteable; 
				}
			} catch (ConnectorServiceException e) {
				logger.error("Can't retrieve node access rights details", e);
			} catch (JSONException e) {
				logger.error("Can't parse node access rights details", e);
			}
		}
		
		return false;
	}

	private String getRootNode(RequestContext context) {
		//configService.getGlobalConfig()
		String rootNode = "alfresco://company/home";
		Config repLibConfig = context.getServiceRegistry().getConfigService().getConfig("RepositoryLibrary");
		if (repLibConfig != null) {
			String rootNodeConfig = repLibConfig.getConfigElementValue("root-node");
			if (!StringUtils.isEmpty(rootNodeConfig)) {
				rootNode = rootNodeConfig;
			}
		}
		return rootNode;
	}
}
