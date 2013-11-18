package io.codebrew
package eval

import org.specs2._

class ServerSpec extends Specification { def is = s2"""

	Eval specification

	Code complete
		work when there is something to complete 				$c1
		fail gracefully when there is nothing to complete 		$c2

	Insight
		work when the code is valid 							$i1
		use the presentation compiler when there is errors		$i2
"""

	def c1 = {
		val server = new EvalImpl()
		server.autocomplete("\"cat\".subS", 9).get.exists(_.name == "substring")
	}

	def c2 = {
		val server = new EvalImpl()
		server.autocomplete("", 0).get must be empty
	}

	def i1 = {
		import api.eval._
		
		val server = new EvalImpl()
		val code = "1+1"
		server.insight(code).get must beLike{ 
			case Result(Some(_), _) => ok
			case _ => ko
		}
	}

	def i2 = {
		import api.eval._
		
		val server = new EvalImpl()
		val code = "invalid"
		server.insight(code).get must beLike{ 
			case Result(None, info) if !info.isEmpty => ok
			case _ => ko
		}
	}
}