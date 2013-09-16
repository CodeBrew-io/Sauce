import play.api._

object Global extends GlobalSettings {
	override def onStart(app: Application) {
		LookupApplication.startup()
	}
	override def onStop(app: Application) {
		LookupApplication.shutdown()
	}
}

import scala.util.Random
import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory
import akka.actor.{ ActorRef, Props, Actor, ActorSystem }
import akka.actor.Identify
import akka.actor.ActorIdentity
import akka.kernel.Bootable
import akka.actor.ReceiveTimeout

object LookupApplication extends Bootable {
  val system = ActorSystem("LookupApplication", ConfigFactory.load.getConfig("remotelookup"))
  val remotePath = "akka.tcp://CalculatorApplication@127.0.0.1:2552/user/simpleCalculator"
  val actor = system.actorOf(Props(classOf[LookupActor], remotePath), "lookupActor")

  def doSomething(op: MathOp): Unit = actor ! op

  def startup() { }
  def shutdown() { system.shutdown() }
}

class LookupActor(path: String) extends Actor {

  context.setReceiveTimeout(3.seconds)
  sendIdentifyRequest()

  def sendIdentifyRequest(): Unit = context.actorSelection(path) ! Identify(path)

  def receive = {
    case ActorIdentity(`path`, Some(actor)) ⇒
      context.setReceiveTimeout(Duration.Undefined)
      context.become(active(actor))
    case ActorIdentity(`path`, None) ⇒ println(s"Remote actor not availible: $path")
    case ReceiveTimeout              ⇒ sendIdentifyRequest()
    case _                           ⇒ println("Not ready yet")
  }

  def active(actor: ActorRef): Actor.Receive = {
    case op: MathOp ⇒ actor ! op
    case result: MathResult ⇒ result match {
      case AddResult(n1, n2, r) ⇒ printf("Add result: %d + %d = %d\n", n1, n2, r)
      case SubtractResult(n1, n2, r) ⇒ printf("Sub result: %d - %d = %d\n", n1, n2, r)
    }
  }
}