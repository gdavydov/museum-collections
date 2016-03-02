package org.alfresco.museum.ucm.sizelimits;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.museum.ucm.UCMConstants;
import org.alfresco.museum.ucm.utils.NodeUtils;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class serves for two purposes:<br/>
 * 1. It creates Runnable which calculates file content size by traversing it
 * recursively and updates corresponding properties if site node. This Runnable
 * will most likely be ran in background thread.<br/>
 * 2. It wraps all operations with site size-related properties. This properties
 * shouldn't be updated directly from other places.<br/>
 * Site size property would likely be inaccurate. Images are recoded by Alfresco
 * after upload, so size of image when it is removed may differ from it's
 * original size by 10-20%.<br/>
 * Performance may be improved by updating local variable instead of site
 * property itself and then doing periodical synchronization.
 */
public class SiteSizeUpdaterFactory {
	private static Log LOGGER = LogFactory.getLog(SiteSizeUpdaterFactory.class);

	public static final String WARNING_MAIL_TEMPLATE_PATH = "Data Dictionary/Email Templates/UCM email templates/site_limits_exceeded_warning.html.ftl";

	private NodeService nodeService;
	private TransactionService transactionService;
	private NodeUtils utils;
	private long sizeLimit;
	private byte warningThresholdPercents;

	public SiteSizeUpdaterFactory() {
	}

	public SiteSizeUpdater getInstance(NodeRef siteNodeRef) {
		return new SiteSizeUpdater(siteNodeRef);
	}

	public class SiteSizeUpdater implements Runnable {
		private final NodeRef siteRef;

		private SiteSizeUpdater(NodeRef siteRef) {
			this.siteRef = siteRef;
		}

		private long getNodeSize(NodeRef nodeRef) {
			long size = 0;

			boolean isArtifact = getUtils().isNodeSubClassOf(nodeRef, UCMConstants.TYPE_UCM_ARTIFACT_QNAME);
			boolean isAttach = getUtils().isNodeSubClassOf(nodeRef, UCMConstants.TYPE_UCM_ATTACHED_FILE_QNAME);
			if (isArtifact || isAttach) {
				try {
					// Collecting current node size
					ContentData contentData = (ContentData) getNodeService().getProperty(nodeRef,
							ContentModel.PROP_CONTENT);
					if (contentData != null) {
						size = contentData.getSize();
					}
				} catch (Exception e) {
				}
			}

			// Collecting child nodes' sizes
			List<ChildAssociationRef> chilAssocsList = getNodeService().getChildAssocs(nodeRef);

			for (ChildAssociationRef childAssociationRef : chilAssocsList) {
				if (childAssociationRef.isPrimary()
						&& ContentModel.ASSOC_CONTAINS.equals(childAssociationRef.getTypeQName())) {
					NodeRef childNodeRef = childAssociationRef.getChildRef();
					size = size + getNodeSize(childNodeRef);
				}
			}

			return size;
		}

		@Override
		public void run() {
			AuthenticationUtil.runAs(new RunAsWork<Void>() {
				public Void doWork() throws Exception {
					long siteSize = getNodeSize(siteRef);
					Date now = new Date();
					String siteName = (String) getNodeService().getProperty(siteRef, ContentModel.PROP_NAME);

					LOGGER.info(String.format("Site %s have content of size %d on %s", siteName, siteSize, now.toString()));

					setSiteSize(siteRef, siteSize, false);
					getNodeService().setProperty(siteRef, UCMConstants.ASPECT_PROP_SITE_SCAN_DATE_QNAME, now);
					return null;
				}
			}, AuthenticationUtil.getSystemUserName());
		}
	}

	private void rollbackTransaction(NodeRef siteRef) {
		UserTransaction tx = this.getTransactionService().getUserTransaction();
		if (tx != null) {
			try {
				tx.rollback();
			} catch (SystemException ex) {
				Serializable siteName = this.getNodeService().getProperty(siteRef, ContentModel.PROP_NAME);
				LOGGER.warn("Can't store new content in site " + siteName + " as it exceeds size limits.");
			}
		}
	}

	/**
	 * Single synchronized entry point for setting value to site size property.
	 */
	public synchronized void setSiteSize(NodeRef siteRef, long value, boolean isDiff) {
		if (this.getNodeService().hasAspect(siteRef, UCMConstants.ASPECT_SITE_SIZE_LIMITED_QNAME)) {
			long newSize = 0;
			if (isDiff) {
				Serializable currentSize = getNodeService().getProperty(siteRef,
						UCMConstants.ASPECT_PROP_SITE_CONTENT_SIZE_QNAME);
				if (currentSize instanceof Long) {
					newSize = value + (Long) currentSize;
				} else {
					newSize = value;
				}
			} else {
				newSize = value;
			}

			if (newSize > getSizeLimit()) {
				long currentSize = (long) this.getNodeService().getProperty(siteRef, UCMConstants.ASPECT_PROP_SITE_CONTENT_SIZE_QNAME);
				try {
					sendWarningEmail(siteRef, currentSize);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				// TODO: 95% ???
				rollbackTransaction(siteRef);
			} else {
				this.getNodeService().setProperty(siteRef, UCMConstants.ASPECT_PROP_SITE_CONTENT_SIZE_QNAME, newSize);
			}
		}
	}

	private void sendWarningEmail(NodeRef siteRef, long siteSize) throws FileNotFoundException {
		NodeRef siteAdmin = (NodeRef) this.getNodeService().getProperty(siteRef, UCMConstants.ASPECT_PROP_UCM_SITE_ASPECT_ADMIN_QNAME);
		String siteAdminFirstName = (String) this.getNodeService().getProperty(siteAdmin, ContentModel.PROP_FIRSTNAME);
		String siteAdminEmail = (String) this.getNodeService().getProperty(siteAdmin, UCMConstants.ASPECT_PROP_UCM_SITE_ASPECT_ADMIN_EMAIL_QNAME);

		String siteShortName = (String) this.getNodeService().getProperty(siteRef, ContentModel.PROP_NAME);
		String siteName = (String) this.getNodeService().getProperty(siteRef, UCMConstants.ASPECT_PROP_UCM_SITE_NAME_QNAME);

		Map<String, Serializable> templateArgs = new HashMap<String, Serializable>();
		templateArgs.put("site_name", siteName);
		templateArgs.put("first_name", siteAdminFirstName);
		templateArgs.put("site_short_name", siteShortName);

		// https://loftux.com/en/blog/fixing-the-invite-email-template-in-alfresco-share#sthash.IQx2RxYt.dpbs
		this.getUtils().sendNotificationEmail(WARNING_MAIL_TEMPLATE_PATH,
				"Site size limit reached!", siteAdminEmail, templateArgs);
	}

	public NodeService getNodeService() {
		return nodeService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public TransactionService getTransactionService() {
		return transactionService;
	}

	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	public NodeUtils getUtils() {
		return utils;
	}

	public void setUtils(NodeUtils utils) {
		this.utils = utils;
	}

	public long getSizeLimit() {
		return sizeLimit;
	}

	public void setSizeLimit(long sizeLimit) {
		this.sizeLimit = sizeLimit;
	}

	public byte getWarningThresholdPercents() {
		return warningThresholdPercents;
	}

	public void setWarningThresholdPercents(byte warningThresholdPercents) {
		this.warningThresholdPercents = warningThresholdPercents;
	}
}
