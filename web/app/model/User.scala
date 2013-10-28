package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import scala.language.postfixOps

case class Account(username: Pk[String] = NotAssigned, firstname: String, lastname: String, providerId: Long,providerName: String, email: String)

object Account {

	  val simple = {
    get[Pk[String]]("account.username") ~
    get[String]("account.firstname") ~
    get[String]("account.lastname") ~
    get[Long]("account.providerId") ~
    get[String]("account.providerName") ~
    get[String]("account.email") map {
      case username~firstname~lastname~providerId~providerName~email => Account(username, firstname, lastname, providerId, providerName, email)
    }
  }

    def findById(username: String): Option[Account] = {
    DB.withConnection { implicit connection =>
      SQL("select * from account where username = {username}").on('username -> username).as(Account.simple.singleOpt)
    }
  }

    def insert(account: Account) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into account values (
            {username}, {firstname},
            {lastname}, {providerId},
            {providerName}, {email}
          )
        """
      ).on(
        'username -> account.firstname,
        'firstname -> account.firstname,
        'lastname -> account.lastname,
        'providerId -> account.providerId,
        'providerName -> account.providerName,
        'email -> account.email
      ).executeUpdate()
    }
  }

    def delete(username: String) = {
    DB.withConnection { implicit connection =>
      SQL("delete from account where username = {username}").on('username -> username).executeUpdate()
    }
  }
}
