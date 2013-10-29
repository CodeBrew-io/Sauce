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
import ca.polymtl.log4900.api._

object Application extends Controller with securesocial.core.SecureSocial {
  implicit val timeout = Timeout(5 seconds)

  def index = UserAwareAction { implicit request =>
    Ok(views.html.index(request.user))
  }

  def eval = WebSocket.using[JsValue] { implicit request =>
    val (enumerator, channel) = Concurrent.broadcast[JsValue]
    val in = Iteratee.foreach[JsValue](content => {
      val code = (content \ "code").as[String]
      val callback = (content \ "callback_id").as[Int]

      Registry.getEval.map(service => {
        service.eval(code).map(result => {
          channel.push(JsObject(Seq(
            "response" -> JsString(result),
            "callback_id" -> JsNumber(callback)
          )))
        })
      })
    }) 
    (in, enumerator)
  }
}