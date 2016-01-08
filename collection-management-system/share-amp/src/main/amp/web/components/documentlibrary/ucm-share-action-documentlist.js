/**
 * This is modified version of Alfresco.QuickShare widget defined in share/js/share.js:846
 */
function()
{
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom,
      Selector = YAHOO.util.Selector;

   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML,
      $combine = Alfresco.util.combinePaths;

   /**
    * Favourite constructor.
    *
    * @param {String|HTMLElement|null} el (Optional) The HTML id of the parent element, the element OR null if markup shall be inserted
    * @return {Alfresco.QuickShare} The new QuickShare instance
    * @constructor
    */
   Alfresco.UCMQuickShare = function UCMQuickShare_constructor(el)
   {
      var id;
      if (el)
      {
         if (YAHOO.lang.isString(el))
         {
            // Assume an html id was used
            id = el;
            el = Dom.get(el);
         }
         else if (!el.getAttribute("id"))
         {
            // Make sure element has a unique id so it can use the onReady callback
            id = Alfresco.util.generateDomId(el);
         }
         YAHOO.util.Dom.addClass(el, "item-social");
      }
      else
      {
         // Make sure element has a unique id so it can use the onReady callback
         id = Alfresco.util.generateDomId();
      }

      // Call superclass constructor
      Alfresco.UCMQuickShare.superclass.constructor.call(this, "Alfresco.UCMQuickShare", id, ["json"]);

      if (el)
      {
         // Save a reference to the base element
         this.widgets.spanEl = el;
      }

      // Make prototype attributes instance attribetus
      this._sharedId = null;
      this._sharedBy = null;
      this._menuWasAlreadyOpened = false;
      this._initialDisplay = true;

      return this;
   };

   YAHOO.extend(Alfresco.UCMQuickShare, Alfresco.component.Base,
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
          * @type String
          */
         nodeRef: null,

         /**
          * Display name of the current document
          *
          * @property displayName
          * @type String
          */
         displayName: null
      },

      /**
       * The shareId if shared
       *
       * @property _sharedId
       * @type String
       * @private
       */
      _sharedId: null,

      /**
       * An object representing the person who share the node, if shared.
       * Contains userName & displayName
       *
       * @property _sharedBy
       * @type Object
       * @private
       */
      _sharedBy: null,

      /**
       * Keeps track if the menu is showed or not
       *
       * @property _menuWasAlreadyOpened
       * @type boolean
       * @default false
       */
      _menuWasAlreadyOpened: false,

      /**
       * Keeps track if we are calling the display method for the first time or not
       *
       * @property _initialDisplay
       * @type boolean
       * @default true
       */
      _initialDisplay: true,

      /**
       * Used after options has been set to activate the widget and also to update it if its state has changed.
       *
       * @method display
       * @param shareId {String}
       * @param sharedBy {Object} containing username and displayName attributes
       * @return {String} html markup for rendering the widget, can be used if no element was provided in the constructor
       */
      display: function(shareId, sharedBy)
      {
         // Update state
         this._sharedId = shareId;
         this._sharedBy = sharedBy;

         // Prepare html to use if this widget that should be inserted
         var innerHTML = this._render(),
            html = '<span id="' + this.id + '" class="item-social">' + innerHTML + '</span>';

         // Update ui if element already has been created
         if (this.widgets.spanEl && this._initialDisplay)
         {
            this.widgets.spanEl.innerHTML = innerHTML;
         }
         else if (this.widgets.spanEl)
         {
            var i18n = "quickshare.document.";
            if (this._sharedId)
            {
               this.widgets.action.setAttribute("title", this.msg(i18n + "shared.tip", this._sharedBy.displayName));
               this.widgets.action.innerHTML = this.msg(i18n + "shared.label");
               Dom.addClass(this.widgets.action, "enabled");
               Dom.addClass(this.widgets.indicator, "enabled");
            }
            else
            {
               this.widgets.action.setAttribute("title", this.msg(i18n + "share.tip"));
               this.widgets.action.innerHTML = this.msg(i18n + "share.label");
               Dom.removeClass(this.widgets.action, "enabled");
               Dom.removeClass(this.widgets.indicator, "enabled");
            }
         }
         this._initialDisplay = false;

         // Returns the markup for this widget so it can be inserted
         return html;
      },

      /**
       * Create html that represent a quickshare widget
       *
       * @method render
       * @return {String} html markup for the part inside the base element of the widget
       */
      _render: function UCMQuickShare_render()
      {
         var i18n = "quickshare.document.",
            tip = this.msg(i18n + "share.tip"),
            label = this.msg(i18n + "share.label"),
            linkCss = "quickshare-action",
            indicatorCss = "quickshare-indicator";

         if (this._sharedId)
         {
            // The widget was enabled, make sure the initial rendering shows that
            tip = this.msg(i18n + "shared.tip", this._sharedBy.displayName);
            label = this.msg(i18n + "shared.label");
            linkCss += " enabled";
            indicatorCss += " enabled";
         }

         var html = '<a href="#" class="' + linkCss + '" title="' + tip + '">' + label + '</a>';
         html += '<span class="' + indicatorCss + '">&nbsp;</span>';
         return html;
      },

      /**
       * Called once the base element is found in the Dom
       */
      onReady: function()
      {
         // Store reference to base el if it wasn't provided in constructor
         this.widgets.spanEl = Dom.get(this.id);

         //TODO: выпотрошить это всё ***************************************************************************************************************
         // Create service instance
         this.services.quickshare = new Alfresco.service.QuickShare();

         // Save reference to link and make it behave differently depending on the widgets state
         this.widgets.action = Selector.query("a.quickshare-action", this.widgets.spanEl, true);
         Alfresco.util.useAsButton(this.widgets.action, function(e)
         {
            if (!this._sharedId)
            {
               this.services.quickshare.share(this.options.nodeRef,
               {
                  successCallback:
                  {
                     fn: function(response)
                     {
                        // Redraw the widget
                        var share = response.json;
                        this.display(share.sharedId, Alfresco.constants.USERNAME);

                        // Open the menu
                        this.showMenu();
                     }, scope: this
                  },
                  failureMessage: this.msg("quickshare.document.share.failure")
               });
            }
            else
            {
               if (this._menuWasAlreadyOpened)
               {
                  this._menuWasAlreadyOpened = false;
               }
               else
               {
                  this.showMenu();
               }
            }

            YAHOO.util.Event.preventDefault(e);
         }, null, this);
         YAHOO.util.Event.addListener(this.widgets.action, "mousedown", function()
         {
            this._menuWasAlreadyOpened = this.widgets.overlay && this.widgets.overlay.cfg.getProperty("visible");
         }, null, this);

         this.widgets.indicator = Selector.query("span.quickshare-indicator", this.widgets.spanEl, true);
      },

      /**
       * Shows the menu dialog
       *
       * @method showMenu
       */
      showMenu: function()
      {
         if (!this.widgets.overlay)
         {
            var overlayEl = document.createElement("div");
            Dom.addClass(overlayEl, "yuimenu");
            Dom.addClass(overlayEl, "quickshare-action-menu");

            var html = '' +
               '<div class="bd">' +
               '  <span class="section">' +
               '     <label for="' + this.id + '-input">' + this.msg("quickshare.link.label") + ':</label> <input id="' + this.id + '-input" type="text" tabindex="0"/>' +
               '     <a href="#" class="quickshare-action-view">' + this.msg("quickshare.view.label") + '</a>' +
               '     <a href="#" class="quickshare-action-unshare">' + this.msg("quickshare.unshare.label") + '</a>' +
               '  </span>';
            if (Alfresco.constants.LINKSHARE_ACTIONS)
            {
               html += '' +
                  '  <span class="section">' +
                  '     <label>' + this.msg("quickshare.linkshare.label") + ':</label> <span class="quickshare-linkshare"></span>' +
                  '  </span>';
            }
            html += '</div>';

            overlayEl.innerHTML = html;

            this.widgets.overlay = Alfresco.util.createYUIOverlay(overlayEl,
            {
               context: [
                  this.widgets.action,
                  "tl",
                  "bl",
                  ["beforeShow", "windowResize"]
               ],
               effect:
               {
                  effect: YAHOO.widget.ContainerEffect.FADE,
                  duration: 0.1
               },
               visible: false
            }, {
               type: YAHOO.widget.Menu
            });
            this.widgets.overlay.render(Dom.get("doc3"));

            this.widgets.unshare = Selector.query("a.quickshare-action-unshare", overlayEl, true);
            Alfresco.util.useAsButton(this.widgets.unshare, function(e)
            {
               YAHOO.util.Event.stopEvent(e);
               this.services.quickshare.unshare(this._sharedId,
               {
                  successCallback:
                  {
                     fn: function(response)
                     {
                        // Redraw the widget as unshared
                        this.display();

                        // Hide the menu
                        this.widgets.overlay.hide();
                     }, scope: this
                  },
                  failureCallback:
                  {
                     fn: function(response)
                     {
                        if (response.json.status.code == 403)
                        {
                           Alfresco.util.PopupManager.displayPrompt(
                           {
                              text: this.msg("quickshare.document.unshare.user.failure")
                           });
                        }
                        else
                        {
                           Alfresco.util.PopupManager.displayPrompt(
                           {
                              text: this.msg("quickshare.document.unshare.failure")
                           });
                        }
                     },
                     scope: this
                  }
               });
            }, null, this);

            // Save references to overlay elements
            this.widgets.link = Selector.query("input", overlayEl, true);
            this.widgets.view = Selector.query("a.quickshare-action-view", overlayEl, true);
            this.widgets.linkshare = Selector.query(".quickshare-linkshare", overlayEl, true);

            // Make sure input is focused when displayed
            YAHOO.util.Event.addListener(this.widgets.link, "focus", this.widgets.link.select, null, this.widgets.link);
            YAHOO.util.Event.addListener(this.widgets.link, "mouseover", this.widgets.link.select, null, this.widgets.link);
            YAHOO.util.Event.addListener(this.widgets.link, "click", this.widgets.link.select, null, this.widgets.link);
            YAHOO.util.Event.addListener(this.widgets.link, "keydown", this.widgets.link.select, null, this.widgets.link);
            YAHOO.util.Event.addListener(this.widgets.link, "keyup", this.widgets.link.select, null, this.widgets.link);
            Alfresco.util.createBalloon(this.widgets.link, {
               text: this.msg("quickshare.link.tooltip" + (YAHOO.env.ua.os == "macintosh" ? ".mac" : ""))
            }, "mouseover", "mouseout");
            this.widgets.overlay.showEvent.subscribe(function (p_event, p_args)
            {
               this.widgets.link.focus();
            }, this, true);
         }

         // Update info
         var url = YAHOO.lang.substitute(Alfresco.constants.QUICKSHARE_URL, { sharedId: this._sharedId });
         if (url.indexOf("/") == 0)
         {
            url = window.location.protocol + "//" + window.location.host + url;
         }
         this.widgets.link.value = url;
         this.widgets.view.setAttribute("href", url);

         if (this.widgets.linkshare)
         {
            this.widgets.linkshare.innerHTML = new Alfresco.LinkShare().setOptions({
               shareUrl: url,
               displayName: this.options.displayName
            }).display();
         }

         // Show overlay
         this.widgets.overlay.show();
      }

   });
})();


