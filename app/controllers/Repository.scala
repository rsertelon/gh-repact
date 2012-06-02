package controllers

import play.api._
import play.api.mvc._
import play.api.libs.ws._
import play.api.libs.json._

case class Repo(owner: String, name: String, url: String)

object Repository extends Controller {

  def search(query: String) = Action { // Search a repo by keyword. Print a list of repos
  	Async {
  		WS.url("http://github.com/api/v2/json/repos/search/" + query).get().map{ response =>
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
  			
      	Ok(views.html.repository.search(query)(repos))
      }
    }
  }
  
  def show(user: String, name: String) = Action {   // Show one repo information
      TODO              //  - List of users (committers & contributors separated)
                        //  - Impact of users based on last 100 commits (graph: pie, impact, other ?)
                        //  - Commits timeline base on last 100 commits
  }
  
}
