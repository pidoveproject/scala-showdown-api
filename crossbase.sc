import $ivy.`io.chris-kipp::mill-ci-release::0.1.9`
import io.kipp.mill.ci.release.CiReleaseModule
import mill._, define._, api.Result
import scalalib._, scalalib.scalafmt._, scalalib.publish._, scalajslib._, scalanativelib._

object versions {
  val scala = "3.3.0"
  val scalaJS = "1.13.2"
}

trait BaseModule extends ScalaModule with ScalafmtModule with CiReleaseModule { outer =>

  def scalaVersion = versions.scala

  def pomSettings =
    PomSettings(
      description = "Strong type constraints for Scala",
      organization = "io.github.iltotore",
      url = "https://github.com/Iltotore/iron",
      licenses = Seq(License.`Apache-2.0`),
      versionControl = VersionControl.github("Iltotore", "iron"),
      developers = Seq(
        Developer("Iltotore", "Raphaël FROMENTIN", "https://github.com/Iltotore")
      )
    )

  trait Tests extends ScalaTests with ScalafmtModule {

    def testFramework = "utest.runner.Framework"

    def ivyDeps = Agg(
      ivy"com.lihaoyi::utest:0.8.1"
    )
  }

  trait CrossModule extends ScalaModule with ScalafmtModule with CiReleaseModule  {

    def segment: String

    def sources = T.sources(outer.sources() :+ PathRef(millSourcePath / s"src-$segment"))

    def scalaVersion = outer.scalaVersion

    def ivyDeps = outer.ivyDeps

    def artifactName = outer.artifactName

    def publishVersion = outer.publishVersion

    def pomSettings = outer.pomSettings

  }

  trait JSCrossModule extends CrossModule with ScalaJSModule {

    def segment = "js"

    def scalaJSVersion = versions.scalaJS

  }
}