function onload() {
 loadSubscriptions();
}

/* GLOBAL VARS */
var lastNotifications = new Array();
var leftTableGlobalVar = "";
var _last_label = "";
var $tabs;
var _current_subscription = "";
var _subscription_response = false;

/* UTILITIES */
function getStringTypeforJSON(_str){
	if(!isNaN(_str)){
		return "NUMBER";
	} else if(_str === "true" || _str === "false"){
		return "BOOLEAN";
	} else {
		return "STRING";
	}
}

function selectLink(_link_ref){
	$(".arrow-right").remove();
	_link_ref.prepend('<img src="img/arrow-right.png" class="arrow-right" />');
}

function createNewJSON(){
	var _s = '{"Name": "","IsActive": false,"DestinationType":"","MessageFilter":{"FilterQuery": "","FilterQueryDelta": "{}","FieldSelection":[""],"DataSource": "","DataType":[""]},"MessageFormatter":{"TypeName": "","ContentOptions":""},"SOICollection": []}';
	var _JSON_obj = JSON.parse(_s);
	var JSONString = JSON.stringify(_JSON_obj, null, "\t");
	$("#theSubscription").val(JSONString);
	createHTMLForm(JSON.parse(_s),"subscription_details");
	$tabs.tabs("select",1);
	// saveSubscriptionForm("subscription_details","subscription-area");
}


function isArray(arr){
	return (typeof arr.push != "undefined");
}

function isObject(obj){
	return (typeof obj == "object");
}

/* JSON to STRING CONVERSION AND MANIPULATION */
function createNode(_jq_ele){
	var _parent = _jq_ele.parent();
	_parent.after(_parent.clone());
	_parent.next().find("input").attr("value","");
	var _label = _parent.next().find("label");
	if(_label.hasClass("arrayLabel")){
		var _txt = _label.text();
		if(_txt.indexOf("Item ")!=-1){
			var _num = _txt.split(" ")[1]-0;
			_label.html(_txt.split(" ")[0] + " " + (_num+1));
		} else {
			_label.html("<input type=\"text\" value=\"\" />").attr("for","");
		}
	} else {
		_label.html("<input type=\"text\" value=\"\" />").attr("for","");
	}
}

function getNodeSelect(_showOnlyText){
	var _s = "";
	_s = _s + "<select class=\"addJSON\" id=\"root_node\">";
	_s = _s + "<option value=\"text\">Text</option>";
	_s = _s + "<option value=\"boolean\">True/False</option>";
	_s = _s + "<option value=\"number\">Number</option>";
	if(!_showOnlyText){
		_s = _s + "<option value=\"object\">JSON</option>";
		_s = _s + "<option value=\"array\">Array</option>";
	}
	_s = _s + "</select>";
	return _s;
}
function getTextFieldforArray(_isArray,_counter,_nc,_ele){
	return "<div data-is-array=\"" + _isArray +"\" class=\"" + _x + "\"><label class=\"label arrayLabel\" for=\"input_" + _counter + "_" + _nc + "\">Item " + _nc + "</label>: <input class=\"val\" type=\"text\" id=\"input_" + _counter + "_" + _nc + "\" value=\"" + _ele + "\" />&nbsp;<button class=\"deleteButton minimal\" onclick=\"$(this).parent().remove(); return false;\">-</button>&nbsp;<button class=\"addJSONButton minimal\" onclick=\"createNode($(this)); return false;\">+</button></div>";
}

function getTextFieldForObject(_isArray,_x,_counter,_nc,_ele,_arrayType,_array_counter){
	return "<div data-is-array=\"" + _isArray +"\" class=\"" + _x + "\"><label class=\"label\" for=\"input_" + _counter + "_" + _nc + "\">" + (_arrayType?"Item "+_array_counter:_x) + "</label>: <input class=\"val\" type=\"text\" id=\"input_" + _counter + "_" + _nc + "\" value=\"" + _ele + "\" />&nbsp;<button class=\"deleteButton minimal\" onclick=\"$(this).parent().remove(); return false;\">-</button>&nbsp;<button class=\"addJSONButton minimal\" onclick=\"createNode($(this)); return false;\">+</button></div>";
}

