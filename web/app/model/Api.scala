package model

import play.api.libs.json._
import play.api.libs.functional.syntax._

import com.twitter.util.Future

import io.codebrew.api.eval._

object EvalService {
	import Api._

	def apply(json: JsValue): Future[JsValue] = {
		all(json, insight _, autocomplete _, fallback _)
	}

	private def insight(code: String, cid: Int): Future[JsValue] = {
		Registry.getEval.map(service => {
			service.insight(code).map(i => insightResult(i, cid))
		}).getOrElse(unavailable("insight", cid))
	}
	private def autocomplete(code: String, position: Int, cid: Int): Future[JsValue] = {
		Registry.getEval.map(service => {
			service.autocomplete(code, position).map(a => autocompleteResult(a, cid))
		}).getOrElse(unavailable("completions", cid))
	}
	private def fallback(): Future[JsValue] = Future {
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
		val (insight, output) = r.insight.map(i => (i.insight, i.output)).getOrElse(("",""))
		val groupedInfos = r.infos.groupBy(_.severity).map{ case (t, sevs) =>
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
	def unavailable(serviceName: String, cid: Int): Future[JsValue] = Future(
		JsObject(Seq(
			serviceName -> JsString(s"$serviceName service unavailable"),
			callback_id -> JsNumber(cid)
		))
	)
}