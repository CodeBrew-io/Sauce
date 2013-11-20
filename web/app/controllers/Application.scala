package controllers

import play.api._
import mvc._
import libs.json._
import libs.iteratee._
import libs.iteratee.Concurrent

import scala.concurrent._
import ExecutionContext.Implicits.global

import duration._
import akka.util.Timeout
import akka.pattern.ask

import model._

import play.api.libs.json.Json._
import play.api.libs.functional.syntax._
import anorm._
import anorm.SqlParser._
import scala.language.postfixOps

object Application extends Controller with securesocial.core.SecureSocial {
	implicit val timeout = Timeout(5 seconds)

	def eval = WebSocket.using[JsValue] { implicit request =>
		val (enumerator, channel) = Concurrent.broadcast[JsValue]
		val in = Iteratee.foreach[JsValue](content => {	
			EvalService(content).map(out => channel.push(out))
		}) 
		(in, enumerator)
	}

	implicit object PkFormat extends Format[Pk[String]] {
        def reads(json: JsValue): JsResult[Pk[String]] = JsSuccess (
            json.asOpt[String].map(id => Id(id)).getOrElse(NotAssigned)
        )
        def writes(username: Pk[String]): JsValue = username.map(JsString(_)).getOrElse(JsNull)
    }

	def userInfo = UserAwareAction { implicit request =>


		val user = request.user.map{ secureSocialUser =>
		 	Account.findProvider(secureSocialUser.identityId.userId, secureSocialUser.identityId.providerId).map{ codeBrewUser =>
				implicit val accountWriter = Json.writes[Account]
				Json.obj("codeBrewUser" -> Json.toJson(codeBrewUser))
				}.getOrElse(Json.obj("secureSocialUser" -> Json.obj(
				"firstname" -> secureSocialUser.firstName,
				"lastname" -> secureSocialUser.lastName,
				"userId" -> secureSocialUser.identityId.userId,
				"providerId" -> secureSocialUser.identityId.providerId,
				"email" -> secureSocialUser.email,
				"gravatar" -> secureSocialUser.avatarUrl)))
			}	

		Ok(user.getOrElse(Json.obj()))
	}


}