package com.alfresco.museum.ucm;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.surf.ServletUtil;
import org.springframework.extensions.surf.UserFactory;
import org.springframework.extensions.surf.exception.ConnectorServiceException;
import org.springframework.extensions.surf.site.AuthenticationUtil;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.ConnectorContext;
import org.springframework.extensions.webscripts.connector.ConnectorService;
import org.springframework.extensions.webscripts.connector.HttpMethod;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletResponse;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

/**
 * This web script authenticates user as a "anonymous" user, and redirects to
 * new URL, which is generally equal to current URL with "/ucm-guest" part
 * removed. E.g.<br/>
 * http://localhost:8080/share/page/ucm-guest/site/test/documentlibrary<br/>
 * -><br/>
 * http://localhost:8080/share/page/site/test/documentlibrary<br/>
 * In this way anonymous users can bypass log in screen - they just need to use
 * this web script as a "proxy".
 */
public class UCMGuestAutoLogin extends AbstractWebScript {
	private static Log LOGGER = LogFactory.getLog(UCMGuestAutoLogin.class);

	public static final String ENDPOINT_ID = "alfresco";

	private UserFactory userFactory;
	private ConnectorService connectorService;

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		HttpServletRequest request = ((WebScriptServletRequest) req).getHttpServletRequest();
		HttpServletResponse response = ((WebScriptServletResponse) res).getHttpServletResponse();

		try {
			JSONObject userCredentialsJson = configureAnonymousUser();
			authenticate(request, response, userCredentialsJson);
		} catch (ConnectorServiceException | JSONException e) {
			String message = "Failed to configure anonymous user!";
			LOGGER.error(message, e);
		}
		// if authentication failed redirect user anyway so it can see login dialog
		redirect(req, response);
	}

	public JSONObject configureAnonymousUser() throws ConnectorServiceException, JSONException {
		String currentUserId = ThreadLocalRequestContext.getRequestContext().getUserId();
		HttpSession currentSession = ServletUtil.getSession(true);
		Connector connector = this.getConnectorService().getConnector(ENDPOINT_ID, currentUserId, currentSession);

		ConnectorContext context = new ConnectorContext(HttpMethod.GET);
		context.setContentType(MediaType.APPLICATION_JSON_VALUE);

		String jsonResponse = connector.call("/ucm/anonymous-user", context).getResponse();
		JSONObject userCredentialsJson = new JSONObject(new JSONTokener(jsonResponse));
		return userCredentialsJson;
	}

	public void authenticate(HttpServletRequest request, HttpServletResponse response, JSONObject userCredentialsJson)
			throws JSONException {
		String username = userCredentialsJson.getString("username");
		String password = userCredentialsJson.getString("password");
		// see if we can authenticate the user
		boolean authenticated = userFactory.authenticate(request, username, password);
		if (authenticated) {
			AuthenticationUtil.login(request, response, username, false);
		}
	}

	// /share/page/ucm-guest/site/test/documentlibrary ->
	// /share/page/site/test/documentlibrary
	public String buildRedirectPath(WebScriptRequest req) {
		// req.getContextPath() = "/share"
		// req.getParameter("pageId") = "site/test/documentlibrary"
		// "/share" + "/page/" + "site/test/documentlibrary";
		String newPath = req.getContextPath() + "/page/" + req.getServiceMatch().getTemplateVars().get("pageId");
		if (!StringUtils.isEmpty(req.getQueryString())) {
			newPath += "?" + req.getQueryString();
		}
		return newPath;
	}

	public void redirect(WebScriptRequest req, HttpServletResponse response) throws IOException {
		String targetUrl = buildRedirectPath(req);
		// in this way original URL won't be saved in browser tab navigation
		// history
		response.sendRedirect(targetUrl);
	}

	public UserFactory getUserFactory() {
		return userFactory;
	}

	public void setUserFactory(UserFactory userFactory) {
		this.userFactory = userFactory;
	}

	public ConnectorService getConnectorService() {
		return connectorService;
	}

	public void setConnectorService(ConnectorService connectorService) {
		this.connectorService = connectorService;
	}
}
