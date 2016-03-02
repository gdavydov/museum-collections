package org.alfresco.museum.ucm.sizelimits;

import java.util.Objects;

import org.alfresco.model.ContentModel;
import org.alfresco.museum.ucm.UCMConstants;
import org.alfresco.museum.ucm.utils.NodeUtils;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * This class' init method should be invoked during Spring context
 * initialization in order to enable some listeners.<br/>
 * This listeners are used to monitor changes which could lead to contend size
 * changes.<br/>
 * Size of only two node types is taken into account: ucm:artifact and
 * ucm:attached_file.
 *
 */
public class SiteSizeLimitsBean implements NodeServicePolicies.OnAddAspectPolicy,
		NodeServicePolicies.BeforeDeleteNodePolicy, ContentServicePolicies.OnContentPropertyUpdatePolicy,
		NodeServicePolicies.OnMoveNodePolicy {
	public static QName[] TYPES_TO_CHECK = new QName[] { UCMConstants.TYPE_UCM_ARTIFACT_QNAME,
			UCMConstants.TYPE_UCM_ATTACHED_FILE_QNAME };

	private NodeService nodeService;
	private PolicyComponent policyComponent;
	private SiteSizeUpdaterFactory sizeUpdFactory;
	private NodeUtils utils;

	public SiteSizeLimitsBean() {
	}

	public void init() {
		Behaviour onContentPropertyUpdate = new JavaBehaviour(this, "onContentPropertyUpdate",
				Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
		Behaviour beforeDeleteNode = new JavaBehaviour(this, "beforeDeleteNode",
				Behaviour.NotificationFrequency.FIRST_EVENT);
		Behaviour onMoveNode = new JavaBehaviour(this, "onMoveNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
		Behaviour onAddAspect = new JavaBehaviour(this, "onAddAspect",
				Behaviour.NotificationFrequency.TRANSACTION_COMMIT);

		for (QName type : TYPES_TO_CHECK) {
			this.getPolicyComponent().bindClassBehaviour(ContentServicePolicies.OnContentPropertyUpdatePolicy.QNAME,
					type, onContentPropertyUpdate);
			this.getPolicyComponent().bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, type,
					beforeDeleteNode);
			this.getPolicyComponent().bindClassBehaviour(NodeServicePolicies.OnMoveNodePolicy.QNAME, type, onMoveNode);
		}

		this.getPolicyComponent().bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME,
				UCMConstants.ASPECT_SITE_SIZE_LIMITED_QNAME, onAddAspect);
	}

	@Override
	public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName) {
		Runnable siteSizeUpdater = this.getSizeUpdFactory().getInstance(nodeRef);
		// update site size properties in background
		// TODO: use threadPoolExecuter?
		new Thread(siteSizeUpdater).start();
	}

	@Override
	public void beforeDeleteNode(NodeRef nodeRef) {
		long size = 0;
		if (getNodeService().exists(nodeRef)) {
			ContentData contentData = (ContentData) getNodeService().getProperty(nodeRef, ContentModel.PROP_CONTENT);
			if (contentData != null) {
				size = contentData.getSize();
			}
		}

		NodeRef siteRef = getUtils().getSiteRefByNode(nodeRef);
		if (siteRef != null) {
			// reduce file size by size of deleted content
			getSizeUpdFactory().setSiteSize(siteRef, -size, true);
		}
	}

	@Override
	public void onContentPropertyUpdate(NodeRef nodeRef, QName propertyQName, ContentData beforeValue,
			ContentData afterValue) {
		long oldSize = (beforeValue != null) ? beforeValue.getSize() : 0;
		long newSize = (afterValue != null) ? afterValue.getSize() : 0;

		NodeRef siteRef = getUtils().getSiteRefByNode(nodeRef);
		if (siteRef != null) {
			// add content size change to site size
			getSizeUpdFactory().setSiteSize(siteRef, newSize - oldSize, true);
		}
	}

	@Override
	public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef) {
		long size = getUtils().getNodeSize(newChildAssocRef.getChildRef());

		NodeRef oldSiteRef = getUtils().getSiteRefByNode(oldChildAssocRef.getChildRef());
		NodeRef newSiteRef = getUtils().getSiteRefByNode(newChildAssocRef.getChildRef());

		if (!Objects.equals(oldSiteRef, newSiteRef)) {
			if (oldSiteRef != null) {
				// subtract content size from old site size
				getSizeUpdFactory().setSiteSize(oldSiteRef, -size, true);
			}

			if (newSiteRef != null) {
				// add content size to new site size
				getSizeUpdFactory().setSiteSize(newSiteRef, size, true);
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

	public SiteSizeUpdaterFactory getSizeUpdFactory() {
		return sizeUpdFactory;
	}

	public void setSizeUpdFactory(SiteSizeUpdaterFactory sizeUpdFactory) {
		this.sizeUpdFactory = sizeUpdFactory;
	}

	public NodeUtils getUtils() {
		return utils;
	}

	public void setUtils(NodeUtils utils) {
		this.utils = utils;
	}
}
