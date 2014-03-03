package io.codebrew.simpleinsight

object Html extends {
	import Instrument._
	import scala.util.parsing.json._
	def show[T](a : T)(implicit ev : Html[T]) = ev.show(a)

	trait Generic {
		import Instrument._
		implicit def generic[T] = new Html[T] { 
			def show(a: T) = Code(a.toString)
		}
	}
	object Generic extends Generic

	private def jsonData[X, Y](as: List[(X, Y)], `type`: String) = 
		Json(JSONObject(Map(
			"data" -> JSONArray(as.map{ case (x_, y_) => JSONArray(scala.List(x_, y_)) }),
			"type" -> `type`
		)))

	object Plot extends Generic {
		implicit def ListTuple2[X, Y] = new Html[List[(X, Y)]] {
			def show(as: List[(X, Y)]) = jsonData(as, "plot")
		}
	}
	object Table extends Generic {
		implicit def ListTuple2[X, Y] = new Html[List[(X, Y)]] { 
			def show(as: List[(X, Y)]) = jsonData(as, "table")
		}
	}
	object HtmlList extends Generic {
		implicit def ListTuple2[X, Y] = new Html[List[(X, Y)]] { 
			def show(as: List[(X, Y)]) = jsonData(as, "list")
		}
	}
}