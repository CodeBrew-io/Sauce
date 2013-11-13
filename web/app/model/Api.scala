package model

import play.api.libs.json._
import play.api.libs.functional.syntax._

import com.twitter.util.Future

import io.codebrew.api._

object EvalService {
	def apply(json: JsValue): Future[JsValue] = {
		Api.all(json, ping _, insight _, autocomplete _, fallback _)
	}

	private val callback = "callback_id"
	private def unavailable(serviceName: String, cid: Int): Future[JsValue] = Future(
		JsObject(Seq(
			"insight" -> JsString(s"$serviceName service unavailable"),
			callback -> JsNumber(cid)
		))
	)
	private def ping(cid: Int): Future[JsValue] = Future {
		JsObject(Seq(callback -> JsNumber(cid)))
	}
	private def insight(code: String, cid: Int): Future[JsValue] = {
		Registry.getEval.map(service => {
			service.insight(code).map(result => {

				val insightResult = result.insight.map( r => Seq(
					"insight" -> JsString(r.insight),
					"output" -> JsString(r.output)
				)).getOrElse(Seq())

				val compilationInfo = 
					if(result.infos.isEmpty) Seq()
					else Seq(
						"compilationInfo" -> JsArray(result.infos.map(c => Json.obj(
							"message" ->  JsString(c.message),
							"pos" ->  JsNumber(c.pos),
							"severity" -> JsNumber(c.severity.value)
						)))
					)
				JsObject(insightResult ++ compilationInfo ++ Seq(callback -> JsNumber(cid)))
			})
		}).getOrElse(unavailable("insight", cid))
	}
	private def autocomplete(code: String, position: Int, cid: Int): Future[JsValue] = {
		Registry.getEval.map(service => {
			service.autocomplete(code, position).map(completions => {
				JsObject(Seq(
					"completions" -> JsArray(completions.map(c => Json.obj(
            			"name" -> c.name,
            			"signature" -> c.signature
          			))),
					callback -> JsNumber(cid)
				))
			})
		}).getOrElse(unavailable("autocomplete", cid))
	}
	private def fallback(): Future[JsValue] = Future {
		JsObject(Seq(
			"error" -> JsString("invalid request")
		))
	}
}

object Api {
	val callback = (__ \ "callback_id").read[Int]
	val ping: Reads[Int] = (__ \ "ping").read(
		callback
	)

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
		pingF: (Int) => T, 
		insightF: (String,Int) => T,
		autocompleteF: (String, Int, Int) => T,
		fallback: () => T 
	): T = {

		ping.reads(json) match {
			case JsSuccess(cid,_) => pingF(cid)
			case _:JsError => insight.reads(json) match {
				case JsSuccess((code, cid),_) => insightF(code, cid)
				case _:JsError => autocomplete.reads(json) match {
					case JsSuccess((code, pos, cid),_) => autocompleteF(code, pos, cid)
					case _:JsError => fallback()
				}
			}
		}
	}
}