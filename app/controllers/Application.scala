package controllers

import play.api._
import play.api.mvc._

import model._
import ca.polymtl.log4900.eval._

object Application extends Controller {
  def index = Action {
  	LookupApplication.doSomething(Add(100, 100))
    Ok(views.html.index("Your new application is ready."))
  }  
}