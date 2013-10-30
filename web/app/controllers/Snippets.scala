package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import model.{Account, Snippet}

object Snippets extends Controller with securesocial.core.SecureSocial {

  def add = UserAwareAction { implicit request =>
    (for {
      user <- request.user
      JsString(code) <- request.body.asJson  
    } yield {
      model.Snippets.add(Snippet("", "", code, "", "", "2.10.3", Account.username(user.lastName, user.firstName)))
      Ok("")
    }).getOrElse(BadRequest(""))
  }

  def queryUser = UserAwareAction { implicit request =>
    Ok(Json.toJson(
      model.Snippets.query(terms = None, userName = request.user.map(u => Account.username(u.lastName, u.firstName))).
        map(_.toJson())
    ))
  }

  def query(terms: Option[String], userName:Option[String], offset: Int) = Action  { implicit request =>
    Ok(Json.toJson(
      model.Snippets.query(terms.filter(_ != ""), userName.filter(_ != ""), offset).
        map(_.toJson())
    ))
  }
}