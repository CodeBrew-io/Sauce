package controllers


import model._

import play.api._
import mvc._
import libs.json._

import play.api.libs.json.Json._
import play.api.libs.functional.syntax._
import anorm._
import anorm.SqlParser._
import scala.language.postfixOps

object Users extends Controller with securesocial.core.SecureSocial {

	def info = UserAwareAction { implicit request =>
		import Account.PkFormat
		import Account.writer

		val user = request.user.map{ secureSocialUser =>
			val id = secureSocialUser.identityId
			val securesocialJson = Json.obj(
				"secureSocialUser" -> Json.obj(
					"firstname" -> secureSocialUser.firstName,
					"lastname" -> secureSocialUser.lastName,
					"email" -> secureSocialUser.email,
					"gravatar" -> secureSocialUser.avatarUrl
				)
			)
		 	Account.findProvider(id.userId, id.providerId).map{ codeBrewUser =>
				Json.obj("codeBrewUser" -> Json.toJson(codeBrewUser))
			}.getOrElse(securesocialJson)
		}	

		Ok(user.getOrElse(Json.obj()))
	}

	def exists = Action { implicit request =>
	    (for {
	      JsObject(Seq(("username", JsString(username)))) <- request.body.asJson
	    } yield {
	           Ok(Json.obj("result" -> Account.exists(username)))
	    }).getOrElse(BadRequest(""))
	}

	def add = SecuredAction { implicit request =>
		import Account.signUpReader
		(for {
			json <- request.body.asJson
			signin <- json.asOpt[SignIn]
		} yield {
			val user = request.user
			Account.insert(Account(
				user.firstName,
				user.lastName,
				user.identityId.userId,
				user.identityId.providerId,
				signin.email,
				user.avatarUrl,
				Id(signin.userName)
			))
			Ok("")
		}).getOrElse(BadRequest(""))
	}
}