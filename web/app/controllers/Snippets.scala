package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import model.{Account, Snippet}

import securesocial.core._

import scala.concurrent.Future

object Snippets extends Controller with securesocial.core.SecureSocial {

  import play.api.libs.concurrent.Execution.Implicits._

  def add = UserAwareAction { implicit request => withUsername { username =>
    val scalaVersion = scala.util.Properties.versionString

    request.body.asJson match {
      case Some(JsObject(Seq(("code", JsString(code))))) => {
        model.Snippets.add(Snippet(code, username, scalaVersion)) map { id =>
          Ok(Json.obj("id" -> id))
        }
      }
      case _ => Future { BadRequest(s"expected: {code: '1+1'}, received: ${request.body}") }
    }
  }}

  def queryUser = UserAwareAction { implicit request => withUsername { username =>
    for {
      response <- model.Snippets.query(terms = None, userName = Some(username))
    } yield Ok(Json.toJson(response.map(_.toJson)))
  }}

  def delete(id: String) = UserAwareAction { implicit request => withUsername { username =>
    model.Snippets.remove(id, username).map{ snippet =>
      if(snippet.isFound) Ok(Json.obj("id" -> id))
      else NotFound(s"snippet not found $id")
    }
  }}

  def find(id: String, username: String) = Action.async { implicit request =>
    model.Snippets.find(id, username).map{
      case Some(snippet) => Ok(Json.toJson(snippet.toJson))
      case None => NotFound(s"snippet not found $id $username")
    }
  }

  def query(terms: Option[String], userName:Option[String], offset: Option[Int]) = Action.async { implicit request =>
    for {
      response <- model.Snippets.query(terms.filter(_ != ""), userName.filter(_ != ""), offset)
    } yield Ok(Json.toJson(response.map(_.toJson())))
  }

  private def withUsername[T](f: (String) => Future[Result])(implicit request: RequestWithUser[T]) = {
    (for {
      su <- request.user
      user <- Account.find(su)
      username <- user.userName
    } yield {
      Async { f(username) }
    }).getOrElse(BadRequest(""))
  }
}