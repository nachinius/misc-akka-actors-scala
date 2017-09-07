package com.nachinius.actors.supervision

// http://doc.akka.io/docs/akka/2.5.4/scala/fault-tolerance.html
class Example {

}

import akka.actor.{Actor, Props}

class Supervisor extends Actor with akka.actor.ActorLogging {

  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._

  import scala.concurrent.duration._

  var n = 0
  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 1, withinTimeRange = 1 minute) {
      case _: ArithmeticException => {
        n = n + 1

        Resume
      }
      case _: NullPointerException => Restart
      case _: IllegalArgumentException => Stop
      case _: Exception => Escalate
    }

  def receive = {
    case p: Props => sender() ! context.actorOf(p)
    case "n" => sender() ! n
    case "reset" => n = 0
  }
}

class Supervisor2 extends Supervisor {
  // without killing childs
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {}

}

import akka.actor.Actor

class Child extends Actor {
  var state = 0

  def receive = {
    case ex: Exception => throw ex
    case x: Int => state = x
    case "get" => sender() ! state
  }
}