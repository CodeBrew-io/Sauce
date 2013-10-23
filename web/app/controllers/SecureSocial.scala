package controllers

import securesocial.controllers.TemplatesPlugin

import play.api.mvc.{RequestHeader, Request}
import play.api.templates.{Html, Txt}
import play.api.Application
import play.api.data.Form
import securesocial.core.{Identity, SecuredRequest}
import securesocial.controllers.Registration.RegistrationInfo
import securesocial.controllers.PasswordChange.ChangeInfo

class SecureSocial(application: Application) extends TemplatesPlugin {
  def getLoginPage[A](
  	implicit request: Request[A], 
  	form: Form[(String, String)], 
  	msg: Option[String] = None): Html = {

    views.html.login(form, msg)
  }

  def getSignUpPage[A](implicit request: Request[A], form: Form[RegistrationInfo], token: String): Html = ???
  def getStartSignUpPage[A](implicit request: Request[A], form: Form[String]): Html = ???
  def getStartResetPasswordPage[A](implicit request: Request[A], form: Form[String]): Html = ???
  def getResetPasswordPage[A](implicit request: Request[A], form: Form[(String, String)], token: String): Html = ???
  def getPasswordChangePage[A](implicit request: SecuredRequest[A], form: Form[ChangeInfo]): Html = ???
  def getNotAuthorizedPage[A](implicit request: Request[A]): Html = ???
  def getSignUpEmail(token: String)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = (None, None)
  def getAlreadyRegisteredEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = (None, None)
  def getWelcomeEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = (None, None)
  def getUnknownEmailNotice()(implicit request: RequestHeader): (Option[Txt], Option[Html]) = (None, None)
  def getSendPasswordResetEmail(user: Identity, token: String)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = (None, None)
  def getPasswordChangedNoticeEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = (None, None)
}