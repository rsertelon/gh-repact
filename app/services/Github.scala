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
  
  def fromSeqToOption[A](seq: Seq[A]): Option[Seq[A]] = {
    seq match {
      case Nil => None
      case s => Some(s)
    }
  }

  def search(q: String)(result: Option[Seq[Repo]] => Result) = {
    WS.url(GithubUrl.apiSearch(q.replaceAll("\\s","\\+"))).get().map{ response =>
      val repos = (response.json \ "repositories") match {
        case JsArray(elements) => {
          fromSeqToOption[Repo] {
            elements.map { repo =>
              for {
                size        <- (repo \ "size"       ).asOpt[Int] if size > 0;
                url         <- (repo \ "url"        ).asOpt[String];
                name        <- (repo \ "name"       ).asOpt[String];
                owner       <- (repo \ "username"   ).asOpt[String];
                description <- (repo \ "description").asOpt[String];
                language    <- (repo \ "language"   ).asOpt[String];
                homepage    <- (repo \ "homepage"   ).asOpt[String]
              } yield new Repo(owner, name, url, description, language, homepage)
            }.flatten
          }
        }
        case _ => None
      }

      result(repos)
    }
  }

  def repoInfo(owner:String,name:String): Promise[Option[Repo]] = {
    WS.url(GithubUrl.apiRepository(owner, name)).get().map { response =>
      response.json match {
        case repo: JsObject => {
          for {
            url         <- (repo \ "html_url"   ).asOpt[String];
            description <- (repo \ "description").asOpt[String];
            language    <- (repo \ "language"   ).asOpt[String];
            homepage    <- (repo \ "homepage"   ).asOpt[String]
          } yield new Repo(owner, name, url, description, language, homepage)
        }
        case _ => None
      }
    }
  }

  def repoContributors(owner:String, name:String): Promise[Option[Seq[Contributor]]] = {
    WS.url(GithubUrl.apiRepositoryContributors(owner, name)).get().map{ response =>
      response.json match {
        case JsArray(elements) => {
          fromSeqToOption[Contributor] {
            elements.map { contributor =>
              for {
                login         <- (contributor \ "login"        ).asOpt[String];
                avatar_url    <- (contributor \ "avatar_url"   ).asOpt[String];
                contributions <- (contributor \ "contributions").asOpt[Int]
              } yield new Contributor(login, avatar_url, contributions)
            }.flatten
          }
        }
        case _ => None
      }
    }
  }

  def commits(owner:String, name:String): Promise[Option[Seq[Commit]]] = {
    WS.url(GithubUrl.apiRepositoryCommits(owner, name)).get().map{ response =>
      response.json match {
        case JsArray(elements) => {
          fromSeqToOption[Commit] {
            elements.map { commit =>
              for {
                sha        <- (commit \ "sha"                             ).asOpt[String];
                message    <- (commit \ "commit"    \ "message"           ).asOpt[String];
                date       <- (commit \ "commit"    \ "committer" \ "date").asOpt[Date];
                login      <- (commit \ "committer" \ "login"             ).asOpt[String];
                avatar_url <- (commit \ "committer" \ "avatar_url"        ).asOpt[String]
              } yield new Commit(new Contributor(login, avatar_url), date, message, sha)
            }.flatten
          }
        }
        case _ => None
      }
    }
  }

}
