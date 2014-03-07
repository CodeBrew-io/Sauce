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
		val code = """|import Plot._
					  |val a = "a"
					  |a
					  |val b = 1
					  |b
					  |val c = List((1,1), (2,2))
					  |c""".stripMargin
		val server = new EvalImpl()
		val result = server.insight(code)
		Option(result.runtimeError) ==== None
	}

	object Lift {
		import simpleinsight.Instrument._
		import simpleinsight.html._
		import Html._
		import Generic._

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