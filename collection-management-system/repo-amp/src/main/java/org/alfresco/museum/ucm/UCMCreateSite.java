package org.alfresco.museum.ucm;

import java.io.IOException;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.museum.ucm.formfilters.UCMCreateCollection;
import org.alfresco.museum.ucm.utils.NodeUtils;
import org.alfresco.museum.ucm.utils.UCMContentImpl;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.domain.node.ContentDataWithId;
import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.repo.thumbnail.ThumbnailRegistry;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.ScriptRemote;
import org.springframework.extensions.webscripts.ScriptRemoteConnector;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.connector.Response;
import org.springframework.extensions.webscripts.servlet.FormData;
import org.springframework.extensions.webscripts.servlet.FormData.FormField;

//TODO: constants for field names?
public class UCMCreateSite extends DeclarativeWebScript {
	private static Log LOGGER = LogFactory.getLog(UCMCreateSite.class);

	// SurfConfigFolderPatch.SURF_CONFIG is private
	public static final String SURF_CONFIG_FOLDER = "surf-config";
	public static final String SYSTEM_FOLDER_NAME = "system";
	public static final String SYSTEM_WIKI_NAME = "wiki";
	public static final String SHARE_ENDPOINT_ID = "share";
	public static final String SITE_TEMPLATE = "ucm-site-dashboard";
	public static final String MODEL_SUCCESS = "success";
	public static final String MODEL_SITE_ADMIN = "siteAdmin";
	public static final String MODEL_SITE_SHORT_NAME = "siteShortName";
	public static final String MODEL_SITE_IS_PRIVATE = "siteIsPrivate";
	public static final String MODEL_SITE_NODE_REF = "siteNodeRef";
	// this file is created by ucm.spacesBootstrap bean
	public static final String NOTIFICATION_MAIL_TEMPLATE_PATH = "Data Dictionary/Email Templates/UCM email templates/notify_admin_user_created_email_template.html.ftl";
	public static final String LOGO_NAME = "logo";
	public static final String COPYRIGHT_DOC_NAME = "Copyright Materials";
	public static final int PASSWORD_LENGTH = 10;
	public static final int MAX_RETRIES_TO_FIND_FREE_USER_NAME_1000 = 1000;

	public static final Collection<String> OBLIGATORY_FIELDS = Collections.unmodifiableCollection(Arrays
			.asList(new String[] { "siteName", "siteAdminFirstName", "siteAdminLastName", "siteAdminEmail",
					"museumPhone", "museumEmail" }));

	@SuppressWarnings("serial")
	public static final Map<String, QName> FORM_FIELD_TO_ADMIN_PROPERTY = Collections
			.unmodifiableMap(new HashMap<String, QName>() {
				{
					this.put("siteAdminFirstName", ContentModel.PROP_FIRSTNAME);
					this.put("siteAdminLastName", ContentModel.PROP_LASTNAME);
					this.put("siteAdminEmail", UCMConstants.ASPECT_PROP_UCM_SITE_ASPECT_CONTACT_EMAIL_QNAME);
					this.put("museumEmail", ContentModel.PROP_EMAIL);
					// TODO: ContentModel.PROP_ORGID,
					// ContentModel.PROP_SIZE_CURRENT,
					// ContentModel.PROP_SIZE_QUOTA ?
				}
			});

	@SuppressWarnings("serial")
	public static final Map<String, QName> FORM_FIELD_TO_COLLECTION_PROPERTY = Collections
			.unmodifiableMap(new HashMap<String, QName>() {
				{
					// TODO: collection's own name?
					this.put("collectionName", ContentModel.PROP_NAME);
					this.put("collectionID", UCMConstants.PROP_UCM_COLLECTION_ID_QNAME);
					// TODO: curator, etc.
				}
			});

