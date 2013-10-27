// based on http://clintberry.com/2013/angular-js-websocket-service/
app.factory('errormessage', ['$q', '$rootScope', '$compile', function($q, $rootScope, $compile) {
	var defer = $q.defer();
	var squigglyElementList = [];

	return  {

		appendSquigglyLine: function(squigglyLineCss) {
			var element = $compile('<div class="squiggly-line ' + squigglyLineCss + '"></div>')($rootScope)[0];
			squigglyElementList.push(element);
		},

		addCodeMirror: function(codeMirror) {			
			var line_number = 0;
			var size = squigglyElementList.length;
			if (size > 0 && size < 27) {
				for (var i = 0; i < size; i++) {
					codeMirror.addWidget({line: line_number, ch: i}, squigglyElementList[i]); 
				}
			}
		}
	}
}]);