package com.alfresco.museum.ucm;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.yahoo.platform.yui.javascript.Scriptable;

/**
 * Site creation process generally consists of two steps:<br/>
 * 1. server side call to {@link org.alfresco.repo.site.SiteServiceImpl#createSite(String, String, String, String, SiteVisibility) SiteService.createSite}<br/>
 * 2. share side call to {@link org.springframework.extensions.webscripts.ScriptSiteData#newPresetnewPreset(String, Scriptable) SiteData.newPreset}<br/>
 * See <a href="https://forums.alfresco.com/forum/developer-discussions/alfresco-share-development/trouble-custome-site-creation-script-04012009">Alfresco forum post</a>.<br/>
 * Implementation of UCM site creation is located at server side and it can't call {@link org.springframework.extensions.webscripts.ScriptSiteData#newPresetnewPreset(String, Scriptable) SiteData.newPreset} directly.<br/>
 * It is also desirable to keep all site creation related modifications is single transaction, so remote calls to share web script should be avoided.<br/>
 * Solution is to write method identical to {@link org.springframework.extensions.webscripts.ScriptSiteData#newPresetnewPreset(String, Scriptable) SiteData.newPreset} at repo side.<br/>
 * But implementation of {@link org.springframework.extensions.webscripts.ScriptSiteData#newPresetnewPreset(String, Scriptable) SiteData.newPreset} uses templates only available is share classpath.<br/>
 * This web script is based on {@link org.springframework.extensions.webscripts.ScriptSiteData#newPresetnewPreset(String, Scriptable) SiteData.newPreset} but unlike it doesn't do actual changes.
 * Methods of {@link org.springframework.extensions.surf.ModelObjectPersister ModelObjectPersister} aren't actually invoked. But arguments of their calls are saved into array and returned as web script output.
 * In this way all necessary information is collected to repo side in a form, which requires only minimal processing.
 */
public class UCMCreateSiteTemplates extends DeclarativeWebScript {
	private static Log LOGGER = LogFactory.getLog(UCMCreateSiteTemplates.class);
	
	private UCMPresetsManager presetsManager;
	
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		String presetId = req.getParameter("presetId");
		String siteId = req.getParameter("siteid");
		Map<String, String> tokens = Collections.singletonMap("siteid", siteId);
		Object json = "";
		try {
			json = presetsManager.constructPreset(presetId, tokens);
		} catch (JSONException e) {
			LOGGER.error("Can't create site templates", e);
		}
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("json", json);
		return result;
	}

	public UCMPresetsManager getPresetsManager() {
		return presetsManager;
	}

	public void setPresetsManager(UCMPresetsManager presetsManager) {
		this.presetsManager = presetsManager;
	}
}
