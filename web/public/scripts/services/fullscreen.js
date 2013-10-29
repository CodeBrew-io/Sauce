app.factory('fullscreen' ,function($rootScope, keyboardManager) {
	var _zen = false;
	var ev = "zen";

	function requestFullScreen(element){
		if(element == null) {
			element = document.documentElement;
		}

		if (element.requestFullScreen) {
			element.requestFullScreen();
		} else if (element.mozRequestFullScreen) {
			element.mozRequestFullScreen();
		} else if (element.webkitRequestFullScreen) {
			element.webkitRequestFullScreen(Element.ALLOW_KEYBOARD_INPUT);
		}else{
			return false;
		}
		return true;
	}

	function cancelFullScreen(){
		if (document.cancelFullScreen) {
			document.cancelFullScreen();
		} else if (document.mozCancelFullScreen) {
			document.mozCancelFullScreen();
		} else if (document.webkitCancelFullScreen) {
			document.webkitCancelFullScreen();
		}else{
			return false;
		}
		return true;
	}

	function isFullScreen(){
		return ((document.fullScreenElement != undefined && document.fullScreenElement !== null) // HTML5 spec
		|| (document.mozFullScreen != undefined && document.mozFullScreen === true) // Mozilla
		|| (document.webkitIsFullScreen != undefined && document.webkitIsFullScreen === true)); // webkit
	}

	function applyZenMode(){
		if (_zen) {
			requestFullScreen(null);
		} else {
			cancelFullScreen();
		}
		$rootScope.$broadcast(ev, _zen);
	}

	return {
		zen: _zen,
		event: ev,
		apply: function(z){
			_zen = z;
			applyZenMode();
		},
		bindKeyboard: function(){
			keyboardManager.bind('f11',function(){
				_zen = !_zen;
				applyZenMode();
			});
			keyboardManager.bind('esc',function(){
				_zen = false;
				applyZenMode();
			});
		}
		
	}
});

app.run(function(fullscreen){
	fullscreen.bindKeyboard();
})