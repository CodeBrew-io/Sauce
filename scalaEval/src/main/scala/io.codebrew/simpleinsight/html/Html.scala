package io.codebrew.simpleinsight
package html

import Instrument._
import scala.util.parsing.json._

object Html {
	def show[T](a : T)(implicit ev : Html[T]): Result = ev.show(a)
}

object Generic extends Generic
object Plot extends Generic {
	private def jsonData[X, Y](as: List[(X, Y)]) = 
		Json(JSONObject(Map(
			"data" -> JSONArray(as.map{ case (x_, y_) => JSONArray(scala.List(x_, y_)) })
		)))
	implicit def ListTuple2[X, Y] = new Html[List[(X, Y)]] {
		def show(as: List[(X, Y)]) = jsonData(as)
	}
}

trait Generic {
	implicit def generic[T] = new Html[T] { 
		def show(a: T) = Code(a.toString)
	}
}