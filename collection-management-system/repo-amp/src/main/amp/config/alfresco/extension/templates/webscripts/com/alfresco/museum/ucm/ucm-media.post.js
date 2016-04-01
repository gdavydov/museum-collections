<import resource="classpath:alfresco/extension/templates/webscripts/com/alfresco/museum/ucm/ucm-media.lib.js">
<!-- Based on alfresco/templates/webscripts/org/alfresco/repository/forms/form.post.js -- >
function main()
{
	var itemKind = "type";
//	var itemId = "cm:content";
	var itemId = "ucm:attached_file";

    if (logger.isLoggingEnabled())
    {
        logger.log("multipart form submission for item:");
        logger.log("\tkind = " + itemKind);
        logger.log("\tid = " + itemId);
    }

    var persistedObject, artifactRef, folderRef;

    try
    {
        // N.B. This repoFormData is a different FormData class to that used above.
        var repoFormData = new Packages.org.alfresco.repo.forms.FormData();

        var filename = '';
        var isTitleSet = false;
        for (var i = 0; i < formdata.fields.length; i++)
        {
           var field = formdata.fields[i];
           if (field.isFile)
           {
              repoFormData.addFieldData(field);
              repoFormData.addFieldData("prop_mimetype", field.mimetype);
              filename = field.value;
           }
           else
           {
               if (field.name == "nodeRef") {
                  artifactRef = field.value;
                  folderRef = getArtifactFolder(artifactRef).nodeRef;
               }
               else {
                  if (field.name == "prop_cm_title") {
                     isTitleSet = true;
                  }

                  // add field to form data
                  repoFormData.addFieldData(field.name, field.value);
              }
           }
        }

        if (filename && !isTitleSet) {
        	repoFormData.addFieldData("prop_cm_title", filename);
        }

        if (folderRef != null) {
        	repoFormData.addFieldData("alf_destination", folderRef.toString());
        	// TODO: repoFormData.addFieldData("prop_cm_name", ???);

        	persistedObject = formService.saveForm(itemKind, itemId, repoFormData);
        }
        else {
        	var msg = "Artifact media folder not found!";
        	status.setCode(500, msg);
        	logger.log(msg);
        }
    }
    catch (error)
    {
        var msg = error.message;

        if (logger.isLoggingEnabled())
            logger.log(msg);

        // determine if the exception was a FormNotFoundException, if so return
        // 404 status code otherwise return 500
        if (msg.indexOf("FormNotFoundException") != -1)
        {
            status.setCode(404, msg);

            if (logger.isLoggingEnabled())
                logger.log("Returning 404 status code");
        }
        else
        {
            status.setCode(500, msg);

            if (logger.isLoggingEnabled())
                logger.log("Returning 500 status code");
        }

        return;
    }

    model.mediaFiles = jsonUtils.toJSONString(getMediaFiles(artifactRef));
    model.persistedObject = persistedObject.toString();
    model.message = "Successfully persisted form for item [" + itemKind + "]" + itemId;
}

main();