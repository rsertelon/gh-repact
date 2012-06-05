package services

import models._

import play.api.mvc._
import play.api.libs.ws._
import play.api.libs.json._
import play.api.libs.concurrent._


object Coderwall {

	def badgesOf(username: String): Promise[Option[Seq[Badge]]] = {
		WS.url("http://coderwall.com/" + username.toLowerCase + ".json").get().map { response =>
			if(response.status == 200) {
				(response.json \ "badges") match {
					case JsArray(elements) => {
						val badgesSeq = elements.map{ badge =>
							(
								(badge \ "name").asOpt[String],
								(badge \ "badge").asOpt[String]
							)
						}.filter{
							case (name,badge) => name.isDefined && badge.isDefined
						}.map {
							case (name,badge) => new Badge(name.get,badge.get)
						}
						if(badgesSeq.isEmpty) None else Some(badgesSeq)
					}
					case _ => None
				}
			} else {
				None
			}
		}
	}
	
}
