import akka.actor.{ActorRef, FSM, Props}
import scala.concurrent.duration._

object SystemAlpha {

  trait StateAlpha

  case object InA extends StateAlpha

//  case object InB extends StateAlpha

  case object Waiting extends StateAlpha

  case class DataAlpha(relay: Option[ActorRef], original: Option[ActorRef])

  trait SystemAlphaMessages

  // For specifying that the message should pass through
  case class Through(msg: String) extends SystemAlphaMessages

  // For telling Main System that the state is finished
  case class End(msg: Through) extends SystemAlphaMessages

}

/**
  * An actor that has State and an deeper State.
  * Inner state is handled by another actor (SystemA)
  */
class SystemAlpha extends FSM[SystemAlpha.StateAlpha, SystemAlpha.DataAlpha] {
  import SystemAlpha._
  import context._

  startWith(Waiting, DataAlpha(None, None))

  when(Waiting) {
    case Event("request A", _) =>
      val a = context.actorOf(Props[SystemA])
      val s = sender()
      goto(InA) using DataAlpha(Some(a), Some(s))
    case Event("where", _) =>
      sender() ! "nowhere"
      stay
  }
  when(InA) {
    // Realy to A
    case Event(msg: String, state) =>
      state.relay.map(_ forward msg)
      stay
    // Reply from A to Original
    case Event(Through(msg), state) =>
      state.original.map(_ forward msg)
      stay
    // Ending state from inside A
    case Event(End(msg), state) => {
      state.original.map(_ forward msg.msg)
      goto(Waiting)
    }
  }
  initialize()
}

