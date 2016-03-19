<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

function getSiteNodeRef(siteShortName) {
	var result = null;
	if (siteShortName != null) {
		var response = remote.call("/api/sites/" + siteShortName);
		if (response.status == 200) {
			result = JSON.parse(response)["ucmNodeRef"];
		}
	}
	return result;
}

function mapToList(map) {
	var result = [];
	for (var key in map) {
		result.push([key, map[key]]);
	}
	return result;
}

function getSizeData(siteRef) {
	var url = "/ucm/site-size-report?nodeRef=" + siteRef;
	var response = remote.call(url);
	return JSON.parse(response);
}

