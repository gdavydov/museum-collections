<import resource="classpath:alfresco/extension/templates/webscripts/com/alfresco/museum/ucm/ucm-media.lib.js">

function main()
{
	model.json = jsonUtils.toJSONString(getMediaFiles(args["nodeRef"]));
}

main();
