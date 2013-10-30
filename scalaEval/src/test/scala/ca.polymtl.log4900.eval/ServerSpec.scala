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
			server.compileAndCompletion(code, 32) must beLike { case (compilationInfos, completions) =>
				println(compilationInfos)
				compilationInfos must not be empty
				completions must contain("substring")
			}
		}
		"work on the partial member name" in {
			val server = new InsightImpl()
			val code = """object wtv {
						 |val aabb = "cat"
						 |aabb.subS
						 |}""".stripMargin
			server.compileAndCompletion(code, 37) must beLike { case (compilationInfos, completions) =>
				println(compilationInfos)
				compilationInfos must not be empty
				completions must contain("substring")
			}
		}
		"work even when there is no wrapping object or class" in {
			val server = new InsightImpl()
			val code = """val aabb = "cat"
						 |aabb.subS""".stripMargin
			server.compileAndCompletion(code, 19) must beLike { case (compilationInfos, completions) =>
				compilationInfos must not be empty
				completions must contain("substring")
			}
		}
		"fail gracefully when there is nothing to complete" in {
			val server = new InsightImpl()
			val code = ""
			server.compileAndCompletion(code, 1) must beLike { case (compilationInfos, completions) =>
				compilationInfos must be empty;
				completions must be empty
			}
		}
	}
}