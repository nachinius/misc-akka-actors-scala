package com.nachinius.actors.fsm

import akka.actor.FSM

object SystemA {
  trait StateA

  case object UnoA extends StateA

  case object DosA extends StateA

  case object Finished extends StateA
}

/**
  * A simple Actor with an State
  */
class SystemA extends FSM[SystemA.StateA, String] {
  import SystemA._

  startWith(UnoA, "")
  import context.parent
  parent ! "welcome to A"
  parent ! "1 o 2"
  when(UnoA) {
    case Event(msg: String, _) =>
      parent ! s"got $msg"
      parent ! "3 o 4"
      goto(DosA) using msg
  }
  when(DosA) {
    case Event(msg: String, str) =>
      parent ! str + msg
      parent ! SystemAlpha.End()
      stop()
  }
  initialize()
}
