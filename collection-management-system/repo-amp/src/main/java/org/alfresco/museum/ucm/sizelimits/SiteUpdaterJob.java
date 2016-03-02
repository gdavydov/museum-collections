package org.alfresco.museum.ucm.sizelimits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.museum.ucm.UCMConstants;
import org.alfresco.museum.ucm.utils.NodeUtils;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class SiteUpdaterJob extends QuartzJobBean {
	private static final String SITES_FOLDER_NAME = "sites";

	private NodeService nodeService;
	private SiteSizeUpdaterFactory sizeUpdFactory;
	private NodeUtils utils;

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {
			public Void doWork() throws Exception {
				List<NodeRef> limitedSites = getLimitedSites();
				for (NodeRef siteRef : limitedSites) {
					//Just run it sequentially. Parallelizing won't help because IO is a bottleneck.
					getSizeUpdFactory().getInstance(siteRef).run();
				}
				return null;
			}
		}, AuthenticationUtil.getAdminUserName());

	}

	private NodeRef getSitesNodeRef() {
		return this.getNodeService().getChildByName(this.getUtils().getCompanyHomeNodeRef(), ContentModel.ASSOC_CONTAINS, SITES_FOLDER_NAME);
	}

	private List<NodeRef> getLimitedSites() {
		ArrayList<NodeRef> result = new ArrayList<NodeRef>();
		NodeRef sitesNodeRef = getSitesNodeRef();
		List<ChildAssociationRef> allUcmSites = this.getNodeService().getChildAssocs(sitesNodeRef, Collections.singleton(UCMConstants.TYPE_UCM_SITE_QNAME));
		for (ChildAssociationRef siteAssocRef : allUcmSites) {
			NodeRef ucmSite = siteAssocRef.getChildRef();
			boolean isLimited = this.getNodeService().hasAspect(ucmSite, UCMConstants.ASPECT_SITE_SIZE_LIMITED_QNAME);
			if (isLimited) {
				result.add(ucmSite);
			}
		}
		return result;
	}

	public NodeService getNodeService() {
		return nodeService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
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
