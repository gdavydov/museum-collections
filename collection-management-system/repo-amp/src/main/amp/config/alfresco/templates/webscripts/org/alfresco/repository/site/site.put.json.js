function main()
{
	// Get the site
	var shortName = url.extension;
	var site = siteService.getSite(shortName);

	if (site != null)
	{
		// Updafte the sites details
		if (json.has("title") == true)
		{
		   site.title = json.get("title");
		}
		if (json.has("description") == true)
		{
		   site.description = json.get("description");
		}

		// Use the visibility flag before the isPublic flag
		if (json.has("visibility") == true)
		{
		   site.visibility = json.get("visibility");
		}
		else if (json.has("isPublic") == true)
		{
		   // Deal with deprecated isPublic flag accordingly
		   var isPublic = json.getBoolean("isPublic");
		   if (isPublic == true)
		   {
		      site.visibility = siteService.PUBLIC_SITE;
		   }
		   else
		   {
		      site.visibility = siteService.PRIVATE_SITE;
		   }
	    }

		//UCM modification begin:
		var siteNode = search.findNode(site.node.nodeRef);
		if (json.has("type") == true) {
			siteNode.properties["ucm:site_type"] = json.get("type");
		}
		if (json.has("address") == true) {
			siteNode.properties["ucm:site_address"] = json.get("address");
		}

		if (json.has("email") == true) {
			siteNode.properties["ucm:site_aspect_contact_email"] = json.get("email");
		}
		if (json.has("phone") == true) {
			siteNode.properties["ucm:site_aspect_contact_phone"] = json.get("phone");
		}
		if (json.has("fax") == true) {
			siteNode.properties["ucm:site_aspect_contact_fax"] = json.get("fax");
		}
		siteNode.save();
		//UCM modification end.

		// Save the site
		site.save();

		// Pass the model to the template
		model.site = site;
	}
	else
	{
		// Return 404
		status.setCode(status.STATUS_NOT_FOUND, "Site " + shortName + " does not exist");
		return;
	}
}

main();