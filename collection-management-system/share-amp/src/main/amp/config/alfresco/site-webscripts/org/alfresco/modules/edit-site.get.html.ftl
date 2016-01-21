<#assign el=args.htmlid?html>
<div id="${el}-dialog" class="edit-site">
   <div class="hd">
      <#-- TITLE -->
      <@markup id="title">${msg("header.editSite")}</@markup>
   </div>
   <div class="bd">
      <form id="${el}-form" method="PUT"  action="">

      <#-- FIELDS -->
      <@markup id="fields">

         <#-- HIDDEN -->
         <input type="hidden" id="${el}-visibility" name="visibility" value="${profile.visibility}"/>
         <input id="${el}-shortName" type="hidden" name="shortName" value="${profile.shortName}"/>

         <#-- TITLE -->
         <div class="yui-gd">
            <div class="yui-u first"><label for="${el}-title">${msg("label.name")}:</label></div>
            <div class="yui-u"><input id="${el}-title" type="text" name="title" value="${profile.title?html}" tabindex="0"/>&nbsp;*</div>
         </div>

<#-- UCM modification begin: -->
         <#if profile.ucmNodeRef?? == true>
         <#-- TYPE -->
         <div class="yui-gd">
            <div class="yui-u first"><label for="${el}-type">${msg("label.type")}:</label></div>
            <div class="yui-u">
               <select id="${el}-type" name="type" value="${(profile.ucmSiteType)!"All"}" style="width: 15.4em; padding: 1px 0px;">
               <#-- TODO: load types list from config? -->
		          <option value = "All"<#if profile.ucmSiteType == "All"> selected</#if>>All</option>
		          <option value = "Modern"<#if profile.ucmSiteType == "Modern"> selected</#if>>Modern Art</option>
		          <option value = "Classical"<#if profile.ucmSiteType == "Classical"> selected</#if>>Classical Art</option>
		          <option value = "Photo"<#if profile.ucmSiteType == "Photo"> selected</#if>>Photo</option>
		          <option value = "Archive"<#if profile.ucmSiteType == "Archive"> selected</#if>>Archive</option>
		          <option value = "Sculpture only"<#if profile.ucmSiteType == "Sculpture only"> selected</#if>>Sculpture</option>
	           </select>&nbsp;*
            </div>

         <#-- ADDRESS -->
         <div class="yui-gd">
            <div class="yui-u first"><label for="${el}-address">${msg("label.address")}:</label></div>
            <div class="yui-u"><input id="${el}-address" type="text" name="address" value="${profile.ucmSiteAddress!""}" tabindex="0"/>&nbsp;*</div>
         </div>

         <#-- EMAIL -->
         <div class="yui-gd">
            <div class="yui-u first"><label for="${el}-email">${msg("label.email")}:</label></div>
            <div class="yui-u"><input id="${el}-email" type="text" name="email" value="${profile.ucmSiteEmail!""}" tabindex="0"/>&nbsp;*</div>
         </div>

         <#-- PHONE -->
         <div class="yui-gd">
            <div class="yui-u first"><label for="${el}-phone">${msg("label.phone")}:</label></div>
            <div class="yui-u"><input id="${el}-phone" type="text" name="phone" value="${profile.ucmSitePhone!""}" tabindex="0"/>&nbsp;*</div>
         </div>

         <#-- FAX -->
         <div class="yui-gd">
            <div class="yui-u first"><label for="${el}-fax">${msg("label.fax")}:</label></div>
            <div class="yui-u"><input id="${el}-fax" type="text" name="fax" value="${profile.ucmSiteFax!""}" tabindex="0"/></div>
         </div>
         <#-- TODO: copyright? -->
         </#if>
<#-- UCM modification end. -->

         <#-- DESCRIPTION -->
         <div class="yui-gd">
            <div class="yui-u first"><label for="${el}-description">${msg("label.description")}:</label></div>
            <div class="yui-u"><textarea id="${el}-description" name="description" rows="3" tabindex="0">${profile.description?html}</textarea></div>
         </div>

         <#-- ACCESS -->
         <div class="yui-gd">
            <div class="yui-u first"><label for="${el}-isPublic">${msg("label.access")}:</label></div>
            <div class="yui-u">
               <input id="${el}-isPublic" type="radio" <#if (profile.visibility == "PUBLIC" || profile.visibility == "MODERATED")>checked="checked"</#if> tabindex="0" name="-" /> ${msg("label.isPublic")}<br />
               <div class="moderated">
                  <input id="${el}-isModerated" type="checkbox" tabindex="0" name="-" <#if (profile.visibility == "MODERATED")>checked="checked"</#if> <#if (profile.visibility == "PRIVATE")>disabled="true"</#if>/> ${msg("label.isModerated")}<br />
                  <span class="help"><label for="${el}-isModerated">${msg("label.moderatedHelp")}</label></span>
               </div>
            </div>
         </div>
         <div class="yui-gd">
            <div class="yui-u first">&nbsp;</div>
            <div class="yui-u">
               <input id="${el}-isPrivate" type="radio" tabindex="0" name="-" <#if (profile.visibility == "PRIVATE")>checked="checked"</#if>/> ${msg("label.isPrivate")}
            </div>
         </div>

      </@markup>

      <div class="bdft">
         <#-- BUTTONS -->
         <input type="submit" id="${el}-ok-button" value="${msg("button.ok")}" tabindex="0"/>
         <input type="button" id="${el}-cancel-button" value="${msg("button.cancel")}" tabindex="0"/>
      </div>
   </form>
   </div>
</div>

<script type="text/javascript">//<![CDATA[
Alfresco.util.addMessages(${messages}, "Alfresco.module.EditSite");
//]]></script>