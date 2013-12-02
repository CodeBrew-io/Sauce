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

import securesocial.core._

object Users extends Controller with securesocial.core.SecureSocial {

	def info = UserAwareAction { implicit request =>
		import Account.PkFormat
		import Account.writer
		val user = request.user.map{ secureSocialUser =>
			val securesocialJson = Json.obj(
				"secureSocialUser" -> Json.obj(
					"fullName" -> secureSocialUser.fullName,
					"gravatar" -> secureSocialUser.avatarUrl
				)
			)
		 	Account.find(secureSocialUser).map{ codeBrewUser =>
				Json.obj("codeBrewUser" -> Json.toJson(codeBrewUser))
			}.getOrElse(securesocialJson)
		}.getOrElse(Json.obj())

		Ok(user)
	}

	def exists(username: String) = Action {
		Ok(Json.obj("result" -> Account.exists(username)))
	}

	def signIn = SecuredAction { implicit request =>
		val user = Account.find(request.user)

		if(user.isEmpty) Ok(views.html.signIn(request.user))
		else Ok(views.html.close())
	}

	def add = SecuredAction { implicit request =>
		import Account.signUpReader
		(for {
			json <- request.body.asJson
			signin <- json.asOpt[SignIn]
		} yield {
			val user = request.user
			val newUser = Account(
				user.fullName,
				user.identityId.userId,
				user.identityId.providerId,
				signin.email,
				user.avatarUrl,
				Id(signin.userName)
			)
			Account.insert(newUser)
			Ok(Json.obj("codeBrewUser" -> Json.toJson(newUser)))
		}).getOrElse(BadRequest(""))
	}
}