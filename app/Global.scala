import play.api._
import model._

object Global extends GlobalSettings {
	override def onStart(app: Application) {
		LookupApplication.startup()
	}
	override def onStop(app: Application) {
		LookupApplication.shutdown()
	}
}