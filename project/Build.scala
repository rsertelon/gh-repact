import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "gh_repact"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      "com.github.twitter" % "bootstrap" % "2.0.3",
      "joda-time" % "joda-time" % "2.1",
      "org.ocpsoft.prettytime" % "prettytime" % "1.0.8.Final"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      resolvers += "webjars" at "http://webjars.github.com/m2"
    )

}
