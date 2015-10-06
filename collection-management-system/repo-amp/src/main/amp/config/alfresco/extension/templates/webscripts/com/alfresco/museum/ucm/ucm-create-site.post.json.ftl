{
"success": ${success?js_string}
<#if siteAdmin?exists>, "siteAdmin": "${siteAdmin?js_string}"</#if>
<#if siteShortName?exists>, "siteShortName": "${siteShortName?js_string}"</#if>
<#if siteIsPrivate?exists>, "siteIsPrivate": "${siteIsPrivate?js_string}"</#if>
<#if siteNodeRef?exists>, "siteNodeRef": "${siteNodeRef?js_string}"</#if>
}