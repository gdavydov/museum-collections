<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

function main()
{
   AlfrescoUtil.param('nodeRef');
   AlfrescoUtil.param('site', null);
   var documentDetails = AlfrescoUtil.getNodeDetails(model.nodeRef, model.site);
   if (documentDetails)
   {
      model.document = documentDetails;
      model.repositoryUrl = AlfrescoUtil.getRepositoryUrl();
      model.fileName = documentDetails.item.fileName;
   }

   // Widget instantiation metadata...
   var documentActions = {
      id: "UCMDocumentLink",
      name: "Alfresco.UCMDocumentLink",
      options: {
         nodeRef: model.nodeRef,
         siteId: model.site,
         fileName: model.fileName
      }
   };
   model.widgets = [documentActions];
}

main();