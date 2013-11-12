package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._

import play.api.libs.iteratee._
import play.api.libs.iteratee.Concurrent
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import akka.util.Timeout
import akka.pattern.ask

import model._
import io.codebrew.api._

object Application extends Controller with securesocial.core.SecureSocial {
  implicit val timeout = Timeout(5 seconds)

  def keepalive = WebSocket.using[String] { implicit request =>
    val (enumerator, channel) = Concurrent.broadcast[String]
    val in = Iteratee.foreach[String](content => {
      channel.push("pong")
    }) 
    (in, enumerator)
  }

  def insight = WebSocket.using[JsValue] { implicit request =>
    val (enumerator, channel) = Concurrent.broadcast[JsValue]
    val in = Iteratee.foreach[JsValue](content => {
      val code = (content \ "code").as[String]
      val cid = (content \ "callback_id").as[Int]

      Registry.getEval.map(service => {
        service.insight(code).map(result => {

          val insight = result.insight.map( r => Seq(
            "insight" -> JsString(r.insight),
            "output" -> JsString(r.output)
          )).getOrElse(Seq())

          val errors = 
            if(result.infos.isEmpty) Seq()
            else Seq(
              "CompilationInfo" -> JsArray(result.infos.map(c => 
                JsObject(Seq(
                  "message" ->  JsString(c.message),
                  "pos" ->  JsNumber(c.pos),
                  "severity" -> JsNumber(c.severity.value)))
                )
              )
            )

          val callback = Seq("callback_id" -> JsNumber(cid))

          channel.push(JsObject(insight ++ errors ++ callback))
        })
      })
    }) 
    (in, enumerator)
  }

  def autocomplete = WebSocket.using[JsValue] { implicit request =>
    val (enumerator, channel) = Concurrent.broadcast[JsValue]
    val in = Iteratee.foreach[JsValue](content => {
      val code = (content \ "code").as[String]
      val position = (content \ "position").as[Int]
      val callback = (content \ "callback_id").as[Int]

      Registry.getEval.map(service => {
        service.autocomplete(code, position).map(completions => {
          channel.push(JsObject(Seq(
            "completions" -> JsArray(completions.map(s => JsString(s))),
            "callback_id" -> JsNumber(callback)
          )))
        })
      })
    }) 
    (in, enumerator)
  }

  def userInfo = UserAwareAction { implicit request =>
    val user =  for {
      user <- request.user
      email <- user.email
    } yield Json.obj(
      "name" -> Account.username(email), 
      "gravatar" -> user.avatarUrl
    )

    Ok(user.getOrElse(Json.obj()))
  }
}