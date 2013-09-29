package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._

import play.api.libs.iteratee._
import play.api.libs.iteratee.Concurrent

import scala.concurrent._
import ExecutionContext.Implicits.global

import scala.concurrent.duration._
import akka.util.Timeout
import akka.pattern.ask

import model._
import ca.polymtl.log4900.api._

object Application extends Controller {
  implicit val timeout = Timeout(5 seconds)

  def index = Action { implicit request =>
    Ok(views.html.index())
  }

  def eval = WebSocket.using[JsValue] { implicit request =>
    val (enumerator, channel) = Concurrent.broadcast[JsValue]
    val in = Iteratee.foreach[JsValue](content => {
      val code = (content \ "code").as[String]
      val callback = (content \ "callback_id").as[Int]
      
      EvalService.client.eval(code).map(r => {
        channel.push(JsObject(Seq(
          "response" -> JsArray(r.map(s => JsString(s))),
          "callback_id" -> JsNumber(callback)
        )))
      })
    }) 
    (in, enumerator)
  }
}