function stringField(_ele,_name,_string,_counter){
	_string = _string + "<div class=\"field formField jsonString\"><label class=\"label\" for=\"input_" + _counter + "\">" + _name + "</label>: <input class=\"val string\" type=\"text\" id=\"input_" + _counter + "\" value=\"" + _ele + "\" />&nbsp;<button class=\"deleteButton minimal\" onclick=\"$(this).parent().remove(); return false;\">-</button>&nbsp;<button class=\"addJSONButton minimal\" onclick=\"createNode($(this)); return false;\">+</button></div>";
	return _string;
}

function booleanField(_ele,_name,_string,_counter){
	_string = _string + "<div class=\"field formField jsonBoolean\"><label class=\"label\" for=\"input_" + _counter + "\">" + _name + "</label>: <input class=\"val boolean\" type=\"text\" id=\"input_" + _counter + "\" value=\"" + _ele + "\" />&nbsp;<button class=\"deleteButton minimal\" onclick=\"$(this).parent().remove(); return false;\">-</button>&nbsp;<button class=\"addJSONButton minimal\" onclick=\"createNode($(this)); return false;\">+</button></div>";
	return _string;
}

function numberField(_ele,_name,_string,_counter){
	_string = _string + "<div class=\"field formField jsonNumber\"><label class=\"label\" for=\"input_" + _counter + "\">" + _name + "</label>: <input class=\"val number\" type=\"text\" id=\"input_" + _counter + "\" value=\"" + _ele + "\" />&nbsp;<button class=\"deleteButton minimal\" onclick=\"$(this).parent().remove(); return false;\">-</button>&nbsp;<button class=\"addJSONButton minimal\" onclick=\"createNode($(this)); return false;\">+</button></div>";
	return _string;
}


function objectField(_ele,_name,_string,_counter){
	_string = _string + "<div class=\"field formField jsonObject\"><div data-is-array=\"false\" class=\"" + _name + "\"><label class=\"objectLabel label\" for=\"input_" + _counter + "\">" + _name + "</label>&nbsp;<button class=\"deleteButton minimal\" onclick=\"$(this).closest('.jsonObject').remove(); return false;\">Remove Node</button></div>";
	var _nc = 0;
	var _array_counter = 0;
	var _arrayType = false;
	
	function recursiveJson(_x,_ele){
		var _isArray = isArray(_ele); 
		if(isArray(_ele) || isObject(_ele)){
			
			if(isArray(_ele)){
				_arrayType = true;
				_array_counter = 0;
			} else {
				_arrayType = false;
			}
			
			for(_y in _ele){
				if(_last_label != _x){
					_string = _string + "<div data-is-array=\"" + _isArray +"\" class=\"" + _x + "\"><label class=\"label\" for=\"input_" + _counter + "_" + _nc + "\">" + _x + "</label>&nbsp;<button class=\"deleteButton minimal\" onclick=\"$(this).closest('.jsonObject').remove(); return false;\">Remove Node</button></div>";
				}
				_last_label = _x;
				_nc = _nc + 1;
					if(_ele[_y]){
						recursiveJson( (isNaN(_y)?_y:_x) ,_ele[_y]);
					} else {
						_string = _string + getTextFieldForObject(_isArray,_x,_counter,_nc,_ele,_arrayType,_array_counter);
						_array_counter = _array_counter + 1;
					}
			}
			
		}else {
			_string = _string + getTextFieldForObject(_isArray,_x,_counter,_nc,_ele,_arrayType,_array_counter);
			_array_counter = _array_counter + 1;
		}
	}
	
	for(_x in _ele){
		recursiveJson(_x,_ele[_x]);
		_nc = _nc + 1;
	}
	_string = _string + "</div>";
	return _string;
}

