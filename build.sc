import $ivy.`io.chris-kipp::mill-ci-release::0.1.9`
import io.kipp.mill.ci.release.{CiReleaseModule, SonatypeHost}
import de.tobiasroeser.mill.vcs.version.VcsVersion
import mill._, define._, api.Result
import scalalib._, scalalib.scalafmt._, scalalib.publish._, scalajslib._, scalanativelib._

object versions {
  val scala = "3.3.3"
  val scalaJS = "1.16.0"
}

object docs extends ProjectModule {

  def scalaVersion = versions.scala

  def artifactName = "scala-showdown-api-docs"

  val modules: Seq[ScalaModule] = Seq(main, tyrian)

  def docSources = T.sources {
    T.traverse(modules)(_.docSources)().flatten
  }

  def compileClasspath = T {
    T.traverse(modules)(_.compileClasspath)().flatten
  }

  def gitTags = T {
    os
      .proc("git", "tag", "-l", "v*.*.*")
      .call(VcsVersion.vcsBasePath)
      .out
      .trim()
      .split("\n")
      .reverse
  }

  def docVersions = T.source {
    val targetDir = T.dest / "_assets"

    val versions =
      gitTags()
        .filterNot(v => v.contains("-RC") || v.isBlank)
        .map(_.substring(1))

    def versionLink(version: String): String =
      s"https://www.javadoc.io/doc/io.github.pidoveproject/scala-showdown-api-docs_3/$version/"

    val links = versions.map(v => (v, ujson.Str(versionLink(v))))
    val withNightly = links :+ ("Nightly", ujson.Str("https://pidoveproject.github.io/scala-showdown-api/"))
    val json = ujson.Obj("versions" -> ujson.Obj.from(withNightly))

    val versionsFile = targetDir / "versions.json"
    os.write.over(versionsFile, ujson.write(json), createFolders = true)

    T.dest
  }

  def docResources = T.sources(millSourcePath, docVersions().path)

  def docRevision = T {
    val version = main.publishVersion()
    if(gitTags().contains(version)) version
    else "main"
  }

  def externalMappings = Map.empty[String, (String, String)]

  def scalaDocOptions = {
    val externalMappingsFlag =
      externalMappings
        .map {
          case (regex, (docType, link)) => s"$regex::$docType::$link"
        }
        .mkString(",")


    Seq(
      "-project", "Pokemon Showdown API",
      "-project-version", main.publishVersion(),
      "-versions-dictionary-url", "https://pidoveproject.github.io/scala-showdown-api/versions.json",
      "-source-links:github://pidoveproject/scala-showdown-api",
      "-revision", docRevision(),
      "-snippet-compiler:nocompile",
      s"-social-links:github::${main.pomSettings().url}"
      //s"-external-mappings:$externalMappingsFlag"
    )
  }
}

object main extends ProjectModule {

  def artifactName = "scala-showdown-api"

  def ivyDeps = Agg(
    ivy"io.github.iltotore::iron::2.6.0",
    ivy"io.github.iltotore::iron-zio-json::2.6.0",
    ivy"dev.zio::zio::2.0.15",
    ivy"dev.zio::zio-http:3.0.0-RC2",
    ivy"dev.zio::zio-json::0.6.0",
    ivy"dev.zio::zio-parser::0.1.9",
    ivy"dev.zio::zio-prelude::1.0.0-RC19"
  )

  object js extends JSCrossModule

  object test extends Tests
}

object cats extends ProjectModule {

  def artifactName = "scala-showdown-api-cats"

  def moduleDeps = Seq(main)

  def ivyDeps = main.ivyDeps() ++ Agg(
    ivy"co.fs2::fs2-core::3.10.2",
    ivy"org.typelevel::cats-core::2.12.0",
    ivy"org.typelevel::cats-effect::3.5.4",
    ivy"dev.zio::zio-interop-cats:23.1.0.3"
  )

  object js extends JSCrossModule
}

object tyrian extends ProjectModule with ScalaJSModule {

  def artifactName = "scala-showdown-api-tyrian"

  def scalaJSVersion = versions.scalaJS

  def moduleDeps = Seq(main.js)

  def ivyDeps = main.ivyDeps() ++ Agg(
    ivy"io.indigoengine::tyrian::0.8.0"
  )
}

object examples extends Module {

  object `zio-client` extends ScalaModule {

    def scalaVersion = versions.scala

    def moduleDeps = Seq(main)

    def ivyDeps = main.ivyDeps() ++ Agg(
      ivy"dev.zio::zio::2.0.15",
      ivy"dev.zio::zio-http:3.0.0-RC2"
    )
  }

  object `tyrian-client` extends ScalaJSModule {

    def scalaVersion = versions.scala

    def scalaJSVersion = versions.scalaJS

    def moduleDeps = Seq(main.js, tyrian, cats.js)

    def ivyDeps = main.ivyDeps() ++ Agg(
      ivy"io.indigoengine::tyrian::0.8.0",
      ivy"io.indigoengine::tyrian-io::0.8.0",
      ivy"org.http4s::http4s-ember-client::0.23.27"
    )

    def moduleKind = T(mill.scalajslib.api.ModuleKind.ESModule)
  }

  object `sync-ping-pong` extends ScalaModule {

    def scalaVersion = versions.scala

    def moduleDeps = Seq(main)
  }
}

//Internals
trait ProjectModule extends ScalaModule with ScalafmtModule with CiReleaseModule { outer =>

  def scalaVersion = versions.scala

  override def sonatypeHost = Some(SonatypeHost.s01)

  def pomSettings =
    PomSettings(
      description = "A Scala wrapper of Pokemon Showdown's API",
      organization = "io.github.pidoveproject",
      url = "https://github.com/pidoveproject/scala-showdown-api",
      licenses = Seq(License.`Apache-2.0`),
      versionControl = VersionControl.github("pidoveproject", "scala-showdown-api"),
      developers = Seq(
        Developer("Iltotore", "Raphaël FROMENTIN", "https://github.com/Iltotore"),
        Developer("pidoveproject", "Pidove Project Team", "https://github.com/pidoveproject")
      )
    )

  def scalacOptions = super.scalacOptions() ++ Seq(
    "-Xmax-inlines", "64",
    "-Wunused:imports"
  )

  trait Tests extends ScalaTests with ScalafmtModule {

    def testFramework = "utest.runner.Framework"

    def ivyDeps = Agg(
      ivy"com.lihaoyi::utest:0.8.1"
    )
  }

  trait CrossModule extends ScalaModule with ScalafmtModule with CiReleaseModule  {

    override def sonatypeHost = outer.sonatypeHost

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
