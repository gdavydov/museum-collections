<#escape x as jsonUtils.encodeJSONString(x)>
{
	"totalRecords": ${data.paging.totalRecords?c},
	"totalRecordsUpper": ${data.paging.totalRecordsUpper?c},
	"startIndex": ${data.paging.startIndex?c},
	"numberFound": ${(data.paging.numberFound!-1)?c},
	"items":
	[
		<#list data.items as item>
		{
			"nodeRef": "${item.nodeRef}",
			"type": "${item.type}",
			"name": "${item.name!''}",
			"displayName": "${item.displayName!''}",
			<#if item.title??>
			"title": "${item.title}",
			</#if>
			"description": "${item.description!''}",
			"modifiedOn": "${xmldate(item.modifiedOn)}",
			"modifiedByUser": "${item.modifiedByUser}",
			"modifiedBy": "${item.modifiedBy}",
			"size": ${item.size?c},
			"mimetype": "${item.mimetype!''}",
			<#if item.site??>
			"site":
			{
				"shortName": "${item.site.shortName}",
				"title": "${item.site.title}"
			},
			"container": "${item.container}",
			</#if>
			<#if item.path??>
			"path": "${item.path}",
			</#if>
			"lastThumbnailModification":
			[
			 <#if item.lastThumbnailModification??>
			 <#list item.lastThumbnailModification as lastThumbnailMod>
			 "${lastThumbnailMod}"
			 <#if lastThumbnailMod_has_next>,</#if>
			 </#list>
			 </#if>
			],
			"tags": [<#list item.tags as tag>"${tag}"<#if tag_has_next>,</#if></#list>],
			"actualType": "${item.actualType}",
			"thumbnailUrl": "${item.thumbnailUrl!''}",
			"ucmLink": "${item.ucmLink!''}",
			"ucmTitle": "${item.ucmTitle!''}",
			"ucmProperties": "${item.ucmProperties!''}"
		}<#if item_has_next>,</#if>
		</#list>
	]
}
</#escape>