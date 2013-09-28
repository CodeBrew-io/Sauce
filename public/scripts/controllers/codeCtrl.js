'use strict';

app.controller('CodeCtrl', function CodeCtrl($scope, $timeout, snippets, scaladoc) {
  (function(){ /* Doc */

    $scope.docs = [];
    $scope.snippets = [];

    $scope.search = function(term){
      if("map" !== term) {
        $scope.docs = [];
        $scope.snippets = [];
        $scope.all = [];
        return;
      }

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

      $scope.code = hereDoc(function() {
/*!case class Person(name: String, age: Int)
def isElligible(guy: Person, legalAge: Int = 18) = {
    val isAdult = guy.age > legalAge
    isAdult || guy.name.startsWith("Sa")
}
isElligible(Person("Sam", 2))
*/})

    $scope.insightCode = hereDoc(function() {
/*!Person(name = "foo", age = 3)
isElligible(guy = Person("foo", 3), legalAge = 18) => false
isAdult = false
false

true*/})

    $scope.options = {
      lineNumbers: true,
      mode: 'text/x-scala',
      theme: 'solarized dark',
      smartIndent: false,
      autofocus: true,
      onChange: function(cm,event) {

        var changes = {
          "val a = 45 + 56":"a = 101",
          "val b = a + 2": "b = 103",
          "def foo(p: Int, z: Boolean) = if (z) p else p -1": "foo(p = 3, z = true) => 3",
          "foo(b, false)": "102",
          "List(1,2,3).map(_+1).": "List(2,3,4)",
          "\treverse.": "List(4,3,2)",
          "\ttake(2)": "List(4,3)",
          'val pics = List("banana.jpg", "orange.jpg", "strawberry.jpg")': 'pics = List("banana.jpg", "orange.jpg", "strawberry.jpg")',
          'pics.map(fruit => new java.io.File(fruit).length)': 'List(2344345, 3467546, 58867865)',
          'Array(1,2,3).map(_ + 1)': 'Array(2,3,4)',
          'List(1,2,3).map(_ + 1)': 'List(2,3,4)',
          'Seq(1,2,3).map(_ + 1)': 'Seq(2,3,4)'
        }

        $scope.insightState = 'fetching';
        if($scope.timer) {
          $timeout.cancel($scope.timer);
        }
        $scope.timer = $timeout(function(){
          $scope.insightState = '';

          var res = [];
          $scope.code.split('\n').forEach(function(line){
            res.push(changes[line]);
          })
          $scope.insightCode = res.join('\n');

        }, 1000);

      }
    };
    $scope.options2 = {
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
  })();
  
});