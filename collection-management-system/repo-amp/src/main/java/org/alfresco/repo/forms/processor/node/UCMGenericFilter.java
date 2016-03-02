package org.alfresco.repo.forms.processor.node;

import static org.alfresco.museum.ucm.UCMConstants.ASPECT_GEOGRAPHICAL_QNAME;
import static org.alfresco.museum.ucm.UCMConstants.CONTENT_PROP_DATA;
import static org.alfresco.museum.ucm.UCMConstants.NAME_PROP_DATA;
import static org.alfresco.repo.forms.processor.node.FormFieldConstants.PROP_DATA_PREFIX;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.alfresco.model.ContentModel;
import org.alfresco.museum.ucm.UCMConstants;
import org.alfresco.museum.ucm.utils.NodeUtils;
import org.alfresco.museum.ucm.utils.UCMContentImpl;
import org.alfresco.repo.content.transform.magick.ImageTransformationOptions;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.FormException;
import org.alfresco.repo.forms.processor.AbstractFilter;
import org.alfresco.repo.forms.processor.AbstractFormProcessor;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.exception.TikaException;
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
	private NodeUtils utils;
	private NodeService nodeService;
	private ContentService contentService;
	private DictionaryService dictionaryService;
	private FileFolderService fileFolderService;
	private MimetypeService mimetypeService;

	private static Log LOGGER = LogFactory.getLog(UCMGenericFilter.class);

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
		while (null != this.getUtils().childByNamePath(parentDirectory, tmpFilename)) {
			tmpFilename = generateFilenameWithIndex(originalFilename, counter);
			++counter;
		}
		return tmpFilename;
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
		return getNodeName(siteNodeRef, UCMConstants.TYPE_UCM_SITE_QNAME);

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
	 * Store "cm:content" property value, which is ignored by default handler.<br/>
	 * See
	 * {@link org.alfresco.repo.forms.processor.node.ContentModelFormProcessor#persistNode(NodeRef, FormData)
	 * persistNode},
	 * {@link org.alfresco.repo.forms.processor.node.ContentModelFormProcessor#processPropertyPersist(NodeRef, Map,FieldData, Map, FormData)
	 * processPropertyPersist},
	 * {@link org.alfresco.repo.forms.processor.node.ContentModelFormProcessor#processContentPropertyPersist(NodeRef, FieldData, Map, FormData)
	 * processContentPropertyPersist} and <a href=
	 * "https://forums.alfresco.com/forum/developer-discussions/alfresco-share-development/file-upload-create-content-06282010-2333"
	 * >discussion thread</a>
	 */
	protected void writeContent(TypeDefinition item, FormData data, NodeRef persistedObject) {
		writeContent(item, data, persistedObject, true);
	}

	protected void writeContent(TypeDefinition item, FormData data, NodeRef persistedObject, boolean allowResetContent) {
		FieldData contentField = data.getFieldData(CONTENT_PROP_DATA);
		if (contentField != null && contentField.isFile()) {
			// if we have a property definition attempt the persist
			PropertyDefinition propDef = item.getProperties().get(ContentModel.PROP_CONTENT);
			if (propDef != null && propDef.getDataType().getName().equals(DataTypeDefinition.CONTENT)) {
				try {
					FieldData mimetypeField = data.getFieldData(PROP_DATA_PREFIX + MimetypeFieldProcessor.KEY);
					String mimetype = null;
					if (mimetypeField != null) {
						mimetype = Objects.toString(mimetypeField.getValue());
					}
					UCMContentImpl ucmContent;
					if (mimetype == null) {
						ucmContent = new UCMContentImpl(this.getMimetypeService(), contentField);
					}
					else {
						ucmContent = new UCMContentImpl(contentField, mimetype);
					}

					boolean isContentReset = ucmContent.isEmpty();
					boolean isUpdateAllowed = (!isContentReset) || allowResetContent;

					if (isUpdateAllowed) {
						//process lat/lon data, etc
						processMetadata(ucmContent.getInputStream(), persistedObject);

						// write the content
						ContentWriter writer = this.getContentService().getWriter(persistedObject, ContentModel.PROP_CONTENT,
								true);
						writer.setMimetype(ucmContent.getMimetype());
						writer.setEncoding(ucmContent.getEncoding());
						writer.putContent(ucmContent.getInputStream());
					}
				} catch (IOException e) {
					LOGGER.error("Can't create copy of image stream", e);
				}
			}
		}
	}

	protected void resizeImage(NodeRef image, int minWidth) {
		// property is updated only after writer stream is closed so it is safe to read and write from same node.
		ContentReader reader = this.getContentService().getReader(image, ContentModel.PROP_CONTENT);
		ContentWriter writer = this.getContentService().getWriter(image, ContentModel.PROP_CONTENT, true);

		ImageTransformationOptions options = new ImageTransformationOptions();
		//If width is less than minWidth then upscale image. Do nothing otherwise ('<')
		options.setCommandOptions(String.format("-resize %dx<", minWidth));

		try {
			this.getContentService().getImageTransformer().transform(reader, writer, options);
		} catch (Exception e) {
			//It is ok to have error here, because node may actually be non-image node.
			LOGGER.warn("Image resize failed!", e);
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
			LOGGER.warn("Can't read image content, skipping", e);
		} catch (TikaException te) {
			LOGGER.warn("Caught tika exception, skipping", te);
		} catch (SAXException se) {
			LOGGER.warn("Caught SAXException, skipping", se);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.reset();
				} catch (IOException e) {
					LOGGER.warn("Can't reset image stream", e);
				}
			}
		}
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

	public NodeUtils getUtils() {
		return utils;
	}

	public void setUtils(NodeUtils utils) {
		this.utils = utils;
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
