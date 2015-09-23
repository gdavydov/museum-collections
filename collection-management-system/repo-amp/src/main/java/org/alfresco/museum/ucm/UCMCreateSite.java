package org.alfresco.museum.ucm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;
import org.springframework.extensions.webscripts.servlet.FormData.FormField;

public class UCMCreateSite extends DeclarativeWebScript {
	private static Log LOGGER = LogFactory.getLog(UCMCreateSite.class);
	
	public static final String MODEL_SUCCESS = "success";

	public static final Collection<String> OBLIGATORY_FIELDS = Collections.unmodifiableCollection(Arrays.asList(new String[]{
			"siteName", "siteAdminFirstName", "siteAdminLastName", "siteAdminEmail", "museumName", "museumEmail", "museumPhone"
	}));
	
	private AuthorityService authorityService;
	private SiteService siteService;
	private NodeService nodeService;
	private ContentService contentService;
	private DictionaryService dictionaryService;
	private FileFolderService fileFolderService;
	private MimetypeService mimetypeService;
	
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
	
	private Map<String, Object> createSuccessModel() {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(MODEL_SUCCESS, true);
		return model;
	}
	
	private Map<String, Object> createErrorModel(String error) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(MODEL_SUCCESS, false);
		model.put("code", false); //TODO constant
		model.put("error", error);  //TODO constant
		return model;
	}
	
	private String createSite(String siteName, String description, boolean isPrivate, FormField logo) {
		String error = null;
		SiteVisibility visibility = (isPrivate) ? SiteVisibility.PRIVATE : SiteVisibility.MODERATED;
		description = (description == null) ? siteName + "site" : description;
		try {
			//TODO: validate short name and title?
			this.getSiteService().createSite("ucm-site-dashboard", siteName, siteName, description, visibility);
			
			//TODO: set logo
		} catch (RuntimeException e) {
			LOGGER.error("Can't create site!", e);
			error = e.getMessage();
		}
		
		return error;
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
}