function arrayField(_ele,_name,_string,_counter){
	_string = _string + "<div class=\"field formField jsonArray jsonObject\"><div data-is-array=\"true\" class=\"" + _name + "\"><label class=\"objectLabel label\" for=\"input_" + _counter + "\">" + _name + "</label>&nbsp;<button class=\"deleteButton minimal\" onclick=\"$(this).closest('.jsonObject').remove(); return false;\">Remove Node</button></div>";
	var _nc = 0;
	
	function recursiveJson(_x,_ele){
		var _isArray = isArray(_ele); 
		if(isArray(_ele) || isObject(_ele)){
			
			for(_y in _ele){
				if(_last_label != _x){
					_string = _string + "<div data-is-array=\"" + _isArray +"\" class=\"" + _x + "\"><label class=\"label\" for=\"input_" + _counter + "_" + _nc + "\">" + _x + "</label>&nbsp;<button class=\"deleteButton minimal\" onclick=\"$(this).closest('.jsonObject').remove(); return false;\">Remove Node</button></div>";
				}
				_last_label = _x;
				_nc = _nc + 1;
					if(_ele[_y]){
						recursiveJson( (isNaN(_y)?_y:_x) ,_ele[_y]);
					} else {
						_string = _string + getTextFieldforArray(_isArray,_counter,_nc,_ele);
					}
			}
			
		}else {
			_string = _string + getTextFieldforArray(_isArray,_counter,_nc,_ele);
		}
	}
	
	for(_x in _ele){
		recursiveJson(_x,_ele[_x]);
		_nc = _nc + 1;
	}
	_string = _string + "</div>";
	return _string;
}

function getJSONForm(_ele,_name,_string,_counter){
	if(typeof(_ele) === "string"){
		return stringField(_ele,_name,_string,_counter);
	} else if(typeof(_ele) === "boolean"){
		return booleanField(_ele,_name,_string,_counter);
	} else if(typeof(_ele) === "number"){
		return numberField(_ele,_name,_string,_counter);
	} else if(typeof(_ele) === "object"){
		if(isArray(_ele)){
			return arrayField(_ele,_name,_string,_counter);
		} else {
			return objectField(_ele,_name,_string,_counter);
		}
	}
}

function addNode(_nodeType,_wrap,_inputValue){
	var _str = "";
	_inputValue = _inputValue || "";
	if(_nodeType=="text"){
		if(_wrap){
			_str = _str + "<div class=\"field formField jsonString\">";
		}
		_str = _str + "<div data-is-array=\"false\" class=\"" + _inputValue + "\">";
		_str = _str + "<label class=\"label\">";
		_str = _str + "<input type=\"text\" value=\"" + _inputValue + "\" " + (_inputValue==""?"":"readonly=\"readonly\"") + " />";
		_str = _str + "</label>:"; 
		_str = _str + "<input class=\"val string\" type=\"text\" />";
		_str = _str + "&nbsp;";
		_str = _str + "<button class=\"deleteButton minimal\" onclick=\"$(this).closest('div[data-is-array]').remove(); $(this).closest('.jsonString').remove(); return false;\">-</button>&nbsp;<button class=\"addJSONButton minimal\" onclick=\"createNode($(this)); return false;\">+</button>";
		_str = _str + "</div>";
		if(_wrap){
			_str = _str + "</div>";
		}
	} else if(_nodeType=="boolean"){
		if(_wrap){
			_str = _str + "<div class=\"field formField jsonBoolean\">";
		}
		_str = _str + "<div data-is-array=\"false\" class=\"" + _inputValue + "\">";
		_str = _str + "<label class=\"label\">";
		_str = _str + "<input type=\"text\" value=\"" + _inputValue + "\" " + (_inputValue==""?"":"readonly=\"readonly\"") + " />";
		_str = _str + "</label>:"; 
		_str = _str + "<input class=\"val boolean\" type=\"text\" />";
		_str = _str + "&nbsp;";
		_str = _str + "<button class=\"deleteButton minimal\" onclick=\"$(this).closest('div[data-is-array]').remove(); $(this).closest('.jsonBoolean').remove(); return false;\">-</button>&nbsp;<button class=\"addJSONButton minimal\" onclick=\"createNode($(this)); return false;\">+</button>";
		_str = _str + "</div>";
		if(_wrap){
			_str = _str + "</div>";
		}
	} else if(_nodeType=="number"){
		if(_wrap){
			_str = _str + "<div class=\"field formField jsonNumber\">";
		}
		_str = _str + "<div data-is-array=\"false\" class=\"" + _inputValue + "\">";
		_str = _str + "<label class=\"label\">";
		_str = _str + "<input type=\"text\" value=\"" + _inputValue + "\" " + (_inputValue==""?"":"readonly=\"readonly\"") + " />";
		_str = _str + "</label>:"; 
		_str = _str + "<input class=\"val number\" type=\"text\" />";
		_str = _str + "&nbsp;";
		_str = _str + "<button class=\"deleteButton minimal\" onclick=\"$(this).closest('div[data-is-array]').remove(); $(this).closest('.jsonNumber').remove(); return false;\">-</button>&nbsp;<button class=\"addJSONButton minimal\" onclick=\"createNode($(this)); return false;\">+</button>";
		_str = _str + "</div>";
		if(_wrap){
			_str = _str + "</div>";
		}
	} else if(_nodeType=="object"){
		if(_wrap){
			_str = _str + "<div class=\"field innerField jsonObject\">";
		}
		_str = _str + "<div class=\"json_mod\"><strong class=\"objectLabel\">Node Type: </strong>";
		_str = _str + getNodeSelect(false);
		_str = _str + "&nbsp;";
		_str = _str + "<button class=\"addJSONButton minimal\" onclick=\"$(this).closest('.jsonObject').append(addNode($(this).parent().find('.addJSON').val(),false)); return false;\">Add Node</button>";
		_str = _str + "&nbsp;";
		_str = _str + "<button class=\"deleteButton minimal\" onclick=\"$(this).closest('.jsonObject').remove(); return false;\">Remove Node</button>";
		_str = _str + "</div>";
		_str = _str + "<div data-is-array=\"false\">";
		_str = _str + "<label class=\"label\">";
		_str = _str + "<input type=\"text\" />";
		_str = _str + "</label>:"; 
		_str = _str + "</div>";
		if(_wrap){
			_str = _str + "</div>";
		}
	} else if(_nodeType=="array"){
		if(_wrap){
			_str = _str + "<div class=\"field innerField jsonObject jsonArray\">";
		}
		_str = _str + "<div class=\"json_mod\"><strong class=\"objectLabel\">Node Type: </strong>";
		_str = _str + getNodeSelect(true);
		_str = _str + "&nbsp;";
		_str = _str + "<button class=\"addJSONButton minimal\" onclick=\"if($(this).parent().next().find('input').val()!=''){$(this).closest('.jsonObject').append(addNode($(this).parent().find('.addJSON').val(),false,$(this).parent().next().find('input').val()));} else { alert('Please choose a name for the array before adding elements to it.') } return false;\">Add Node</button>";
		_str = _str + "&nbsp;";
		_str = _str + "<button class=\"deleteButton minimal\" onclick=\"$(this).closest('.jsonObject').remove(); return false;\">Remove Node</button>";
		_str = _str + "</div>";
		_str = _str + "<div data-is-array=\"true\">";
		_str = _str + "<label class=\"label\">";
		_str = _str + "Array Name: <input type=\"text\" onchange=\"$(this).closest('div[data-is-array]').attr('class',$(this).val())\" />";
		_str = _str + "</label>:"; 
		_str = _str + "</div>";
		if(_wrap){
			_str = _str + "</div>";
		}
	}
	return _str;
}

