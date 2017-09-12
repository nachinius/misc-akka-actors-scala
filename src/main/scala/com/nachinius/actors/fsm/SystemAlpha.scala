package com.nachinius.actors.fsm

import akka.actor.{ActorRef, FSM, Props}

object SystemAlpha {

  trait State

  case object InA extends State

  //  case object InB extends StateAlpha

  case object Waiting extends State

  case class Data(relay: Option[ActorRef], original: Option[ActorRef])

  trait SystemAlphaMessages

  // For telling Main System that the state is finished
  case class End() extends SystemAlphaMessages

}

/**
  * Using FSM to handle state in a toy situation where the first system (this one)
  * relays a substate to another FSM that this system creates. This system works as a proxy
  * for every message between its subsystem and the user.
  */
class SystemAlpha extends FSM[SystemAlpha.State, SystemAlpha.Data] {

  import SystemAlpha._

  startWith(Waiting, Data(None, None))

  when(Waiting) {
    case Event("request A", _) =>
      val a = context.actorOf(Props[SystemA])
      val s = sender()
      goto(InA) using Data(relay = Some(a), original = Some(s))
    case Event("where", _) =>
      sender() ! "nowhere"
      stay
  }
  when(InA) {
    case Event(End(), state) =>
      goto(Waiting) using Data(None, None)
    // Realy to A
    case Event(msg: String, state) =>
      // if sender is the original actor, then send the mssage down,
      // if sender is the raelayed actor, then send the message to the  actor that started the FSM
      // code is in purpose complex because i'm playing with functional functions
      state.original.filter(_ eq sender()).flatMap(_ => state.relay).orElse(state.original).foreach(_ forward msg)
      // another tricky way to do it
//      Seq(state.original, state.relay).flatten.filterNot(_ equals sender()).foreach(_ forward msg)
      stay
  }
  initialize()
}

