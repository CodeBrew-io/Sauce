app.controller('header', function header($scope) {
	'use strict';

	$scope.profileOpen = false;
	$scope.outProfile = function(){
		$scope.profileOpen = false;
	}
	$scope.toogleProfile = function(){
		$scope.profileOpen = !$scope.profileOpen;
	}
});	