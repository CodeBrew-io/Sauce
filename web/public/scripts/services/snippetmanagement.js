
app.factory('snippetmanagement', ['$q', '$http', function($q, $http) {
	var defer = $q.defer();

      return {

      	getUserSnippetJsonData: function() {
	        $http({method: 'GET', url: '/snippets/search'}).
			  success(function(data, status, headers, config) {
			    // this callback will be called asynchronously
			    // when the response is available
				defer.resolve(data);
			  }).
			  error(function(data, status, headers, config) {
			    // called asynchronously if an error occurs
			    // or server returns response with an error status.
				defer.reject({error:"temporary error message"});
			  });

			return defer.promise;
      	}
      }
}]);