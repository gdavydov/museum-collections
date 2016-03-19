var random = new Packages.java.util.Random();

function createContent(destinationNode, name) {
	try {
		var node = destinationNode.createFile(name);
		//node.properties["cm:name"] = name;
		node.content = Packages.org.apache.commons.lang.RandomStringUtils.randomAlphanumeric(random.nextInt(10000));
		//node.addAspect("ucm:artifact");
		node.save();
	} catch (e) {
		logger.log(e);
	}
}

function main() {
	var root = search.findNode(args["nodeRef"]);
	for (var i = 0; i < 50; ++i) {
		var folderA = root.createFolder(i);
		for (var j = 0; j < 10; ++j) {
			var folderB = folderA.createFolder(j);
			for (var k = 0; k < 10; ++k) {
				var folderC = folderB.createFolder(k);
				createContent(folderC, "file" + i + j + k);
			}
		}
	}
	//logger.warn(Packages.org.apache.commons.lang.RandomStringUtils.randomAlphanumeric(random.nextInt(1000)));
}

main();
