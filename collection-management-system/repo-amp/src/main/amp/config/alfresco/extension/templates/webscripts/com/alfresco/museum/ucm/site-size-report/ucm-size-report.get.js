function traverse(node, context, process) {
	if (node.isContainer) {
		var newContext = context.slice().concat(node.name);

		for each (var dir in node.childFileFolders()) {
			traverse(dir, newContext, process);
		}
	} else {
		process(node, context);
	}
}

function getAttachFolder(artifactNode) {
	var result = null;
	var artifactContents = artifactNode.childAssocs["ucm:artifact_contains"];
	if (artifactContents != null && artifactContents.length > 0) {
		var attachFolder = artifactContents[0];
		if (attachFolder != null && attachFolder.isContainer) {
			result = attachFolder;
		}
	}
	return result;
}

function main() {
	var siteNode = search.findNode(args["nodeRef"]);
	var shortName = siteNode.properties["cm:name"];
	var title = siteNode.properties["cm:title"];

	var docLibNode = siteNode.childByNamePath("documentLibrary");

	var results = {};

	function register(path, size) {
		if (size != 0) {
			results[path] = size;
		}
	}

	traverse(docLibNode, [], function processArtifact(artifactNode, context) {
		if (artifactNode.isSubType("ucm:artifact")) {
			var artifactName = artifactNode.name;
			var artifactPath = context.join("/") + "/" + artifactName;

			register(artifactPath, artifactNode.size);

			var attachFolder = getAttachFolder(artifactNode);
			if (attachFolder != null) {
				traverse(attachFolder, [], function processAttach(attachNode, _) {
					if (attachNode.isSubType("ucm:attached_file")) {
						// Pretend that attaches are contained inside artifact nodes
						register(artifactPath + "/" + attachNode.name, attachNode.size);
					}
				});
			}
		}
	});

	model.name = jsonUtils.toJSONString(shortName);
	model.title = jsonUtils.toJSONString(title);
	model.data = jsonUtils.toJSONString(results);
}

main();