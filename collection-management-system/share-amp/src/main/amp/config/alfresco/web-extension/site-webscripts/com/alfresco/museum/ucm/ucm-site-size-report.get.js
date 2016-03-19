<import resource="classpath:/alfresco/web-extension/site-webscripts/com/alfresco/museum/ucm/ucm-site-size-report.lib.js">

function main() {
	var siteShortName = page.url.templateArgs.site;
	var siteRef = getSiteNodeRef(siteShortName);

	var json = getSizeData(siteRef);

	model.siteName = json.name;
	model.siteTitle = json.title;
	model.data = mapToList(json.data);
	model.siteRef = siteRef;
}

main();