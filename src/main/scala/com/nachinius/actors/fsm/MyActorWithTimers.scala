package com.nachinius.actors.fsm

import akka.actor._

import scala.concurrent.duration._


object MyActorWithTimers {
  case object TimeMessage
}

/**
  * An actor using
  *   - become/unbecome
  *   - timers
  */
class MyActorWithTimers extends Actor with Timers {
  import MyActorWithTimers._

  def receive = start

  var ref: Option[ActorRef] = None

  def start: Receive = {
    case "Hello" =>
      // To set in a response to a message
      ref = Some(sender())
      //      context.setReceiveTimeout(100 milliseconds)
      timers.startSingleTimer("a", TimeMessage, 100 milliseconds)
      context.become(next)
  }

  def next: Receive = {
    case TimeMessage =>
      // To turn it off
      context.become(start)
      ref.map(_ ! "back to start 2")
    case "continue" => {
      timers.startSingleTimer("a", TimeMessage, 100 milliseconds)
      sender() ! "receive continue"
      context.become(last)
    }
  }

  def last: Receive = {
    case TimeMessage =>
      // To turn it off
      ref.map(_ ! "expired")
      context.become(start)
    case "last" => {
      sender() ! "receive last"
      context stop self
    }
  }
}



