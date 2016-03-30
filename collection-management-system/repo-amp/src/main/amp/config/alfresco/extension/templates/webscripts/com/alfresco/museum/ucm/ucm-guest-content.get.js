function main() {
	// Get the node from the URL
	var reference = [ url.templateArgs.store_type, url.templateArgs.store_id ]
			.concat(url.templateArgs.id.split("/"));
	var node = search.findNode('node', reference);

	// 404 if the node is not found
	if (node == null) {
		status.setCode(status.STATUS_NOT_FOUND,
				"Source node could not be found");
		return;
	}

	// 400 if the node is not a subtype of ucm:content (ucm:artifact or ucm:attached_file)
	if (!node.isSubType("ucm:content")) {
		status.setCode(status.STATUS_BAD_REQUEST,
				"The source node is not a subtype of ucm:content");
		return;
	}

	model.contentNode = node;
}

main();