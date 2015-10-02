function safeAccess() {
	var object = arguments[0];
	for (var i = 1; i < arguments.length; ++i) {
		object = (object == undefined || object == null) ? object
				: object[arguments[i]];
	}
	return object;
}


function ucmIsEditable() {
    var actionsComponent = Alfresco.util.ComponentManager.findFirst("Alfresco.DocumentActions");
    return !!safeAccess(actionsComponent, 'options', 'documentDetails', 'item', 'node', 'permissions', 'user', 'Write');
}


function ucmShowDialog(url, wdth, hght, tltl) {
	var $dialog = $('<div/>', {
		class : 'ucm-media-dialog'
	}).html('<iframe class="ucm-media-frame" src="' + url + '"/>').dialog({
		autoOpen : false,
		modal : false,
		height : wdth,
		width : hght,
		title : tltl
	});
	$dialog.dialog('open');
}

function ucmShowVideo(url, wdth, hght, tltl) {
	// TODO: may width be in percents? autoplay?
	var video = $(
			'<video controls width="' + (wdth - 35)
					+ '">Your browser does not support HTML5 video.</video>')
			.append($('<source/>', {
				src : url,
				type : 'video/mp4'
			}));
	var $dialog = $('<div class="ucm-media-dialog"/>')
	// .html('<iframe class="ucm-media-frame" src="' + url + '"/>')
	.html(video).dialog({
		autoOpen : false,
		modal : false,
		height : wdth,
		width : hght,
		title : tltl,
		beforeClose : function(event, ui) {
			// stops video
			$dialog.dialog('destroy').remove();
		}
	});
	$dialog.dialog('open');
}

function ucmMediaServiceUrl() {
	var result = appContext + "/proxy/alfresco/ucm/media";
	if (Alfresco.util.CSRFPolicy && Alfresco.util.CSRFPolicy.isFilterEnabled()) {
		result += "?" + Alfresco.util.CSRFPolicy.getParameter() + "="
				+ encodeURIComponent(Alfresco.util.CSRFPolicy.getToken());
	}
	return result;
}

function ucmAddControlButtons(container, deleteButton, editButton) {
	if (ucmIsEditable()) {
		container.append(deleteButton);
		container.append(editButton);
	}
}

