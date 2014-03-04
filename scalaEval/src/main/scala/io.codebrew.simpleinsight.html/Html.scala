package io.codebrew.simpleinsight
package html

import Instrument._
import scala.util.parsing.json._

object Html {
	def show[T](a : T)(implicit ev : Html[T]): Result = ev.show(a)

	private def jsonData[X, Y](as: List[(X, Y)]) = 
		Json(JSONObject(Map(
			"data" -> JSONArray(as.map{ case (x_, y_) => JSONArray(scala.List(x_, y_)) })
		)))

	object Plot {
		implicit def ListTuple2[X, Y] = new Html[List[(X, Y)]] {
			def show(as: List[(X, Y)]) = jsonData(as)
		}
	}
}