function createHTMLForm(jd,_form_id){
	var _s = "";
	
	_s = _s + "<div class=\"json_mod\"><strong class=\"objectLabel\">Node Type: </strong>";
	_s = _s + getNodeSelect(false);
	_s = _s + "&nbsp;";
	_s = _s + "<button class=\"addJSONButton minimal\">Add Node</button>";
	_s = _s + "</div>";
	
	var _indent = 0;
	var _counter = 0;
	for(_i in jd){
		_s = getJSONForm(jd[_i],_i,_s,_counter);
		_counter = _counter + 1;
	}
	
	$("#" + _form_id +"_wrapper").html(_s);
	var _max_width = 0;
	var _max_width_label = 0;
	$("#" + _form_id + " input").each(function(_n){
		var _t = $(this).val().length;
		var _mw = ((_t*12)+100);
		if(_max_width<_mw){
			_max_width=_mw;
		};
		var _t1 = $(this).parent().find(".label").text().length;
		var _mw1 = ((_t1*10));
		if(_max_width_label<_mw1){
			_max_width_label=_mw1;
		};
	});
	
	$("#" + _form_id + " .label").width(_max_width_label);
	$("#" + _form_id + " input").width(_max_width);
	
	// make id readonly
	$("._id").parent().find("button").remove();
	$("._id").parent().find("input").attr("readonly","readonly").css({"background-color":"#CCC","border":"1px solid #2C2C2C"});
	
	// make SOICollection readonly
	$(".SOICollections").parent().find("button").remove();
	$(".SOICollections").parent().find("input").attr("readonly","readonly").css({"background-color":"#CCC","border":"1px solid #2C2C2C"});
	
	$(".addJSONButton").on("click",function(event){
		$(this).parent().after( addNode($(this).parent().find(".addJSON").val(),true) );
		return false;
	});
	
	_last_label = "";
	// display JSON form by default
	$('#subscription-area').show();
	$('#subscription-form').hide();
	$('#subscriptionsoi-area').show();
	$('#subscriptionsoi-form').hide();
	// display arrow on form hyperlink
	selectLink($("#" + _form_id).parent().parent().find(".s-link1"));
	// $("#" + _form_id).parent().show();
}

