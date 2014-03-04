package io.codebrew
package eval

import org.specs2._

class Representation extends Specification { def is = s2"""
	
	Html Representation
		type class should kick in when using
			only any			$any
			only Plot			$plot
			both any and Plot 	$both
		
"""
	def any = {
		// val server = new EvalImpl()
		// server.insight()
		pending
	}

	def plot = {
		// val server = new EvalImpl()
		// server.insight()
		pending
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
}