	@SuppressWarnings("serial")
	public static final Map<String, QName> FORM_FIELD_TO_SITE_ASPECT_PROPERTY = Collections
			.unmodifiableMap(new HashMap<String, QName>() {
				{
					this.put("siteName", UCMConstants.ASPECT_PROP_UCM_SITE_NAME_QNAME);
					this.put("siteType", UCMConstants.ASPECT_PROP_UCM_SITE_TYPE_QNAME);
					this.put("museumAddress", UCMConstants.ASPECT_PROP_UCM_SITE_ADDRESS_QNAME);
					// this.put("siteBuildYear",
					// UCMConstants.ASPECT_PROP_UCM_SITE_BUILD_YEAR_QNAME);
					// this.put("siteType",
					// UCMConstants.ASPECT_PROP_UCM_SITE_ACPECT_TYPE_QNAME);
					// this.put("siteContactPerson",
					// UCMConstants.ASPECT_PROP_UCM_SITE_ASPECT_CONTACT_PERSON_QNAME);
					// this.put("siteContactTweed",
					// UCMConstants.ASPECT_PROP_UCM_SITE_ASPECT_CONTACT_TWEED_QNAME);
					this.put("museumPhone", UCMConstants.ASPECT_PROP_UCM_SITE_ASPECT_CONTACT_PHONE_QNAME);
					// this.put("museumFax",
					// UCMConstants.ASPECT_PROP_UCM_SITE_ASPECT_CONTACT_FAX_QNAME);
					this.put(MODEL_SITE_IS_PRIVATE, UCMConstants.ASPECT_PROP_UCM_SITE_ASPECT_VISIBILITY_QNAME);
				}
			});

	private AuthorityService authorityService;
	private PersonService personService;
	private PermissionService permissionService;
	private MutableAuthenticationService authenticationService;
	private SiteService siteService;
	private NodeService nodeService;
	private ContentService contentService;
	private DictionaryService dictionaryService;
	private FileFolderService fileFolderService;
	private MimetypeService mimetypeService;
	private ActionService actionService;
	private ScriptRemote remote;
	private NodeUtils utils;
	private Properties properties;
	private ServiceRegistry serviceRegistry;

	/**
	 * Late phases of site creation depend on objects created earlier. UCMSite
	 * is intended to keep state of site attributes during initialization.
	 */
	public static class UCMSite {
		public UCMSite(String name, String description, boolean isPrivate) {
			this.name = name.trim();
			this.shortName = siteTitleToShortName(name);
			this.description = StringUtils.defaultString(description);
			this.isPrivate = isPrivate;
		}

		public final String name;
		public final String shortName;
		public final String description;
		public final boolean isPrivate;
		public SiteInfo site;
		public NodeRef system;
		public NodeRef surfConfig;
		public NodeRef documentLibrary;
		public NodeRef wiki;
		public String adminName;
	}

	private static void logAndThrow(String description) throws AlfrescoRuntimeException {
		logAndThrow(description, null);
	}

	private static void logAndThrow(String description, Exception cause) throws AlfrescoRuntimeException {
		LOGGER.error("Site creation was cancelled due to error: " + description);
		if (cause != null) {
			throw new AlfrescoRuntimeException(description, cause);
		} else {
			throw new AlfrescoRuntimeException(description);
		}
	}

	@Override
	public Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		LOGGER.warn("Site creation process started!");

		Object parseContent = req.parseContent();
		if (!(parseContent instanceof FormData)) {
			logAndThrow("Wrong form format! Form should be submitted with type \"multipart/form-data\".");
			return null;
		}

		FormData formData = (FormData) parseContent;
		String validationError = validateForm(formData);
		if (validationError != null) {
			logAndThrow(validationError);
			return null;
		}

		String siteName = getOnlyValue(formData, "siteName");
		String siteDescription = getOnlyValue(formData, "siteDescription");
		boolean isPrivate = StringUtils.equals("on", getOnlyValue(formData, MODEL_SITE_IS_PRIVATE));

		UCMSite site = new UCMSite(siteName, siteDescription, isPrivate);
		Map<QName, Serializable> siteData = fillPropertiesWithFormData(FORM_FIELD_TO_SITE_ASPECT_PROPERTY, formData);

		LOGGER.warn(String.format("Site will have name %s, and is %s", siteName, (isPrivate) ? "private" : "moderated"));
		// create site node, fill site aspect properties
		site = createSite(site, siteData);
		LOGGER.info("Site node created.");

		try {
			LOGGER.info("Retrieving site templates data.");
			// get data about surf config files from Share web script
			JSONObject templatesJson = getTemplatesData(SITE_TEMPLATE, site.shortName);
			LOGGER.info("Site templates retrieved succesfully.");

			// create folders "system" and "documentLibrary", files required by
			// Surf,
			site = createSiteContent(site, templatesJson);
			LOGGER.info("Site templates and service folders created.");
		} catch (IOException | JSONException e) {
			logAndThrow("Can't create template instance elements.", e);
			return null;
		}

