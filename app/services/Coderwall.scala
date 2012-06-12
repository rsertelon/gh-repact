package services

import models._

import play.api.mvc._
import play.api.libs.ws._
import play.api.libs.json._
import play.api.libs.concurrent._


object Coderwall {
  
  def badgesOf(username: String): Promise[Option[Seq[Badge]]] = {
    WS.url("http://coderwall.com/" + username.toLowerCase + ".json").get().map { response =>
      response.status match {
        case 200 =>
          (response.json \ "badges") match {
            case JsArray(elements) => {
              val badgesSeq = elements.map{ badge =>
                for {
                  name <- (badge \ "name").asOpt[String];
                  image <- (badge \ "badge").asOpt[String]
                } yield new Badge(name,image)
              }.flatten

              badgesSeq match {
                case Nil => None
                case seq => Some(seq)
              }
            }
            case _ => None
          }
        case _ => None
      }
    }
  }
  
}
