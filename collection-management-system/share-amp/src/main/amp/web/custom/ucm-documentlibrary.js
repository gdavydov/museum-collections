//http://docs.alfresco.com/4.0/concepts/EXIF-renderer-source-code.html#Share-doclib-EXIF-renderer-source-code
(function()
{
/*
   var $html = Alfresco.util.encodeHTML,
      $isValueSet = Alfresco.util.isValueSet;

   if (Alfresco.DocumentList)
   {     
        YAHOO.Bubbling.fire("registerRenderer",
        {
           propertyName: "exposure",
           renderer: function exif_renderer(record, label)
           {
              var jsNode = record.jsNode,              
                 properties = jsNode.properties,
                 html = "";
                 
              var expTime = properties["exif:exposureTime"] || 0,
                 exifObj =              
                 {                 
                     exposureFraction: expTime > 0 ? "1/" + Math.ceil(1/expTime) : expTime,                 
                     fNumber: properties["exif:fNumber"] || 0,                 
                     isoSpeedRatings: properties["exif:isoSpeedRatings"] || 0              
                 };
                 
              html = '<span class="item">' + label + '<b>' +
YAHOO.lang.substitute(this.msg("exif.metadata.exposure"), exifObj) + '</b></span>';
              
              return html;
          }
      });
   }
)();*/
})();