		try {
			// create site logo file in "system"
			site = createSiteLogo(site, getLogoField(formData));
		} catch (IOException | JSONException e) {
			LOGGER.warn("Can't set site logo. It could be set later manually.", e);
		}

		Map<QName, Serializable> collectionData = fillPropertiesWithFormData(FORM_FIELD_TO_COLLECTION_PROPERTY,
				formData);
		// only if coll. id and coll. name are both filled
		if (collectionData.size() == 2) {
			Map<QName, Serializable> fullCollectionData = mergeMaps(siteData, collectionData);
			LOGGER.info("Creating collection.");
			site = createCollection(site, fullCollectionData);
			LOGGER.info("Collection created.");
		}

		// museum document has no specific fields except those inherited from
		// site aspect
		LOGGER.info("Creating \"About museum\" document.");
		site = createAboutMuseumDocument(site, siteData);
		LOGGER.info("\"About museum\" document has been created.");

		// museum document has no specific fields except those inherited from
		// site aspect
		LOGGER.info("Creating \"Wiki template\" document.");
		site = createWikiTemplateDocument(site, siteData);
		LOGGER.info("\"AWiki template\" document has been created.");

		LOGGER.info("Creating custom folders.");
		site = createAdditionalFolders(site, formData.getParameters().get("siteFoldersSelectedOptions"));
		LOGGER.info("Custom folders have been created.");

		String copyrightText = getOnlyValue(formData, "copyright");
		if (copyrightText != null) {
			LOGGER.info("Creating copyright document.");
			site = createCopyrightDocument(site, copyrightText, siteData);
			LOGGER.info("Copyright document created.");
		}

		try {
			LOGGER.info("Adding user \"visitor\" to site consumers group.");
			site = giveAccessToVisitor(site);
			LOGGER.info("User \"visitor\" was granted site consumer privilegies.");
		} catch (RuntimeException | JSONException e) {
			LOGGER.warn("Can't add user visitor to site consumers group!", e);
		}

		// Admin is created in the end of site creation. Nothing should rollback
		// transaction after notification email was sent.
		Map<QName, Serializable> adminData = fillPropertiesWithFormData(FORM_FIELD_TO_ADMIN_PROPERTY, formData);
		try {
			LOGGER.info("Creating admin user, setting up groups and access rules.");
			site = createAdminUser(site, adminData);
			LOGGER.info("Admin user created. New user name: " + site.adminName);
		} catch (FileNotFoundException e) {
			logAndThrow("Can not create site Adminstrator. Mail template not found ",e);
			return null;
		}