// !!!! TODO create custom jquery player
// (https://www.theparticlelab.com/building-a-custom-html5-audio-player-with-jquery/)
// https://serversideup.net/style-the-html-5-audio-element/
// http://webdesign.tutsplus.com/tutorials/create-a-customized-html5-audio-player--webdesign-7081
// http://www.sanwebe.com/2013/03/addremove-input-fields-dynamically-with-jquery
function ucmCreateMediaFile(mediaFile, containerSelector, parentNodeRef) {
	var url = ucmMediaServiceUrl();
	var wrapper = $('<div class="ucm-media-wrapper"/>');

	var name = mediaFile.name;
	var lang = mediaFile.language;
	var nameWithLang = name + ' (' + lang + ')';
	var title = mediaFile.title;

	/*
	 * var deleteButton = "&nbsp;"; var deleteButton = "&nbsp;";
	 * 
	 * if (mediaFile.permission="write") {
	 * 
	 * deleteButton = $('<button class="ucm-media-file-delete-button"><img
	 * src="/share/images/delete-16-red.png"/></button>' ).click(function() {
	 * ucmDeleteFile(mediaFile.nodeRef, wrapper); });
	 * 
	 * editButton = $('<button class="ucm-media-file-edit-button"><img
	 * src="/share/images/edit-16.png"/></button>' ).click(function() {
	 * ucmEditFile(mediaFile.nodeRef, wrapper); }); }
	 * 
	 */

	var deleteButton = $(
			'<button class="ucm-media-file-delete-button"><img src="/share/images/delete-16-red.png"/></button>')
			.click(
					function() {
						ucmDeleteFile(mediaFile, wrapper,
								containerSelector, parentNodeRef);
					});

	var editButton = $(
			'<button class="ucm-media-file-edit-button"><img src="/share/images/edit-16.png"/></button>')
			.click(
					function() {
						ucmEditFile(mediaFile, wrapper,
								containerSelector, parentNodeRef);
					});

	var contentLink = appContext + '/proxy/alfresco/api/node' + mediaFile.link
			+ '/content';

	switch (mediaFile.type) {
	case 'audio/mpeg':
	case 'audio/mp3':
		wrapper.addClass('ucm-audio-wrapper');
		wrapper.append(nameWithLang);
		var audioSpan = $('<span>', {
			'class' : 'ucm-media-right'
		})
		audioSpan.append('<audio src="' + contentLink
				+ '" class="ucm-media-audio" preload="none" controls/>')
		ucmAddControlButtons(audioSpan, deleteButton, editButton);
		wrapper.append(audioSpan);
		break;
	case 'application/pdf':
		var link = $('<a/>', {
			href : contentLink,
			'class' : 'ucm-media-pdf'
		}).html(nameWithLang).click(function(e) {
			e.preventDefault();
			ucmShowDialog(contentLink, 600, 600, name);
		});
		wrapper.append(link);
		ucmAddControlButtons(wrapper, deleteButton, editButton);
		break;
	case 'application/msword':
	case 'application/vnd.openxmlformats-officedocument.wordprocessingml.document':
	case 'text/plain':
		var link = $('<a/>', {
			href : contentLink,
			'class' : 'ucm-media-pdf'
		}).html(nameWithLang).click(function(e) {
			e.preventDefault();
			ucmShowDialog(contentLink, 600, 600, name);
		});
		wrapper.append(link);
		ucmAddControlButtons(wrapper, deleteButton, editButton);
		break;
	case 'video/mp4':
		var link = $('<a/>', {
			href : contentLink,
			'class' : 'ucm-media-video'
		}).html(nameWithLang + '. Video.').click(function(e) {
			e.preventDefault();
			ucmShowVideo(contentLink, 600, 600, name);
		});
		wrapper.append(link);
		ucmAddControlButtons(wrapper, deleteButton, editButton);
		break;
	case 'text/uri-list':
		var link = $('<a/>', {
			href : mediaFile.title,
			'class' : 'ucm-media-link',
			target : '_blank',
			text : nameWithLang
		});
		// Some sites can't be opened inside frame.
		/*
		 * .click(function(e) { e.preventDefault();
		 * ucmShowDialog(mediaFile.title, 600, 600, name); });
		 */
		wrapper.html(link);
		ucmAddControlButtons(wrapper, deleteButton, editButton);
		break;
	default: // TODO: let user save content instead of showing dialog?
		var link = $('<a/>', {
			href : contentLink,
			'class' : 'ucm-media-other'
		}).html(name).click(function(e) {
			e.preventDefault();
			ucmShowDialog(contentLink, 600, 600, name);
		});
		wrapper.append(link);
		ucmAddControlButtons(wrapper, deleteButton, editButton);
		break;
	}

	return wrapper;
}

function ucmEditFile(mediaFile, element, containerSelector, parentNodeRef) {
	// Intercept before dialog show
	//TODO: i18n
	var doBeforeDialogShow = function ucm_onEditMedia_doBeforeDialogShow(
			p_form, p_dialog) {
		// Dialog title
		Alfresco.util.populateHTML([ p_dialog.id + "-dialogTitle",
				'Edit Properties: <span class="light">' + Alfresco.util.encodeHTML(mediaFile.name || "") + '</span>' ]);

		// Edit metadata link button
		Alfresco.util.ComponentManager.findFirst('Alfresco.module.SimpleDialog').editMetadata = Alfresco.util.createYUIButton(p_dialog,
				"editMetadata", null, {
					type : "link",
					label : "All properties",
					href : Alfresco.util.siteURL("edit-metadata?nodeRef=" + mediaFile.nodeRef)
				});
	};

	var templateUrl = YAHOO.lang
			.substitute(
					Alfresco.constants.URL_SERVICECONTEXT
							+ "components/form?itemKind={itemKind}&itemId={itemId}&destination={destination}&mode={mode}&submitType={submitType}&formId={formId}&showCancelButton=true",
					{
						itemKind : "node",
						itemId : mediaFile.nodeRef,
						mode : "edit",
						submitType : "json",
						formId : "doclib-simple-metadata"
					});

	// Using Forms Service, so always create new instance
	var editMedia = new Alfresco.module.SimpleDialog(this.id + "-editDetails-"
			+ Alfresco.util.generateDomId());

	//TODO: scope: editMedia ?
	editMedia.setOptions({
		width : "auto",
		templateUrl : templateUrl,
		actionUrl : null,
		destroyOnHide : true,
		doBeforeDialogShow : {
			fn : doBeforeDialogShow,
			scope : this
		},
		onSuccess : {
			fn : function ucm_onEditMedia_success(response) {
				ucmAjaxRefreshMediaFileList(containerSelector, parentNodeRef);
			},
			scope : this
		},
		onFailure : {
			fn : function ucm_onEditMedia_failure(response) {
				Alfresco.util.PopupManager.displayMessage({
					text : "Edit failed!"
				});
				editMedia.widgets.cancelButton.set("disabled", false);
			},
			scope : this
		}
	});
	editMedia.show();
	return editMedia;
}

