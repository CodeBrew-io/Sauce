package io.codebrew
package eval

import org.specs2._

class Representation extends Specification { def is = s2"""
	
	Html Representation
		type class should kick in when using
			only any			$any
			only Plot			$plot
			both any and Plot 	$both

	Type classes should
		lift any 				${Lift.any}
		lift plot 				${Lift.plot}
		lift any and plot 		${Lift.both}
		
"""
	def any = {
		val code = """|val a = "a"
					  |a""".stripMargin
		val server = new EvalImpl()
		val result = server.insight(code)
		Option(result.runtimeError) ==== None
	}

	def plot = {
		val code = """|import Plot._
					  |val c = List((1,1), (2,2))
					  |c""".stripMargin
		val server = new EvalImpl()
		val result = server.insight(code)
		Option(result.runtimeError) ==== None
	}

	def both = {
		val code = """|val a = "a"
					  |a
					  |val b = 1
					  |b
					  |import io.codebrew.simpleinsight.html.Plot._
					  |val c = List((1,1), (2,2))
					  |c""".stripMargin
		val server = new EvalImpl()
		val result = server.insight(code)

		import scala.collection.JavaConverters._
		import api.eval._
		import InstrumentationType._
		import Severity._

		def itype(it: InstrumentationType)(other: Instrumentation) = it === other.itype
		def info(sev: Severity)(other: CompilationInfo) = sev ==== other.severity

		(Option(result.runtimeError) ==== None) and
		(result.infos.asScala must not contain(info(ERROR) _)) and
		(result.insight must not beNull) and
		(result.insight.asScala must contain(itype(CODE) _).exactly(2.times)) and
		(result.insight.asScala must contain(itype(JSON) _).exactly(1.times))
	}

	object Lift {
		import _root_.io.codebrew.simpleinsight.Instrument._
		import _root_.io.codebrew.simpleinsight.html._
		import _root_.io.codebrew.simpleinsight.html.Generic._
		
		def any = {
			val b = 1
			show(b) must beLike {
				case Code(_) => ok
				case _ => ko
			}
		}

		def plot = {
			import Plot._
			val c = List((1,1), (2,2))
			show(c) must beLike {
				case Json(_) => ok
				case _ => ko
			}
		}

		def both = {
			import Plot._
			val a = "a"
			val b = 1
			val c = List((1,1), (2,2))
			List(show(a), show(b), show(c)) must beLike {
				case List(Code(_), Code(_), Json(_)) => ok
				case _ => ko
			}
		}
	}
}

/*

import scala.math.Ordering

val a = List(1,2,3) map (_+2)

def quicksort[T](xs: List[T])(implicit cmp: Ordering[T]): List[T] = {
	if(xs.isEmpty) Nil
    else {
    	val (small, large) = xs.tail.partition(v => cmp.lt(v, xs.head))
        quicksort(small) ++ List(xs.head) ++ quicksort(large)
    }
}

quicksort(List(1,5,2,3,4))

case class Test(a: Int) {
	def f = a + 1
}

Test(1).f
*/