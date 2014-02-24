import play.api._

import model._

object Global extends GlobalSettings {
  override def onStart(app: Application): Unit = {
	EvalClient.start()
  }
  
  override def onStop(app: Application): Unit = {
    EvalClient.stop()
  }
}