/**
 * This is modified version of generateQuickShare function defined in share/components/documentlibrary/documentlist.js:773
 * It uses Alfresco.UCMQuickShare widget instead of Alfresco.QuickShare.
 */
Alfresco.DocumentList.ucmGenerateQuickShare = function DL_generateQuickShare(scope, record) {
	return new Alfresco.UCMQuickShare().setOptions({
		nodeRef: record.jsNode.nodeRef,
		displayName: record.displayName
	}).display(record.jsNode.properties.qshare_sharedId, record.jsNode.properties.qshare_sharedBy);
};

YAHOO.lang.augmentObject(Alfresco.DocumentList.prototype, {
	// Custom registerRenderer for social features, originally defined in:
	// share/components/documentlibrary/documentlist.js:2158
	ucmSocialRenderer: function(record) {
		var jsNode = record.jsNode,
		html = "";

		/* Favourite / Likes / Comments */
		html += '<span class="item item-social">' + Alfresco.DocumentList.generateFavourite(this, record) + '</span>';
		html += '<span class="item item-social item-separator">' + Alfresco.DocumentList.generateLikes(this, record) + '</span>';
		if (jsNode.permissions.user.CreateChildren) {
			html += '<span class="item item-social item-separator">' + Alfresco.DocumentList.generateComments(this, record) + '</span>';
		}
		if (!record.node.isContainer && Alfresco.constants.QUICKSHARE_URL) {
			html += '<span class="item item-separator">' + Alfresco.DocumentList.generateQuickShare(this, record) + '</span>';
		}

		//UCM modification: collections and artists don't contain QuickShare widget because they both are containers. They are handled separately.
		if (["ucm:collection", "ucm:artist"].indexOf(jsNode.type) != -1 ) {
			html += '<span class="item item-separator">' + Alfresco.DocumentList.ucmGenerateQuickShare(this, record) + '</span>';
		}

		return html;
	},

	// Overwrite registerRenderer which was originally defined in:
	// webapps/share/components/documentlibrary/documentlist.js:1803
	registerRenderer: function DL_registerRenderer(propertyName, renderer) {
		if (Alfresco.util.isValueSet(propertyName) && Alfresco.util.isValueSet(renderer)) {
			if (propertyName === "social") {
				this.renderers[propertyName] = this.ucmSocialRenderer;
			} else {
				this.renderers[propertyName] = renderer;
			}
			return true;
		}
		return false;
	}
}, true);