// based on http://clintberry.com/2013/angular-js-websocket-service/
app.factory('insight', ['$q', '$rootScope', "$location", function($q, $rootScope, $location) {
	var url;
	if($location.host() === "codebrew.io") {
		url = "wss://codebrew.io/eval"
	} else {
		url = "ws://localhost:9000/eval"
	}
	var socket = new WebSocket(url);
	var callbacks = {};
	var currentCallbackId = 0;
	var lastMessage = null;

	function getCallbackId() {
		currentCallbackId += 1;
		/* max: http://ecma262-5.com/ELS5_HTML.htm#Section_8.5*/
		if(currentCallbackId >= 9007199254740992 - 1) {
			currentCallbackId = 0;
		}
		return currentCallbackId;
	}

	function listener(data) {
		if(callbacks.hasOwnProperty(data.callback_id)) {
			var insight = data;

			$rootScope.$apply(callbacks[data.callback_id].resolve(insight));
			delete callbacks[data.callback_id];
		}
    }

	socket.onmessage = function(message){
		listener(JSON.parse(message.data));
	};

	socket.onopen = function(){
		socket.send(lastMessage);
	};

	return function(code, position){
		var request = {};
		var defer = $q.defer();
		var callbackId = getCallbackId();
		callbacks[callbackId] = defer;
		request.callback_id = callbackId;
		request.code = code;
		request.position = position;

		if( socket.readyState === socket.CONNECTING ) {
			lastMessage = JSON.stringify(request)
		} else {
			socket.send(JSON.stringify(request));
		}
		
		return defer.promise;		  
	}
}]);