function ucmDeleteFile(mediaFile, element, containerSelector, parentNodeRef) {
	require(
			[ "jquery" ],
			function($) {
				jQuery = $;
				var deleteUrl = appContext
						+ "/proxy/alfresco/slingshot/doclib/action/files?alf_method=delete";
				var headers = {};
				headers[Alfresco.util.CSRFPolicy.getParameter()] = Alfresco.util.CSRFPolicy
						.getToken();

				var request = $.ajax({
					url : deleteUrl,
					method : 'POST',
					contentType : 'application/json',
					headers : headers,
					data : JSON.stringify({
						nodeRefs : [ mediaFile.nodeRef ]
					}),
					dataType : 'json'
				});

				request.done(function(msg) {
					element.remove();
				});
			});
}

function isAudioFile(file) {
	return (file.type === 'audio/mpeg' || file.type === 'audio/mp3');
}

function ucmRefreshMediaFileList(containerSelector, mediaFiles, nodeRef) {
	require([ "jquery" ], function($) {
		jQuery = $;
		var container = $(containerSelector);
		container.empty();

		var audio = $.grep($(mediaFiles), isAudioFile, false);
		var rest = $.grep($(mediaFiles), isAudioFile, true);

		$.each($.merge(audio, rest), function(idx, file) {
			ucmCreateMediaFile(file, containerSelector, nodeRef).appendTo(
					container);
		});
	});
}

function ucmAjaxRefreshMediaFileList(containerSelector, nodeRef) {
	// TODO: sync: true ?
	require([ "jquery" ], function($) {
		jQuery = $;
		$.ajax(ucmMediaServiceUrl(), {
			method : 'GET',
			contentType : false,
			processData : true,
			data : {
				nodeRef : nodeRef
			},
			success : function(e, file, response) {
				ucmRefreshMediaFileList(containerSelector,
						response.responseJSON.mediaFiles, nodeRef);
			},
			error : function(xhr, status, error) {
				console.error("Media file list update error: " + error);
				Alfresco.util.PopupManager.displayMessage({
					text : "Error: can't update list of attachments!"
				});
			}
		});
	});
}

