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
		if(term == '') {
			$scope.docs = [];
			$scope.snippets = [];
			$scope.all = [];
		} else {
			snippets.query({terms: term}, function(data){
				$scope.snippets = data;
				scaladoc.query(term).then(function(data){
					$scope.docs = data;
					$scope.all = $scope.snippets.concat($scope.docs);
				});
			});
		}
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