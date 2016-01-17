<import resource="classpath:/alfresco/site-webscripts/org/alfresco/share/imports/share-header.lib.js">

function hideItemById() {
	for (var i = 0; i < arguments.length; ++i) {
		widgetUtils.deleteObjectFromArray(model.jsonModel, "id", arguments[i]);
	}
}

/*  determine user type  */

var isAdmin = user.isAdmin;
var isVisitor = (user.id == "visitor");
var isSiteAdmin = false;
var isPrivateSite = true;

if (!isAdmin) {
	var siteData = getSiteData();
	if (siteData != null) {
		//"Public" UCM site has actually "MODERATED" visibility under the hood
		isPrivateSite = (siteData.profile == null || siteData.profile.visibility !== "MODERATED");
		isSiteAdmin = (siteData != null && siteData.userIsSiteManager);
	}
}

/*  apply modifications specific to user type  */

var sitesMenu = widgetUtils.findObject(model.jsonModel, "id", "HEADER_SITES_MENU");
if (sitesMenu != null) {
	//hide "Create site" item as this way of creating sites shouldn't be used by anybody
	sitesMenu.config.showCreateSite = false;
}

if (isSiteAdmin || isVisitor) {
	//hideItemById("HEADER_HOME", "HEADER_USER_MENU_SET_STATUS", "HEADER_USER_MENU_LOGOUT");
	hideItemById("HEADER_MY_FILES", "HEADER_SHARED_FILES", "HEADER_TASKS", "HEADER_PEOPLE", "HEADER_LEAVE_SITE");

	if (!isPrivateSite) {
		hideItemById("HEADER_CUSTOMIZE_SITE");
	}
}

//visitor shouldn't be able to customize anything
if (isVisitor) {
	//remove whole submenu on the right side with cog icon
	hideItemById("HEADER_CUSTOMIZE_SITE_DASHBOARD", "HEADER_SITE_CONFIGURATION_DROPDOWN");

	//The "My Sites", Favorites, "Remove Current site from favorites" should be removed for visitor only
	if (sitesMenu != null) {
		sitesMenu.config.showMySites = false;
		sitesMenu.config.showRecentSites = false;
		//hides "add" and "remove" buttons as well as "Favourites" button itself
		sitesMenu.config.showFavourites = false;
	}
}

//TODO: also set client-debug=false (disable debug menu)
