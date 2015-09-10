package com.alfresco.museum.ucm.evaluators;

import java.util.Map;

import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.extensibility.SubComponentEvaluator;
import org.springframework.extensions.surf.extensibility.impl.DefaultSubComponentEvaluator;

public class InvertedEvaluator extends DefaultSubComponentEvaluator {
	protected SubComponentEvaluator evaluator = null;

	public void setEvaluator(SubComponentEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	@Override
	public boolean evaluate(RequestContext context, Map<String, String> params) {
		return !this.evaluator.evaluate(context, params);
	}
}
