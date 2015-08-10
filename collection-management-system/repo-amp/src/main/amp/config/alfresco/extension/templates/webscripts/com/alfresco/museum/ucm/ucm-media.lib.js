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
	}
	return result;
}

function getMediaFiles(artifactRef) 
{
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
