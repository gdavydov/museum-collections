<#-- Image uploading preview  -->
<link rel="stylesheet" type="text/css" href="${url.context}/res/css/simple-file-preview.css"/>

<div id="${fieldHtmlId}" class="form-field">
	<label for="${fieldHtmlId}">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
	<input type="file" id="${fieldHtmlId}-input" name="${field.name}"/>
</div>
<#--
This script tag is placed after tags objects it operates on. It works well if everything is loaded as is. E.g. on ucm-create-content page.
However Alfresco.util.Ajax._successHandler function is used to load form to popup dialog during artist creation.  
This script splits form html into two parts: scripts and "sanitized" html.
Scripts part is executed first, then html is inserted. It works well with Alfresco internal scripts, 
Both of this tasks are scheduled in Alfresco.util.Ajax._successHandler function with delay 0 msec.
As a workaround simpleFilePreview() is scheduled with 1 msec delay instead of immediate execution.
Desired event sequence is:
	Alfresco.util.Ajax._successHandler {
		...
		window.setTimeout(scripts, 0);
		YAHOO.lang.later(0, this, this._successHandlerPostExec, serverResponse);
	}

	scripts() {
		...
		/* Html isn't ready now, it is too early to call simpleFilePreview */
		window.setTimeout(function(){ $("#${fieldHtmlId}-input").simpleFilePreview(); }, 1);
	}

	_successHandlerPostExec() {
		/*html is initialized here*/
	}

	function(){
		$("#${fieldHtmlId}-input").simpleFilePreview();
	}
-->
<script type="text/javascript">
	(function() {
		require(["jquery"], function($) {
			jQuery = $;
			
			//Prevent calling default handler on documentlibrary page
			var stub = function(e){e.stopPropagation();};
			$("#${fieldHtmlId}").bind('dragstart', stub).bind('dragover', stub).bind('dragenter', stub);

			require(["${url.context}/res/js/jquery.simple-file-preview.js"], function() {
			//TODO: Don't show file picker if artifact creation was initiated via file uploading
				//if (typeof SoftwareLoop == 'undefined') {
					window.setTimeout(function(){ $("#${fieldHtmlId}-input").simpleFilePreview(); }, 1);
				//}
				//else {
				//	$("#${fieldHtmlId}").remove();
				//}
			});
		});
	})();
</script>
