package models

import java.util.Date
import org.ocpsoft.pretty.time.PrettyTime

case class Repo(owner: String, name: String, url: String, description: String)

case class Contributor(login: String, avatar_url: String, contributions: Int, url: String)

case class Commit(committer: Contributor, date: Date, message: String, sha: String) {
	def url(repoInfo: Repo) = "https://github.com/" + repoInfo.owner + "/" + repoInfo.name + "/commit/" + sha
	
	val printedMessage = if(message.length > 32) message.substring(0,32) + "..." else message
	
	val prettyDate = new PrettyTime().format(date)
}
