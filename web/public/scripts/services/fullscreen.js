app.factory('fullscreen' ,['$rootScope', function($rootScope) {

	return {
		requestFullScreen: function(element) {
			if (element === null || element === undefined) {
				element = document.documentElement;
			}

			if (element.requestFullScreen) {
            	element.requestFullScreen();
			} else if (element.mozRequestFullScreen) {

			} else if (element.webkitRequestFullScreen) {

			}
		}
	}

}]);