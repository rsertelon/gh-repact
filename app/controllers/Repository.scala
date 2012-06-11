package controllers

import play.api._
import play.api.mvc._
import play.api.libs.ws._
import play.api.libs.json._
import play.api.libs.concurrent._

import models._
import services._

object Repository extends Controller {

  def search(query: String) = Action {
    Async {
      Github.search(query) { (repos: Option[Seq[Repo]]) =>
        Ok(views.html.repository.search(query)(repos))
      }
    }
  }

  def show(user: String, name: String) = Action {
    Async {
      Github.repoInfo(user,name).flatMap{ repoInfo =>
        Github.commits(user,name).flatMap { repoCommits =>
          Github.repoContributors(user,name).flatMap { repoContributors =>
            Coderwall.badgesOf("bluepyth").map { badges =>
              val total = repoContributors.get.foldLeft(0)((sum,c) => sum + c.contributions)
              Ok(views.html.repository.show(repoInfo.get)(repoContributors.get.map(r => Contributor(r, total, badges)))(repoCommits.get))
            }
          }
        }
      }
    }
  }

}