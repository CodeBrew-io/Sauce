'use strict';

app.controller('searchbar', function code($scope, $timeout, snippets, scaladoc, keyboardManager) {
	$scope.codemirrorOptions = {
		mode: 'text/x-scala',
		theme: 'solarized light',
		readOnly: 'nocursor'
	};

	$scope.docs = [];
	$scope.snippets = [];
	$scope.all = [];

	$scope.search = function(term){
		snippets.query({terms: term}, function(data){
			$scope.snippets = data;
			$scope.all = data;
		})
		// $scope.snippets = [{
		// 	snippet: {
		// 		code: "1+1"
		// 	}
		// }];
		// $scope.all = $scope.snippets;
	};

	$scope.hasDocs = function(){
		return $scope.docs.length > 0;
	};

	$scope.hasSnippets = function(){
		return $scope.snippets.length > 0;
	};

	$scope.select = function(item){
		// todo select
		// $scope.code += '\n\n' + item.code;
		// $scope.term = "";
	};
});