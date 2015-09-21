function addOption(theSel, theText, theValue) {
	var newOpt = new Option(theText, theValue);
	var selLength = theSel.length;
	theSel.options[selLength] = newOpt;
}

function deleteOption(theSel, theIndex) {
	var selLength = theSel.length;
	if (selLength > 0) {
		theSel.options[theIndex] = null;
	}
}

function moveOptions(from, to) {
	theSelFrom = document.getElementById(from);
	theSelTo = document.getElementById(to);

	var selLength = theSelFrom.length;
	var selectedText = new Array();
	var selectedValues = new Array();
	var selectedCount = 0;

	var i;

	// Find the selected Options in reverse order
	// and delete them from the 'from' Select.
	for (i = selLength - 1; i >= 0; i--) {
		if (theSelFrom.options[i].selected) {
			selectedText[selectedCount] = theSelFrom.options[i].text;
			selectedValues[selectedCount] = theSelFrom.options[i].value;
			deleteOption(theSelFrom, i);
			selectedCount++;
		}
	}

	// Add the selected text/values in reverse order.
	// This will add the Options to the 'to' Select
	// in the same order as they were in the 'from' Select.
	for (i = selectedCount - 1; i >= 0; i--) {
		addOption(theSelTo, selectedText[i], selectedValues[i]);
	}
}

function submitForm(form) {
	//TODO: show wait indicator
	form.submit()
}

var form = $("#ucm-create-site-form").show();

form.steps(
		{
			headerTag : "h3",
			bodyTag : "fieldset",
			transitionEffect : "slideLeft",
			onStepChanging : function(event, currentIndex, newIndex) {
				// Always allow previous action
				if (currentIndex > newIndex) {
					return true;
				}

				// Needed in some cases if the user went back (clean up)
				if (currentIndex < newIndex) {
					// To remove error styles
					form.find(".body:eq(" + newIndex + ") label.error")
							.remove();
					form.find(".body:eq(" + newIndex + ") .error").removeClass(
							"error");
				}
				form.validate().settings.ignore = ":disabled,:hidden";
				return form.valid();
			},
			onStepChanged : function(event, currentIndex, priorIndex) {
				/**/
			},
			onFinishing : function(event, currentIndex) {
				form.validate().settings.ignore = ":disabled";
				return form.valid();
			},
			onFinished : function(event, currentIndex) {
				submitForm(form);
			}
		}).validate({
	errorPlacement : function errorPlacement(error, element) {
		element.before(error);
	}
/*TODO: validate phone http://jqueryvalidation.org/documentation/
 * , rules : { confirm : { equalTo : "#password-2" } }
 */
});

$('#siteLogo').simpleFilePreview();
$(function() {
	$(document).tooltip();
});