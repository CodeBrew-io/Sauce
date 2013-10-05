// based on http://clintberry.com/2013/angular-js-websocket-service/
app.factory('scaladoc', ['$q', '$rootScope', function($q, $rootScope) {
	return {
		query: function(term){
			var defer = $q.defer();
			$.ajax({
		      	url: "http://api.scalex.org/",
		      	data: {
		        	q: term,
		        	callback: "scalex_jc",
		        	page: 1,
		        	per_page: 4
		      	},
		      	dataType: "jsonp",
		      	jsonp: false,
		      	jsonpCallback: "scalex_jc",
		      	cache: true,
		      	success: function (data) {
		        	if (data.error) console.log(data.error)
		        	else {
		        		console.log(data.results);
		          		$rootScope.$apply(defer.resolve(data.results));
		       		}
		        },
	    	});
			return defer.promise;
			// return [
			// 	{ class: "List", fun: "map", signature: "[B](f: (a) => b): List[B]", code: "List(1,2,3).map(_ + 1)", insight: "List(2,3,4)"},
			// 	{ class: "Array", fun: "map", signature: "[B](f: (a) => b): Array[B]", code: "Array(1,2,3).map(_ + 1)", insight: "Array(2,3,4)" },
			// 	{ class: "Seq", fun: "map", signature: "[B](f: (a) => b): Seq[B]", code: "Seq(1,2,3).map(_ + 1)", insight: "Seq(2,3,4)" }
			// ];
		}
	}
}]);

// $scope.search = "";
//   var fdoc = new Fuse([
//     { 
//       class: "Array",
//       fun: "flatMap"
//     },
//     {
//       class: "Array",
//       fun: "map"
//     }
//   ], {keys: ['class','fun']});

//   var fsnippet = new Fuse([
//     { 
//       title: "Map over array",
//       code: "bla bla"
//     },
//     {
//       title: "Map over array 2",
//       code: "bla bli"
//     }
//   ], {keys: ['title','code']});