package model;

import scala.util.Random
import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory
import akka.actor.{ ActorRef, Props, Actor, ActorSystem }
import akka.actor.Identify
import akka.actor.ActorIdentity
import akka.kernel.Bootable
import akka.actor.ReceiveTimeout

import ca.polymtl.log4900.eval._

object LookupApplication extends Bootable {
  lazy val system = ActorSystem("LookupApplication", ConfigFactory.load.getConfig("remotelookup"))
  lazy val remotePath = "akka.tcp://CalculatorApplication@127.0.0.1:2552/user/simpleCalculator"
  lazy val actor = system.actorOf(Props(classOf[LookupActor], remotePath), "lookupActor")

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
    case ActorIdentity(`path`, None) ⇒ println(s"Remote actor not availaible: $path")
    case ReceiveTimeout              ⇒ sendIdentifyRequest()
    case _                           ⇒ println("Not ready yet")
  }

  def active(actor: ActorRef): Actor.Receive = {
    case op: MathOp ⇒ actor ! op
    case result: MathResult ⇒ result match {
      case AddResult(n1, n2, r) ⇒ printf("Add result: %d + %d = %d\n", n1, n2, r)
      case SubtractResult(n1, n2, r) ⇒ printf("Sub result: %d - %d = %d\n", n1, n2, r)
      case MultiplicationResult(n1, n2, r) ⇒ printf("Multiplication result: %d + %d = %d\n", n1, n2, r)
      case DivisionResult(n1, n2, r) ⇒ printf("Division result: %d - %d = %d\n", n1, n2, r)
    }
  }
}