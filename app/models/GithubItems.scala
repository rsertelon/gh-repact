package models

import java.util.Date
import org.ocpsoft.pretty.time.PrettyTime

object GithubUrl {
  val base = "https://github.com/"
  val apiBase = "https://api.github.com/"
  def user(user: String) = base + user
  def commit(user: String, repoName: String, commitSha: String) = base + user + "/" + repoName + "/commit/" + commitSha
  def apiSearch(q: String) = "http://github.com/api/v2/json/repos/search/" + q
  def apiRepository(owner: String, name: String) = apiBase + "repos/" + owner + "/" + name
  def apiRepositoryContributors(owner: String, name: String) = apiRepository(owner, name) + "/contributors"
  def apiRepositoryCommits(owner: String, name: String, number: Int = 100) = apiRepository(owner, name) + "/commits?per_page=" + number
}

case class Repo(owner: String, name: String, url: String, description: String, language: String, homepage: String)

object Contributor {
	def apply(c: Contributor, total: Int, badges: Option[Seq[Badge]]) = {
		new Contributor(c.login, c.avatar_url, c.contributions, Some(total), badges)
	}
}

case class Contributor(login: String, avatar_url: String, contributions: Int = 0, totalContribs: Option[Int] = None, badges: Option[Seq[Badge]] = None) {
	val contribPercent = totalContribs.map{t => contributions.toDouble / t * 100}.getOrElse(0)
	val url = GithubUrl.user(login)
}

object Commit {
	val prettyTime = new PrettyTime
}

case class Commit(committer: Contributor, date: Date, message: String, sha: String) {
	def url(repoInfo: Repo) = GithubUrl.commit(repoInfo.owner, repoInfo.name, sha)
	val printedMessage = message.split("\n")(0)
	val prettyDate = Commit.prettyTime.format(date)
}
