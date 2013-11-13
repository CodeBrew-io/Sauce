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

object Application extends Controller with securesocial.core.SecureSocial {
	implicit val timeout = Timeout(5 seconds)

	def eval = WebSocket.using[JsValue] { implicit request =>
		val (enumerator, channel) = Concurrent.broadcast[JsValue]
		val in = Iteratee.foreach[JsValue](content => {	
			EvalService(content).map(out => channel.push(out))
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