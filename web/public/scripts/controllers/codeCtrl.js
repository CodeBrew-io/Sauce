'use strict';

app.controller('CodeCtrl', function CodeCtrl($scope, $timeout, snippets, scaladoc, insight) {
  $scope.code = "";
  $scope.insightCode = "";

  $scope.options = {
    fixedGutter: false,
    lineNumbers: true,
    mode: 'text/x-scala',
    theme: 'solarized dark',
    smartIndent: false,
    autofocus: true,
    onChange : function(cm) {
      if($scope.code != "") {
        $scope.insightCode = insight($scope.code);
      }
    }
  };
  $scope.options2 = {
    fixedGutter: false,
    lineNumbers: true,
    mode: 'text/x-scala',
    theme: 'solarized light',
    readOnly: 'nocursor'
  };

  $scope.options3 = {
    mode: 'text/x-scala',
    theme: 'solarized light',
    readOnly: 'nocursor'
  };
});