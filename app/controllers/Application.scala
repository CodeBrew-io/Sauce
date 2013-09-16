package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {
  def index = Action {
  	LookupApplication.doSomething(Add(Random.nextInt(100), Random.nextInt(100)))
    Ok(views.html.index("Your new application is ready."))
  }  
}