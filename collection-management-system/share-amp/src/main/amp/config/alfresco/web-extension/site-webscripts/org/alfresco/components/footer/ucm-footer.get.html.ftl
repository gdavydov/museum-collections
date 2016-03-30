<@markup id="css" >
   <#-- CSS Dependencies -->
   <@link href="${url.context}/res/modules/about-share.css" group="footer"/>
   <@link href="${url.context}/res/modules/ucm-about-share.css" group="footer"/>
   <@link href="${url.context}/res/components/footer/footer.css" group="footer"/>
</@>

<@markup id="js">
   <@script src="${url.context}/res/modules/about-share.js" group="footer"/>
   <@script src="${url.context}/res/modules/ucm-about-share.js" group="footer"/>
</@>

<@markup id="widgets">
   <@createWidgets/>
</@>

<@markup id="html">
   <@uniqueIdDiv>
<#--
      <#assign fc=config.scoped["Edition"]["footer"]>
      <#assign fc=config.scoped["Edition"]["ucm-footer"]!ucm-logo.png>
      <div class="footer ${fc.getChildValue("css-class")!"footer-com"}">
         <span class="copyright">
            <a href="#" onclick="Alfresco.module.getAboutShareInstance().show(); return false;"><img src="${url.context}/res/components/images/logos/${fc.getChildValue("logo")!"ucm-logo.png"}" alt="${fc.getChildValue("alt-text")!"artium-galleries"}" border="0"/></a>
            <#if licenseHolder != "" && licenseHolder != "UNKNOWN">
               <span class="licenseHolder">${msg("label.licensedTo")} ${licenseHolder}</span><br>
            </#if>
            <span>${msg(fc.getChildValue("label")!"label.copyright")}</span>
         </span>
      </div>
-->
      <#assign fc=config.scoped["Edition"]["footer"]>
      <#assign logo="ucm-logo.png">
      <div class="footer ${fc.getChildValue("css-class")!"footer-com"}">
         <span class="copyright">
            <a href="#" onclick="Alfresco.module.getUCMAboutShareInstance().show(); return false;"><img src="${url.context}/res/components/images/logos/${logo}" alt="${fc.getChildValue("alt-text")!"artium-galleries"} width="40" height="40"" border="0"/></a>
            <#if licenseHolder != "" && licenseHolder != "UNKNOWN">
               <span class="licenseHolder">${msg("label.licensedTo")} ${licenseHolder}</span><br>
            </#if>
            <span>${msg(fc.getChildValue("label")!"label.copyright")}</span>
         </span>
      </div>
   </@>
</@>
