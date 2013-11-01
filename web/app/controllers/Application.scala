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

    val account = for {
      user <- request.user
      email <- user.email
    } yield {
      Account.find(Account.username(email)) match {
        case Some(account) => account
        case None => {
          val account = Account(
            firstName = user.firstName,
            lastName = user.lastName,
            userId = user.identityId.userId,
            providerId = user.identityId.providerId,
            email = Some(Account.username(email)),
            avatarUrl = user.avatarUrl
          )
          Account.insert(account)
          account
        }
      }
    }

    Ok(views.html.index(account))
  }

  def eval = WebSocket.using[JsValue] { implicit request =>
    val (enumerator, channel) = Concurrent.broadcast[JsValue]
    val in = Iteratee.foreach[JsValue](content => {
      val code = (content \ "code").as[String]
      val position = (content \ "position").as[Int]
      val callback = (content \ "callback_id").as[Int]

      Registry.getEval.map(service => {

        service.eval(code, position).map(result => {

          val jsonValue = JsObject(Seq(
            "insight" -> JsString(result.insight),
            "output" -> JsString(result.output),
            "CompilationInfo" -> JsArray(result.infos.map(c => 
              JsObject(Seq(
                "message" ->  JsString(c.message),
                "pos" ->  JsNumber(c.pos),
                "severity" -> JsNumber(c.severity.value)))
              )
            ),
            "completions" -> JsArray(result.completions.map(c => JsString(c))),
            "callback_id" -> JsNumber(callback)
          ))

          

          channel.push(jsonValue)
        })
      })
    }) 
    (in, enumerator)
  }
}