function ucmCreateMediaFileUploader(elementIdPrefix, nodeRef) {
	//Uploader can't be used if user has no write permission on the node
	if (!ucmIsEditable()) return;
	
	require([ "jquery" ], function($) {
		jQuery = $;
		require([ appContext + "/res/js/formstone/core.js" ], function() {
			require([ appContext + "/res/js/formstone/upload.js" ], function() {
				$("#" + elementIdPrefix + "-upload-target").upload({
					action : ucmMediaServiceUrl(),
					postKey : "prop_cm_content",
					maxQueue : 1,
					maxSize : 200 * 1024 * 1024,
					postData : {
						nodeRef : nodeRef
					}
				}).on("start.upload", onStart)
						.on("complete.upload", onComplete).on(
								"filestart.upload", onFileStart).on(
								"fileprogress.upload", onFileProgress).on(
								"filecomplete.upload", onFileComplete).on(
								"fileerror.upload", onFileError);

				function onStart(e, files) {
					console.log("Start");
				}
				function onComplete(e) {
					console.log("Complete");
				}
				function onFileStart(e, file) {
					console.log("File Start");
				}
				function onFileProgress(e, file, percent) {
					console.log("File Progress");
				}
				function onFileComplete(e, file, response) {
					console.log("File Complete");
					var containerSelector = '#' + elementIdPrefix
							+ "-body.document-ucm-media-files";
					ucmRefreshMediaFileList(containerSelector,
							response.mediaFiles, nodeRef);
				}
				function onFileError(e, file, error) {
					console.error("File Error: " + error);
				}

				var dropzone = $("#" + elementIdPrefix + "-upload-target")
						.find(".fs-upload-target");
				dropzone.css("height", "auto");

				dropzone.append("<br/>");

				var urlInput = $("<input>", {
					type : "url",
					"class" : "ucm-media-url-input"
				});
				setInlineInputDescription(urlInput, "... or enter URL here");
				// suppress file choser dialog opening on URL input click
				urlInput.click(function() {
					return false;
				});
				// 'enter' handler
				urlInput.keypress(function(e) {
					if (e.which == 13) {
						ucmSubmitMediaUrl(elementIdPrefix, nodeRef, urlInput
								.val());
						return false;
					}
					return true;
				});

				dropzone.append(urlInput);

				var linkSubmitButton = $("<button>", {
					"class" : "yui-button ucm-media-url-button",
					text : "Add link"
				});
				linkSubmitButton
						.click(function() {
							ucmSubmitMediaUrl(elementIdPrefix, nodeRef,
									urlInput.val());
							return false;
						});
				linkSubmitButton.keypress(function(e) {
					if (e.which == 13) {
						ucmSubmitMediaUrl(elementIdPrefix, nodeRef, urlInput
								.val());
						return false;
					}
					return true;
				});
				dropzone.append(linkSubmitButton);
			});
		});
	});
}

function setInlineInputDescription(input, descriptionText) {
	require([ "jquery" ], function($) {
		jQuery = $;
		$(input).focus(function(srcc) {
			if ($(this).hasClass("inline-input-description")) {
				$(this).removeClass("inline-input-description");
				$(this).val("");
			}
		});

		$(input).blur(function() {
			if ($(this).val() === "") {
				$(this).addClass("inline-input-description");
				$(this).val(descriptionText);
			}
		});

		$(input).blur();
	});
}

// see https://gist.github.com/dperini/729294
function isUrlValid(url) {
	var re_weburl = new RegExp("^" +
	// protocol identifier
	"(?:(?:https?|ftp)://)" +
	// user:pass authentication
	"(?:\\S+(?::\\S*)?@)?" + "(?:" +
	// IP address exclusion
	// private & local networks
	"(?!(?:10|127)(?:\\.\\d{1,3}){3})"
			+ "(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})"
			+ "(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})" +
			// IP address dotted notation octets
			// excludes loopback network 0.0.0.0
			// excludes reserved space >= 224.0.0.0
			// excludes network & broacast addresses
			// (first & last IP address of each class)
			"(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])"
			+ "(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}"
			+ "(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))" + "|" +
			// host name
			"(?:(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)" +
			// domain name
			"(?:\\.(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)*" +
			// TLD identifier
			"(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))" +
			// TLD may end with dot
			".?" + ")" +
			// port number
			"(?::\\d{2,5})?" +
			// resource path
			"(?:[/?#]\\S*)?" + "$", "i");

	return re_weburl.test(url);
}

function ucmSubmitMediaUrl(elementIdPrefix, nodeRef, url) {
	if (url.indexOf('http') != 0) {
		url = 'http://' + url;
	}
	if (isUrlValid(url)) {
		var formData = new FormData();
		formData.append('nodeRef', nodeRef);
		formData.append('prop_cm_title', url);
		formData.append('prop_cm_content', url);
		formData.append('prop_mimetype', 'text/uri-list');

		require([ "jquery" ], function($) {
			jQuery = $;
			$.ajax(ucmMediaServiceUrl(), {
				method : 'POST',
				contentType : false,
				processData : false,
				data : formData,
				success : function(e, file, response) {
					console.log("URL Upload Complete");
					var containerSelector = '#' + elementIdPrefix
							+ "-body.document-ucm-media-files";
					ucmRefreshMediaFileList(containerSelector,
							response.responseJSON.mediaFiles, nodeRef);
				},
				error : function(xhr, status, error) {
					console.error("URL Error: " + error);
				}
			});
		});
	} else {
		Alfresco.util.PopupManager.displayMessage({
			text : "Please enter valid URL"
		})
	}
}