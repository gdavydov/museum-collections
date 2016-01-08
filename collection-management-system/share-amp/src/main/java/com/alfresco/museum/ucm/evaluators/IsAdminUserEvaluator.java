/**
 * 
 */
package com.alfresco.museum.ucm.evaluators;

/**
 * @author gdavydov
 *
 */
import org.alfresco.web.evaluator.BaseEvaluator;
import org.json.simple.JSONObject;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.webscripts.connector.User;

public class IsAdminUserEvaluator extends BaseEvaluator {
	 
	@Override
	public boolean evaluate(JSONObject jsonObject) {
		RequestContext rc = ThreadLocalRequestContext.getRequestContext();
		User user = rc.getUser();
 
		return (user != null && user.isAdmin());
	}
}
