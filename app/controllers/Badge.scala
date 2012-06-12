package controllers

import play.api._
import play.api.mvc._
import play.api.libs.ws._
import play.api.libs.json._
import play.api.libs.concurrent._

import models._
import services._

object Badge extends Controller {

  def forUser(username: String) = Action {
    Async {
      Coderwall.badgesOf(username).map { badges =>
        Ok(views.html.badge.index(badges))
      }
    }
  }

}
