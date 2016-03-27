function getDocumentDetailsUrl(shareId)
{
   var result = remote.connect("alfresco").get("/api/internal/shared/share/" + encodeURIComponent(page.url.args.id));
   if (result.status == 200)
   {
      var info = JSON.parse(result);

      //UCM modification: check if node is of type "ucm:artifact". Use "artifact-details" page if it is.
      var pageType = 'document-details';

      var metadataResult = remote.connect("alfresco").get("/slingshot/doclib/node/" + info.nodeRef.replace(':/', ''));
      if (metadataResult.status == 200)
      {
         var metadata = JSON.parse(metadataResult);
         if (metadata.item.nodeType == 'ucm:artifact') {
        	 pageType = 'artifact-details';
         }
      }

      return url.context + "/page" + (info.siteId ? "/site/" + encodeURIComponent(info.siteId) : "") + '/' + pageType + '?nodeRef=' + encodeURIComponent(info.nodeRef);
   }
   else
   {
      // In the unlikely case it was not found just go to the dashboard
      return url.context;
   }
}

function main()
{
   model.redirectUrl = getDocumentDetailsUrl(page.url.args.id);
}

main();
