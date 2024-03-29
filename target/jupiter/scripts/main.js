(function(){

    // entry 
    function init(){
    	document.querySelector('#login-form-btn').addEventListener('click', onSessionInvalid);
    	document.querySelector('#register-form-btn').addEventListener('click', showRegisterForm);
    	document.querySelector('#login-btn').addEventListener('click', login);

		validateSession();
	}
	
	//check session
	function validateSession(){
		onSessionInvalid();
	}
	
	// hide register form, logout button, dummy data, etc
	// show login form
	function onSessionInvalid(){
	    //var loginForm = document.querySelector('#login-form');
	    var loginForm = document.getElementById('login-form');
	    var registerForm = document.querySelector('#register-form');
	    var itemNav = document.querySelector('#item-nav');
	    var itemList = document.querySelector('#item-list');
	    var avatar = document.querySelector('#avatar');
	    var welcomeMsg = document.querySelector('#welcome-msg');
	    var logoutBtn = document.querySelector('#logout-link');
	    
	    hideElement(itemNav);
	    hideElement(itemList);
	    hideElement(avatar);
	    hideElement(logoutBtn);
	    hideElement(welcomeMsg);
	    hideElement(registerForm);

	    clearLoginError();
	    showElement(loginForm);
	}
	
	
	function login() {
	    var username = document.querySelector('#username').value;
	    var password = document.querySelector('#password').value;
	    //md5 加密
	    password = md5(username + md5(password));

	    // The request parameters
	    var url = './login';
	    var req = JSON.stringify({
	      user_id : username,
	      password : password,
	    });

	    ajax('POST', url, req,
	      // successful callback
	      function(res) {
	        var result = JSON.parse(res);

	        // successfully logged in
	        if (result.status === 'OK') {
	        	   console.log(result);
	        }
	      },
	      // error
	      function() {
	        showLoginError();
	      });
	  }

	
	// show register form
	function showRegisterForm() {
        var loginForm = document.querySelector('#login-form');
        var registerForm = document.querySelector('#register-form');
        var itemNav = document.querySelector('#item-nav');
        var itemList = document.querySelector('#item-list');
        var avatar = document.querySelector('#avatar');
        var welcomeMsg = document.querySelector('#welcome-msg');
        var logoutBtn = document.querySelector('#logout-link');

        hideElement(itemNav);
        hideElement(itemList);
        hideElement(avatar);
        hideElement(logoutBtn);
        hideElement(welcomeMsg);
        hideElement(loginForm);
    
        clearRegisterResult();
        showElement(registerForm);
    }  
	

	    
        init();
})();
 //立即执行函数

