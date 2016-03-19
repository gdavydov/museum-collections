function main() {
	var results = search.luceneSearch('PATH:"/app:company_home/st:sites/cm:museum-test-site/cm:perf-test-temporary-folder//*" AND +TYPE:"cm:content"');
	var size = 0;
	for (i = 0; i < results.length; ++i) {
		size += results[i].size;
	}
	model.size = size;
}
main();