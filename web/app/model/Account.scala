package model

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import scala.language.postfixOps

case class Account(
  firstName: String, 
  lastName: String, 
  userId: String,
  providerId: String, 
  email: Option[String],
  avatarUrl: Option[String],
  userName: Pk[String] = NotAssigned
)

object Account {
  val simple = {
    get[String]("account.firstName") ~
    get[String]("account.lastName") ~
    get[String]("account.userId") ~
    get[String]("account.providerId") ~
    get[Option[String]]("account.email") ~
    get[Option[String]]("account.avatarUrl") ~
    get[Pk[String]]("account.userName") map {
      case      firstName ~ lastName ~ userId ~ providerId ~ email ~ avatarUrl ~ userName => 
        Account(firstName,  lastName,  userId,  providerId,  email,  avatarUrl,  userName)
    }
  }

  def find(userName: String): Option[Account] = {
    DB.withConnection { implicit connection =>
      SQL("select * from account where userName = {userName} limit 1;").
        on('userName -> userName).
        as(simple.singleOpt)
    }
  }

  def findAll: List[Account] = {
    DB.withConnection { implicit connection =>
      SQL("select * from account;").as(simple *)
    }
  }

  def insert(account: Account) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into account values (
            {userName},
            {firstName},
            {lastName},
            {userId},
            {providerId},
            {email},
            {avatarUrl}
          )
        """
      ).on(
        'userName -> s"${account.lastName}${account.firstName}",
        'firstName -> account.firstName,
        'lastName -> account.lastName,
        'userId -> account.userId,
        'providerId -> account.providerId,
        'email -> account.email,
        'avatarUrl -> account.avatarUrl
      ).executeUpdate()
    }
  }

  def delete(userName: String) = {
    DB.withConnection { implicit connection =>
      SQL("delete from account where userName = {userName}").
        on('userName -> userName).executeUpdate()
    }
  }
}
