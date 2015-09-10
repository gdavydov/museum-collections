<#import "/org/alfresco/components/form/form.lib.ftl" as formLib />
<@link rel="stylesheet" type="text/css" href="${url.context}/res/css/jquery-ui.css"/>
<@link rel="stylesheet" type="text/css" href="${url.context}/res/css/smoothness/jquery-ui.css"/>
<!-- Fix form width -->
<style type="text/css">
   .share-form .form-container .form-fields {
      width: 500px !important;
   }
   .share-form .form-container .caption {
      width: 520px !important;
   }
   
   .share-form .form-manager {
      width: 527px !important;
   }
</style>

<script type="text/javascript">
   function ucmFormLoaded() {
      <!-- Accordion -->
      require(["jqueryui"], function() {
         $(".accordion-wrapper").accordion({collapsible: true, heightStyle: "content"});
      });
   }
</script>

<#if error?exists>
   <div class="error">${error}</div>
<#elseif form?exists>

   <#assign formId=args.htmlid + "-form">
   <#assign el=args.htmlid?html>
   <#assign formUI><#if args.formUI??>${args.formUI}<#else>true</#if></#assign>

   <#if formUI == "true">
      <@formLib.renderFormsRuntime formId=formId />
   </#if>

   <div id="${el}-dialog">
      <div id="${el}-dialogTitle" class="hd"></div>
      <div class="bd">
         <div id="${formId}-container" class="form-container">
            <div class="yui-g">
               <h2 id="${el}-dialogHeader"></h2>
            </div>
            <#if form.showCaption?exists && form.showCaption && form.mode != "view">
               <div id="${formId}-caption" class="caption"><span class="mandatory-indicator">*</span>${msg("form.required.fields")}</div>
            </#if>

            <#if form.mode != "view">
               <form id="${formId}" method="${form.method}" accept-charset="utf-8" enctype="multipart/form-data" action="${form.submissionUrl}">
            </#if>

            <#if form.mode == "create" && form.destination?? && form.destination?length &gt; 0>
               <input id="${formId}-destination" name="alf_destination" type="hidden" value="${form.destination?html}" />
            </#if>
      
            <#if form.mode != "view" && form.redirect?? && form.redirect?length &gt; 0>
               <input id="${formId}-redirect" name="alf_redirect" type="hidden" value="${form.redirect?html}" />
            </#if>
      
            <div id="${formId}-fields" class="form-fields"> 
               <div class="yui-content">
                  <#list form.structure as item>
                     <#if item.kind == "set">
                        <@renderSetWithoutColumns set=item />
                     </#if>
                   </#list>
                   <#list form.structure as item>
                      <#if item.kind != "set">
                         <@formLib.renderField field=form.fields[item.id] />
                     </#if>
                   </#list>
               </div> 
            </div>
   
            <#if form.mode != "view">
               <@formLib.renderFormButtons formId=formId />
               </form>
            </#if>
         </div>
      </div>
   </div>
<#--
This script tag is placed after tags objects it operates on. It works well if everything is loaded as is. E.g. on ucm-create-content page.
However Alfresco.util.Ajax._successHandler function is used to load form to popup dialog during artist creation.  
This script splits form html into two parts: scripts and "sanitized" html.
Scripts part is executed first, then html is inserted. 
Both of this tasks are scheduled in Alfresco.util.Ajax._successHandler function with delay 0 msec.
As a workaround ucmFormLoaded() call is scheduled with 1 msec delay instead of immediate execution.
Desired event sequence is:
	Alfresco.util.Ajax._successHandler {
		...
		window.setTimeout(scripts, 0);
		YAHOO.lang.later(0, this, this._successHandlerPostExec, serverResponse);
	}

	scripts() {
		...
		/* Html isn't ready now, it is too early to call ucmFormLoaded */
		window.setTimeout(function(){ ucmFormLoaded() }, 1);
	}

	_successHandlerPostExec() {
		/*html is initialized here*/
	}

	function(){
		$("#${fieldHtmlId}-input").simpleFilePreview();
	}
-->
   <script type="text/javascript">window.setTimeout(function(){ ucmFormLoaded() }, 1);</script>
</#if>

<#macro renderSet set>
   <#if set.appearance?exists>
      <#if set.appearance == "fieldset">
         <fieldset><legend>${set.label}</legend>
      <#elseif set.appearance == "panel">
         <div class="form-panel">
            <div class="form-panel-heading">${set.label}</div>
            <div class="form-panel-body">
      <#elseif set.appearance == "accordion-element">
      <div class="hd">${set.label}</div>
      <div class="bd">
         <div class="fixed">
      </#if>
   </#if>
   
   <#list set.children as item>
      <#if item.kind == "set">
         <@renderSet set=item />
      <#else>
         <@formLib.renderField field=form.fields[item.id] />
      </#if>
   </#list>
   
   <#if set.appearance?exists>
      <#if set.appearance == "fieldset">
         </fieldset>
      <#elseif set.appearance == "panel">
            </div>
         </div>
      <#elseif set.appearance == "accordion-element">
         </div>
      </div>
      </#if>
   </#if>
</#macro>

<#macro renderSetWithoutColumns set>
   <#if set.appearance?exists>
      <#if set.appearance == "fieldset">
         <fieldset><legend>${set.label}</legend>
      <#elseif set.appearance == "panel">
         <div class="form-panel">
            <div class="form-panel-body">
      <#elseif set.appearance == "accordion-wrapper">
         <div class="form-panel-body accordion-wrapper">
      </#if>
   </#if>
   
   <#list set.children as item>
      <#if item.kind == "set">
         <@renderSet set=item />
      <#else>
         <@formLib.renderField field=form.fields[item.id]></@>
      </#if>
   </#list>
   
   <#if set.appearance?exists>
      <#if set.appearance == "fieldset">
         </fieldset>
      <#elseif set.appearance == "panel">
            </div>
         </div>
      <#elseif set.appearance == "accordion-wrapper">
         </div>
      </#if>
   </#if>
</#macro>
