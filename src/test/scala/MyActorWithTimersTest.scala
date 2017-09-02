import akka.actor.ActorSystem
import akka.actor._
import akka.testkit.{ImplicitSender, TestActors, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._

class MyActorWithTimersTest() extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "An Echo actor" must {

    "walk the path" in {
      val echo = system.actorOf(Props[MyActorWithTimers])
      echo ! "Hello"
      echo ! "continue"
      expectMsg("receive continue")
      echo ! "last"
      expectMsg("receive last")
    }
    "back to start when no message" in {
      val echo = system.actorOf(Props[MyActorWithTimers])
      echo ! "Hello"
      expectNoMsg(100 milliseconds)
      expectMsg("back to start 2")
    }
    "walk the path twice" in {
      val echo = system.actorOf(Props[MyActorWithTimers])
      echo ! "Hello"
      echo ! "continue"
      expectMsg("receive continue")
      expectNoMsg(100 milliseconds)
      expectMsg("expired")
      echo ! "Hello"
      echo ! "continue"
      expectMsg("receive continue")
      echo ! "last"
      expectMsg("receive last")
    }
  }
}

