package models

import java.util.Date
import org.ocpsoft.pretty.time.PrettyTime

object Contributor {
	def apply(c: Contributor, total: Int, badges: Seq[Badge]) = {
		new Contributor(c.login, c.avatar_url, c.contributions, Some(total), Some(badges))
	}
}

case class Repo(owner: String, name: String, url: String, description: String)

case class Contributor(login: String, avatar_url: String, contributions: Int = 0, totalContribs: Option[Int] = None, badges: Option[Seq[Badge]] = None) {
	val contribPercent = totalContribs.map{t => contributions.toDouble / t * 100}.getOrElse(0)
    
    val url = "https://github.com/" + login
}

case class Commit(committer: Contributor, date: Date, message: String, sha: String) {
	def url(repoInfo: Repo) = "https://github.com/" + repoInfo.owner + "/" + repoInfo.name + "/commit/" + sha
	
	val printedMessage = if(message.length > 30) message.substring(0,30) + "..." else message
	
	val prettyDate = new PrettyTime().format(date)
}