/* LOAD */
function loadSubscriptions() {
	mule.rpc("/getAllSubscriptions", "", processLoadSubscriptionsResponse);
}



function processLoadSubscriptionsResponse(message) {
	/* the following line is in to take care of double response */
	/*
	 * the issue is on the back end and this should be removed in case the issue
	 * becomes intermittent
	 */
	if(_subscription_response){
		_subscription_response = false;
		return;
	} else {
		_subscription_response=true;
	}
	/* end */

	
	var allSubscriptions = JSON.parse(message.data);
	
	var results = "<table class=\"subscription-table\">";
	results += "<th>Name</th><th>Filter</th><th>Protocol</th><th>Active</th><th>Delete</th>";
	for ( var i = 0; i < allSubscriptions.length; i++) {
		var obj = allSubscriptions[i];
		 if(console){console.debug("obj.Name is: " + obj.Name);}

		var output = JSON.stringify(obj, null, "\t");
		results += "<tr><td><a class=\"subscription-table-link\" href=\"#!\" onclick=\"showSubscriptionDetails( \'"
				+ obj.Name
				+ "\');\">"
				+ obj.Name
				+ "</a></td><td>"
				+ obj.MessageFilter.FilterQuery + "</td>";
		results += "<td>" + obj.DestinationType + "</td>";
		results += "<td>" + obj.IsActive + "</td>";
		results += "<td class=\"center\"><button class=\"minimal\" title=\"Delete Subscription\" onclick=\"deleteSubscription('" + obj.Name + "'); return false;\">X" + "</button></td>";
		
		results += "</tr>";
	}
	results += "</table>";
	dojo.byId("allsubscriptions").innerHTML = results;
	
	// check if we have a URL param and load the last modified subscription
	var _current_subscription_param = $.url().param('_current_subscription');
	if(_current_subscription_param){
		$(".subscription-table .subscription-table-link").each(function(_n){
			if($(this).text()==_current_subscription_param){
				$(this).trigger("click");
				return false;
			}
		});
	}
	/* make the upload form visible */
	showFileUploadForm();
}

