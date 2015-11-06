<import resource="classpath:/alfresco/extension/templates/webscripts/com/alfresco/museum/ucm/search/ucm-search.lib.js">

function main()
{
	// data types set should be limited because we don't want to make internal
	// system nodes info to be readable by unauthorized users
	const ALLOWED_DATATYPES = ["ucm:artifact", "ucm:site"];

	if (ALLOWED_DATATYPES.indexOf(args.datatype) == -1) {
		status.code = 500;
		status.message = "Wrong datatype value!";
		status.redirect = true;
		return;
	}

	// getSearchResults function was designed to handle single tag. It converts
	// it's value into query like this: 'classic' -> 'TAG:classic '
	// Fortunately it is easy to add support for multiple tags by replacing
	// commas with ' AND TAG:'
	// Turn 'classic, portrait' into 'classic AND TAG:portrait' so that final
	// query fragment will be 'TAG:classic AND TAG:portrait'
	var tagsQuery = (args.tags !== null) ? args.tags.replace(/\s+/g, '').replace(/,/g, ' AND TAG:') : null;

	// TODO:"prop_ucm_artist_study_workshop", "prop_ucm_artifact_medium",
	// "prop_ucm_artifact_technique", "prop_ucm_artifact_on_display"
	const QUERY_NAME_TO_PROPERTY_NAME =
		{
//			"artifactName":"prop_ucm_artifact_name",
		 "artifactName":"prop_cm_name",
		 "artistName":	"prop_ucm_artist_name",
		 "siteType":	"prop_ucm_site_type",
		 "siteLocation":"prop_ucm_site_address",
		 "siteName":	"prop_ucm_site_name",
		 "period":		"prop_ucm_artist_period"
		};

	var queryParams = {"datatype": args.datatype};
	for (var queryParamName in QUERY_NAME_TO_PROPERTY_NAME) {
		if (args[queryParamName] !== null && args[queryParamName].length > 0 && QUERY_NAME_TO_PROPERTY_NAME[queryParamName] !== null) {
			var propertyName = QUERY_NAME_TO_PROPERTY_NAME[queryParamName];
			var propertyValue = args[queryParamName];
			queryParams[propertyName] = propertyValue;
		}
	}

	//Search for specific site type may include sites of type All.
	if (queryParams["prop_ucm_site_type"] && queryParams["prop_ucm_site_type"].length > 0) {
		queryParams["prop_ucm_site_type-mode"] = "OR";
		queryParams["prop_ucm_site_type"] = queryParams["prop_ucm_site_type"] + ",All";
	}

	// E.g.:
	// {"prop_ucm_artist_name":"testname","prop_ucm_artifact_name":"","prop_ucm_artifact_on_display":"false","datatype":"ucm:artifact"}
	var query = jsonUtils.toJSONString(queryParams);
	var maxResults = (args.maxResults !== null && args.maxResults.length > 0) ? parseInt(args.maxResults, 10) : DEFAULT_MAX_RESULTS;

	var params = {
			siteId: null,
			containerId: null,
			repo: true,
			term: null,
			tag: tagsQuery,
			query: query,
			rootNode: 'alfresco://company/home',
			sort: null,
			maxResults: maxResults,
			pageSize: maxResults,
			startIndex: 0,
			facetFields: null,
			filters: null,
			spell: false
	};

	model.data = getSearchResults(params);
}

main();
