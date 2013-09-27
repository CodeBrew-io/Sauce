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

import ca.polymtl.log4900.eval.Add
import ca.polymtl.log4900.eval.MathResult
import ca.polymtl.log4900.eval.AddResult

object Application extends Controller {
  implicit val timeout = Timeout(5 seconds)

  def index = Action { implicit request =>
    Ok(views.html.index(request))
  }

  def eval = WebSocket.using[JsValue] { implicit request =>
    val (enumerator, channel) = Concurrent.broadcast[JsValue]

    val in = Iteratee.foreach[JsValue](content => {
      val op1 = (content \ "op1").as[Int]
      val op2 = (content \ "op2").as[Int]

      (LookupApplication.actor ? Add(1, 1)).mapTo[MathResult] onComplete {
        case(e) => println("fail")
      }
//      } onSuccess {
//        case AddResult(o1,o2,res) => println(s"$o1 + $o2 = $res")
//      }
    })
    
    (in, enumerator)
  }

}