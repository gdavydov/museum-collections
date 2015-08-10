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


// !!!! TODO create custom jquery player 
// (https://www.theparticlelab.com/building-a-custom-html5-audio-player-with-jquery/)
// https://serversideup.net/style-the-html-5-audio-element/
// http://webdesign.tutsplus.com/tutorials/create-a-customized-html5-audio-player--webdesign-7081
//http://www.sanwebe.com/2013/03/addremove-input-fields-dynamically-with-jquery
function ucmCreateMediaFile(mediaFile) {
	var url = ucmMediaServiceUrl();
	var wrapper = $('<div class="ucm-media-wrapper"/>');

	var name = mediaFile.name;
	var title = mediaFile.title;
	var lang = mediaFile.language;

/*
    var deleteButton = "&nbsp;";
    var deleteButton = "&nbsp;";

	if (mediaFile.permission="write") {
	
		deleteButton = $('<button class="ucm-media-file-delete-button"><img src="/share/images/delete-16-red.png"/></button>'
		).click(function() {
			ucmDeleteFile(mediaFile.nodeRef, wrapper);
		});
	
		editButton = $('<button class="ucm-media-file-edit-button"><img src="/share/images/edit-16.png"/></button>'
		).click(function() {
			ucmEditFile(mediaFile.nodeRef, wrapper);
		});
	}

 */
	
	var deleteButton = $('<button class="ucm-media-file-delete-button"><img src="/share/images/delete-16-red.png"/></button>'
	).click(function() {
		ucmDeleteFile(mediaFile.nodeRef, wrapper);
	});

	var editButton = $('<button class="ucm-media-file-edit-button"><img src="/share/images/edit-16.png"/></button>'
	).click(function() {
		ucmEditFile(mediaFile.nodeRef, wrapper);
	});

	var contentLink = appContext + '/proxy/alfresco/api/node' + mediaFile.link
			+ '/content';
	
	switch (mediaFile.type) {
	case 'audio/mpeg':
	case 'audio/mp3':
		wrapper.addClass('ucm-audio-wrapper');
		wrapper.append(name+' ('+lang+')'+
				     '<span class="ucm-media-right">'+'<audio src="' + contentLink
				+ '" class="ucm-media-audio" preload="none" controls/>'+deleteButton.get(0).outerHTML+editButton.get(0).outerHTML+"</span>");
		break;
	case 'application/pdf':
		var link = $('<a/>', {
			href : contentLink,
			'class' : 'ucm-media-pdf'
		}).html(name+' ('+lang+')').click(function(e) {
			e.preventDefault();
			ucmShowDialog(contentLink, 600, 600, name);
		});
		wrapper.append(link);
		wrapper.append(deleteButton);
		wrapper.append(editButton);
		break;
	case 'application/msword':
	case 'application/vnd.openxmlformats-officedocument.wordprocessingml.document':
	case 'text/plain':
		var link = $('<a/>', {
			href : contentLink,
			'class' : 'ucm-media-pdf'
		}).html(name+' ('+lang+')').click(function(e) {
			e.preventDefault();
			ucmShowDialog(contentLink, 600, 600, name);
		});
		wrapper.append(link);
		wrapper.append(deleteButton);
		wrapper.append(editButton);
		break;
	case 'video/mp4':
		var link = $('<a/>', {
			href : contentLink,
			'class' : 'ucm-media-video'
		}).html(name+' ('+lang+')' +'. Video.').click(function(e) {
			e.preventDefault();
			ucmShowVideo(contentLink, 600, 600, name);
		});
		wrapper.append(link);
		wrapper.append(deleteButton);
		wrapper.append(editButton);
		break;
	case 'text/uri-list':
		var link = $('<a/>', {
			href : mediaFile.title,
			'class' : 'ucm-media-link',
			target : '_blank',
			text : mediaFile.name+" ("+lang+")"
		});
		// Some sites can't be opened inside frame.
		/*
		 * .click(function(e) { e.preventDefault();
		 * ucmShowDialog(mediaFile.title, 600, 600, name); });
		 */
		wrapper.html(link);
		wrapper.append(deleteButton);
		wrapper.append(editButton);
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
		wrapper.append(deleteButton);
		wrapper.append(editButton);
		break;
	}

	return wrapper;
}

function ucmEditFile(nodeRef, element) {
	require(
			[ "jquery" ],
			function($) {
				jQuery = $;
				var editUrl = appContext
						+ "/proxy/alfresco/slingshot/doclib/action/files?alf_method=edit";
/* TODO needs proper implementation !!!!!!!!
				var headers = {};
				headers[Alfresco.util.CSRFPolicy.getParameter()] = Alfresco.util.CSRFPolicy
						.getToken();

				var request = $.ajax({
					url : deleteUrl,
					method : 'POST',
					contentType : 'application/json',
					headers : headers,
					data : JSON.stringify({
						nodeRefs : [ nodeRef ]
					}),
					dataType : 'json'
				});
/*
				request.done(function(msg) {
					element.remove();
				});
*/	
			});
}

function ucmDeleteFile(nodeRef, element) {
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
						nodeRefs : [ nodeRef ]
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

function ucmRefreshMediaFileList(containerSelector, mediaFiles) {
	require([ "jquery" ], function($) {
		jQuery = $;
		var container = $(containerSelector);
		container.empty();

		var audio = $.grep($(mediaFiles), isAudioFile, false);
		var rest = $.grep($(mediaFiles), isAudioFile, true);

		$.each($.merge(audio, rest), function(idx, file) {
			ucmCreateMediaFile(file).appendTo(container);
		});
	});
}

function ucmCreateMediaFileUploader(elementIdPrefix, nodeRef) 
{
	// TODO Add security here (permision==write) otherwise do not do the rest
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
							response.mediaFiles);
				}
				function onFileError(e, file, error) {
					console.error("File Error: " + error);
				}

				var dropzone = $("#" + elementIdPrefix + "-upload-target")
						.find(".fs-upload-target");
				dropzone.css("height", "auto");

				dropzone.append("<br/>");

				var urlInput = $("<input>", {
					type : "text",
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
							response.responseJSON.mediaFiles);
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