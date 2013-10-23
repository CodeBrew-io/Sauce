'use strict';

app.controller('CodeCtrl', function CodeCtrl($scope, $timeout, insight) {
  var cmLeft, cmRight, flag = true;
  $scope.code = "";
  $scope.insightCode = "";

  $scope.options = {
    lineNumbers: true,
    mode: 'text/x-scala',
    theme: 'solarized dark',
    smartIndent: false,
    autofocus: true,
    onChange : function(cm) {
      $scope.insightCode = insight($scope.code);
    },
    onScroll: function(cm) {
      if (cmRight !== null) {
        cmRight.scrollTo(null, cm.getScrollInfo()['top']);
      }
    },
    onLoad: function(cm) {
      cmLeft = cm;
    }
  };
  $scope.options2 = {
    lineNumbers: true,
    mode: 'text/x-scala',
    theme: 'solarized dark',
    readOnly: 'nocursor',
    onScroll: function(cm) {
      if (cmLeft !== null) {
        cmLeft.scrollTo(null, cm.getScrollInfo()['top']);
      }
    },
    onLoad: function(cm) {
       cmRight = cm;
    }
  };
});