package services

import models._

import java.util.Date

import play.api.mvc._
import play.api.libs.ws._
import play.api.libs.json._
import play.api.libs.concurrent._

import org.joda.time.format._

object Github {

	val parser = ISODateTimeFormat.dateTimeNoMillis();

  // Try parsing date from iso8601 format
  implicit object DateFormat extends Reads[Date] {
    def reads(json: JsValue): Date = json match {
        // Need to throw a RuntimeException, ParseException beeing out of scope of asOpt
        case JsString(s) => parser.parseDateTime(s).toDate
        case _ => throw new RuntimeException("Parse exception")
    }
  }


	def search(q: String)(result: (String, Option[Seq[Repo]]) => Result) = {
		WS.url("http://github.com/api/v2/json/repos/search/" + q).get().map{ response =>
			val repos = (Json.parse(response.body) \ "repositories") match {
				case JsArray(elements) => {
					val repos = elements.map { repo =>
						(
							(repo \ "username").asOpt[String], 
							(repo \ "name").asOpt[String], 
							(repo \ "url").asOpt[String],
							(repo \ "description").asOpt[String],
							(repo \ "size").asOpt[Int]
						)
					}.filter{ 
						case (username,name,url,desc,size) => 
							size.get != 0 && username.isDefined && name.isDefined && url.isDefined && desc.isDefined
					}.map{
						case (username,name,url,desc,size) => 
							new Repo(username.get, name.get, url.get, desc.get)
					}
					if(repos.isEmpty) None else Some(repos)
				}
				case _ => None
			}
			
    	result(q, repos)
    }
	}
	
	def repoInfo(owner:String,name:String): Promise[Option[Repo]] = {
		WS.url("https://api.github.com/repos/" + owner + "/" + name).get().map { response =>
			Json.parse(response.body) match {
				case jsObj: JsObject => {
					(jsObj \ "html_url").asOpt[String].map { url =>
						(jsObj \ "description").asOpt[String].map { desc =>
							new Repo(owner,name,url, desc)
						}.get
					}
				}
				case _ => None
			}
		}
	}
	
	def repoContributors(owner:String, name:String): Promise[Option[Seq[Contributor]]] = {
		WS.url("https://api.github.com/repos/" + owner + "/" + name + "/contributors").get().map{ response =>
			Json.parse(response.body) match {
				case JsArray(elements) => {
					val contribs = elements.map { contrib =>
						((contrib \ "login").asOpt[String], (contrib \ "avatar_url").asOpt[String], (contrib \ "contributions").asOpt[Int])
					}.filter{ 
						case (login,avatar_url,contributions) => 
							login.isDefined && avatar_url.isDefined && contributions.isDefined
					}.map{
						case (login,avatar_url,contributions) => 
							new Contributor(login.get,avatar_url.get,contributions.get,"https://github.com/" + login.get)
					}
					if(contribs.isEmpty) None else Some(contribs)
				}
				case _ => None
			}
    }
	}
	
	def commits(owner:String, name:String): Promise[Option[Seq[Commit]]] = {
		WS.url("https://api.github.com/repos/" + owner + "/" + name + "/commits?per_page=100").get().map{ response =>
			Json.parse(response.body) match {
				case JsArray(elements) => {
					val commitsSeq = elements.map { commit =>
						(
							(commit \ "committer" \ "login").asOpt[String], 
							(commit \ "committer" \ "avatar_url").asOpt[String], 
							(commit \ "commit" \ "committer" \ "date").asOpt[Date], 
							(commit \ "commit" \ "message").asOpt[String],
							(commit \ "sha").asOpt[String]
						)
					}.filter{ 
						case (login,avatar_url,date,message,sha) => 
							login.isDefined && avatar_url.isDefined && date.isDefined && message.isDefined & sha.isDefined
					}.map{
						case (login,avatar_url,date,message,sha) => 
							new Commit(new Contributor(login.get,avatar_url.get,0,"https://github.com/" + login.get),date.get,message.get,sha.get)
					}
					if(commitsSeq.isEmpty) None else Some(commitsSeq)
				}
				case _ => None
			}
    }
	}
}
