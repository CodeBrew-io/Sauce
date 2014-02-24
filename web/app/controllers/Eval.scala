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

object Eval extends Controller {
	implicit val timeout = Timeout(5 seconds)

	def eval = WebSocket.using[JsValue] { implicit request =>
		val (enumerator, channel) = Concurrent.broadcast[JsValue]
		val in = Iteratee.foreach[JsValue](content => {	
			channel.push(EvalService(content))
		}) 
		(in, enumerator)
	}
}