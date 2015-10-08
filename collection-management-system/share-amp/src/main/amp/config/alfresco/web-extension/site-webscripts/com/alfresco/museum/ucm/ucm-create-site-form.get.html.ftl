<@standalone>
   <@markup id="css" >
      <#-- CSS Dependencies -->
      <@link rel="stylesheet" type="text/css" href="${url.context}/res/css/jquery-ui.css" />
      <@link rel="stylesheet" type="text/css" href="${url.context}/res/css/smoothness/jquery-ui.css" />
      <@link rel="stylesheet" type="text/css" href="${url.context}/res/css/jquery.steps.css" />
      <@link rel="stylesheet" type="text/css" href="${url.context}/res/css/simple-file-preview.css" />
      <@link rel="stylesheet" type="text/css" href="${url.context}/res/css/ucm-create-site-form.css" />
   </@>

	<style type="text/css"><#-- See http://stackoverflow.com/questions/1964839/jquery-please-wait-loading-animation -->
		.modal {
		    display:    none;
		    position:   fixed;
		    z-index:    1000;
		    top:        0;
		    left:       0;
		    height:     100%;
		    width:      100%;
		    background: rgba( 255, 255, 255, .8 ) 
		                url('${url.context}/res/images/ucm-spinner.gif') 
		                50% 50% 
		                no-repeat;
		}
		
		/* When the body has the loading class, we turn
		   the scrollbar off with overflow:hidden */
		body.loading {
		    overflow: hidden;   
		}
		
		/* Anytime the body has the loading class, our
		   modal element will be visible */
		body.loading .modal {
		    display: block;
		}
	</style>

	<form id="ucm-create-site-form" action="${url.context}/proxy/alfresco/ucm/create-site" method="post" enctype="multipart/form-data" onsubmit="return false;">
	    <h3>Create Site</h3>
	    <fieldset>
	        <legend>Site</legend>
	 
	        <label for="siteName">Site name *</label>
	        <input id="siteName" name="siteName" type="text" class="required">
			
	        <label for="siteLogo">Company logo</label>
	        <input id="siteLogo" name="siteLogo" type="file">
			<small>Upload your site logo here. Please note, that this logo could be changed any time later.</small><br/><br/>
	        
			<label for="siteAdminFirstName">Site administrator first name *</label>
	        <input id="siteAdminFirstName" name="siteAdminFirstName" type="text" class="required">
			
			<label for="siteAdminLastName">Site administrator last name *</label>
	        <input id="siteAdminLastName" name="siteAdminLastName" type="text" class="required">
			
			<label for="siteAdminEmail">Site administrator email *</label>
	        <input id="siteAdminEmail" name="siteAdminEmail" type="text" class="required email">
			
			<label for="siteIsPrivate">Private site:</label>
	        <input id="siteIsPrivate" name="siteIsPrivate" type="checkbox">
			<img src="${url.context}/res/images/icon_info.gif" class="infoButton" alt="info"
				title="When creating a site, you have the option of making it public or private.&#10;All users can view a public site, whether or not they have joined the site.&#10;Users who join the site are listed as site members and can work with the site content, depending on their assigned roles.&#10;A private site is available only to the site manager and any users invited to join the site.&#10;Company administrator (superuser)  will ALWAYS have access to your site.">
			<br/>
			
			<label for="siteDescription" class="clearfix">Site description</label><br/>
	        <textarea id="siteDescription" style="resize: none;" name="siteDescription" rows="4"></textarea>
			
	        <p>(*) Mandatory</p>
	    </fieldset>
	 
	    <h3>Museum/Gallery Information</h3>
	    <fieldset>
	        <legend>Information about Museum/Gallery</legend>
	 
	        <label for="siteType">Type</label>
	        <select id="siteType" name="siteType" class="siteType required" value="All">
				<option value="All">All</option>
				<option value="Modern">Modern</option>
				<option value="Classical">Classical</option>
				<option value="Sculpture only">Sculpture only</option>
	        </select>

	        <label for="museumAddress">Address</label>
	        <input id="museumAddress" name="museumAddress" type="text">
			
	        <label for="museumPhone">Phone *</label>
	        <input id="museumPhone" name="museumPhone" type="tel" maxlength="14" class="required phone">
			
	        <label for="museumFax">Fax</label>
	        <input id="museumFax" name="museumFax" type="tel" maxlength="14" class="phone">
			
	        <p>(*) Mandatory</p>
	    </fieldset>
	 
	    <h3>Collection and Site folders (Optional)</h3>
	    <fieldset>
	        <legend>Collection (Optional)</legend>
	
			<label for="collectionName">Collection name</label>
	        <input id="collectionName" name="collectionName" type="text">
			
			<label for="collectionID">Collection ID</label>
	        <input id="collectionID" name="collectionID" type="text">
			
			<legend>Additional Site Folders (Optional)
				<img src="${url.context}/res/images/icon_info.gif" class="infoButton" alt="info" title="Selected folders woll be created inside site root folder">
			</legend>
			
	        <table width="400px" border="0" cellspacing="0" cellpadding="0">
			<tbody>
			<tr>
				<th width="40%"><label for="siteFoldersAvailableOptions">Available</label></th>
				<th width="20%"/>
				<th width="40%"><label for="siteFoldersSelectedOptions">Selected</label></th>
			</tr>
			<tr>
				<td align="center">
				<select id="siteFoldersAvailableOptions" name="siteFoldersAvailableOptions" size="7" class="siteFolders">
					<option value="Exhibitions">Exhibitions</option>
					<option value="Learn">Learn</option>
					<option value="Blogs">Blogs</option>
					<option value="About the museum">About the museum</option>
				</select>
				</td>
				<td align="center" valign="middle">
				<input type="button" id="siteFolderSelect" class="smallButton" value=">>" onclick="ucmMoveOptions('siteFoldersAvailableOptions', 'siteFoldersSelectedOptions')">
				<input type="button" id="siteFolderDeselect" class="smallButton" value="<<" onclick="ucmMoveOptions('siteFoldersSelectedOptions', 'siteFoldersAvailableOptions')">
				</td>
				<td align="center">
				<select id="siteFoldersSelectedOptions" name="siteFoldersSelectedOptions" size="7" class="siteFolders">
					<option value="Home">Home</option>
					<option value="Visit">Visit</option>
					<option value="Collections">Collections</option>
				</select>
				</td>
			</tr>
			</tbody>
			</table>
	    </fieldset>
	 
	    <h3>Copyright</h3>
	    <fieldset>
	        <legend>Copyright</legend>
	        <textarea id="copyright" name="copyright" rows="10"></textarea>
	    </fieldset>
	</form>
	
	<script type="text/javascript">
		function ucmAddOption(theSel, theText, theValue) {
			var newOpt = new Option(theText, theValue);
			var selLength = theSel.length;
			theSel.options[selLength] = newOpt;
		}
		
		function ucmDeleteOption(theSel, theIndex) {
			var selLength = theSel.length;
			if (selLength > 0) {
				theSel.options[theIndex] = null;
			}
		}
		
		function ucmMoveOptions(from, to) {
			theSelFrom = document.getElementById(from);
			theSelTo = document.getElementById(to);
		
			var selLength = theSelFrom.length;
			var selectedText = new Array();
			var selectedValues = new Array();
			var selectedCount = 0;
		
			var i;
		
			// Find the selected Options in reverse order
			// and delete them from the 'from' Select.
			for (i = selLength - 1; i >= 0; i--) {
				if (theSelFrom.options[i].selected) {
					selectedText[selectedCount] = theSelFrom.options[i].text;
					selectedValues[selectedCount] = theSelFrom.options[i].value;
					ucmDeleteOption(theSelFrom, i);
					selectedCount++;
				}
			}
		
			// Add the selected text/values in reverse order.
			// This will add the Options to the 'to' Select
			// in the same order as they were in the 'from' Select.
			for (i = selectedCount - 1; i >= 0; i--) {
				ucmAddOption(theSelTo, selectedText[i], selectedValues[i]);
			}
		}
		
		function ucmGetCookie(name) {
			var value = "; " + document.cookie;
			var parts = value.split("; " + name + "=");
			if (parts.length == 2) return parts.pop().split(";").shift();
		}
		
		function ucmGetToken() {
			var token = ucmGetCookie('Alfresco-CSRFToken');
			if (token) return token;
		}
		
		function ucmSubmitForm(form) {
			var csrfToken = ucmGetToken()
			if (csrfToken) {
				form[0].action += '?Alfresco-CSRFToken=' + csrfToken;
			}
			
			$('#siteFoldersAvailableOptions').prop('disabled', 'disabled');
			$('#siteFoldersSelectedOptions').attr('multiple', '');
			$('#siteFoldersSelectedOptions option').prop('selected', true);
			
			var submitFrame = $('<iframe>').css('display', 'none').uniqueId();
			var submitFrameId = submitFrame.attr('id');
			submitFrame.attr({'name': submitFrameId}).appendTo(document.body);
			form.attr({'target': submitFrameId});
			
			// makes it possible to target the frame properly in IE.
			window.frames[submitFrameId].name = submitFrameId;
			
			submitFrame.load(function ucmHandleSiteSubmit() {
				$('body').removeClass('loading');
				var jsonText = submitFrame.contents().find('body').text();
				if (jsonText) {
					var json = JSON.parse(jsonText);
					var success = json.success;
					var siteShortName = json.siteShortName;
					if (success && siteShortName) {
						window.location.href = Alfresco.constants.URL_CONTEXT + 'page/site/' + siteShortName + '/dashboard';
					}
					else {
						console.log(json);
						Alfresco.util.PopupManager.displayMessage({ text: "Error: " + json.message, displayTime: 5, modal: true });
					}
				}
			});
			
			$('body').addClass('loading');
			form[0].submit();
		}

		require([ "jquery", "jqueryui"], function($) {
			jQuery = $;

			require([
				"${url.context}/res/js/jquery.steps.min.js",
				"${url.context}/res/js/jquery.simple-file-preview.js",
				"${url.context}/res/js/jquery.validate.min.js"
				], function() {
				var form = $("#ucm-create-site-form").show();
				
				form.steps(
						{
							headerTag : "h3",
							bodyTag : "fieldset",
							transitionEffect : "slideLeft",
							onStepChanging : function(event, currentIndex, newIndex) {
								// Always allow previous action
								if (currentIndex > newIndex) {
									return true;
								}
				
								// Needed in some cases if the user went back (clean up)
								if (currentIndex < newIndex) {
									// To remove error styles
									form.find(".body:eq(" + newIndex + ") label.error")
											.remove();
									form.find(".body:eq(" + newIndex + ") .error").removeClass(
											"error");
								}
								form.validate().settings.ignore = ":disabled,:hidden";
								return form.valid();
							},
							onStepChanged : function(event, currentIndex, priorIndex) {
								/**/
							},
							onFinishing : function(event, currentIndex) {
								form.validate().settings.ignore = ":disabled";
								return form.valid();
							},
							onFinished : function(event, currentIndex) {
								ucmSubmitForm(form);
							}
						}).validate({
					errorPlacement : function errorPlacement(error, element) {
						element.before(error);
					}
				/*TODO: validate phone http://jqueryvalidation.org/documentation/
				 * , rules : { confirm : { equalTo : "#password-2" } }
				 */
				});
				
				$('#siteLogo').simpleFilePreview();
				$(document).tooltip();
			});	
		});
	</script>
	
	<div class="modal"></div><#-- See http://stackoverflow.com/questions/1964839/jquery-please-wait-loading-animation -->
</@>