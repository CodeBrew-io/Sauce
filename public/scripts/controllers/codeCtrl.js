'use strict';

app.controller('CodeCtrl', function CodeCtrl($scope, $timeout, snippets, scaladoc, insight) {
  (function(){ /* Doc */

    $scope.docs = [];
    $scope.snippets = [];

    $scope.search = function(term){
      $scope.docs = scaladoc.query(term);
      $scope.snippets = snippets.query(term);
      $scope.all = $scope.docs.concat($scope.snippets);
    };

    $scope.hasDocs = function(){
      return $scope.docs.length > 0;
    };

    $scope.hasSnippets = function(){
      return $scope.snippets.length > 0;
    };

    $scope.select = function(item){
      $scope.code += '\n\n' + item.code;
      $scope.insight += '\n\n' + item.insight;
      // $scope.term = "";
    };
  })();
  
  (function(){ /* Code & Insight */
    $scope.timer = null;
    $scope.code = "";
    $scope.insightCode = "";

    $scope.options = {
      fixedGutter: false,
      lineNumbers: true,
      mode: 'text/x-scala',
      theme: 'solarized dark',
      smartIndent: false,
      autofocus: true,
      onChange: function(cm,event) {

        // $scope.insightState = 'fetching';
        // if($scope.timer) {
        //   $timeout.cancel($scope.timer);
        // }
        // $scope.timer = $timeout(function(){
        //   $scope.insightState = '';

        //   var res = [];
        //   $scope.code.split('\n').forEach(function(line){
        //     res.push(changes[line]);
        //   })
        //   $scope.insightCode = res.join('\n');

        // }, 1000);

        $scope.insightCode = insight($scope.code);
          $scope.cmRight.scrollTo(null, scrollLeftInfo['top']);
      }
    };
    $scope.options2 = {
      fixedGutter: false,
      lineNumbers: true,
      mode: 'text/x-scala',
      theme: 'solarized light',
      readOnly: 'nocursor'
          $scope.cmLeft.scrollTo(null, scrollRightInfo['top']);
    };

    $scope.options3 = {
      mode: 'text/x-scala',
      theme: 'solarized light',
      readOnly: 'nocursor'
    };
  })();

  (function() { /* Insight toggling */
    $scope.insightToggler = function() {
        $scope.insightShow = !$scope.insightShow;
    $scope.userDropDownShow = !$scope.userDropDownShow;
    }
  })();

  (function() { /* This part of the code is for the User's behavior in the header of the site */
    $scope.userDropDownShow = false;
    $scope.onUserClick = function() {
      $scope.userDropDownShow = !$scope.userDropDownShow;
    }
  })();

  (function() { /* The showing of the Modal */
    $scope.modalShow = true;
    $scope.showSettingsModal = function() {
      $scope.modalShow = !$scope.modalShow;
    }
  })();
});