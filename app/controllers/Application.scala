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
    Ok(views.html.index(request))
  }

  def eval = WebSocket.using[JsValue] { implicit request =>
    val (enumerator, channel) = Concurrent.broadcast[JsValue]

    val in = Iteratee.foreach[JsValue](content => {
      val firstName = (content \ "firstName").as[String]
      val lastName = (content \ "lastName").as[String]

      EvalService.client.hello(User(firstName, lastName)).map(r => {
        println(r)
        channel.push(JsObject(Seq("response" -> JsString(r))))
      })
    }) 
    (in, enumerator)
  }
}