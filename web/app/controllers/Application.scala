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
      val callback = (content \ "callback_id").as[Int]

      Registry.getEval.map(service => {
        service.eval(code, 0).map(result => {
          channel.push(JsObject(Seq(
            "response" -> JsString(result.insight),
            "callback_id" -> JsNumber(callback)
          )))
        })
      })
    }) 
    (in, enumerator)
  }
}