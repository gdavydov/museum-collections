<#include "../../../../org/alfresco/include/alfresco-template.ftl" />
<@templateHeader>
   <@script type="text/javascript" src="${url.context}/res/components/documentlibrary/actions.js" group="document-details"/>
   <@script type="text/javascript" src="${url.context}/res/components/documentlibrary/actions-util.js" group="document-details"/>
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/documentlibrary/actions.css" group="document-details"/>
   <@script type="text/javascript" src="${url.context}/res/modules/documentlibrary/doclib-actions.js" group="document-details"/>
   <@script type="text/javascript" src="${url.context}/res/js/artifact-preview.js" group="artifact"/>
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/css/artifact-preview.css" group="artifact"/>
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/css/artifact-details.css" group="artifact"/>
   <@templateHtmlEditorAssets />
</@>
<@markup id="js">
   <#-- JavaScript Dependencies -->
   <#include "../../../../org/alfresco/components/form/ucm-form.js.ftl"/> 
   <@script src="${url.context}/components/document-details/document-metadata.js" group="document-details"/>
</@>

<@templateBody>
   <@markup id="alf-hd">
   <div id="alf-hd">
      <@region id="mobile-app" scope="template"/>
      <@region scope="global" id="share-header" chromeless="true"/>
   </div>
   </@>
   <@markup id="bd">
   <div id="bd">
      <@region id="actions-common" scope="template"/>
      <@region id="actions" scope="template"/>
      <@region id="node-header" scope="template"/>
      <div id="ucm-horizontal-splitter" class="yui-gc ucm-artifact-horizontal-splitter">
     	<div id="ucm-vertical-splitter" class="yui-u first ucm-artifact-vertical-splitter">
            <#if (config.scoped['DocumentDetails']['document-details'].getChildValue('display-web-preview') == "true")>
			   <script type="text/javascript">
			      // Image initialization is done asynchronously and it raises no event to listen to.
			      // To make sure that zoom will be enabled after image loading we are "monkey patching" Image_display function.
			      Alfresco.WebPreview.prototype.Plugins.Image.prototype.display = Image_displayUCM;
			   </script>
			   <div id="ucm-artifact-image" class="artifact-preview">
			   	  <@region id="web-preview" scope="template"/>
			   </div>
            </#if>
			<div id="ucm-left-bottom">
				<@region id="ucm-media-files" scope="template"/>
			</div>
         </div>

         <div id="ucm-metadata" class="yui-u ucm-metadata">
         	<!--@region id="document-actions" scope="template"/-->
            <@region id="document-links" scope="template"/>
			<@region id="document-tags" scope="template"/>
            <@markup id="bd">
			    <div id="bd">
			       <div class="share-form">
			          <@region id="edit-metadata-mgr" scope="template" />
                      <script type="text/javascript">
                         function safeAccess() {
                            var object = arguments[0];
                            for (var i = 1; i < arguments.length; ++i) {
                               object = (object == undefined || object == null) ? object : object[arguments[i]];
                            }
                            return object;
                         }
                         <!-- Customized version of js/alfresco.js:FormManager_navigateForward -->
                         Alfresco.component.FormManager.prototype.navigateForward = function UCM__navigateForward(nodeRef) {
                            var actionsComponent = Alfresco.util.ComponentManager.findFirst("Alfresco.DocumentActions");
			    		    var path = safeAccess(actionsComponent, 'options', 'documentDetails', 'item', 'location', 'path');
			    		    var file = safeAccess(actionsComponent, 'options', 'documentDetails', 'item', 'location', 'file');
                            if (path && file) {
			    		       /* Navigate to parent directory */
                               document.location.href = Alfresco.util.siteURL("documentlibrary") + '?path=' + encodeURIComponent(path) + '&file=' + encodeURIComponent(file);
                            }
                            else {
                               history.go(-1);
                            }
                            return;
			    		 };
			          </script>
			          <@region id="edit-metadata" scope="template" />
			       </div>
			    </div>
		    </@>
	        <@region id="actions-common" scope="template"/>
	        <@region id="document-versions" scope="template"/>
			<@region id="document-actions" scope="template"/>
<#--
			<@region id="comments" scope="template"/>
-->    
          </div>
		  <div class="yui-u">
<#--                 
	            <@region id="document-actions" scope="template"/>
	            <@region id="document-metadata" scope="template"/>
	            <@region id="document-sync" scope="template"/>
	            <@region id="document-permissions" scope="template"/>
	            <@region id="document-workflows" scope="template"/>    
	            <@region id="document-versions" scope="template"/>
				<@region id="document-attachments" scope="template"/>
-->    
            </div>
         </div>
      </div>

      <@region id="html-upload" scope="template"/>
      <@region id="flash-upload" scope="template"/>
      <@region id="file-upload" scope="template"/>
      <@region id="dnd-upload" scope="template"/>
   </div>
   <@region id="doclib-custom" scope="template"/>
   </@>
</@>

<@templateFooter>
   <@markup id="alf-ft">
   <div id="alf-ft">
      <@region id="footer" scope="global"/>
   </div>
   </@>
</@> 