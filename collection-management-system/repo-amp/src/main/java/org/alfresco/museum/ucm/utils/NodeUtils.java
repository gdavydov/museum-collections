package org.alfresco.museum.ucm.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

public class NodeUtils {
	private NodeService nodeService;
	private FileFolderService fileFolderService;
	private ContentService contentService;

	public NodeService getNodeService() {
		return nodeService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public FileFolderService getFileFolderService() {
		return fileFolderService;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	public ContentService getContentService() {
		return contentService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public NodeRef getOrCreateFolder(NodeRef parentRef, String name, boolean isHidden) {
		NodeRef result = this.getFileFolderService().searchSimple(parentRef, name);
		if (result == null) {
			result = this.getFileFolderService().create(parentRef, name, ContentModel.TYPE_FOLDER).getNodeRef();
			if (isHidden) {
				Map<QName, Serializable> aspectHiddenProperties = new HashMap<QName, Serializable>(1);
				// aspectHiddenProperties.put(ContentModel.PROP_VISIBILITY_MASK,
				// true);
				this.getNodeService().addAspect(result, ContentModel.ASPECT_HIDDEN, aspectHiddenProperties);
				if (isHidden) {
					this.getNodeService().addAspect(result, ContentModel.ASPECT_HIDDEN, aspectHiddenProperties);
				}
			}
		}
		return result;
	}

	public NodeRef getOrCreateFolderByPath(NodeRef parentRef, List<String> path) {
		NodeRef currentFolder = parentRef;
		for (String folder : path) {
			currentFolder = getOrCreateFolder(parentRef, folder, false);
		}
		return currentFolder;
	}

	/**
	 * See <a
	 * href="http://docs.alfresco.com/5.0/tasks/api-java-content-create.html"
	 * >documentation</a>
	 */
	public NodeRef createContentNode(NodeRef parent, String name, String text) {
		// Create a map to contain the values of the properties of the node
		Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
		props.put(ContentModel.PROP_NAME, name);

		// use the node service to create a new node
		NodeRef node = this.nodeService.createNode(parent, ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), ContentModel.TYPE_CONTENT, props)
				.getChildRef();

		// Use the content service to set the content onto the newly created
		// node
		ContentWriter writer = this.contentService.getWriter(node, ContentModel.PROP_CONTENT, true);
		writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
		writer.setEncoding("UTF-8");
		writer.putContent(text);

		// Return a node reference to the newly created node
		return node;
	}

	public NodeRef getSiteRefByNode(NodeRef nodeRef) {
		while (nodeRef != null && !SiteModel.TYPE_SITE.equals(this.getNodeService().getType(nodeRef))) {
			nodeRef = this.getNodeService().getPrimaryParent(nodeRef).getParentRef();
		}

		return nodeRef;
	}

	public NodeRef childByNamePath(NodeRef parent, String filename) {
		return this.getNodeService().getChildByName(parent, ContentModel.ASSOC_CONTAINS, filename);
	}
}
