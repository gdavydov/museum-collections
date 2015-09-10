package com.alfresco.museum.ucm.evaluators;

import java.util.ArrayList;
import java.util.Map;

import org.alfresco.web.extensibility.SlingshotEvaluatorUtil;
import org.alfresco.web.extensibility.SlingshotGroupComponentElementEvaluator;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.extensibility.impl.DefaultSubComponentEvaluator;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;

/**
 * This is a modification of
 * {@link org.alfresco.web.evaluator.HasGroupMembershipsEvaluator} which extends
 * {@link org.springframework.extensions.surf.extensibility.SubComponentEvaluator
 * SubComponentEvaluator}. It may be used in template-instance configs.
 */
public class HasGroupMembershipsEvaluator extends DefaultSubComponentEvaluator {
	protected SlingshotEvaluatorUtil util = null;

	public void setSlingshotEvaluatorUtil(SlingshotEvaluatorUtil slingshotExtensibilityUtil) {
		this.util = slingshotExtensibilityUtil;
	}

	private ArrayList<String> groups;

	public void setGroups(ArrayList<String> groups) {
		this.groups = groups;
	}

	private String relation = SlingshotGroupComponentElementEvaluator.AND;

	public void setRelation(String relation) {
		this.relation = relation;
	}

	@Override
	public boolean evaluate(RequestContext context, Map<String, String> params) {
		boolean memberOfAllGroups = (this.relation == null || this.relation.trim().equalsIgnoreCase(
				SlingshotGroupComponentElementEvaluator.AND));
		final RequestContext rc = ThreadLocalRequestContext.getRequestContext();
		boolean hasMembership = this.util.isMemberOfGroups(rc, this.groups, memberOfAllGroups);
		return hasMembership;
	}
}
