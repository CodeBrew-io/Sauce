'use strict';

app.controller('searchbar', function code($scope, $timeout, snippets, scaladoc, keyboardManager) {
	$scope.codemirrorOptions = {
		mode: 'text/x-scala',
		theme: 'solarized light',
		readOnly: 'nocursor'
	};
	$scope.docs = [];
	$scope.snippets = [];

	function SetAllItems(scope) {
		if (scope.docs.concat !== undefined && scope.docs.concat !== null) {
			scope.all = scope.docs.concat(scope.snippets);
		} else {
			scope.all = scope.snippets;
		}
	}

	$scope.search = function(term){
		// We make a promise with Scalex for the documentations.
		scaladoc.query(term).then(function(data) {
			$scope.docs = data;
			SetAllItems($scope);
		});

		$scope.snippets = snippets.query(term);
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