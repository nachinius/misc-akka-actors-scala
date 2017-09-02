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
  context.parent ! SystemAlpha.Through("welcome to A")
  context.parent ! SystemAlpha.Through("1 o 2")
  when(UnoA) {
    case Event(msg: String, _) =>
      context.parent ! SystemAlpha.Through("3 o 4")
      goto(DosA) using msg
  }
  when(DosA) {
    case Event(msg: String, str) =>
      context.parent ! SystemAlpha.End(SystemAlpha.Through(str + msg))
      context stop self // suicide because our job is done
      goto(Finished) // safeguard to avoid
  }
  when(Finished) {
    case _ => {
      context stop self // insist on suicide
      stay
    }
  }
  initialize()
}
