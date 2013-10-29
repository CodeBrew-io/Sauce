package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._

import play.api.data._
import play.api.data.Forms._

import model._
import model.SnippetsService._


/**
 * Created with IntelliJ IDEA.
 * User: shmed
 * Date: 10/15/13
 * Time: 6:09 PM
 * To change this template use File | Settings | File Templates.
 */
object Snippets extends Controller {

  val snippetForm = Form(
    tuple(
      "title"       -> text,
      "description" -> text,
      "codeOrigin"  -> text,
      "codeParsed"  -> text,
      "tags"        -> text,
      "scalaVer"    -> text,
      "user"        -> text
    )
  )

  def page = Action { implicit request =>
    Ok(views.html.elasticsearchtest(Array()))
  }

  def add = Action { implicit request =>

    val (title, description, codeOrigin, codeParsed, tags, scalaVar, user) = snippetForm.bindFromRequest.get

    val snippet = Snippet(title, description, codeOrigin, codeParsed, tags, scalaVar, user )

    // val returnJson = SnippetsService.addSnippet(snippet)

    Ok(views.html.elasticsearchtest(Array()))
  }

  def search(q: Option[String], u: Option[String]) = Action  { implicit request =>

    val response = SnippetsService.search(q.filter(_ != ""), u.filter(_ != ""))

    val jsonArrayResponse = response.map(snippet => snippet.toJson())

    Ok(Json.toJson(jsonArrayResponse))
    /*Ok(
      Json.toJson(JsObject (
        "snippets" -> JsArray(
        response.map {
        s => s.toJson()
      })
      )
    )
    )*/

  }

}
