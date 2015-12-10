<import resource="classpath:/alfresco/site-webscripts/org/alfresco/share/imports/share-header.lib.js">

if (!user.isAdmin) {
	//hide "Create site" item as this way of creating sites shouldn't be used
	var sitesMenu = widgetUtils.findObject(model.jsonModel, "id", "HEADER_SITES_MENU");
	if (sitesMenu != null) {
		sitesMenu.config.showCreateSite = false;
	}

	//	widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_HOME");
	widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_MY_FILES");
	widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_SHARED_FILES");
	widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_TASKS");
	widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_PEOPLE");
	// widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_USER_MENU_SET_STATUS");
	// widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_USER_MENU_LOGOUT");

	widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_LEAVE_SITE");

	var siteData = getSiteData();
	//"Public" UCM site has actually "MODERATED" visibility under the hood
	if (siteData != null && siteData.profile != null && siteData.profile.visibility === "MODERATED") {
		widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_CUSTOMIZE_SITE");

		//visitor shouldn't be able to customize anything
		if (siteData == null || !siteData.userIsSiteManager) {
			widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_CUSTOMIZE_SITE_DASHBOARD");
			//remove whole submenu on the right side with cog icon
			widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_SITE_CONFIGURATION_DROPDOWN");
		}
	}

	// also set client-debug=false (disable debug menu)
}
