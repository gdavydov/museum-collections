<#macro siteJSON site>
<@siteJSONManagers site=site roles="managers"/>
</#macro>

<#macro siteJSONManagers site roles>
<#local userid=person.properties.userName>
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"url": "${url.serviceContext + "/api/sites/" + site.shortName}",
	"sitePreset": "${site.sitePreset}",
	"shortName": "${site.shortName}",
	"title": "${site.title}",
	"description": "${site.description}",
	<#if site.node?exists>
	<#-- UCM modifications begin -->
	<#if site.node.isSubType("ucm:site")??>"ucmNodeRef": "${site.node.nodeRef.toString()}",
	<#if site.node.properties['ucm:site_type']??>"ucmSiteType": "${site.node.properties['ucm:site_type']}",</#if>
	<#if site.node.properties['ucm:site_address']??>"ucmSiteAddress": "${site.node.properties['ucm:site_address']}",</#if>
	<#if site.node.properties['ucm:site_aspect_contact_email']??>"ucmSiteEmail": "${site.node.properties['ucm:site_aspect_contact_email']}",</#if>
	<#if site.node.properties['ucm:site_aspect_contact_phone']??>"ucmSitePhone": "${site.node.properties['ucm:site_aspect_contact_phone']}",</#if>
	<#if site.node.properties['ucm:site_aspect_contact_fax']??>"ucmSiteFax": "${site.node.properties['ucm:site_aspect_contact_fax']}",</#if>
	</#if>
	<#-- UCM modifications end -->
	"node": "${url.serviceContext + "/api/node/" + site.node.storeType + "/" + site.node.storeId + "/" + site.node.id}",
	"tagScope": "${url.serviceContext + "/api/tagscopes/" + site.node.storeType + "/" + site.node.storeId + "/" + site.node.id}",
	</#if>
	<#if site.customProperties?size != 0>
	"customProperties":
	{
		<#list site.customProperties?keys as prop>
		<#assign propDetails = site.customProperties[prop]>
		"${prop}":
		{
			"name": "${prop}",
			"value":
			<#if propDetails.value?is_enumerable>
			[
			<#list propDetails.value as v>
			"${v?string}"<#if v_has_next>,</#if>
			</#list>
			]
			<#else>
			"${propDetails.value?string}"
			</#if>,
			"type": <#if propDetails.type??>"${propDetails.type}"<#else>null</#if>,
			"title": <#if propDetails.title??>"${propDetails.title}"<#else>null</#if>
		}
		<#if prop_has_next>,</#if>
		</#list>
	},
	</#if>
	<#if roles = "user">
	"siteRole": "${site.getMembersRole(userid)!""}",
	<#elseif roles = "managers">
	"siteManagers":
	[
		<#assign managers = site.listMembers("", "SiteManager", 0, true)?keys />
		<#list managers as manager>
			"${manager}"<#if manager_has_next>,</#if>
		</#list>
	],
	</#if>
	<#nested>
	"isPublic": ${site.isPublic?string("true", "false")},
	"visibility": "${site.visibility}"
}
</#escape>
</#macro>
