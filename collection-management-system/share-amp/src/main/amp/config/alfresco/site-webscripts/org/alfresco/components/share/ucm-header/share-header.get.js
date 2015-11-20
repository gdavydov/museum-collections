<import resource="classpath:/alfresco/site-webscripts/org/alfresco/share/imports/share-header.lib.js">

if (!user.isAdmin) {
	widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_HOME");
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
  if (siteData == null || !siteData.userIsSiteManager) {
			widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_CUSTOMIZE_SITE_DASHBOARD");
		}
		widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_CUSTOMIZE_SITE");
	}

	// also set client-debug=false (disable debug menu)
}
