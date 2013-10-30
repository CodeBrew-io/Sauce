app.factory('snippets', function($http) {
  return {
    query: function(term){
    	return $http.get('/snippets/search?q=' + term);
    }
  };
});