		LOGGER.warn("Site creation completed!");
		return createSuccessModel(site);
	}

	public String validateForm(FormData formData) {
		String error = null;
		List<String> missed = new ArrayList<String>(5);
		for (String fieldName : OBLIGATORY_FIELDS) {
			if (getOnlyValue(formData, fieldName) == null) {
				missed.add(fieldName);
			}
		}

		if (!missed.isEmpty()) {
			error = "Obligatory fields aren't filled: " + StringUtils.join(missed, ", ");
		}

		return error;
	}

	public UCMSite createSite(UCMSite site, Map<QName, Serializable> siteData) {
		SiteVisibility visibility = (site.isPrivate) ? SiteVisibility.PRIVATE : SiteVisibility.MODERATED;

		//ensure that visibility property contains safe value: "PRIVATE" or "PUBLIC"
		String siteVisibilityPropValue = (site.isPrivate) ? "PRIVATE" : "PUBLIC";
		siteData.put(UCMConstants.ASPECT_PROP_UCM_SITE_ASPECT_VISIBILITY_QNAME, siteVisibilityPropValue);

		// create site node
		site.site = this.getSiteService().createSite(SITE_TEMPLATE, site.shortName, site.name, site.description,
				visibility, UCMConstants.TYPE_UCM_SITE_QNAME);

		// fill site aspect properties
		this.getNodeService().addAspect(site.site.getNodeRef(), UCMConstants.ASPECT_SITE_QNAME, siteData);

		return site;
	}

	/**
	 * Retrieve from share side data about all the elements which should be
	 * created for site to work.
	 */
	public JSONObject getTemplatesData(String presetId, String siteId) throws IOException, JSONException {
		Response response = null;

		String context = this.getProperties().getProperty("share.context", "share");
		String encodedPresetId = URLEncoder.encode(presetId, StandardCharsets.UTF_8.name());
		String encodedSiteId = URLEncoder.encode(siteId, StandardCharsets.UTF_8.name());
		String path = String.format("/%s/page/ucm/create-site-templates?presetId=%s&siteid=%s", context,
				encodedPresetId, encodedSiteId);

		// TODO: use empty endpoint and build complete URL with Share protocol,
		// host and port from "share.*" properties?
		ScriptRemoteConnector connector = remote.connect(SHARE_ENDPOINT_ID);
		response = connector.call(path);

		return (response != null) ? new JSONObject(response.getText()) : null;
	}

	/**
	 * Creates component and page files required for site to operate. File
	 * structure to create inside site root node:
	 *
	 * <pre>
	 * surf-config/
	 * 		components/
	 * 			page.title.site~(siteName)~dashboard.xml
	 * 			...
	 * 		pages/
	 * 			site/
	 * 				(siteName)/
	 * 					dashboard.xml
	 * 					...
	 * 	documentLibrary/
	 * 	system/
	 * </pre>
	 *
	 * @param site
	 *            Site to create content in.
	 * @param objectsData
	 *            Expected data structure:
	 *
	 *            <pre>
	 * {
	 * 	"pages": [{ "id":"site/{siteName}/dashboard", "xml":"..."}, ...],
	 * 	"components":[{"id":"page.title.site~{siteName}~dashboard", "xml":"..." }, ...]
	 * }
	 * </pre>
	 *
	 * <br/>
	 * @throws JSONException
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	public UCMSite createSiteContent(UCMSite site, JSONObject objectsData) throws JSONException {
		NodeRef siteNodeRef = site.site.getNodeRef();
		site.surfConfig = this.getFileFolderService().create(siteNodeRef, SURF_CONFIG_FOLDER, ContentModel.TYPE_FOLDER)
				.getNodeRef();
		site.documentLibrary = this.getFileFolderService()
				.create(siteNodeRef, SiteService.DOCUMENT_LIBRARY, ContentModel.TYPE_FOLDER).getNodeRef();
		site.system = this.getFileFolderService().create(siteNodeRef, SYSTEM_FOLDER_NAME, ContentModel.TYPE_FOLDER)
				.getNodeRef();
		site.wiki = this.getFileFolderService().create(siteNodeRef, SYSTEM_WIKI_NAME, ContentModel.TYPE_FOLDER)
				.getNodeRef();


		for (Iterator<String> iterator = objectsData.keys(); iterator.hasNext();) {
			// E.g. "pages" or "components"
			String objectsTypeName = iterator.next();
			// E.g. [{ "id":"site/{siteName}/dashboard", "xml":"..."}, ...]
			JSONArray objectsList = objectsData.getJSONArray(objectsTypeName);
			for (int i = 0; i < objectsList.length(); i++) {
				// E.g. { "id":"site/{siteName}/dashboard", "xml":"..."}
				JSONObject object = objectsList.getJSONObject(i);
				// E.g. "site/{siteName}/dashboard"
				String objectId = object.getString("id");
				String objectXml = object.getString("xml");

				List<String> objectPath = new ArrayList<String>(4);
				// E.g. ["pages"] or ["components"]
				objectPath.add(objectsTypeName);
				// E.g. ["site", "{siteName}", "dashboard"] or just
				// ["page.title.site~{siteName}~dashboard"]
				LinkedList<String> pathTokens = new LinkedList<String>(Arrays.asList(objectId.split("/")));
				String fileName = pathTokens.pollLast() + ".xml";
				objectPath.addAll(pathTokens);

				NodeRef objectFolderNodeRef = this.getUtils().getOrCreateFolderByPath(site.surfConfig, objectPath);

				NodeRef objectNodeRef = this.getUtils().createContentNode(objectFolderNodeRef, fileName, objectXml);
			}
		}

		return site;
	}

	/**
	 * Set logo NodeRef to site dashboard template property at Share side and
	 * retrieve updated template. Template should be saved under path
	 * surf-config/pages/site/{siteName}/dashboard.xml
	 *
	 * @return {"id": "site/{siteName}/dashboard", "xml": ${...}}
	 */
	public JSONObject getPageTemplateWithLogo(String siteId, NodeRef logoNodeRef) throws IOException, JSONException {
		Response response = null;

		String context = this.getProperties().getProperty("share.context", "share");
		String encodedSiteId = URLEncoder.encode(siteId, StandardCharsets.UTF_8.name());
		String encodedLogoNodeRef = URLEncoder.encode(logoNodeRef.toString(), StandardCharsets.UTF_8.name());
		String path = String.format("/%s/page/ucm/get-page-template?siteId=%s&logoNodeRef=%s", context, encodedSiteId,
				encodedLogoNodeRef);

		// TODO: use empty endpoint and build complete URL with Share protocol,
		// host and port from "share.*" properties?
		ScriptRemoteConnector connector = remote.connect(SHARE_ENDPOINT_ID);
		response = connector.call(path);

		return (response != null) ? new JSONObject(response.getText()) : null;
	}

	public UCMSite createSiteLogo(UCMSite site, FormField logoField) throws IOException, JSONException {
		if (logoField != null && logoField.getContent() != null && logoField.getContent().getSize() > 0) {
			LOGGER.info("Saving site logo to database.");
			NodeRef logoNodeRef = this.getUtils().createContentNode(site.system, LOGO_NAME,
					new UCMContentImpl(logoField), UCMConstants.TYPE_UCM_DOCUMENT_QNAME);

			LOGGER.info("Retrieving site dashboard template.");
			JSONObject updatedTemplate = getPageTemplateWithLogo(site.shortName, logoNodeRef);
			LOGGER.info("Adding site logo to dashboard template.");

			// E.g. pages/site/{siteName}/dashboard.xml
			String fullObjectPath = "pages/" + updatedTemplate.getString("id") + ".xml";
			// E.g. ["pages", "site", {siteName}, "dashboard.xml"]
			LinkedList<String> pathTokens = new LinkedList<String>(Arrays.asList(fullObjectPath.split("/")));
			// E.g. "dashboard.xml"
			String fileName = pathTokens.pollLast();
			String objectXml = updatedTemplate.getString("xml");

			NodeRef objectFolderNodeRef = this.getUtils().getOrCreateFolderByPath(site.surfConfig, pathTokens);

			NodeRef existingTemplate = this.getNodeService().getChildByName(objectFolderNodeRef,
					ContentModel.ASSOC_CONTAINS, fileName);
			if (existingTemplate != null) {
				this.getNodeService().removeChild(objectFolderNodeRef, existingTemplate);
			}

			// This node is used as a config file by Share, so now Share knows
			// where to look for site logo
			@SuppressWarnings("unused")
			NodeRef objectNodeRef = this.getUtils().createContentNode(objectFolderNodeRef, fileName, objectXml);

			// create doclib thumbnail
			this.createThumbnail(logoNodeRef, "doclib");

			// store reference to logo inside site node
			// TODO: use association?
			this.getNodeService().setProperty(site.site.getNodeRef(), UCMConstants.PROP_UCM_SITE_LOGO_REF_QNAME,
					logoNodeRef.toString());

			LOGGER.info("Site logo have been set.");
		}
		return site;
	}

	/**
	 * @see {@link org.alfresco.repo.jscript.ScriptNode#createThumbnail(String) ScriptNode.createThumbnail()}
	 */
	public NodeRef createThumbnail(NodeRef nodeRef, String thumbnailName) {
		final ThumbnailService thumbnailService = this.getServiceRegistry().getThumbnailService();

		NodeRef result = null;

		// Use the thumbnail registy to get the details of the thumbnail
		ThumbnailRegistry registry = thumbnailService.getThumbnailRegistry();
		ThumbnailDefinition details = registry.getThumbnailDefinition(thumbnailName);
		if (details == null) {
			return null;
		}

		// If there's nothing currently registered to generate thumbnails for
		// the specified mimetype, then log a message and bail out
		String nodeMimeType = null;
		long size = 0l;
		ContentDataWithId content = (ContentDataWithId) this.getNodeService().getProperties(nodeRef)
				.get(ContentModel.PROP_CONTENT);
		if (content != null) {
			nodeMimeType = content.getMimetype();
			size = content.getSize();
		}
		Serializable value = this.nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
		ContentData contentData = DefaultTypeConverter.INSTANCE.convert(ContentData.class, value);
		if (!ContentData.hasContent(contentData)
				|| !this.getContentService().getReader(nodeRef, ContentModel.PROP_CONTENT).exists()) {
			LOGGER.debug("Unable to create thumbnail '" + details.getName() + "' as there is no content");
			return null;
		}
		if (!registry.isThumbnailDefinitionAvailable(contentData.getContentUrl(), nodeMimeType, size, nodeRef, details)) {
			LOGGER.info("Unable to create thumbnail '" + details.getName() + "' for " + nodeMimeType
					+ " as no transformer is currently available.");
			return null;
		}

		try {
			// Create the thumbnail
			result = thumbnailService.createThumbnail(nodeRef, ContentModel.PROP_CONTENT, details.getMimetype(),
					details.getTransformationOptions(), details.getName());
		} catch (AlfrescoRuntimeException e) {
			LOGGER.debug("Unable to create thumbnail '" + details.getName() + "' as " + e.getMessage());
			return null;
		}
		return result;
	}

	/**
	 * Create node of type ucm:collection inside documentLibrary, and set
	 * collection properties to it.
	 */
	public UCMSite createCollection(UCMSite site, Map<QName, Serializable> collectionData) {
		String collectionName = collectionData.get(ContentModel.PROP_NAME).toString();

		FileInfo collection = this.getFileFolderService().create(site.documentLibrary, collectionName,
				UCMConstants.TYPE_UCM_COLLECTION_QNAME);

		this.getNodeService().addProperties(collection.getNodeRef(), collectionData);

		// Collection has been created programmatically and CreateCollection
		// filter wasn't invoked. We need to run processing manually.
		// Otherwise "Uploader Plus" plugin wouldn't be configured properly.
		UCMCreateCollection.afterCreateCollection(collection.getNodeRef(), this.getNodeService());

		return site;
	}

	/**
	 * Create node of type ucm:document inside documentLibrary, and set site
	 * aspect properties to it.
	 */
	public UCMSite createAboutMuseumDocument(UCMSite site, Map<QName, Serializable> museumProps) {
		String documentName = "About " + site.shortName;
		String documentTitle = "About " + site.name;
		museumProps.put(ContentModel.PROP_TITLE, documentTitle);

		// create empty "about museum" document
		NodeRef aboutMuseum = this.getUtils().createContentNode(site.documentLibrary, documentName, "",
				UCMConstants.TYPE_UCM_DOCUMENT_QNAME);

		this.getNodeService().addAspect(aboutMuseum, UCMConstants.ASPECT_SITE_QNAME, museumProps);

		return site;
	}

	/**
	 * Create node of type cm:wiki inside site:wiki, and set site
	 * aspect properties to it.
	 */
	public UCMSite createWikiTemplateDocument(UCMSite site, Map<QName, Serializable> siteData)
	{
		String documentName = "Main_Page";
		String documentTitle = "Main_Page";
		String defaultWikiMessage="Add content to your first wiki page.";
		siteData.put(ContentModel.PROP_TITLE, documentTitle);

		// create empty "Wiki" document
		NodeRef wikiPage = this.getUtils().createContentNode(site.wiki, documentName, defaultWikiMessage,
				UCMConstants.TYPE_UCM_DOCUMENT_QNAME);
		this.getNodeService().addAspect(wikiPage, UCMConstants.ASPECT_SITE_QNAME, siteData);

		return site;
	}

	public UCMSite createAdditionalFolders(UCMSite site, String[] folders) {
		if (folders != null && folders.length > 0) {
			for (String folderName : folders) {
				if (StringUtils.isNotBlank(folderName)) {
					try {
						this.getFileFolderService().create(site.documentLibrary, folderName,
								UCMConstants.TYPE_UCM_FOLDER_QNAME);
					} catch (RuntimeException e) {
						// Do not fail transaction. Most probably folder name is
						// invalid.
						LOGGER.error("Can't create additional folder \"" + folderName + "\"", e);
					}
				}
			}
		}
		return site;
	}

	public UCMSite createCopyrightDocument(UCMSite site, String text, Map<QName, Serializable> siteData) {
		NodeRef documentRef = this.getUtils().createContentNode(site.documentLibrary, COPYRIGHT_DOC_NAME, text,
				UCMConstants.TYPE_UCM_DOCUMENT_QNAME);
		this.getNodeService().addAspect(documentRef, UCMConstants.ASPECT_SITE_QNAME, siteData);
		return site;
	}

	// add visitor to site consumers group
	public UCMSite giveAccessToVisitor(UCMSite site) throws JSONException {
		if (!site.isPrivate) {
			ScriptRemoteConnector connector = remote.connect();
			Response response = connector.call(UCMConstants.ANONIMOUS_USER_DETAILS_WEBSCRIPT_PATH);
			if (response != null) {
				JSONObject visitorUserDetails = new JSONObject(response.getText());
				String visitorUsername = visitorUserDetails.getString(UCMAnonymousUser.USERNAME_PROERTY_NAME);
				this.getAuthorityService().addAuthority(getSiteConsumerGroupName(site.shortName), visitorUsername);
			}
		}
		return site;
	}

	/**
	 * See <a href=
	 * "https://forums.alfresco.com/forum/general/non-technical-alfresco-discussion/creating-users-using-java-api-10032008-1248"
	 * >discussion thread</a>
	 *
	 * @throws FileNotFoundException
	 */
	public UCMSite createAdminUser(UCMSite site, Map<QName, Serializable> userProps) throws FileNotFoundException {
		String firstName = userProps.get(ContentModel.PROP_FIRSTNAME).toString();
		String lastName = userProps.get(ContentModel.PROP_LASTNAME).toString();

		// Find free user name
		site.adminName = createFreeUserName(firstName, lastName);

		userProps.put(ContentModel.PROP_USERNAME, site.adminName);

		// create the node to represent the Person
		NodeRef newPerson = this.getPersonService().createPerson(userProps);

		String allPermission = this.getPermissionService().getAllPermission();
		// ensure the user can access their own Person object
		this.getPermissionService().setPermission(newPerson, site.adminName, allPermission, true);

		// add user to site managers group
		this.getAuthorityService().addAuthority(getSiteManagerGroupName(site.shortName), site.adminName);

		String email = userProps.get(UCMConstants.ASPECT_PROP_UCM_SITE_ASPECT_CONTACT_EMAIL_QNAME).toString();
		String password = createPassword();

		// create the ACEGI Authentication instance for the new user
		this.getAuthenticationService().createAuthentication(site.adminName, password.toCharArray());

		// https://loftux.com/en/blog/fixing-the-invite-email-template-in-alfresco-share#sthash.IQx2RxYt.dpbs
		sendNotificationEmail(email, site.name, site.shortName, firstName, lastName, site.adminName, password);
		return site;
	}

	/**
	 * See <a href=
	 * "http://www.codified.com/alfresco-send-emails-using-html-email-template/"
	 * >link</a>
	 */
	public void sendNotificationEmail(String email, String siteName, String siteShortName, String firstName,
			String lastName, String adminName, String password) throws FileNotFoundException {
		Action mailAction = this.getActionService().createAction(MailActionExecuter.NAME);
		mailAction.setParameterValue(MailActionExecuter.PARAM_TO, email);
		mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, "New site has been created successfully!");
		// TODO: mailAction.setParameterValue(MailActionExecuter.PARAM_HTML,
		// "true");

		NodeRef companyHome = this.getUtils().getCompanyHomeNodeRef();
		List<String> templatePath = Arrays.asList(StringUtils.split(NOTIFICATION_MAIL_TEMPLATE_PATH, '/'));
		NodeRef emailTemplate = this.getFileFolderService().resolveNamePath(companyHome, templatePath).getNodeRef();
		mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, emailTemplate);

		Map<String, Serializable> templateArgs = new HashMap<String, Serializable>();
		templateArgs.put("site_name", siteName);
		templateArgs.put("site_short_name", siteShortName);
		templateArgs.put("first_name", firstName);
		templateArgs.put("last_name", lastName);
		templateArgs.put("user_name", adminName);
		templateArgs.put("user_password", password);

		mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable) templateArgs);

		// mailAction.setParameterValue(MailActionExecuter.PARAM_FROM, "TODO!");

		// send mail immediately
		mailAction.setExecuteAsynchronously(false);

		this.getActionService().executeAction(mailAction, null);
	}

	public String createFreeUserName(String firstName, String lastName) {
		firstName = firstName.replaceAll("[^\\w]", "");
		firstName = StringUtils.defaultIfBlank(firstName, "_");
		lastName = lastName.replaceAll("[^\\w]", "");
		String userName = (firstName.substring(0, 1) + lastName).toLowerCase();
		if (this.getPersonService().personExists(userName)) {
			for (int i = 1; i < MAX_RETRIES_TO_FIND_FREE_USER_NAME_1000; ++i) {
				String userNameTry = userName + i;
				if (!this.getPersonService().personExists(userNameTry)) {
					userName = userNameTry;
					break;
				}
			}
		}
		return userName;
	}

	// E.g. "Site officiel du musee du 'Louvre'" ->
	// "site_officiel_du_musee_du_louvre"
	public static String siteTitleToShortName(String title) {
		return title.trim().replaceAll("\\s+", "_").replaceAll("[^\\w]", "").toLowerCase();
	}

	// TODO: something more secure?
	public static String createPassword() {
		return RandomStringUtils.randomAlphanumeric(PASSWORD_LENGTH);
	}

	//GROUP_site_testsite_SiteConsumer
	public static String getSiteConsumerGroupName(String shortSiteName) {
		return "GROUP_site_" + shortSiteName + "_SiteConsumer";
	}

	//GROUP_site_testsite_SiteManager
	public static String getSiteManagerGroupName(String shortSiteName) {
		return "GROUP_site_" + shortSiteName + "_SiteManager";
	}

	public static Map<String, Object> createSuccessModel(UCMSite site) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(MODEL_SUCCESS, true);
		model.put(MODEL_SITE_NODE_REF, site.site.getNodeRef().toString());
		model.put(MODEL_SITE_IS_PRIVATE, site.isPrivate);
		model.put(MODEL_SITE_SHORT_NAME, site.shortName);
		model.put(MODEL_SITE_ADMIN, site.adminName);
		return model;
	}

	public static Map<QName, Serializable> fillPropertiesWithFormData(Map<String, QName> fieldMapping, FormData formData) {
		Map<QName, Serializable> result = new HashMap<QName, Serializable>();
		for (String fieldName : fieldMapping.keySet()) {
			QName propQName = fieldMapping.get(fieldName);
			String value = getOnlyValue(formData, fieldName);
			if (propQName != null && value != null) {
				result.put(propQName, value);
			}
		}
		return result;
	}

	public static FormField getField(FormData formData, String fieldName) {
		FormField result = null;
		for (FormField field : formData.getFields()) {
			if (StringUtils.equals(fieldName, field.getName())) {
				result = field;
				break;
			}
		}
		return result;
	}

	public static FormField getLogoField(FormData formData) {
		return getField(formData, "siteLogo");
	}

	public static String getOnlyValue(FormData formData, String paramName) {
		String result = null;
		String[] values = formData.getParameters().get(paramName);
		if (values != null) {
			for (String value : values) {
				if (StringUtils.isNotBlank(value)) {
					result = value;
					break;
				}
			}
		}
		return result;
	}

	public static <K, V> Map<K, V> mergeMaps(Map<K, V> defaults, Map<K, V> override) {
		HashMap<K, V> result = new HashMap<K, V>(defaults);
		result.putAll(override);
		return result;
	}

	public AuthorityService getAuthorityService() {
		return authorityService;
	}

	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	public PersonService getPersonService() {
		return personService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public PermissionService getPermissionService() {
		return permissionService;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public MutableAuthenticationService getAuthenticationService() {
		return authenticationService;
	}

	public void setAuthenticationService(MutableAuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	public SiteService getSiteService() {
		return siteService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
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

	public ActionService getActionService() {
		return actionService;
	}

	public void setActionService(ActionService actionService) {
		this.actionService = actionService;
	}

	public ScriptRemote getRemote() {
		return remote;
	}

	public void setRemote(ScriptRemote remote) {
		this.remote = remote;
	}

	public NodeUtils getUtils() {
		return utils;
	}

	public void setUtils(NodeUtils utils) {
		this.utils = utils;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}
}
