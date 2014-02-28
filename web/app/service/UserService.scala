package service

import play.api.{Logger, Application}
import securesocial.core._
import securesocial.core.providers.Token
import securesocial.core.IdentityId


class UserService(application: Application) extends UserServicePlugin(application) {
  private var users = Map[String, Identity]()
  private var tokens = Map[String, Token]()

  def find(id: IdentityId): Option[Identity] = {
    users.get(id.userId + id.providerId)
  }

  def findByEmailAndProvider(email: String, providerId: String): Option[Identity] = {
    users.values.find( u => u.email.map( e => e == email && u.identityId.providerId == providerId).getOrElse(false))
  }

  def save(user: Identity): Identity = {
    users = users + (user.identityId.userId + user.identityId.providerId -> user)
    user
  }

  def save(token: Token) {
    tokens += (token.uuid -> token)
  }

  def findToken(token: String): Option[Token] = {
    tokens.get(token)
  }

  def deleteToken(uuid: String) {
    tokens -= uuid
  }

  def deleteTokens() {
    tokens = Map()
  }

  def deleteExpiredTokens() {
    tokens = tokens.filter(!_._2.isExpired)
  }
}