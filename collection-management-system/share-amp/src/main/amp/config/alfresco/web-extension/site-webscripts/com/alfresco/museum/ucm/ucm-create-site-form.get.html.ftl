<@standalone>
   <@markup id="css" >
      <#-- CSS Dependencies -->
      <@link rel="stylesheet" type="text/css" href="${url.context}/res/css/jquery-ui.css" />
      <@link rel="stylesheet" type="text/css" href="${url.context}/res/css/smoothness/jquery-ui.css" />
      <@link rel="stylesheet" type="text/css" href="${url.context}/res/css/jquery.steps.css" />
      <@link rel="stylesheet" type="text/css" href="${url.context}/res/css/simple-file-preview.css" />
      <@link rel="stylesheet" type="text/css" href="${url.context}/res/css/ucm-create-site-form.css" />
   </@>

   <@markup id="js" >
      <#-- JS Dependencies -->
      <@script type="text/javascript" src="${url.context}/res/js/jquery.js" />
      <@script type="text/javascript" src="${url.context}/res/js/jquery-ui.min.js" />
      <@script type="text/javascript" src="${url.context}/res/js/jquery.steps.min.js" />
      <@script type="text/javascript" src="${url.context}/res/js/jquery.simple-file-preview.js" />
      <@script type="text/javascript" src="${url.context}/res/js/jquery.validate.min.js" />
   </@>	
	
	<!-- TODO: html, head, body? -->
	
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
				title="When creating a site, you have the option of making it public or private.&lt;br/&gt;All users can view a public site, whether or not they have joined the site.&lt;br/&gt;Users who join the site are listed as site members and can work with the site content, depending on their assigned roles.&lt;br/&gt;A private site is available only to the site manager and any users invited to join the site.&lt;br/&gt;Company administrator (superuser)  will ALWAYS have access to your site.">
			<br/>
			
			<label for="siteDescription" class="clearfix">Site description</label><br/>
	        <textarea id="siteDescription" style="resize: none;" name="siteDescription" rows="4"></textarea>
			
	        <p>(*) Mandatory</p>
	    </fieldset>
	 
	    <h3>Museum/Gallery Information</h3>
	    <fieldset>
	        <legend>Information about Museum/Gallery</legend>
	 
	        <label for="museumName">Name *</label>
	        <input id="museumName" name="museumName" type="text" class="required">
			
	        <label for="museumAddress">Address</label>
	        <input id="museumAddress" name="museumAddress" type="text">
			
	        <label for="museumEmail">Email *</label>
	        <input id="museumEmail" name="museumEmail" type="text" class="required">
			
	        <label for="museumPhone">Phone *</label>
	        <input id="museumPhone" name="museumPhone" type="text" maxlength="14" class="required phone">
			
	        <label for="museumFax">Fax</label>
	        <input id="museumFax" name="museumFax" type="text" maxlength="14" class="phone">
			
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
				<img src="${url.context}/res/images/icon_info.gif" class="infoButton" alt="info" title="TODO: about folders">
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
					<option>Exhibitions</option>
					<option>Learn</option>
					<option>Blogs</option>
					<option>About the museum</option>
				</select>
				</td>
				<td align="center" valign="middle">
				<input type="button" id="siteFolderSelect" class="smallButton" value=">>" onclick="ucmMoveOptions('siteFoldersAvailableOptions', 'siteFoldersSelectedOptions')">
				<input type="button" id="siteFolderDeselect" class="smallButton" value="<<" onclick="ucmMoveOptions('siteFoldersSelectedOptions', 'siteFoldersAvailableOptions')">
				</td>
				<td align="center">
				<select id="siteFoldersSelectedOptions" name="siteFoldersSelectedOptions" size="7" class="siteFolders">
					<option>Home</option>
					<option>Visit</option>
					<option>Collections</option>
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
	
	<script type="text/javascript" src="${url.context}/res/js/ucm-create-site-form.js"></script>
</@>