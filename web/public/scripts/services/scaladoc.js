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
                }
            });
 
            return defer.promise;
        }
    }
}]);