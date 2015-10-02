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

import org.alfresco.model.ContentModel;
import org.alfresco.museum.ucm.utils.NodeUtils;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
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

//TODO: proper error handling
//TODO: constants for field names?
public class UCMCreateSite extends DeclarativeWebScript {
	private static Log LOGGER = LogFactory.getLog(UCMCreateSite.class);

	// SurfConfigFolderPatch.SURF_CONFIG is private
	public static final String SURF_CONFIG_FOLDER = "surf-config";
	public static final String SYSTEM_FOLDER_NAME = "system";
	public static final String SHARE_ENDPOINT_ID = "share";
	public static final String SITE_TEMPLATE = "ucm-site-dashboard";
	public static final String MODEL_SUCCESS = "success";
	public static final String NOTIFICATION_MAIL_TEMPLATE_PATH = "Data Dictionary/Email Templates/Notify Email Templates/notify_user_email.html.ftl";
	public static final String LOGO_NAME = "logo";
	public static final String COPYRIGHT_DOC_NAME = "Copyright Materials";
	public static final int PASSWORD_LENGTH = 10;
	public static final int MAX_RETRIES_TO_FIND_FREE_USER_NAME_1000 = 1000;

	public static final Collection<String> OBLIGATORY_FIELDS = Collections.unmodifiableCollection(Arrays
			.asList(new String[] { "siteName", "siteAdminFirstName", "siteAdminLastName", "siteAdminEmail",
					"museumName", "museumEmail", "museumPhone" }));

