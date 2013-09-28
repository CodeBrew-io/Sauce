  $scope.code = hereDoc(function() {
/*!case class Person(name: String, age: Int)
def isElligible(guy: Person, legalAge: Int = 18) = {
    val isAdult = guy.age > legalAge
    isAdult || guy.startsWith("Sa")
}
isElligible(Person("Sam", 2))
*/})

  $scope.insight = hereDoc(function() {
/*!Person(name = "foo", age = 3)
isElligible(guy = Person("foo", 3), legalAge = 18) => false
isAdult = false
false

true*/})