/* SAVE */
function saveSubscriptionForm(subscription_form_id, subscription_area_id){
	var subscriptionText = "";
	subscriptionText = subscriptionText + "{";
	
	$("#" + subscription_form_id + "_wrapper > .field").each(function(_n){
		if($(this).hasClass("jsonString")){
			subscriptionText = subscriptionText + "\"" + ($(this).find("label input").size()>0?$(this).find("label input").val():$(this).find("label").text()) + "\":\"" + $(this).find(".val").val() + "\"" + ",";
		} else if($(this).hasClass("jsonBoolean")){
			subscriptionText = subscriptionText + "\"" + ($(this).find("label input").size()>0?$(this).find("label input").val():$(this).find("label").text()) + "\":" + $(this).find(".val").val() + ","
		} else if($(this).hasClass("jsonNumber")){
			subscriptionText = subscriptionText + "\"" + ($(this).find("label input").size()>0?$(this).find("label input").val():$(this).find("label").text()) + "\":" + $(this).find(".val").val() + ","
		}  else if($(this).hasClass("jsonArray") || $(this).hasClass("jsonObject")){
			var _this = $(this);
			var _par_array = [];
			
			_this.children(":not(.json_mod)").each(function(_n){
				
				var _label = $(this).find("label").text();
				var _isArrayLabel = $(this).find("label").hasClass("arrayLabel");
				
				if($(this).find("label input").size()>0){
					_label = $(this).find("label input").val();
				}
				var _value = "";
				if($(this).find(".val").size()>0){
					_value = $(this).find(".val").val();
				}
				
				if(_value==""){
					if($(this).attr("data-is-array")=="true"){
						subscriptionText = subscriptionText + "\"" + _label + "\":[";
						var _className = $(this).attr("class");
						$("." + _className + " input.val").each(function(_n1){
							var _val = $(this).val();
							if(getStringTypeforJSON(_val)==="STRING"){
								subscriptionText = subscriptionText + "\"" + _val + "\"" + ","
							} else if(getStringTypeforJSON(_val)==="BOOLEAN"){
								subscriptionText = subscriptionText + "\"" + _val + ","
							} else if(getStringTypeforJSON(_val)==="NUMBER"){
								subscriptionText = subscriptionText + "\"" + _val + ","
							}
						});
						_par_array.push("]");
					} else {
						subscriptionText = subscriptionText + "\"" + _label + "\":{";
						_par_array.push("}");
					}
				} else {
					if(subscriptionText.indexOf("\"" + $(this).attr("class") + "\":[")==-1){
						if(getStringTypeforJSON(_value)==="STRING"){
							if(_isArrayLabel){
								subscriptionText = subscriptionText + "\"" + _value + "\"" + ",";
							} else {
								subscriptionText = subscriptionText + "\"" + _label + "\":\"" + _value + "\"" + ",";
							}
						} else if(getStringTypeforJSON(_value)==="BOOLEAN"){
							if(_isArrayLabel){
								subscriptionText = subscriptionText + "\"" + _value + "\"" + ",";
							} else {
								subscriptionText = subscriptionText + "\"" + _label + "\":" + _value + ",";
							}
						} else if(getStringTypeforJSON(_value)==="NUMBER"){
							if(_isArrayLabel){
								subscriptionText = subscriptionText + "\"" + _value + "\"" + ",";
							} else {
								subscriptionText = subscriptionText + "\"" + _label + "\":" + _value + ",";
							}
						}
					}
				}
			});
			
			if(subscriptionText.charAt(subscriptionText.length-1)==","){
				subscriptionText = subscriptionText.substring(0,subscriptionText.length-1);
			}
			_par_array.reverse();
			for(_i=0;_i<_par_array.length;_i++){
				subscriptionText = subscriptionText + _par_array[_i];
			}
			subscriptionText = subscriptionText + ",";
		}
	});
	subscriptionText = subscriptionText.substring(0,subscriptionText.length-1);
	subscriptionText = subscriptionText + "}";
	
	if(console){console.log(subscriptionText);}
	
	var _JSON_obj = JSON.parse(subscriptionText);
	var JSONString = JSON.stringify(_JSON_obj, null, "\t");
	
	if(console){console.log("saving \n" + JSONString + "\n for " + subscription_area_id);}
	
	$("#" + subscription_area_id).val(JSONString);
	// if(console){console.log(JSONString);}
	saveSubscription(subscriptionText);
}

function saveSubscription(subscriptionText) {
	mule.rpc("/saveSubscription", subscriptionText, saveResult);
	createHTMLForm(JSON.parse(subscriptionText),"subscription_details");
}

function saveSubscriptionSOI(subscriptionText) {
	mule.rpc("/saveSubscriptionSOI", subscriptionText, "");
	var _href = document.location.href.split("#!")[0]; 
	document.location.replace(_href);
	// onload();
}

function saveResult(message) {
	var result = JSON.parse(message.data);
	if (result.Status == 'Error') {
		var allErrors = result.Errors;
		var s = "";
		for ( var i = 0; i < allErrors.length; i++) {
			var obj = allErrors[i];
			var s = s + "<br/>Error: " + obj.message 
// + "<br/>Expected Enum: "
// + eval('obj.enum') + " - Received Enum: " + obj.value
// + "<br/>Expected Tyepe: " + obj.expected
// + " - Received Type: " + obj.found
// + "<br/>Required: "
// + obj.required
					+" <br /> Missing: " + obj.missing
					+"<br /><br /><a href='javascript: document.location.reload()'>Reload Subscriptions</a>";
		}
		dojo.byId("allsubscriptions").innerHTML = s;
		$tabs.tabs("select",0);
	} else {
		// reload the page after save
		var _href = document.location.href.indexOf("?")==-1 ? document.location.href.split("#!")[0] + "?_current_subscription=" + _current_subscription : (document.location.href.split("?")[0]).split("#!")[0] + "?_current_subscription=" + _current_subscription; 
		document.location.replace(_href);
		// onload();
	}
}

