function test1(selector,words){
    document.querySelector(selector+"").textContent = words;
    return;
}

window.onload = function(){
	document.getElementById('nativeLoginStatus').innerHTML = "heres the test div!";
	return;
}

document.querySelector("#main_header_sub2").onclick = function(){
  if(document.querySelector("#main_header_sub2").textContent === "KlaymoJones"){
    test1("#main_header_sub2","KlaySquared")
  }
  else{
    test1("#main_header_sub2","KlaymoJones");
  }
	return;
};




//function to login using normal method
function standardLogin(){
//TODO: MAKE DIS!!
	var username;
	var password;
	username = document.getElementById('usernameIn').value;
	password = document.getElementById('passwordIn').value;
	//check to see if values are undefined or null and if they are report error to the status div
	if (!String.trim) {
  		String.prototype.trim = function() {return this.replace(/^\s+|\s+$/g, "");};
	}
	if(username == null || username.trim()=="" || password == null | password.trim() == ""){
		
		document.getElementById('nativeLoginStatus').innerHTML = "please enter something for username/password";
	}	
	else{//send a post to the /login servlet and redirect on success or report error if prablem

		loginPost(null,'standard',username,password);

	}

	return;

	
}




//function to send xmlPost to create session using facebook AUth written by Klay Klay Krueger
function loginPost(response,method,username,password){
	
	var xmlRequest = new XMLHttpRequest();
	var url = "http://localhost:8080/ThingPile/login";
	var params;
	alert
	if(method === 'facebook'){
		params = "username=" + response.authResponse.userID +"&method=facebook&token=" + response.authResponse.accessToken;
	}else if(method === 'standard'){
		params = "username=" + username +"&method=standard&password=" + password;
	}
	xmlRequest.open("POST", url, true);
	
	xmlRequest.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
		
	xmlRequest.onreadystatechange = function() {//Call a function when the state changes.
		
    		if(xmlRequest.readyState == 4 ) {
			var redirectHeader;
			redirectHeader = xmlRequest.getResponseHeader("redirect")
			alert(redirectHeader);
			if(redirectHeader === "false"){
				
				document.getElementById('nativeLoginStatus').innerHTML = xmlRequest.responseText;
			}else{
        			//TODO: manual redirect? YES
				var redirectUrl;
				redirectUrl = xmlRequest.responseURL;
				if(redirectUrl != null && redirectUrl !== 'undefined'){
					window.location = xmlRequest.responseURL;
				}
			}
    		}
	}

	
	xmlRequest.send(params);
	//alert("in your apparently shitty post function. Params: " + params);
	
	return;
	
	 

}

function statusChangeCallback(response) {
    console.log('statusChangeCallback');
    console.log(response);
    // The response object is returned with a status field that lets the
    // app know the current login status of the person.
    // Full docs on the response object can be found in the documentation
    // for FB.getLoginStatus().
    if (response.status === 'connected') {
      // Logged into your app and Facebook.
      //testAPI();
	loginPost(response,'facebook',null,null);
    } else if (response.status === 'not_authorized') {
      // The person is logged into Facebook, but not your app.
      document.getElementById('status').innerHTML = 'Please log ' +
        'into this app.';
    } else {
      // The person is not logged into Facebook, so we're not sure if
      // they are logged into this app or not.
      document.getElementById('status').innerHTML = 'Please log ' +
        'into Facebook.';
    }
  }





// This function is called when someone finishes with the Login
  // Button.  See the onlogin handler attached to it in the sample
  // code below.
  function checkLoginState() {
    FB.getLoginStatus(function(response) {
      statusChangeCallback(response);
    });
  }

  window.fbAsyncInit = function() {
  FB.init({
    appId      : 'INSERTAPPIDHERE',
    cookie     : true,  // enable cookies to allow the server to access 
                        // the session
    xfbml      : true,  // parse social plugins on this page
    version    : 'v2.5' // use graph api version 2.5
  });

  // Now that we've initialized the JavaScript SDK, we call 
  // FB.getLoginStatus().  This function gets the state of the
  // person visiting this page and can return one of three states to
  // the callback you provide.  They can be:
  //
  // 1. Logged into your app ('connected')
  // 2. Logged into Facebook, but not your app ('not_authorized')
  // 3. Not logged into Facebook and can't tell if they are logged into
  //    your app or not.
  //
  // These three cases are handled in the callback function.

  FB.getLoginStatus(function(response) {
    statusChangeCallback(response);
  });

  };

  // Load the SDK asynchronously
  (function(d, s, id) {
    var js, fjs = d.getElementsByTagName(s)[0];
    if (d.getElementById(id)) return;
    js = d.createElement(s); js.id = id;
    js.src = "//connect.facebook.net/en_US/sdk.js";
    fjs.parentNode.insertBefore(js, fjs);
  }(document, 'script', 'facebook-jssdk'));

  // Here we run a very simple test of the Graph API after login is
  // successful.  See statusChangeCallback() for when this call is made.
  function testAPI() {
    console.log('Welcome!  Fetching your information.... ');
    FB.api('/me', function(response) {
      console.log('Successful login for: ' + response.name);
      document.getElementById('status').innerHTML =
        'Thanks for logging in, ' + response.name + '!';
    });
  }

