<#include "../../../../org/alfresco/include/alfresco-template.ftl" />
<@templateHeader/>

<@templateBody>
   <@markup id="alf-hd">
   <div id="alf-hd">
      <@region scope="global" id="share-header" chromeless="true"/>
   </div>
   </@>
   <@markup id="bd">
   <div id="bd">
      <div id="create-content-form" class="share-form">
         <@region id="create-content-mgr" scope="template" />
         <@region id="create-content" scope="template" />
         <!-- Customized version of components/create-content/create-content-mgr.js:CreateContentMgr__navigateForward -->
         <script type="text/javascript">
            var contentMgr = Alfresco.util.ComponentManager.findFirst("Alfresco.CreateContentMgr");
            contentMgr._navigateForward = function UCM__navigateForward(nodeRef)
    		{
    		   /* Have we been given a nodeRef from the Forms Service? */
    		   if (YAHOO.lang.isObject(nodeRef))
    		   {
    		      window.location.href = Alfresco.util.siteURL((this.options.isContainer ? "folder" : "artifact") + "-details?nodeRef=" + nodeRef.toString());
    		   }
    		   else if (document.referrer)
    		   {
    		      /* Did we come from the document library? If so, then direct the user back there */
    		      if (document.referrer.match(/documentlibrary([?]|$)/) || document.referrer.match(/repository([?]|$)/))
    		      {
    		         // go back to the referrer page
    		         history.go(-1);
    		      }
    		      else
    		      {
    		          document.location.href = document.referrer;
    		      }
    		   }
    		   else if (this.options.siteId && this.options.siteId !== "")
    		   {
    		      // In a Site, so go back to the document library root
    		      window.location.href = Alfresco.util.siteURL("documentlibrary");
    		   }
    		   else
    		   {
    		      window.location.href = Alfresco.constants.URL_CONTEXT;
    		   }
    		};
         </script>
      </div>
   </div>
   </@>
</@>

<@templateFooter>
   <@markup id="alf-ft">
   <div id="alf-ft">
      <@region id="footer" scope="global" />
   </div>
   </@>
</@>
