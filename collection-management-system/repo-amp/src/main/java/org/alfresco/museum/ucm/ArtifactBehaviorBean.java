package org.alfresco.museum.ucm;

import java.util.List;

import org.alfresco.museum.ucm.formfilters.UCMCreateArtifact;
import org.alfresco.museum.ucm.utils.NodeUtils;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Delete artifact attachments when artifact is deleted.
 */
//TODO: support moving artifact attachments from site to site?
public class ArtifactBehaviorBean implements NodeServicePolicies.BeforeDeleteNodePolicy, NodeServicePolicies.OnSetNodeTypePolicy {
	private UCMCreateArtifact createArtifactFilter;
	private NodeService nodeService;
	private PolicyComponent policyComponent;
	private NodeUtils utils;

	public ArtifactBehaviorBean() {
	}

	public void init() {
		Behaviour beforeDeleteNode = new JavaBehaviour(this, "beforeDeleteNode",
				Behaviour.NotificationFrequency.FIRST_EVENT);
		Behaviour onSetNodeType = new JavaBehaviour(this, "onSetNodeType",
				Behaviour.NotificationFrequency.FIRST_EVENT);
		//Behaviour onMoveNode = new JavaBehaviour(this, "onMoveNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);


		this.getPolicyComponent().bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, UCMConstants.TYPE_UCM_ARTIFACT_QNAME,
					beforeDeleteNode);
		this.getPolicyComponent().bindClassBehaviour(NodeServicePolicies.OnSetNodeTypePolicy.QNAME, UCMConstants.TYPE_UCM_ARTIFACT_QNAME,
				onSetNodeType);
//			this.getPolicyComponent().bindClassBehaviour(NodeServicePolicies.OnMoveNodePolicy.QNAME, type, onMoveNode);
	}

	@Override
	public void beforeDeleteNode(NodeRef nodeRef) {
		List<ChildAssociationRef> childAssocs = this.getNodeService().getChildAssocs(nodeRef);
		for (ChildAssociationRef childAssocRef : childAssocs) {
			if (UCMConstants.ASSOC_UCM_ARTIFACT_CONTAINS.equals(childAssocRef.getTypeQName())) {
				this.getNodeService().deleteNode(childAssocRef.getChildRef());
			}
		}
	}

	@Override
	public void onSetNodeType(NodeRef nodeRef, QName oldType, QName newType) {
		this.getCreateArtifactFilter().processNewArtifact(nodeRef);
	}

//	@Override
//	public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef) {}

	public UCMCreateArtifact getCreateArtifactFilter() {
		return createArtifactFilter;
	}

	public void setCreateArtifactFilter(UCMCreateArtifact createArtifactFilter) {
		this.createArtifactFilter = createArtifactFilter;
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
