// based on http://clintberry.com/2013/angular-js-websocket-service/
app.factory('errormessage', ['$q', '$rootScope', '$compile', function($q, $rootScope, $compile) {
	var defer = $q.defer();
	return  {
		/*
		This function will create the SquigglyLine HtmlElement that will be used by CodeMirror
		*/
		createSquigglyLine: function(squigglyLineCss) {
			return $compile('<div class="squiggly-line ' + squigglyLineCss + '"></div>')($rootScope)[0];
		},
		/*
		This function makes the necessary waiting through angular's promise.
		*/
		waitingCodeMirror: function() {
			return defer.promise;
		},
		/*
		This adds the codeMirror to the errorMessage factory.
		*/
		addCodeMirror: function(codeMirror) {
			$rootScope.$apply(function() {
				defer.resolve(codeMirror);
			});

			return defer.promise;
		}
	}
}]);