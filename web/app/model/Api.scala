package model

import play.api.libs.json._
import play.api.libs.functional.syntax._

import io.codebrew.api.eval._

//import scala.collection.JavaConversions
import scala.collection.JavaConverters._

object EvalService {
	import Api._

	def apply(json: JsValue): JsValue = {
		all(json, insight _, autocomplete _, fallback _)
	}

	private def insight(code: String, cid: Int): JsValue = {
		insightResult(EvalClient.get.insight(code), cid)
	}
	private def autocomplete(code: String, position: Int, cid: Int): JsValue = {
		autocompleteResult(EvalClient.get.autocomplete(code, position).asScala, cid)
	}
	private def fallback(): JsValue = {
		JsObject(Seq("error" -> JsString("invalid request")))
	}
}

object Api {
	val callback = (__ \ "callback_id").read[Int]
	
	val autocomplete: Reads[(String,Int,Int)] =
		(__ \ "autocomplete").read(
			(__ \ "code").read[String] and
			(__ \ "position").read[Int] and
			callback
			tupled
		)
	val insight: Reads[(String,Int)] =
		(__ \ "insight").read(
			(__ \ "code").read[String] and
			callback
			tupled
		)

	def all[T](
		json: JsValue,
		insightF: (String,Int) => T,
		autocompleteF: (String, Int, Int) => T,
		fallback: () => T 
	): T = {

		insight.reads(json) match {
			case JsSuccess((code, cid),_) => insightF(code, cid)
			case _:JsError => autocomplete.reads(json) match {
				case JsSuccess((code, pos, cid),_) => autocompleteF(code, pos, cid)
				case _:JsError => fallback()
			}
		}
	}

	private val callback_id = "callback_id"
	def insightResult(r: Result, cid: Int): JsValue = {
		val (insight, output) = Some(r.insight).map(i => (i.insight, i.output)).getOrElse(("",""))
		val groupedInfos = r.infos.asScala.groupBy(_.severity).map{ case (t, sevs) =>
			s"${t.toString.toLowerCase}s" -> JsArray(sevs.map(s => JsObject(Seq(
				"message" -> JsString(s.message),
				"position" -> JsNumber(s.pos)
			))))
		}.to[Seq]

		val infos = 
			if(groupedInfos.isEmpty) Seq("errors" -> JsArray(), "warnings" -> JsArray(), "infos" -> JsArray())
			else groupedInfos

		JsObject(
			infos ++
			Seq(
				"insight" -> JsString(insight),
				"output" -> JsString(output),
				"timeout" -> JsBoolean(r.timeout),
				callback_id -> JsNumber(cid)
			)
		)
	}
	def autocompleteResult(completions: Seq[Completion], cid: Int): JsValue = {
		JsObject(Seq(
			"completions" -> JsArray(completions.map(c => JsObject(Seq(
				"name" -> JsString(c.name),
				"signature" -> JsString(c.signature)
			)))),
			callback_id -> JsNumber(cid)
		))
	}
	def unavailable(serviceName: String, cid: Int): JsValue = {
		JsObject(Seq(
			serviceName -> JsString(s"$serviceName service unavailable"),
			callback_id -> JsNumber(cid)
		))
	}
}