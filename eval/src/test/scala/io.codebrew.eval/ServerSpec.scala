package io.codebrew
package eval

import org.specs2.mutable._

class ServerSpec extends Specification {

	"Eval" should {
		"Code complete" in {
			"work when there is something to complete" in {
				val server = new EvalImpl()
				val code = """object wtv {
							 |val aabb = "cat"
							 |aabb.subS
							 |}""".stripMargin
				server.autocomplete(code, 32).get must contain("substring")
			}.pendingUntilFixed("Update test")
			"work on the partial member name" in {
				val server = new EvalImpl()
				val code = """object wtv {
							 |val aabb = "cat"
							 |aabb.subS
							 |}""".stripMargin
				server.autocomplete(code, 37).get must contain("substring")
			}.pendingUntilFixed("Update test")
			"work even when there is no wrapping object or class" in {
				val server = new EvalImpl()
				val code = """val aabb = "cat"
							 |aabb.subS""".stripMargin
				server.autocomplete(code, 19).get must contain("substring")
			}.pendingUntilFixed("Update test")
			"fail gracefully when there is nothing to complete" in {
				val server = new EvalImpl()
				val code = ""
				server.autocomplete(code, 0).get must be empty
			}.pendingUntilFixed("Update test")
		}
	}
}