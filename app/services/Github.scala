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

    // Following code copied from Play Framework test case:
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
			val repos = (response.json \ "repositories") match {
				case JsArray(elements) => {
					val reposSeq = elements.map { repo =>
					    for {
                            url         <- (repo \ "url"        ).asOpt[String];
                            name        <- (repo \ "name"       ).asOpt[String];
                            size        <- (repo \ "size"       ).asOpt[Int] if size > 0;
                            owner       <- (repo \ "username"   ).asOpt[String];
                            description <- (repo \ "description").asOpt[String]
					    } yield new Repo(owner, name, url, description)
					}.flatten
                    
                    reposSeq match {
                        case Nil => None
                        case seq => Some(seq)
                    }
				}
				case _ => None
			}
			
    	    result(q, repos)
        }
	}
	
	def repoInfo(owner:String,name:String): Promise[Option[Repo]] = {
		WS.url("https://api.github.com/repos/" + owner + "/" + name).get().map { response =>
			response.json match {
				case jsObj: JsObject => {
                    for {
                        url         <- (jsObj \ "html_url"   ).asOpt[String];
                        description <- (jsObj \ "description").asOpt[String]
                    } yield new Repo(owner, name, url, description)
				}
				case _ => None
			}
		}
	}
	
	def repoContributors(owner:String, name:String): Promise[Option[Seq[Contributor]]] = {
		WS.url("https://api.github.com/repos/" + owner + "/" + name + "/contributors").get().map{ response =>
			response.json match {
				case JsArray(elements) => {
                    val contributorsSeq = elements.map { contributor =>
                        for {
                            login         <- (contributor \ "login"        ).asOpt[String];
                            avatar_url    <- (contributor \ "avatar_url"   ).asOpt[String];
                            contributions <- (contributor \ "contributions").asOpt[Int]
                        } yield new Contributor(login, avatar_url, contributions)
					}.flatten
                    
                    contributorsSeq match {
                        case Nil => None
                        case seq => Some(seq)
                    }
				}
				case _ => None
			}
        }
	}
	
	def commits(owner:String, name:String): Promise[Option[Seq[Commit]]] = {
		WS.url("https://api.github.com/repos/" + owner + "/" + name + "/commits?per_page=100").get().map{ response =>
			response.json match {
				case JsArray(elements) => {
					val commitsSeq = elements.map { commit =>
                        for {
                            sha        <- (commit \ "sha"                             ).asOpt[String];
                            login      <- (commit \ "committer" \ "login"             ).asOpt[String];
                            message    <- (commit \ "commit"    \ "message"           ).asOpt[String];
                            avatar_url <- (commit \ "committer" \ "avatar_url"        ).asOpt[String];
                            date       <- (commit \ "commit"    \ "committer" \ "date").asOpt[Date]
                        } yield new Commit(new Contributor(login, avatar_url), date, message, sha)
					}.flatten
					
                    commitsSeq match {
                        case Nil => None
                        case seq => Some(seq)
                    }
				}
				case _ => None
			}
        }
	}
}
