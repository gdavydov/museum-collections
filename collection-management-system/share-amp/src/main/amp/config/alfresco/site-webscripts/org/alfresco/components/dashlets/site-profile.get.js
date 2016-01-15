/**
 * Site Profile component GET method
 */
function main()
{
   // Call the repo for the sites profile
   var profile =
   {
      title: "",
      shortName: "",
      description: ""
   }

   var profile = null;
   var json = remote.call("/api/sites/" + page.url.templateArgs.site);
   if (json.status == 200)
   {
      // Create javascript object from the repo response
      var obj = JSON.parse(json);
      if (obj)
      {
         profile = obj;
      }
   }

   // Find the manager for the site
   var sitemanagers = [];

   json = remote.call("/api/sites/" + page.url.templateArgs.site + "/memberships?rf=SiteManager");
   if (json.status == 200)
   {
      var obj = JSON.parse(json);
      if (obj)
      {
         var managers = [];
         for (var x=0; x < obj.length; x++)
         {
            managers.push(obj[x]);
         }

         sitemanagers = managers;
      }
   }

   //UCM modification: enrich profile object with full site properties data
   if (profile.node) {
	   //"/alfresco/s/api/node/workspace/SpacesStore/d6cfb932-db07-41cb-9591-d22b3d90ae22" -> "/slingshot/node/workspace/SpacesStore/d6cfb932-db07-41cb-9591-d22b3d90ae22"
	   var nodeDetailsUrl = profile.node.replace("/alfresco/s/api/", "/slingshot/");
	   json = remote.call(nodeDetailsUrl);
	   logger.warn(nodeDetailsUrl);
	   if (json.status == 200) {
		   var obj = JSON.parse(json);
		   if (obj && obj.properties) {
			   var props = {};
			   for (var i = 0; i < obj.properties.length; ++i) {
				   var name = obj.properties[i].name.prefixedName.replace(":", "_");
				   props[name] = obj.properties[i].values[0].value;
			   }
			   profile.props = props;
		   }
	   }
   }
   //End of UCM modification

   // Prepare the model
   model.profile = profile;
   model.sitemanagers = sitemanagers;

   // Widget instantiation metdata...
   var dashletTitleBarActions = {
      id : "DashletTitleBarActions",
      name : "Alfresco.widget.DashletTitleBarActions",
      useMessages : false,
      options : {
         actions: [
            {
               cssClass: "help",
               bubbleOnClick:
               {
                  message: msg.get("dashlet.help")
               },
               tooltip: msg.get("dashlet.help.tooltip")
            }
         ]
      }
   };
   model.widgets = [dashletTitleBarActions];
}

main();