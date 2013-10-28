app.factory('snippets', function() {
  return {
    query: function(term){
      return [
        { code: hereDoc(function() {
/*!val pics = List("banana.jpg", "orange.jpg", "strawberry.jpg")
pics.map(fruit => new java.io.File(fruit).length)
            */}),
          insight: 'pics = List("banana.jpg", "orange.jpg", "strawberry.jpg")\nList(345834958, 3458345, 34583745)'
        },
        { code: hereDoc(function() {
/*!List(1,2,3).map(_+1).
    reverse.
    take(2)*/
          }),
        insight: "\n\n\nList(4,3)"
      }];
    }
  };
});