package org.alfresco.museum.ucm;

import java.io.IOException;
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
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
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

//TODO: transaction?!
public class UCMCreateSite extends DeclarativeWebScript {
	private static Log LOGGER = LogFactory.getLog(UCMCreateSite.class);
	
	public static final String MODEL_SUCCESS = "success";

	public static final Collection<String> OBLIGATORY_FIELDS = Collections.unmodifiableCollection(Arrays.asList(new String[] {
			"siteName", "siteAdminFirstName", "siteAdminLastName", "siteAdminEmail", "museumName", "museumEmail", "museumPhone"
	}));
	
	private AuthorityService authorityService;
	private SiteService siteService;
	private NodeService nodeService;
	private ContentService contentService;
	private DictionaryService dictionaryService;
	private FileFolderService fileFolderService;
	private MimetypeService mimetypeService;
	private ScriptRemote remote;
	private NodeUtils utils;
	private Properties properties;
	
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		Object parseContent = req.parseContent();
		if (!(parseContent instanceof FormData)) {
			return createErrorModel("Wrong form format! Form should have type \"multipart/form-data\".");
		}
		
		FormData formData = (FormData) parseContent;
		String validationError = validateForm(formData);
		if (validationError != null) {
			return createErrorModel(validationError);
		}
		
		String siteName = getOnlyValue(formData, "siteName");
		String siteDescription = getOnlyValue(formData, "siteDescription");
		boolean isPrivate = StringUtils.equals("true", getOnlyValue(formData, "siteIsPrivate"));
		createSite(siteName, siteDescription, isPrivate, getField(formData, "siteLogo"));
		
		return createSuccessModel();
	}
	
	protected Map<String, Object> createSuccessModel() {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(MODEL_SUCCESS, true);
		return model;
	}
	
	protected Map<String, Object> createErrorModel(String error) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(MODEL_SUCCESS, false);
		model.put("code", false); //TODO constant
		model.put("error", error);  //TODO constant
		return model;
	}
	
	protected String createSite(String siteName, String description, boolean isPrivate, FormField logo) {
		String error = null;
		SiteVisibility visibility = (isPrivate) ? SiteVisibility.PRIVATE : SiteVisibility.MODERATED;
		description = (description == null) ? siteName + "site" : description;
		try {
			//TODO: validate short name and title?
			SiteInfo site = this.getSiteService().createSite("ucm-site-dashboard", siteName, siteName, description, visibility);
			
			JSONObject templatesJson = getTemplatesData("ucm-site-dashboard", siteName);
			createSiteContent(site, templatesJson);
			
			//TODO: set logo
		} catch (RuntimeException e) {
			LOGGER.error("Can't create site!", e);
			error = e.getMessage();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return error;
	}
	
	/**
	 * Creates component and page files required for site to operate.
     * File structure to create inside site root node: <pre>surf-config/
	components/
		page.title.site~(siteName)~dashboard.xml
		...
	pages/
		site/
			(siteName)/
				dashboard.xml
				...
documentLibrary/
SYSTEM/
     * </pre>
	 * @param site Site to create content in.
	 * @param objectsData Expected data structure: <pre>{
	 *	"pages": [{ "id":"site/{siteName}/dashboard", "xml":"..."}, ...],
     *	"components":[{"id":"page.title.site~{siteName}~dashboard", "xml":"..." }, ...]
     *}</pre><br/>
	 * @throws JSONException 
	 */
	protected void createSiteContent(SiteInfo site, JSONObject objectsData) throws JSONException {
		NodeRef siteNodeRef = site.getNodeRef();
		FileInfo surfConfig = this.getFileFolderService().create(siteNodeRef, "surf-config", ContentModel.TYPE_FOLDER);
		FileInfo documentLibrary = this.getFileFolderService().create(siteNodeRef, "documentLibrary", ContentModel.TYPE_FOLDER);
		FileInfo system = this.getFileFolderService().create(siteNodeRef, "SYSTEM", ContentModel.TYPE_FOLDER);
		
		for (Iterator<String> iterator = objectsData.keys(); iterator.hasNext();) {
			String objectsTypeName = iterator.next(); //E.g. "pages" or "components"
			JSONArray objectsList = objectsData.getJSONArray(objectsTypeName); // E.g. [{ "id":"site/{siteName}/dashboard", "xml":"..."}, ...]
			for (int i = 0; i < objectsList.length(); i++) {
				JSONObject object = objectsList.getJSONObject(i); // E.g. { "id":"site/{siteName}/dashboard", "xml":"..."}
				String objectId = object.getString("id"); // E.g. "site/{siteName}/dashboard"
				String objectXml = object.getString("xml");
				
				List<String> objectPath = new ArrayList<String>(4);
				objectPath.add(objectsTypeName); //E.g. ["pages"] or ["components"]
				LinkedList<String> pathTokens = new LinkedList<String>(Arrays.asList(objectId.split("/"))); //E.g. ["site", "{siteName}", "dashboard"] or just ["page.title.site~{siteName}~dashboard"]
				String fileName = pathTokens.getLast();
				pathTokens.removeLast();
				objectPath.addAll(pathTokens);
				
				NodeRef objectFolderNodeRef = this.getUtils().getOrCreateFolderByPath(surfConfig.getNodeRef(), objectPath);
				
				NodeRef objectNodeRef = this.getUtils().createContentNode(objectFolderNodeRef, fileName, objectXml);
			}
		}
	}
	
	protected JSONObject getTemplatesData(String presetId, String siteId) throws IOException, JSONException {
		Response response = null;

		String context = this.getProperties().getProperty("share.context", "share");
		String path = String.format("/%s/page/ucm/create-site-templates?presetId=%s&siteid=%s", context, presetId, siteId);
		
		//TODO: use empty endpoint and build complete URL with Share protocol, host and port from share.* properties?
		ScriptRemoteConnector connector = remote.connect("share");
		response = connector.call(path);
		
		return new JSONObject(response.getText());
	}
	
	private String validateForm(FormData formData) {
		List<String> missed = new ArrayList<String>(5);
		for (String fieldName : OBLIGATORY_FIELDS) {
			if (getOnlyValue(formData, fieldName) == null) {
				missed.add(fieldName);
			}
		}
		
		if (!missed.isEmpty()) {
			return "Obligatory fields aren't filled: " + StringUtils.join(missed, ", ");
		}
		
		if (getOnlyValue(formData, "siteName").toString().contains(" ")) {
			return "Site name shouldn't contain spaces!";
		}
		
		//TODO: validate site name, user name, emails, etc.
		return null;
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
	
	public static String getOnlyValue(FormData formData, String paramName) {
		String result = null;
		String[] values = formData.getParameters().get(paramName);
		if (values != null) {
			for (String value : values) {
				if (!StringUtils.isEmpty(value)) {
					result = value;
					break;
				}
			}
		}
		return result;
	}
	
	public AuthorityService getAuthorityService() {
		return authorityService;
	}

	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
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
