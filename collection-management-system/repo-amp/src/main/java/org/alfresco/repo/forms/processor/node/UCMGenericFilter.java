package org.alfresco.repo.forms.processor.node;

import static org.alfresco.museum.ucm.UCMConstants.ASPECT_GEOGRAPHICAL_QNAME;
import static org.alfresco.museum.ucm.UCMConstants.CONTENT_PROP_DATA;
import static org.alfresco.museum.ucm.UCMConstants.DEFAULT_CONTENT_MIMETYPE;
import static org.alfresco.museum.ucm.UCMConstants.NAME_PROP_DATA;
import static org.alfresco.repo.forms.processor.node.FormFieldConstants.PROP_DATA_PREFIX;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.museum.ucm.UCMConstants;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.FormException;
import org.alfresco.repo.forms.processor.AbstractFilter;
import org.alfresco.repo.forms.processor.AbstractFormProcessor;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.IOUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.jpeg.JpegParser;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Form fields of type "file" are currently ignored in
 * {@link org.alfresco.repo.forms.processor.node.ContentModelFormProcessor
 * FormProcessor's}
 * {@link org.alfresco.repo.forms.processor.node.ContentModelFormProcessor#persistNode(NodeRef, FormData)
 * persistNode} method. This form filter is used to force persisting of
 * "cm:content" field content as a node property. It is placed it this package
 * in order to get access to protected fields of
 * {@link org.alfresco.repo.forms.processor.node.ContentModelFormProcessor form
 * processor class}.
 */
public class UCMGenericFilter<T> extends AbstractFilter<T, NodeRef> {
	private NodeService nodeService;
	private ContentService contentService;
	private DictionaryService dictionaryService;
	private FileFolderService fileFolderService;
	private MimetypeService mimetypeService;

	private static Log logger = LogFactory.getLog(UCMGenericFilter.class);

	protected String getFilename(FormData data) {
		String filename = "";

		// If there is field for property "cm:name" use it as new content name
		FieldData nameField = data.getFieldData(NAME_PROP_DATA);
		if (nameField != null && !StringUtils.isEmpty(nameField.getValue())) {
			filename = nameField.getValue().toString();
		} else {
			// If "cm:content" property is undefined value of property
			// "cm:content" is used instead. It contains name of file uploaded
			// by user.
			FieldData contentField = data.getFieldData(CONTENT_PROP_DATA);
			if (contentField != null && contentField.isFile() && !StringUtils.isEmpty(contentField.getValue())) {
				filename = contentField.getValue().toString();
			}
		}

		return filename;
	}

	protected String findFreeFilename(NodeRef parentDirectory, String originalFilename) {
		String tmpFilename = originalFilename;
		int counter = 1;
		while (null != childByNamePath(parentDirectory, tmpFilename)) {
			tmpFilename = generateFilenameWithIndex(originalFilename, counter);
			++counter;
		}
		return tmpFilename;
	}

	private NodeRef childByNamePath(NodeRef parent, String filename) {
		return getNodeService().getChildByName(parent, ContentModel.ASSOC_CONTAINS, filename);
	}

	private static String generateFilenameWithIndex(String oldFilename, int index) {
		String result = null;
		int dotIndex = oldFilename.lastIndexOf(".");
		if (dotIndex == 0) {
			// File didn't have a proper 'name' instead it had just a suffix and
			// started with a ".", create "1.txt"
			result = index + oldFilename;
		} else if (dotIndex > 0) {
			// Filename contained ".", create "filename-1.txt"
			result = oldFilename.substring(0, dotIndex) + "-" + index + oldFilename.substring(dotIndex);
		} else {
			// Filename didn't contain a dot at all, create "filename-1"
			result = oldFilename + "-" + index;
		}
		return result;
	}

	protected NodeRef getOrCreateFolder(NodeRef parentRef, String name, boolean isHidden) {
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
	
	/**
	 * Create configuration for the current site. SHould run when site is accessed the first time
	 * @param nodeRef
	 * @return
	 */
	public String getSiteName(NodeRef nodeRef) 
	{
		while (nodeRef != null && !SiteModel.TYPE_SITE.equals(this.getNodeService().getType(nodeRef))) {
			nodeRef = this.getNodeService().getPrimaryParent(nodeRef).getParentRef();
		}

		NodeRef siteNodeRef = nodeRef;
		return getNodeName(siteNodeRef, UCMConstants.PROP_UCM_SITE_QNAME);
		
/*
		int sitePosition = 3;
		Path path = this.getNodeService().getPaths(nodeRef, true).get(0);
		return path.get(3).getElementString(); //siteName
*/		
	}

	/**
	 * Return node name for specified QName
	 * @param nodeRef
	 * @param qName
	 * @return String 
	 */
	public String getNodeName(NodeRef nodeRef, QName qName) 
	{
		return getNodeService().getProperty(nodeRef, qName).toString();
	}
	/**
	 * Return node name for generic cm:object
	 * @param nodeRef
	 * @return String
	 */
	public String getNodeName(NodeRef nodeRef) 
	{
		return getNodeService().getProperty(nodeRef, UCMConstants.PROP_CM_CMOBJECTNAME_QNAME).toString();
	}

	/**
	 * TODO Do I need it?
	 * @param nodeRef
	 * @return
	 */
	private InputStream getContent(NodeRef nodeRef)
	{
		try {
			ContentReader reader = this.getContentService().getReader(nodeRef, ContentModel.PROP_CONTENT);
			return reader.getContentInputStream();	
		}
		catch(org.alfresco.service.cmr.dictionary.InvalidTypeException ite) {
			logger.error("Invalid node type for "+getNodeName(nodeRef));
		}
		return  null;
	}

	protected void writeContent(TypeDefinition item, FormData data, NodeRef persistedObject) {
		FieldData contentField = data.getFieldData(CONTENT_PROP_DATA);
		if (contentField != null && contentField.isFile()) {
			// if we have a property definition attempt the persist
			PropertyDefinition propDef = item.getProperties().get(ContentModel.PROP_CONTENT);
			if (propDef != null && propDef.getDataType().getName().equals(DataTypeDefinition.CONTENT)) {
				InputStream inputStream = contentField.getInputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try {
					int streamSize = IOUtils.copy(inputStream, baos);
					if (streamSize > 0) {
						ContentWriter writer = this.getContentService().getWriter(persistedObject, ContentModel.PROP_CONTENT,
								true);
						if (writer != null) {
							
							processMetadata(new ByteArrayInputStream(baos.toByteArray()), persistedObject);
							
							// write the content
							writer.putContent(new ByteArrayInputStream(baos.toByteArray()));
							
							// content data has not been persisted yet so get it from
							// the node
							ContentData contentData = (ContentData) this.getNodeService().getProperty(persistedObject,
									ContentModel.PROP_CONTENT);
							if (contentData != null) {
								String mimetype = determineDefaultMimetype(data);
								contentData = ContentData.setMimetype(contentData, mimetype);
								Map<QName, Serializable> propsToPersist = new HashMap<QName, Serializable>();
								propsToPersist.put(ContentModel.PROP_CONTENT, contentData);
								this.getNodeService().addProperties(persistedObject, propsToPersist);
							}
						}
					}
				} catch (IOException e) {
					logger.error("Can't create copy of image stream", e);
				}
			}
		}
	}

	/**
	 * @see <a
	 *      href="https://github.com/jpotts/alfresco-api-java-examples/blob/master/alfresco-api-examples/src/main/java/com/alfresco/api/example/CmisGeographicAspectExample.java">source</a>
	 *      Only handle JPEG. inputStream is reset in the end.
	 */
	public void processMetadata(InputStream inputStream, NodeRef node) {
		try {
			Metadata metadata = new Metadata();
			metadata.set(Metadata.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE);

			new JpegParser().parse(inputStream, new DefaultHandler(), metadata, new ParseContext());

			String lat = metadata.get("geo:lat");
			String lon = metadata.get("geo:long");

			if (lat != null && lon != null) {
				HashMap<QName, Serializable> geoProps = new HashMap<QName, Serializable>();
				geoProps.put(ContentModel.PROP_LATITUDE, lat);
				geoProps.put(ContentModel.PROP_LONGITUDE, lon);
				this.getNodeService().addAspect(node, ASPECT_GEOGRAPHICAL_QNAME, geoProps);
			}
		} catch (IOException e) {
			logger.warn("Can't read image content, skipping", e);
		} catch (TikaException te) {
			logger.warn("Caught tika exception, skipping", te);
		} catch (SAXException se) {
			logger.warn("Caught SAXException, skipping", se);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.reset();
				} catch (IOException e) {
					logger.warn("Can't reset image stream", e);
				}
			}
		}
	}

	protected NodeRef getSiteRefByNode(NodeRef nodeRef) {
		while (nodeRef != null && !SiteModel.TYPE_SITE.equals(this.getNodeService().getType(nodeRef))) {
			nodeRef = this.getNodeService().getPrimaryParent(nodeRef).getParentRef();
		}

		return nodeRef;
	}
	
	// <site>/system/artifact_attachments/<artist>/<artifact_name>
	protected NodeRef getOrCreateArtistMediaFolder(NodeRef artifactRef) {
		// TODO: LOG
		NodeRef site = getSiteRefByNode(artifactRef);
		if (site == null)
			return null;

		Serializable artistNameValue = this.getNodeService().getProperty(artifactRef,
				UCMConstants.PROP_UCM_ARTIST_QNAME);
		Serializable artifactNameValue = this.getNodeService().getProperty(artifactRef, ContentModel.PROP_NAME);

		if (artistNameValue == null || artifactNameValue == null)
			return null;

		String artistName = artistNameValue.toString();
		String artifactName = artifactNameValue.toString();

		NodeRef systemFolder = getOrCreateFolder(site, UCMConstants.SYSTEM_FOLDER_NAME, false);
		
//		NodeRef doclibFolder = getOrCreateFolder(site, "documentLibrary", false);
//		NodeRef systemFolder = getOrCreateFolder(doclibFolder, UCMConstants.SYSTEM_FOLDER_NAME, false);
		/*
		 * NodeRef systemFolder = getOrCreateFolder(site,
		 * UCMConstants.SYSTEM_FOLDER_NAME, true);
		 */
		NodeRef mediaFolder = getOrCreateFolder(systemFolder, UCMConstants.MEDIA_FOLDER_NAME, false);
		NodeRef artistFolder = getOrCreateFolder(mediaFolder, artistName, false);
		NodeRef artifactFolder = getOrCreateFolder(artistFolder, artifactName, false);

		// set media folder caption
		this.getNodeService().setProperty(artifactFolder, ContentModel.PROP_TITLE, "Media content for " + artifactName);

		// save reference to folder in artifact association
		this.getNodeService().addChild(artifactRef, artifactFolder, UCMConstants.ASSOC_UCM_ARTIFACT_CONTAINS_QNAME,
				QName.createQName(UCMConstants.UCM_NAMESPACE, artifactName));

		return mediaFolder;
	}
	
	/*
	 * TODO: ideally metadata should be extracted in the same way it is done in
	 * upload.post.js:extractMetadata
	 * "actions.create("extract-metadata").execute(file, false, false)"
	 * equivalent Java code would be
	 * "create("extract-metadata").execute(persistedObject, false, false);" But
	 * unfortunately registryService bean isn't available in current context.
	 * Are there any workarounds? private ScriptAction create(String actionName)
	 * { ScriptAction scriptAction = null; ActionService actionService =
	 * serviceRegistry.getActionService(); ActionDefinition actionDef =
	 * actionService.getActionDefinition(actionName); if (actionDef != null) {
	 * Action action = actionService.createAction(actionName); scriptAction =
	 * new ScriptAction(this.serviceRegistry, action, actionDef);
	 * scriptAction.setScope(new BaseScopableProcessorExtension().getScope()); }
	 * return scriptAction; }
	 */

	/**
	 * Looks through the form data for the 'mimetype' transient field and
	 * returns it's value if found, otherwise the default 'text/plain' is
	 * returned
	 * 
	 * @param data
	 *            Form data being persisted
	 * @return The default mimetype
	 */
	protected String determineDefaultMimetype(FormData data) {
		String mimetype = DEFAULT_CONTENT_MIMETYPE;

		if (data != null) {
			FieldData mimetypeField = data.getFieldData(PROP_DATA_PREFIX + MimetypeFieldProcessor.KEY);
			if (mimetypeField != null) {
				String mimetypeFieldValue = (String) mimetypeField.getValue();
				if (mimetypeFieldValue != null && mimetypeFieldValue.length() > 0) {
					mimetype = mimetypeFieldValue;
				}
			} else {
				FieldData contentField = data.getFieldData(CONTENT_PROP_DATA);
				if (contentField != null) {
					String fileName = contentField.getValue().toString();
					InputStream inputStream = contentField.getInputStream();
					mimetype = getMimetypeService().guessMimetype(fileName, inputStream);
				}
			}
		}

		return mimetype;
	}

	protected void resolvePossibleFilenameConflict(TypeDefinition item, FormData data) {
		// firstly, ensure we have a destination to create the node in
		NodeRef parentRef = null;
		FieldData destination = data.getFieldData(AbstractFormProcessor.DESTINATION);
		if (destination == null) {
			throw new FormException("Failed to persist form for '" + item.getName() + "' as '"
					+ AbstractFormProcessor.DESTINATION + "' data was not provided.");
		}
		// create the parent NodeRef
		parentRef = new NodeRef((String) destination.getValue());

		String originalFilename = getFilename(data);
		String validFilename = findFreeFilename(parentRef, originalFilename);
		// Use name of uploaded file as new content name
		data.addFieldData(NAME_PROP_DATA, validFilename, true);
	}

	/*
	 * Fills properties of "to" node with values of "from" node. To be updated
	 * property should: 1. be defined in child node type description in types
	 * model file; 2. be set in parent node.
	 */
	protected void inheritProperties(TypeDefinition toType, NodeRef fromNode, NodeRef toNode) {
		Set<QName> propsSet = getAllProperties(toType).keySet();
		for (QName property : propsSet) {
			Serializable fromValue = this.getNodeService().getProperty(fromNode, property);
			Serializable toValue = this.getNodeService().getProperty(toNode, property);
			if (toValue == null && fromValue != null) {
				this.getNodeService().setProperty(toNode, property, fromValue);
			}
		}
	}

	protected void fillMandatoryProperties(TypeDefinition type, NodeRef node, Serializable value) {
		for (PropertyDefinition propDef : getAllProperties(type).values()) {
			if (propDef.isMandatory()) {
				Serializable currentValue = this.getNodeService().getProperty(node, propDef.getName());
				if (currentValue == null) {
					this.getNodeService().setProperty(node, propDef.getName(), value);
				}
			}
		}
	}
	
	protected void synchronizeUCMPropertyValues(NodeRef from, NodeRef to, Set<QName> exclude) {
		QName fromType = this.getNodeService().getType(from);
		QName toType = this.getNodeService().getType(to);
		
		Set<QName> fromProps = getAllProperties(fromType).keySet();
		Set<QName> toProps = getAllProperties(toType).keySet();
		
		Set<QName> commonProps = new HashSet<QName>(fromProps);
		commonProps.retainAll(toProps);
		commonProps.removeAll(exclude);
		
		for (QName propQname : commonProps) {
			if (UCMConstants.UCM_NAMESPACE.equals(propQname.getNamespaceURI())) {
				Serializable value = this.getNodeService().getProperty(from, propQname);
				if (value != null) {
					this.getNodeService().setProperty(to, propQname, value);
				}
			}
		}
	}
	
	public HashMap<QName, PropertyDefinition> getAllProperties(QName typeQname) {
		TypeDefinition typeDef = this.getDictionaryService().getType(typeQname);
		return getAllProperties(typeDef);
	}
	
	public static HashMap<QName, PropertyDefinition> getAllProperties(TypeDefinition type) {
		HashMap<QName, PropertyDefinition> result = new HashMap<QName, PropertyDefinition>();
		result.putAll(type.getProperties());
		for (AspectDefinition aspect : type.getDefaultAspects()) {
			result.putAll(aspect.getProperties());
		}
		return result;
	}
	
	@Override
	public void beforeGenerate(T item, List<String> fields, List<String> forcedFields, Form form,
			Map<String, Object> context) {
	}

	@Override
	public void afterGenerate(T item, List<String> fields, List<String> forcedFields, Form form,
			Map<String, Object> context) {
	}

	@Override
	public void beforePersist(T item, FormData data) {
	}

	@Override
	public void afterPersist(T item, FormData data, NodeRef persistedObject) {
	}
	
	public NodeService getNodeService() {
		return nodeService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public DictionaryService getDictionaryService() {
		return dictionaryService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public ContentService getContentService() {
		return contentService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public FileFolderService getFileFolderService() {
		return fileFolderService;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	public MimetypeService getMimetypeService() {
		return mimetypeService;
	}

	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
	}
}
