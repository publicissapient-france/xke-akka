import sbt._
import de.element34.sbteclipsify._


class Manufacturing(info: ProjectInfo) extends DefaultProject(info) with IdeaProject with Eclipsify with AkkaProject {
    val snapshots = "snapshots" at "http://scala-tools.org/repo-snapshots"
    val releases = "releases" at "http://scala-tools.org/repo-releases"

    val scalatest = "org.scalatest" % "scalatest_2.9.0" % "1.4.1" withSources()
    val mockitoCore = "org.mockito" % "mockito-core" % "1.8.5" withSources()

    val akkaRemote = akkaModule("remote") withSources()
    val akkaTestKit = akkaModule("testkit") withSources()
}
