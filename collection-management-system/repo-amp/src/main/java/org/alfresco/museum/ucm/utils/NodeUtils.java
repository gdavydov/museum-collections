package org.alfresco.museum.ucm.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.museum.ucm.UCMConstants;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.ObjectUtils;

public class NodeUtils {
	private NodeService nodeService;
	private FileFolderService fileFolderService;
	private ContentService contentService;
	private DictionaryService dictionaryService;
	private SearchService searchService;

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
			currentFolder = getOrCreateFolder(currentFolder, folder, false);
		}
		return currentFolder;
	}

	public NodeRef createContentNode(NodeRef parent, String name, String text) {
		return createContentNode(parent, name, text, ContentModel.TYPE_CONTENT);
	}

	public NodeRef createContentNode(NodeRef parent, String name, String text, QName nodeType) {
		return createContentNode(parent, name, new ByteArrayInputStream(text.getBytes()), nodeType, null, null);
	}

	public NodeRef createContentNode(NodeRef parent, String name, UCMContentImpl content, QName nodeType) {
		return createContentNode(parent, name, content.getInputStream(), nodeType, content.getMimetype(), content.getEncoding());
	}

	/**
	 * See <a
	 * href="http://docs.alfresco.com/5.0/tasks/api-java-content-create.html"
	 * >documentation</a>
	 */
	public NodeRef createContentNode(NodeRef parent, String name, InputStream content, QName nodeType, String mimetype, String encoding) {
		// Create a map to contain the values of the properties of the node
		Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
		props.put(ContentModel.PROP_NAME, name);

		// use the node service to create a new node
		NodeRef node = this.getNodeService().createNode(parent, ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), nodeType, props)
				.getChildRef();

		// Use the content service to set the content onto the newly created
		// node
		ContentWriter writer = this.getContentService().getWriter(node, ContentModel.PROP_CONTENT, true);

		writer.setMimetype(ObjectUtils.defaultIfNull(mimetype, MimetypeMap.MIMETYPE_TEXT_PLAIN));
		writer.setEncoding(ObjectUtils.defaultIfNull(encoding, StandardCharsets.UTF_8.name()));

		writer.putContent(content);

		// Return a node reference to the newly created node
		return node;
	}

	public TypeDefinition getNodeTypeDefinition(NodeRef nodeRef) {
		QName nodeTypeQName = this.getNodeService().getType(nodeRef);
		return this.getDictionaryService().getType(nodeTypeQName);
	}

	public void fillMandatoryProperties(TypeDefinition type, NodeRef node, Serializable value) {
		for (PropertyDefinition propDef : getAllProperties(type).values()) {
			if (propDef.isMandatory()) {
				Serializable currentValue = this.getNodeService().getProperty(node, propDef.getName());
				if (currentValue == null) {
					this.getNodeService().setProperty(node, propDef.getName(), value);
				}
			}
		}
	}

	public void synchronizeUCMPropertyValues(NodeRef from, NodeRef to) {
		Set<QName> fromAspects = this.getNodeService().getAspects(from);
		for (QName aspect : UCMConstants.INHERITABLE_ASPECTS) {
			if (fromAspects.contains(aspect)) {
				Set<QName> allAspectProperties = this.getDictionaryService().getAspect(aspect).getProperties().keySet();

				Map<QName, Serializable> fromProperties = this.getNodeService().getProperties(from);
				fromProperties.keySet().retainAll(allAspectProperties);

				this.getNodeService().addAspect(to, aspect, fromProperties);
			}
		}
	}

	public static HashMap<QName, PropertyDefinition> getAllProperties(TypeDefinition type) {
		HashMap<QName, PropertyDefinition> result = new HashMap<QName, PropertyDefinition>();
		result.putAll(type.getProperties());
		for (AspectDefinition aspect : type.getDefaultAspects()) {
			result.putAll(aspect.getProperties());
		}
		return result;
	}

	public boolean isSiteNode(NodeRef nodeRef) {
		return isNodeSubClassOf(nodeRef, SiteModel.TYPE_SITE);
	}

	public boolean isNodeSubClassOf(NodeRef nodeRef, QName type) {
		boolean result = false;
		if (nodeRef != null) {
			QName nodeType = this.getNodeService().getType(nodeRef);
			result = this.getDictionaryService().isSubClass(nodeType, type);
		}
		return result;
	}

	public NodeRef getSiteRefByNode(NodeRef nodeRef) {
		while (nodeRef != null && !isSiteNode(nodeRef)) {
			nodeRef = this.getNodeService().getPrimaryParent(nodeRef).getParentRef();
		}

		return nodeRef;
	}

	public NodeRef childByNamePath(NodeRef parent, String filename) {
		return this.getNodeService().getChildByName(parent, ContentModel.ASSOC_CONTAINS, filename);
	}

	/**
	 * See <a href=
	 * "https://forums.alfresco.com/forum/developer-discussions/repository-services/get-company-home-noderef-solved-06222009-1515"
	 * >discussion</a>
	 *
	 * @return
	 */
	//TODO: repository.getCompanyHome() ?
	public NodeRef getCompanyHomeNodeRef() {
		StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
		ResultSet rs = searchService.query(storeRef, SearchService.LANGUAGE_XPATH, "/app:company_home");

		NodeRef companyHomeNodeRef = null;
		try {
			if (rs.length() == 0) {
				throw new AlfrescoRuntimeException("Didn't find Company Home");
			}
			companyHomeNodeRef = rs.getNodeRef(0);
		} finally {
			rs.close();
		}
		return companyHomeNodeRef;
	}

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

	public DictionaryService getDictionaryService() {
		return dictionaryService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public SearchService getSearchService() {
		return searchService;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}
}
