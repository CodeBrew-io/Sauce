package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import model.{Account, Snippet}

object Snippets extends Controller with securesocial.core.SecureSocial {

  def add = UserAwareAction { implicit request =>
    (for {
      su <- request.user
      user <- Account.find(su)
      username <- user.userName
      JsObject(Seq(("code", JsString(code)))) <- request.body.asJson  
    } yield {
      val id = model.Snippets.add(Snippet("", "", "", code, "", "", "2.10.3", username))
      Ok(Json.obj("id" -> id))
    }).getOrElse(BadRequest(""))
  }

  def queryUser = UserAwareAction { implicit request =>
    (for {
      su <- request.user
      user <- Account.find(su)
      username <- user.userName
    } yield {
      Ok(Json.toJson(
        model.Snippets.query(terms = None, userName = Some(username)).map(_.toJson())
      ))
    }).getOrElse(BadRequest(""))
  }

  def delete(id: String) = UserAwareAction { implicit request =>
    (for {
      su <- request.user
      user <- Account.find(su)
      username <- user.userName
    } yield {
      if(model.Snippets.delete(id, username)) Ok("")
      else BadRequest("")
    }).getOrElse(BadRequest(""))
  }

  def query(terms: Option[String], userName:Option[String], offset: Option[Int]) = Action  { implicit request =>
    Ok(Json.toJson(
      model.Snippets.query(terms.filter(_ != ""), userName.filter(_ != ""), offset).
        map(_.toJson())
    ))
  }

  def queryDistinct(terms: Option[String], userName:Option[String], offset: Option[Int]) = Action  { implicit request =>
    Ok(Json.toJson(
      model.Snippets.queryDistinct(terms.filter(_ != ""), userName.filter(_ != ""), offset).
        map(_.toJson())
    ))
  }
}