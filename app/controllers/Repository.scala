package controllers

import play.api._
import play.api.mvc._

object Repository extends Controller {

  def search(query: String) = Action { // Search a repo by keyword. Print a list of repos
      TODO
  }
  
  def show(user: String, name: String) = Action {   // Show one repo information
      TODO              //  - List of users (committers & contributors separated)
                        //  - Impact of users based on last 100 commits (graph: pie, impact, other ?)
                        //  - Commits timeline base on last 100 commits
  }
  
}