$scope.code = hereDoc(function() {
/*implicit class Equal[L](val left: L) extends AnyVal {
  def ===[R](right: R)(implicit ev: Equality[L, R]): Boolean = ev(left, right)
}

trait Equality[L, R] {
  def apply(left: L, right: R): Boolean
}

object Equality extends LowPriorityEqualityImplicits {
  implicit def rightToLeftEquality[L, R](implicit view: R => L): Equality[L, R] =
    new RightToLeftViewEquality(view)
}

trait LowPriorityEqualityImplicits {
  implicit def leftToRightEquality[L, R](implicit view: L => R): Equality[L, R] =
    new LeftToRightViewEquality(view)
}

private class LeftToRightViewEquality[L, R](view: L => R) extends Equality[L, R] {
  def apply(left: L, right: R): Boolean = view(left) == right
}

private class RightToLeftViewEquality[L, R](view: R => L) extends Equality[L, R] {
  def apply(left: L, right: R): Boolean = left == view(right)
}

1 === 1



1.0 === 1

1 === 1.0*/});

  $scope.insight = hereDoc(function() {
/*! 

























(Equals(1) === 1)(implicit ev = Equality[Int,Int])
eq(1,1)(implicit ev = Equality[Int,Int])
true

true

true
*/});