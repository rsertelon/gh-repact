package services

import models._

import play.api.mvc._
import play.api.libs.ws._
import play.api.libs.json._

object Github {
	def search(q: String)(result: (String, Option[Seq[Repo]]) => Result) = {
		WS.url("http://github.com/api/v2/json/repos/search/" + q).get().map{ response =>
			val repos = (Json.parse(response.body) \ "repositories") match {
				case JsArray(elements) => {
					val repos = elements.map { repo =>
						((repo \ "username").asOpt[String], (repo \ "name").asOpt[String], (repo \ "url").asOpt[String])
					}.filter{ 
						case (username,name,url) => username.isDefined && name.isDefined && url.isDefined
					}.map{
						case (username,name,url) => new Repo(username.get, name.get, url.get)
					}
					if(repos.isEmpty) None else Some(repos)
				}
				case _ => None
			}
			
    	result(q, repos)
    }
	}
}