/* DELETE */
function deleteSubscription(subscriptionName) {
	mule.rpc("/deleteSubscription", "{ \"Name\":\"" + subscriptionName + "\"}", resultDelete);
}

function resultDelete(message) {
	var result = JSON.parse(message.data);
	if (result.Status == 'Error') {
		var s = "<br/>Error: " + result.Error + "<br/>"
		+ "<br /><br /><a href='javascript: document.location.reload()'>Reload Subscriptions</a>";	
		dojo.byId("allsubscriptions").innerHTML = s;
		$tabs.tabs("select",0);
	} else {
		var _href = document.location.href.split("#!")[0]; 
		document.location.replace(_href);
		// onload();
	}
}

function  showme(subscriptionName, SOIPath) {
	console.debug("showme : subscriptionName :  " + subscriptionName);
	console.debug("deleteSubscriptionSOI : subscriptionName :  " + subscriptionName);
	console.debug("deleteSubscriptionSOI : SOIPath :  " + SOIPath);
	var payload = "{ \"Name\":\"" + subscriptionName + "\", \"SOIPath\":\"" + SOIPath + "\"}";
	
	mule.rpc("/deleteSubscriptionSOI", payload, "");
	var _href = document.location.href.split("#!")[0]; 
	document.location.replace(_href);
}


function deleteSubscriptionSOICallback(message)
{
	console.debug("deleteSubscriptionSOICallback : message.data :  " + message.data);
	
	}

/* DISPLAY */
function showSubscriptionDetails(subscriptionName) {
	if(console){console.debug("calling showSubscriptionDetails " + subscriptionName);}
	
	_current_subscription = subscriptionName;
	$(".current-subscription").html(subscriptionName);
	$tabs.tabs("select",1);
	mule.rpc("/getSubscriptionByName", "{ \"Name\": \"" + subscriptionName
			+ "\"}", showDetail);

}

function showSOIListForThisSubscription(theSubscription) {
	if (typeof (theSubscription.SOICollections) != "undefined") {

		var results = "<table>";
		results += "<th>Subscription Name</th><th>Delete</th>";
		var len = theSubscription.SOICollections.length;
		for ( var i = 0; i < len; i++) {
			var SOIPath = theSubscription.SOICollections[i].toString().replace(/___/gi,".");
			results += "<tr><td><a href=\"#!\" onclick=\"showSubscriptionSOIDetails(\'"
					+ theSubscription.Name
					+ "\', \'"+SOIPath+"\');\">"
					+ SOIPath
					+ "</a></td>";


			results += "<td class=\"center\"><button class=\"minimal\" title=\"Delete Subscription SOI\" onclick=\"showme('" + theSubscription.Name + "','" + SOIPath + "'); return false;\">X" + "</button></td>";
			
			results += "</tr>";
		}
		results += "</table>";
		dojo.byId("allsubscriptionssoi").innerHTML = results;
	} else {
		$("#allsubscriptionssoi").html("");
		$("#subscriptionsoi_details_wrapper").html("");
		$("#subscriptionsoi-area").val("");
		$('#subscriptionsoi-area').hide();
		$('#subscriptionsoi-form').hide();
		$('.current-soi-subscription').html("");
	}
}

function showDetail(message) {
	if(console){console.debug("calling  " + message);}
	
	createHTMLForm(JSON.parse(message.data),"subscription_details");
	
	var _JSON_obj = JSON.parse(message.data);
	var JSONString = JSON.stringify(_JSON_obj, null, "\t");
	document.getElementById('theSubscription').value = JSONString;
	
	// document.getElementById('theSubscription').value = message.data;
	var theSubscription = JSON.parse(message.data);
	showSOIListForThisSubscription(theSubscription);
}

function showSubscriptionSOIDetails(subscriptionName, SOIPath) {
	$(".current-soi-subscription").html(subscriptionName);
	$tabs.tabs("select",3);
	document.getElementById('theSubscriptionsoi').value="loading the SOI list...please wait..";
	document.getElementById('downloadlinkdiv').innerHTML = '';
	
	var payload = "{ \"Name\":\"" + subscriptionName + "\", \"SOIPath\":\"" + SOIPath + "\"}";
	mule.rpc("/getSubscriptionSOI", payload, showDetailSOIMy);
}

