/* This is modified version of components/document-details/document-links.js */

/**
 * Show link to document content.
 *
 * @namespace Alfresco
 * @class Alfresco.UCMDocumentLink
 */
(function()
{
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom,
      Event = YAHOO.util.Event,
      Selector = YAHOO.util.Selector;

   /**
    * Alfresco Slingshot aliases
    */
   var $combine = Alfresco.util.combinePaths;

   /**
    * UCMDocumentLink constructor.
    *
    * @param {String} htmlId The HTML id of the parent element
    * @return {Alfresco.UCMDocumentLink} The new UCMDocumentLink instance
    * @constructor
    */
   Alfresco.UCMDocumentLink = function(htmlId)
   {
      Alfresco.UCMDocumentLink.superclass.constructor.call(this, "Alfresco.UCMDocumentLink", htmlId, []);

      // Initialise prototype properties
      this.hasClipboard = window.clipboardData && window.clipboardData.setData;

      return this;
   };

   YAHOO.extend(Alfresco.UCMDocumentLink, Alfresco.component.Base,
   {
      /**
       * Object container for initialization options
       *
       * @property options
       * @type object
       */
      options:
      {
         /**
          * Reference to the current document
          *
          * @property nodeRef
          * @type string
          */
         nodeRef: null,

         /**
          * Current siteId, if any.
          *
          * @property siteId
          * @type string
          */
         siteId: null,

         /**
          * File name
          *
          * @property fileName
          * @type string
          */
    	  fileName: null
      },

      /**
       * Does the browser natively support clipboard data?
       *
       * @property hasClipboard
       * @type boolean
       */
      hasClipboard: null,

      /**
       * @method: onReady
       */
      onReady: function DocumentLinks_onReady()
      {
          // Display copy links
          if (this.hasClipboard)
          {
             Dom.removeClass(Selector.query("a.hidden", this.id), "hidden");
          }

          // Make sure text fields auto select the text on focus
          Event.addListener(Selector.query("input", this.id), "focus", this._handleFocus);

          var nodeRefString = this.options.nodeRef.replace('://', '/');
          var fileName = this.options.fileName;
          var proxyNoauthUrl = Alfresco.constants.PROXY_URI.replace(new RegExp('alfresco/$'), 'alfresco-noauth/');
          var fixedLink = $combine(proxyNoauthUrl, '/ucm/ucm-guest-content/' + nodeRefString + '/' + fileName);
          Dom.get(this.id + "-page").value = fixedLink;
      },

      /**
       * called when the "onCopyLinkClick" link has been clicked.
       * Tries to copy URLs to the system clipboard.
       *
       * @method onCopyLinkClick
       * @param rel {string} The Dom Id of the element holding the URL to copy
       */
      onCopyLinkClick: function DocumentLinks_onCopyLinkClick(rel, anchor)
      {
         var link = Dom.getPreviousSibling(anchor);
         window.clipboardData.setData("Text", link.value);
      },

      /**
       * Event handler used to select text in the field when focus is received
       *
       * @method _handleFocus
       */
      _handleFocus: function DocumentLinks__handleFocus()
      {
         this.select();
      }
   });
})();
