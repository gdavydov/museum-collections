function ucmImageZoom() {
	require(['jquery'], function($) {
		jQuery = $;
		require([appContext + "/res/js/jquery.elevatezoom.js"], function() {
			var image = $('#ucm-artifact-image').find('img');
			image.attr( {'data-zoom-image': image.attr('src')} );
			image.elevateZoom( {zoomType: 'inner', cursor: 'crosshair', scrollZoom : true} );
		});
	});
}

/**
 * Customized version of Alfresco.WebPreview.prototype.Plugins.Image.prototype.display function, defined in components/preview/Image.js
 * Display the image with zoom. In both cases of image creation ucmImageZoom function is called after it.
 */
function Image_displayUCM()
{ 
   var srcMaxSize = this.attributes.srcMaxSize;
   if (!this.attributes.src && srcMaxSize.match(/^\d+$/) && this.wp.options.size > parseInt(srcMaxSize))
   {
      // The node's content was about to be used and its to big to display
      var msg = '';
      msg += this.wp.msg("Image.tooLargeFile", this.wp.options.name, Alfresco.util.formatFileSize(this.wp.options.size));
      msg += '<br/>';
      msg += '<a class="theme-color-1" href="' + this.wp.getContentUrl(true) + '">';
      msg += this.wp.msg("Image.downloadLargeFile");
      msg += '</a>';
      msg += '<br/>';
      //UCM customization
      msg += '<a style="cursor: pointer;" class="theme-color-1" onclick="javascript: this.parentNode.parentNode.innerHTML = \'<img src=' + this.wp.getContentUrl(false) + '>\'; ucmImageZoom();">';
      msg += this.wp.msg("Image.viewLargeFile");
      msg += '</a>';
      return '<div class="message">' + msg + '</div>';
   }
   else
   {
      var src = this.attributes.src ? this.wp.getThumbnailUrl(this.attributes.src) : this.wp.getContentUrl();

      var image = new Image;
      image.onload = function()
      {
         if ('naturalHeight' in this)
         {
            if (this.naturalHeight + this.naturalWidth === 0)
            {
               this.onerror();
               return;
            }
         } else if (this.width + this.height == 0)
         {
            this.onerror();
            return;
         }
         // At this point, there's no error.
         this.wp.widgets.previewerElement.innerHTML = '';
         this.wp.widgets.previewerElement.appendChild(image);
         //UCM customization
         ucmImageZoom(); 
      };
      image.onerror = function()
      {
         //display error
         this.wp.widgets.previewerElement.innerHTML = '<div class="message">'
               + this.wp.msg("label.noPreview", this.wp.getContentUrl(true))
               + '</div>';
      };
      image.wp = this.wp;
      image.src = src;

      return null;
   }
}
