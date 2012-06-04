package controllers

import play.api._
import play.api.mvc._
import play.api.libs.ws._
import play.api.libs.json._
import play.api.libs.concurrent._

import models._

object Repository extends Controller {

  def search(query: String) = Action {
  	Async {
  		services.Github.search(query) { (query: String, repos: Option[Seq[Repo]]) => 
      	Ok(views.html.repository.search(query)(repos))
      }
    }
  }
  
  def show(user: String, name: String) = Action {   // Show one repo information
  		
		Async {
  		services.Github.repoInfo(user,name).flatMap{ repoInfo =>
				services.Github.commits(user,name).flatMap { repoCommits =>
					services.Github.repoContributors(user,name).map { repoContributors =>
						val total = repoContributors.get.foldLeft(0)((s,c) => s + c.contributions)
						Ok(views.html.repository.show(repoInfo.get)(repoContributors.get.map(r => Contributor(r, total)))(repoCommits.get))
					}
				}
  		}
		}
  
    //  - List of users (committers & contributors separated)
    //  - Impact of users based on last 100 commits (graph: pie, impact, other ?)
    //  - Commits timeline base on last 100 commits
  }
  
}
