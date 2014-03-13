package test

import scala.concurrent._
import scala.concurrent.duration._

import play.api.test._
import play.api.test.Helpers._

import model._
import org.specs2.time.NoTimeConversions

import org.specs2._

object Config {
	val timeout = 10.seconds
}

class SnippetsSpec extends mutable.Specification with NoTimeConversions { 
	
	"After user insert snippet" in {

		"any user should be able to query it" in {
			"with a username" in new snippet {
				// test(None, Some(username), snippet)
				pending
			}
			"with code" in new snippet {
				// test(Some("reverse"), None, snippet)
				pending
			}
			"both" in new snippet {
				// test(Some("reverse"), Some(username), snippet)
				pending
			}
		}
	}

	import ExecutionContext.Implicits.global
	def test(terms: Option[String], userName: Option[String], snippet: Snippet) = {
		TestSnippets.query(terms, userName).map{ snippets =>
			snippets must contain(snippet)
		}
	}.await(timeout = Config.timeout)
}

trait snippet extends mutable.BeforeAfter{
	var id: String = _
	def before = {
		// id = Await.result(
		// 	TestSnippets.add(snippet),
		// 	Config.timeout
		// )
		// println(s"""
		// before
		// $id
		// ${Await.result(TestSnippets.query(None, None), Config.timeout)}
		// ${Await.result(TestSnippets.size, Config.timeout)}
		// """)
		// ${Await.result(TestSnippets.find((id, username)), Config.timeout)}
	}

	def after = {
		// Await.result(
		// 	TestSnippets.remove((id, username)), 
		// 	Config.timeout
		// )
		// println(s"""
		// after
		// $id
		// ${Await.result(TestSnippets.query(None, None), Config.timeout)}
		// ${Await.result(TestSnippets.size, Config.timeout)}
		// """)
		// ${Await.result(TestSnippets.find((id, username)), Config.timeout)}
	}

	val scalaVersion = "2.11.0"
	val username = "_bob_"
	val code = "List(1,2,3).reverse"
// 	val code =	"""
// import scala.math.Ordering

// val a = List(1,2,3) map (_+2)

// def quicksort[T](xs: List[T])(implicit cmp: Ordering[T]): List[T] = {
// 	if(xs.isEmpty) Nil
//     else {
//     	val (small, large) = xs.tail.partition(v => cmp.lt(v, xs.head))
//         quicksort(small) ++ List(xs.head) ++ quicksort(large)
//     }
// }

// quicksort(List(1,5,2,3,4))

// case class Test(a: Int) {
// 	def f = a + 1
// }

// Test(1).f
// """
	val snippet = Snippet(code, username, scalaVersion)
}