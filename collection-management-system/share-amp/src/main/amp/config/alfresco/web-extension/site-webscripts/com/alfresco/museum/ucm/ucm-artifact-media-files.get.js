<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

function main()
{
	AlfrescoUtil.param('nodeRef');
	var url = "/ucm/media.json?nodeRef=" + model.nodeRef;
	model.response = remote.call(url);
}

main();
