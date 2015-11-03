///<import resource="classpath:/alfresco/site-webscripts/org/alfresco/share/imports/share-header.lib.js">

//widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_MY_FILES");

var siteData = getSiteData();

if (!user.isAdmin)
{

  widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_HOME");
  widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_MY_FILES");
  widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_SHARED_FILES");
  widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_TASKS");
  widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_PEOPLE");
  
//  widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_USER_MENU_SET_STATUS");
//  widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_USER_MENU_LOGOUT");
  
  widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_LEAVE_SITE");

  // the following only for public sites. Need configuration item
  if (! siteData.userIsSiteManager) {
  	widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_CUSTOMIZE_SITE_DASHBOARD");
  }

  widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_CUSTOMIZE_SITE");
  
  // also set client-debug=false (disable debug menu)
}
