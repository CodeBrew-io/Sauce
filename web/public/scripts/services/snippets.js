app.factory('snippets', function($resource) {
  return $resource('/snippets',{},{
  	"queryUser": { method: 'GET', url: '/snippets/' },
  	"query": { method: 'GET', isArray: true },
  	"": { method: 'POST' }
  })
});