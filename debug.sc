
import io.kipp.mill.ci.release.{CiReleaseModule, SonatypeHost}
import de.tobiasroeser.mill.vcs.version.VcsVersion
import mill._, define._, api.Result, eval.Evaluator
import scalalib._, scalalib.scalafmt._, scalalib.publish._, scalajslib._, scalanativelib._

package foo

object DebugModule extends mill.define.ExternalModule {

  def publishAll(ev: Evaluator) = T.command {
    val modules = ev.rootModule.millInternal.modules.collect { case m: CiReleaseModule => m }

    T.log.info(s"Modules: $modules")
  }

  lazy val millDiscover: mill.define.Discover[this.type] =
    mill.define.Discover[this.type]
}