import akka.actor.{ActorRef, FSM, Props}
import scala.concurrent.duration._

object SystemAlpha {

  trait State

  case object InA extends State

  //  case object InB extends StateAlpha

  case object Waiting extends State

  case class Data(relay: Option[ActorRef], original: Option[ActorRef])

  trait SystemAlphaMessages

  // For telling Main System that the state is finished
  case class End(msg: String) extends SystemAlphaMessages

}

/**
  * An actor that has State and an deeper State.
  * Inner state is handled by another actor (SystemA)
  */
class SystemAlpha extends FSM[SystemAlpha.State, SystemAlpha.Data] {

  import SystemAlpha._
  import context._

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
    case Event(End(msg), state) =>
      state.original.foreach(_ forward msg)
      goto(Waiting) using Data(None, None)
    // Realy to A
    case Event(msg: String, state) =>
      // if sender is the original actor, then send the mssage down,
      // if sender is the raelayed actor, then send the message to the  actor that started the FSM
      // code is in purpose complex because i'm playing with functional functions
      state.original.filter(_ eq sender()).flatMap(_ => state.relay).orElse(state.original).foreach(_ forward msg)
      stay
  }
  initialize()
}