	@SuppressWarnings("serial")
	public static final Map<String, QName> FORM_FIELD_TO_ADMIN_PROPERTY = Collections
			.unmodifiableMap(new HashMap<String, QName>() {
				{
					this.put("siteAdminFirstName", ContentModel.PROP_FIRSTNAME);
					this.put("siteAdminLastName", ContentModel.PROP_LASTNAME);
					this.put("siteAdminEmail", ContentModel.PROP_EMAIL);
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
					this.put("museumAddress", UCMConstants.ASPECT_PROP_UCM_SITE_ADDRESS_QNAME);
					// this.put("siteBuildYear",
					// UCMConstants.ASPECT_PROP_UCM_SITE_BUILD_YEAR_QNAME);
					// this.put("siteType",
					// UCMConstants.ASPECT_PROP_UCM_SITE_ACPECT_TYPE_QNAME);
					// this.put("siteContactPerson",
					// UCMConstants.ASPECT_PROP_UCM_SITE_ASPECT_CONTACT_PERSON_QNAME);
					// this.put("siteContactTweed",
					// UCMConstants.ASPECT_PROP_UCM_SITE_ASPECT_CONTACT_TWEED_QNAME);
					this.put("museumEmail", UCMConstants.ASPECT_PROP_UCM_SITE_ASPECT_CONTACT_EMAIL_QNAME);
					this.put("museumPhone", UCMConstants.ASPECT_PROP_UCM_SITE_ASPECT_CONTACT_PHONE_QNAME);
					// this.put("museumFax",
					// UCMConstants.ASPECT_PROP_UCM_SITE_ASPECT_CONTACT_FAX_QNAME);
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

	/**
	 * Late phases of site creation depend on objects created earlier. UCMSite
	 * is intended to keep current state.
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
		public String adminName;
	}

	@Override
	public Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		Object parseContent = req.parseContent();
		if (!(parseContent instanceof FormData)) {
			return createErrorModel("Wrong form format! Form should be submitted with type \"multipart/form-data\".");
		}

		FormData formData = (FormData) parseContent;
		String validationError = validateForm(formData);
		if (validationError != null) {
			return createErrorModel(validationError);
		}

		String siteName = getOnlyValue(formData, "siteName");
		String siteDescription = getOnlyValue(formData, "siteDescription");
		boolean isPrivate = StringUtils.equals("true", getOnlyValue(formData, "siteIsPrivate"));

		UCMSite site = new UCMSite(siteName, siteDescription, isPrivate);
		Map<QName, Serializable> siteData = fillPropertiesWithFormData(FORM_FIELD_TO_SITE_ASPECT_PROPERTY, formData);

		// create site node, fill site aspect properties
		site = createSite(site, siteData);

		try {
			// get data about surf config files from Share web script
			JSONObject templatesJson = getTemplatesData(SITE_TEMPLATE, site.shortName);

			// create folders "system" and "documentLibrary", files required by
			// Surf,
			site = createSiteContent(site, templatesJson);
		} catch (IOException | JSONException e) {
			LOGGER.error("Can't create template instance elements", e);
			// TODO: fail transaction by throwing AlfrescoRuntimeException
		}

		try {
			// create site logo file in "system"
			site = createSiteLogo(site, getLogoField(formData));
		} catch (IOException e) {
			LOGGER.error("Can't set site logo", e);
		}

		Map<QName, Serializable> adminData = fillPropertiesWithFormData(FORM_FIELD_TO_ADMIN_PROPERTY, formData);
		try {
			// create admin user, set up groups and access
			site = createAdminUser(site, adminData);
		} catch (FileNotFoundException e) {
			LOGGER.error("Can't add site moderator", e);
			// TODO: fail transaction by throwing AlfrescoRuntimeException
		}

		Map<QName, Serializable> collectionData = fillPropertiesWithFormData(FORM_FIELD_TO_COLLECTION_PROPERTY, formData);
		// only if coll. id and coll. name are both filled
		if (collectionData.size() == 2) {
			Map<QName, Serializable> fullCollectionData = mergeMaps(siteData, collectionData);
			site = createCollection(site, fullCollectionData);
		}

		// museum document has no specific fields except those inherited from
		// site aspect
		site = createAboutMuseumDocument(site, siteData);

		site = createAdditionalFolders(site, formData.getParameters().get("siteFoldersSelectedOptions"));

		String copyrightText = getOnlyValue(formData, "copyright");
		if (copyrightText != null) {
			site = createCopyrightDocument(site, copyrightText, siteData);
		}

		return createSuccessModel();
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
				String fileName = pathTokens.getLast() + ".xml";
				pathTokens.removeLast();
				objectPath.addAll(pathTokens);

				NodeRef objectFolderNodeRef = this.getUtils().getOrCreateFolderByPath(site.surfConfig, objectPath);

				NodeRef objectNodeRef = this.getUtils().createContentNode(objectFolderNodeRef, fileName, objectXml);
			}
		}

		return site;
	}

	public UCMSite createSiteLogo(UCMSite site, FormField logoField) throws IOException {
		if (logoField != null && logoField.getContent() != null) {
			this.getUtils().createContentNode(site.system, LOGO_NAME, new UCMContentImpl(logoField),
					UCMConstants.TYPE_UCM_DOCUMENT_QNAME);
		}

		// TODO: set logo node ref as site property?
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

		@SuppressWarnings("unused")
		String email = userProps.get(ContentModel.PROP_EMAIL).toString();
		String password = createPassword();

		// create the ACEGI Authentication instance for the new user
		this.getAuthenticationService().createAuthentication(site.adminName, password.toCharArray());

		// https://loftux.com/en/blog/fixing-the-invite-email-template-in-alfresco-share#sthash.IQx2RxYt.dpbs
		// sendNotificationEmail(email, password);
		return site;
	}

	/**
	 * See <a href=
	 * "http://www.codified.com/alfresco-send-emails-using-html-email-template/"
	 * >link</a>
	 * 
	 * @param password
	 * @param email
	 */
	// TODO: find proper template. Include full name, user and site names.
	// TODO: send mail without "mail" action (and without smtp server)?
	public void sendNotificationEmail(String email, String password) throws FileNotFoundException {
		Action mailAction = this.getActionService().createAction(MailActionExecuter.NAME);
		mailAction.setParameterValue(MailActionExecuter.PARAM_TO, email);
		mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, "Alfresco user successfully created!");

		NodeRef companyHome = this.getUtils().getCompanyHomeNodeRef();
		List<String> templatePath = Arrays.asList(StringUtils.split(NOTIFICATION_MAIL_TEMPLATE_PATH));
		NodeRef emailTemplate = this.getFileFolderService().resolveNamePath(companyHome, templatePath).getNodeRef();
		mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, emailTemplate);

		// Map<String, Serializable> templateArgs = new HashMap<String,
		// Serializable>();
		// templateArgs.put("contactFirstName", contactFirstName);
		// templateArgs.put("contactLastName", contactLastName);
		// mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL,
		// (Serializable) templateArgs);
		// mailAction.setParameterValue(MailActionExecuter.PARAM_FROM, "TODO!");
		// schedule send mail at midnight
		mailAction.setExecuteAsynchronously(true);

		this.getActionService().executeAction(mailAction, null);
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

		FileInfo aboutMuseum = this.getFileFolderService().create(site.documentLibrary, documentName,
				UCMConstants.TYPE_UCM_DOCUMENT_QNAME);

		this.getNodeService().addAspect(aboutMuseum.getNodeRef(), UCMConstants.ASPECT_SITE_QNAME, museumProps);

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

	public static String getSiteManagerGroupName(String shortSiteName) {
		return "GROUP_site_" + shortSiteName + "_SiteManager";
	}

	public static Map<String, Object> createSuccessModel() {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(MODEL_SUCCESS, true);
		return model;
	}

	public static Map<String, Object> createErrorModel(String error) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(MODEL_SUCCESS, false);
		model.put("code", false);
		model.put("error", error);
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
}
