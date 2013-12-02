package test

import org.specs2._

import play.api._
import play.api.libs.json._

import model._

class SnippetsSpec extends Specification { def is = s2"""
	After user insert snippet he should be able to query it $insertThenQuery

    """

    def insertThenQuery = {

    	val user = "testUser"
    	val code = """|/** Maps are easy to use in Scala. */
					  |object Maps {
					  |  val colors = Map("red" -> 0xFF0000,
					  |                   "turquoise" -> 0x00FFFF,
					  |                   "black" -> 0x000000,
					  |                   "orange" -> 0xFF8040,
					  |                   "brown" -> 0x804000)
					  |  def main(args: Array[String]) {
					  |    for (name <- args) println(
					  |      colors.get(name) match {
					  |        case Some(code) =>
					  |          name + " has code: " + code
					  |        case None =>
					  |          "Unknown color: " + name
					  |      }
					  |    )
					  |  }
					  |}""".stripMargin

	    val initialSnippetCount = Snippets.query(terms = None, userName = Some(user)).size
		val id = Snippets.add(Snippet("", "", "", code, "", "", "2.10.3", user))
		println(id)
	    val finalSnippetCount = Snippets.query(terms = None, userName = Some(user)).size
	    (initialSnippetCount + 1) ==== finalSnippetCount

	}
}