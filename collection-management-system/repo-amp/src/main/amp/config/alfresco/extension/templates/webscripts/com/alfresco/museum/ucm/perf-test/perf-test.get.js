function traverse(node) {
	var result = {};
	if (node.isContainer) {
		var nodeName = node.name;
		for each (var dir in node.childFileFolders()) {
			var dirResult = traverse(dir);
			for each (var path in Object.keys(dirResult)) {
				result[nodeName + "/" + path] = dirResult[path];
			}
		}
	}
	else {
		result[node.name] = node.size
	}
	return result;
}

function main() {
	model.json = jsonUtils.toJSONString(traverse(search.findNode(args["nodeRef"])));
}

main();