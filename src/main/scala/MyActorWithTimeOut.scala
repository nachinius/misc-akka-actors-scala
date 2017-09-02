import akka.actor._
import akka.actor.ReceiveTimeout
import scala.concurrent.duration._

/**
  * An Actor example using:
  *   - become/unbecome
  *   - setReceiveTimeout
  */
class MyActorWithTimeOut extends Actor {
  def receive = start

  var ref: Option[ActorRef] =None
  def start: Receive = {
    case "Hello" =>
      // To set in a response to a message
      ref=Some(sender())
      context.setReceiveTimeout(100 milliseconds)
      context.become(next)
  }

  def next: Receive = {
    case ReceiveTimeout =>
      // To turn it off
      context.setReceiveTimeout(Duration.Undefined)
      context.become(start)
      ref.map(_ ! "back to start")
    case "continue" => {
      context.setReceiveTimeout(Duration.Undefined)
      context.setReceiveTimeout(100 milliseconds)
      sender() ! "receive continue"
      context.become(last)
    }
  }

  def last: Receive = {
    case ReceiveTimeout =>
      // To turn it off
      context.setReceiveTimeout(Duration.Undefined)
      ref.map(_ ! "expired")
      context.become(start)
    case "last" => {
      context.setReceiveTimeout(Duration.Undefined)
      sender() ! "receive last"
      context stop self
    }
  }
}