function showDetailSOIMy(message) {
	

	var _JSON_obj = JSON.parse(message.data);
	console.debug("BigSOIList is"+_JSON_obj.BigSOIList);
	console.debug("_JSON_obj.fileName is"+_JSON_obj.fileName);

	
		var a = document.createElement('a');
		
		if(_JSON_obj.BigSOIList=="YES")
		{
			var elem2 = document.createTextNode("The SOI list exceeds 500 elements...We are just showing the first 500.Please use the link below to download all the elements.");
			document.getElementById('bigsoiinfodiv').innerHTML = '';
			document.getElementById('bigsoiinfodiv').appendChild(elem2);
		
		}
		if(_JSON_obj.BigSOIList=="NO")
		{
			document.getElementById('bigsoiinfodiv').innerHTML = '';
		}
		
		var linkText = document.createTextNode("please download All SOI elements here...."+_JSON_obj.Name);
		a.appendChild(linkText);
		
		
		a.href = "SOIFolder/"+_JSON_obj.fileName;
		document.getElementById('downloadlinkdiv').innerHTML = '';
		document.getElementById('downloadlinkdiv').appendChild(a);
	
		
		delete _JSON_obj.BigSOIList;
		delete _JSON_obj.fileName;
	var JSONString = JSON.stringify(_JSON_obj, null, "\t");
	
	document.getElementById('theSubscriptionsoi').value = JSONString;
}



$(function(){
	$tabs = $("#tabs").tabs();
});


function showFileUploadForm(message)
{
	

	
    if(navigator.appName=="Netscape")
	{
    	createFormForChrome();
	}
    else
    	{
    	createFormForIE();
    	}
}
function createFormForIE()
{
	document.getElementById('formuploaddiv').innerHTML = '';
	var form = document.createElement("form");
	form.setAttribute('method',"post");
	var hostname= window.location.hostname;
	var actionAttr = "http://"+hostname+":8081/uploadFile";
	form.setAttribute('action',actionAttr);

	form.setAttribute('enctype',"multipart/form-data");
	form.setAttribute('id',"upload_file_form");

	
	var myfile = document.createElement("input");
	myfile.setAttribute('type',"file");
	myfile.setAttribute('id',"myfile");
	myfile.setAttribute('name',"myfile");
	myfile.setAttribute('onchange',"changeTheValueOfInput()");


	var upload = document.createElement("input");
	upload.setAttribute('type',"submit");
	upload.setAttribute('id',"upload");
	upload.setAttribute('value',"submit");    
	
	 form.appendChild(myfile);
	 form.appendChild(upload);
    document.getElementById('formuploaddiv').appendChild(form);
 
	
	}
function createFormForChrome()
{
	document.getElementById('formuploaddiv').innerHTML = '';
	var form = document.createElement("form");
	form.setAttribute('method',"post");
	var hostname= window.location.hostname;
	var actionAttr = "http://"+hostname+":8081/uploadFile";
	form.setAttribute('action',actionAttr);

	form.setAttribute('enctype',"multipart/form-data");
	form.setAttribute('id',"upload_file_form");

	var p=document.createElement("p");
	p.setAttribute('class',"form");

	var path = document.createElement("input");
	path.setAttribute('id',"path");
	path.setAttribute('type',"text");

	var label = document.createElement("label");
	label.setAttribute('class',"classname");	
	label.innerHTML="choose file";

	var span = document.createElement('span')
		
	var myfile = document.createElement("input");
	myfile.setAttribute('type',"file");
	myfile.setAttribute('id',"myfile");
	myfile.setAttribute('name',"myfile");
	myfile.setAttribute('onchange',"changeTheValueOfInput()");



	var upload = document.createElement("input");
	upload.setAttribute('type',"submit");
	upload.setAttribute('id',"upload");
	upload.setAttribute('class',"classname");


	span.appendChild(myfile);
	label.appendChild(span);

	p.appendChild(path);
	p.appendChild(label);
	p.appendChild(upload);
	form.appendChild(p);
	document.getElementById('formuploaddiv').appendChild(form);

}
function changeTheValueOfInput() {
	 var myfile = document.getElementById("myfile").value;
	document.getElementById("path").value=myfile;
}
 


	
