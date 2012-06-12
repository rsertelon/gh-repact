package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok(views.html.application.index())
  }

  def about = Action {
    Ok(views.html.application.about())
  }
  
  def jsRoutes = Action {
    import routes.javascript._
    Ok(Routes.javascriptRouter("jsRoutes")(Badge.forUser)).as("text/javascript")
  }

}
