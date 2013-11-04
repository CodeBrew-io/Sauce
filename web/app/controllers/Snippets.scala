package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import model.{Account, Snippet}

object Snippets extends Controller with securesocial.core.SecureSocial {

  def add = SecuredAction { implicit request =>
    (for {
      email <- request.user.email
      JsObject(Seq(("code", JsString(code)))) <- request.body.asJson  
    } yield {
      model.Snippets.add(Snippet("", "", code, "", "", "2.10.3", Account.username(email)))
      Ok("")
    }).getOrElse(BadRequest(""))
  }

  def queryUser = SecuredAction { implicit request =>
    val email = request.user.email.map(e => Account.username(e))
    Ok(Json.toJson(
      model.Snippets.query(terms = None, userName = email).map(_.toJson())
    ))
  }

  def query(terms: Option[String], userName:Option[String], offset: Option[Int]) = Action  { implicit request =>
    Ok(Json.toJson(
      model.Snippets.query(terms.filter(_ != ""), userName.filter(_ != ""), offset).
        map(_.toJson())
    ))
  }
}