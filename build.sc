import $ivy.`io.chris-kipp::mill-ci-release::0.1.9`
import io.kipp.mill.ci.release.CiReleaseModule
import mill._, define._, api.Result
import scalalib._, scalalib.scalafmt._, scalalib.publish._, scalajslib._, scalanativelib._

object versions {
  val scala = "3.3.0"
  val scalaJS = "1.13.2"
}

object main extends ProjectModule {

  def ivyDeps = Agg(
    ivy"io.github.iltotore::iron::2.2.0",
    ivy"io.github.iltotore::iron-zio-json::2.2.0",
    ivy"dev.zio::zio-json::0.6.0",
    ivy"dev.zio::zio-parser::0.1.9",
    ivy"dev.zio::zio-prelude::1.0.0-RC19"
  )

  object js extends JSCrossModule

  object test extends Tests
}

object tyrian extends ProjectModule with ScalaJSModule {

  def scalaJSVersion = versions.scalaJS

  def moduleDeps = Seq(main)

  def ivyDeps = main.ivyDeps() ++ Agg(
    ivy"io.indigoengine::tyrian::0.8.0"
  )
}

object zio extends ProjectModule {

  def moduleDeps = Seq(main)

  def ivyDeps = main.ivyDeps() ++ Agg(
    ivy"dev.zio::zio::2.0.15",
    ivy"dev.zio::zio-http:3.0.0-RC2"
  )
}

object examples extends Module {

  object `zio-client` extends ProjectModule {

    def moduleDeps = Seq(main, zio)

    def ivyDeps = main.ivyDeps() ++ Agg(
      ivy"dev.zio::zio::2.0.15",
      ivy"dev.zio::zio-http:3.0.0-RC2"
    )
  }
}

//Internals
trait ProjectModule extends ScalaModule with ScalafmtModule with CiReleaseModule { outer =>

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

  def scalacOptions = super.scalacOptions() ++ Seq(
    "-Xmax-inlines", "64"
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

    def moduleDeps = outer.moduleDeps

    def artifactName = outer.artifactName

    def publishVersion = outer.publishVersion

    def pomSettings = outer.pomSettings

  }

  trait JSCrossModule extends CrossModule with ScalaJSModule {

    def segment = "js"

    def scalaJSVersion = versions.scalaJS

  }
}