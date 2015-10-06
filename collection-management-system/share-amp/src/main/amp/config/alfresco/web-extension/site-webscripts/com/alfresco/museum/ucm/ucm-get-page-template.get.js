<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

function main() {
	AlfrescoUtil.param('logoNodeRef');
	AlfrescoUtil.param('siteId');

	var page = sitedata.getPage('site/' + model.siteId + '/dashboard');
	page.properties.siteLogo = model.logoNodeRef;
	
	model.id = page.getModelObject().getId();
	model.xml = page.getModelObject().toXML();
}

main();