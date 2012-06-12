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
          Github.repoContributors(user,name).map { repoContributors =>
            (
              for {
                information <- repoInfo;
                contributors <- repoContributors;
                commits <- repoCommits
              } yield Ok(views.html.repository.show(information)(contributors.map(r => Contributor(r, contributors.foldLeft(0)((sum,c) => sum + c.contributions))))(commits))
            ).getOrElse(NotFound(views.html.application.notfound()))
          }
        }
      }
    }
  }

}
