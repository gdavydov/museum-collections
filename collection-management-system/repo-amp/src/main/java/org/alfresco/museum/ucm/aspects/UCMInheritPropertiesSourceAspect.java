package org.alfresco.museum.ucm.aspects;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.museum.ucm.UCMConstants;
import org.alfresco.museum.ucm.utils.NodeUtils;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * When properties of node with aspect "ucm:inherit_properties_source_aspect"
 * are updated or new child is created properties of node children should be
 * updated.
 */
public class UCMInheritPropertiesSourceAspect implements NodeServicePolicies.OnUpdatePropertiesPolicy {
	private NodeService nodeService;
	private PolicyComponent policyComponent;
	private NodeUtils utils;

	public void init() {
		this.getPolicyComponent().bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
				UCMConstants.ASPECT_INHERIT_PROPERTIES_SOURCE_QNAME,
				new JavaBehaviour(this, "onUpdateProperties"));
	}

	// TODO: update only those child properties which were actually updated
	// (different in before and after)?
	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		processNodeTree(nodeRef, nodeRef);
	}

	protected void processSingleNode(ChildAssociationRef childAssociationRef) {
		processSingleNode(childAssociationRef.getParentRef(), childAssociationRef.getChildRef());
	}

	protected void processSingleNode(NodeRef parentRef, NodeRef childRef) {
		boolean isChildValidTarget = this.getNodeService().getAspects(childRef)
				.contains(UCMConstants.ASPECT_INHERIT_PROPERTIES_TARGET_QNAME);
		if (isChildValidTarget) {
			this.getUtils().synchronizeUCMPropertyValues(parentRef, childRef);
		}
	}

	/**
	 * Process all children of the node by traversing node hierarchy recursively
	 */
	protected void processNodeTree(NodeRef sourceNodeRef, NodeRef nodeRef) {
		List<ChildAssociationRef> childAssocs = this.getNodeService().getChildAssocs(nodeRef);
		for (ChildAssociationRef childAssociationRef : childAssocs) {
			if (childAssociationRef.isPrimary()) {
				processSingleNode(childAssociationRef);
				processNodeTree(sourceNodeRef, childAssociationRef.getChildRef());
			}
		}
	}

	public NodeService getNodeService() {
		return nodeService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public PolicyComponent getPolicyComponent() {
		return policyComponent;
	}

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public NodeUtils getUtils() {
		return utils;
	}

	public void setUtils(NodeUtils utils) {
		this.utils = utils;
	}
}
