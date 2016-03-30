<#assign el=args.htmlid?html>
<#assign aboutConfig=config.scoped["Edition"]["about"]>
<div id="${el}-dialog" class="about-share">
   <div class="bd">
      <div id="${el}-logo" class="logo-ucm logo">
         <div class="about">
            <br/>
            <div class="header">Artium Galleries v1.0</div>
            <br/>
            <#assign split=serverVersion?index_of(" ")>
            <div class="header">Alfresco ${serverEdition?html} v${serverVersion?substring(0, split)?html}</div>
            <div>${serverVersion?substring(split+1)?html} schema ${serverSchema?html}</div>
            <div class="contributions-bg"></div>
            <div class="contributions-wrapper">
               <div id="${el}-contributions" class="contributions">
Artium Galleries Contributors...
<br/><br/>
Greg Davydov<br/>
Dmitry Alexandrov<br/>
               </div>
            </div>
            <div class="copy-ucm">
               <span>&copy; Artium Galleries Inc. All rights reserved.</span>
               <a href="http://www.alfresco.com" target="new">www.alfresco.com</a>
               <a href="http://www.alfresco.com/legal/agreements/" target="new">Legal and License</a>
            </div>
         </div>
      </div>
   </div>
</div>