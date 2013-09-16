package ca.polymtl.log4900.eval

import akka.kernel.Bootable
import akka.actor.{ Props, Actor, ActorSystem }
import com.typesafe.config.ConfigFactory

class SimpleCalculatorActor extends Actor {
  def receive = {
    case Add(n1, n2) ⇒
      println("Calculating %d + %d".format(n1, n2))
      sender ! AddResult(n1, n2, n1 + n2)
    case Subtract(n1, n2) ⇒
      println("Calculating %d - %d".format(n1, n2))
      sender ! SubtractResult(n1, n2, n1 - n2)
  }
}

class CalculatorApplication extends Bootable {
  val system = ActorSystem("CalculatorApplication", ConfigFactory.load.getConfig("calculator"))
  val actor = system.actorOf(Props[SimpleCalculatorActor], "simpleCalculator")

  def startup() { }
  def shutdown() { system.shutdown() }
}

object CalcApp {
  def main(args: Array[String]) {
    new CalculatorApplication
    println("Started Calculator Application - waiting for messages")
  }
}

trait MathOp
case class Add(nbr1: Int, nbr2: Int) extends MathOp
case class Subtract(nbr1: Int, nbr2: Int) extends MathOp
case class Multiply(nbr1: Int, nbr2: Int) extends MathOp
case class Divide(nbr1: Double, nbr2: Int) extends MathOp

trait MathResult
case class AddResult(nbr: Int, nbr2: Int, result: Int) extends MathResult
case class SubtractResult(nbr1: Int, nbr2: Int, result: Int) extends MathResult
case class MultiplicationResult(nbr1: Int, nbr2: Int, result: Int) extends MathResult
case class DivisionResult(nbr1: Double, nbr2: Int, result: Double) extends MathResult