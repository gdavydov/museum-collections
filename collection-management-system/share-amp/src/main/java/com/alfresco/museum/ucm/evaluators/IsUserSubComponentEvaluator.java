package com.alfresco.museum.ucm.evaluators;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.extensibility.impl.DefaultSubComponentEvaluator;

public class IsUserSubComponentEvaluator extends DefaultSubComponentEvaluator {
	@Override
	public boolean evaluate(RequestContext context, Map<String, String> params) {
		String actual = context.getUser().getName();
		String expected = params.get("username");
		return StringUtils.equals(actual, expected);
	}
}
