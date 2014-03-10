// package io.codebrew
// package eval

// import org.specs2._

// class Behavior extends Specification { def is = s2"""

// 	Eval specification

// 	Code complete
// 		work when there is something to complete 				$complete
// 		fail gracefully when there is nothing to complete 		$completeFail

// 	Insight
// 		work when the code is valid 							$insightValid
// 		use the presentation compiler when there is errors		
// 			$insightErrors 
// 			$insightErrors2
// 		must timeout after some time 							$insightTimeout
// """

// 	def complete = {
// 		val server = new EvalImpl()
// 		server.autocomplete("\"cat\".subS", 9).get.exists(_.name == "substring")
// 	}

// 	def completeFail = {
// 		val server = new EvalImpl()
// 		server.autocomplete("", 0).get must be empty
// 	}

// 	def insightValid = {
// 		import api.eval._
		
// 		val server = new EvalImpl()
// 		val code = "1+1"
// 		server.insight(code).get must beLike{ 
// 			case Result(Some(_), _, false) => ok
// 			case _ => ko
// 		}
// 	}

// 	def insightErrors = {
// 		import api.eval._
		
// 		val server = new EvalImpl()
// 		val code = "invalid"
// 		server.insight(code).get must beLike{ 
// 			case Result(None, info, false) if !info.isEmpty => ok
// 			case _ => ko
// 		}
// 	}

// 	def insightErrors2 = {
// 		import api.eval._
		
// 		val server = new EvalImpl()
// 		val code = 
// 			"""|e
// 			   |
// 			   |e2""".stripMargin
// 		server.insight(code).get must beLike{ 
// 			case Result(None, info, false) if info.size == 2 => ok
// 			case _ => ko
// 		}	
// 	}

// 	def insightTimeout = {
// 		import api.eval._

// 		val server = new EvalImpl()
// 		val code = "while(true){}"
// 		server.insight(code).get must beLike{ 
// 			case Result(None, Nil, true) => ok
// 			case _ => ko
// 		}	
// 	}
// }