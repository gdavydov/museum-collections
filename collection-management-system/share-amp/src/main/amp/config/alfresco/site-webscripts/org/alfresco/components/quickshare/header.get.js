function main() {
	var shareId = args.shareId;
	//UCM modification: Show "Document details" button even to unauthenticated user. Pass link through 'ucm-guest' webscript.
	model.linkButtons = [ {
		id : "document-details",
		href : url.context + "/page/ucm-guest/quickshare-redirect?id=" + args.shareId,
		label : msg.get("button.document-details"),
		cssClass : "brand-bgcolor-2"
	} ];

}

main();
