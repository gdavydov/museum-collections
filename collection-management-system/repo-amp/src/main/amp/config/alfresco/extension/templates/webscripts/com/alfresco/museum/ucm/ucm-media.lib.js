function getArtifactFolder(artifactRef) {
	var result = null;
	if (artifactRef != null) {
		var node = search.findNode(artifactRef);
		if (node != null) {
			var artifactContents = node.childAssocs["ucm:artifact_contains"];
			if (artifactContents != null && artifactContents.length > 0) {
				var mediaFolder = artifactContents[0];
				if (mediaFolder != null && mediaFolder.isContainer) {
					result = mediaFolder;
				}
			}
		}

		if (result == null) {
			result = createArtifactMediaFolder(node);
		}
	}

	return result;
}

//similar to org.alfresco.museum.ucm.utils.NodeUtils.getSiteRefByNode(NodeRef)
function getSiteByNode(node) {
	while (node != null && !node.isSubType("st:site")) {
		node = node.parent;
	}

	return node;
}


//similar to org.alfresco.museum.ucm.utils.NodeUtils.getOrCreateFolder(NodeRef, String, boolean)
function getOrCreateFolder(parent, name) {
	var result = parent.childByNamePath(name);
	if (result == null) result = parent.createFolder(name);
	return result;
}

//logic here is similar to org.alfresco.museum.ucm.utils.NodeUtils.getOrCreateArtifactMediaFolder(NodeRef)
function createArtifactMediaFolder(artifact) {
	var site = getSiteByNode(artifact);
	if (site == null) {
		logger.warn("Can't determine which site node belongs to. Media attachments folder wasn't created.");
		return null;
	}

	var artistName = artifact.properties['ucm:artist_name'] || 'UNKNOWN_ARTIST';

	var artifactName = artifact.properties['cm:name'];
	if (!artifactName) {
		// "workspace://SpacesStore/1bcdb278-acf4-4477-a0ca-8d50d91be8d1" -> "1bcdb278-acf4-4477-a0ca-8d50d91be8d1"
		var artifactId = artifact.getNodeRef().toString().replaceAll(".*/", "");
		artifactName = 'UNKNOWN_ARTIFACT_' + artifactId;
	}

	var systemFolder = getOrCreateFolder(site, 'system');
	var mediaFolder = getOrCreateFolder(systemFolder, 'artifact_attachments');
	var artistFolder = getOrCreateFolder(mediaFolder, artistName);
	var artifactFolder = getOrCreateFolder(artistFolder, artifactName);

	// set media folder caption
	artifactFolder.properties['cm:title'] = "Media content for " + artifactName;
	artifactFolder.save;

	//clean up existing associations
	try {
		var assocs = artifact.childAssocs['ucm:artifact_contains'];
		for each (var assoc in assocs) {
			artifact.removeAssociation(assoc, 'ucm:artifact_contains');
		}
		artifact.removeAssociation(artifactFolder, 'ucm:artifact_contains');
	} catch (e) {}

	// save reference to folder in artifact association
	artifact.createAssociation(artifactFolder, 'ucm:artifact_contains');
	artifact.save();

	return artifactFolder;
}

function getMediaFiles(artifactRef) {
/*
  for each(permission in node.fullPermissions) {
    if (/;DIRECT$/.test(permission)) {
      logger.log(node.displayPath + "/" + node.name + ";" + permission);
    }
  }
getPermissions(noderef)
*/
	var mediaFiles = [];
	var mediaFolder = getArtifactFolder(artifactRef);
	if (mediaFolder != null) {
		var files = mediaFolder.children;
		if (files != null) {
			for (var i = 0; i < files.length; ++i) {
				var file = files[i];
				mediaFiles.push({
					nodeRef : file.nodeRef,
					name : file.name,
					title : (file.properties["cm:title"]) ? file.properties["cm:title"] : "",
					link : "/" + file.storeType + "/" + file.storeId + "/" + file.id,
					type : file.mimetype,
					size : file.size,
					permission : "",
					language : (file.properties["ucm:attached_file_language"]) ? file.properties["ucm:attached_file_language"] : "",
					description : (file.properties["cm:description"]) ? file.properties["cm:description"] : ""
				});
			}
		}
	}
	return mediaFiles;
}
