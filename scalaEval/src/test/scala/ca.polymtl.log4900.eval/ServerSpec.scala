package ca.polymtl.log4900
package eval

import org.specs2.mutable._

class ServerSpec extends Specification {

	"codeComplete" should {
		"work when there is something to complete" in {
			val server = new InsightImpl()
			val code = """object wtv {
						 |val aabb = "cat"
						 |aabb.subS
						 |}""".stripMargin
			val result = server.codeComplete(code, 32).get
			result must contain("substring")
		}
		"work even when there is no wrapping object or class" in {
			val server = new InsightImpl()
			val code = """val aabb = "cat"
						 |aabb.subS""".stripMargin
			val result = server.codeComplete(code, 19).get
			result must contain("substring")
		}
		"fail gracefully when there is nothing to complete" in {
			val server = new InsightImpl()
			val code = ""
			val result = server.codeComplete(code, 1).get
			result must beEmpty
		}
	}
}