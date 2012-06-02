package models

import java.util.Date

case class Repo(owner: String, name: String, url: String, description: String)

case class Contributor(login: String, avatar_url: String, contributions: Int, url: String)

case class Commit(committer: Contributor, date: Date, message: String)
