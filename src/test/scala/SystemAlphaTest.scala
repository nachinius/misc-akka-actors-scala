import akka.actor.ActorSystem
import akka.actor._
import akka.testkit.{ImplicitSender, TestActors, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._

class SystemAlphaTest extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "system alpha" must {
    "switch to A" in {
      val alpha = system.actorOf(Props[SystemAlpha])
      alpha ! "request A"
      expectMsg("welcome to A")
      expectMsg("1 o 2")
      alpha ! "2"
      expectMsg("3 o 4")
      alpha ! "4"
      expectMsg("24")
      alpha ! "where"
      expectMsg("nowhere")
    }

  }
}
