package org.alfresco.museum.ucm.aspects;

import java.util.Map;

import org.alfresco.museum.ucm.UCMConstants;
import org.alfresco.museum.ucm.utils.NodeUtils;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * When node with aspect "ucm:inherit_properties_target_aspect" is moved or
 * copied to new location inherited properties should be updated.
 */
public class UCMInheritPropertiesTargetAspect implements NodeServicePolicies.OnMoveNodePolicy, NodeServicePolicies.OnCreateNodePolicy,
		CopyServicePolicies.OnCopyCompletePolicy {
	private NodeService nodeService;
	private PolicyComponent policyComponent;
	private NodeUtils utils;

	public void init() {
		this.getPolicyComponent().bindClassBehaviour(CopyServicePolicies.OnCopyCompletePolicy.QNAME,
				UCMConstants.ASPECT_INHERIT_PROPERTIES_TARGET_QNAME, new JavaBehaviour(this, "onCopyComplete"));

		this.getPolicyComponent().bindClassBehaviour(NodeServicePolicies.OnMoveNodePolicy.QNAME,
				UCMConstants.ASPECT_INHERIT_PROPERTIES_TARGET_QNAME, new JavaBehaviour(this, "onMoveNode"));

		this.getPolicyComponent().bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME,
				UCMConstants.ASPECT_INHERIT_PROPERTIES_TARGET_QNAME, new JavaBehaviour(this, "onCreateNode"));
	}

	/**
	 * Find closest (grand)parent and inherit properties from it.
	 */
	protected void processNode(NodeRef nodeRef) {
		for (NodeRef parentRef = getParentNodeRef(nodeRef); parentRef != null; parentRef = getParentNodeRef(parentRef)) {
			boolean isParentValidSource = this.getNodeService().getAspects(parentRef)
					.contains(UCMConstants.ASPECT_INHERIT_PROPERTIES_SOURCE_QNAME);
			if (isParentValidSource) {
				this.getUtils().synchronizeUCMPropertyValues(parentRef, nodeRef);
				break;
			}
		}
	}

	@Override
	public void onCopyComplete(QName classRef, NodeRef sourceNodeRef, NodeRef targetNodeRef, boolean copyToNewNode,
			Map<NodeRef, NodeRef> copyMap) {
		for (NodeRef copiedNodeRef : copyMap.values()) {
			boolean isValidTarget = this.getNodeService().getAspects(copiedNodeRef)
					.contains(UCMConstants.ASPECT_INHERIT_PROPERTIES_TARGET_QNAME);
			if (isValidTarget) {
				processNode(copiedNodeRef);
			}
		}
	}

	@Override
	public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef) {
		processNode(newChildAssocRef.getChildRef());
	}

	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {
		processNode(childAssocRef.getChildRef());
	}

	protected NodeRef getParentNodeRef(NodeRef parentRef) {
		return this.getNodeService().getPrimaryParent(parentRef).getParentRef();
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
