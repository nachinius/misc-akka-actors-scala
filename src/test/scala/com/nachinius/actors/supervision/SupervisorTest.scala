package com.nachinius.actors.supervision

import akka.actor.{ActorRef, ActorSystem, Props, Terminated}
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.concurrent.duration._

class SupervisorTest(_system: ActorSystem) extends TestKit(_system)
  with ImplicitSender with FlatSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem(
    "FaultHandlingDocSpec",
    ConfigFactory.parseString(
      """
      akka {
        loggers = ["akka.testkit.TestEventListener"]
        loglevel = "DEBUG"
        log-dead-letters = 10
        log-dead-letters-during-shutdown = on
        actor {
        debug {
       receive = on
       unhandled=on
     }
      }
      }
      """)))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A supervisor" must "apply the chosen strategy for its child" in {
    // code here
    val supervisor = system.actorOf(Props[Supervisor], "supervisor")

    supervisor ! "n"
expectMsg(0)

    supervisor ! Props[Child]
    val child = expectMsgType[ActorRef] // retrieve answer from TestKit’s testActor

    child ! 12
    child ! "get"
    expectMsg(12)

    child ! new ArithmeticException()
    child ! "get"
    expectMsg(12)

    supervisor ! "n"
    expectMsg(1)

    (1 to 15).map(x => {
      child ! new ArithmeticException()
    supervisor ! "n"
      expectMsgClass(classOf[Int])
    })//.foreach(println)

    child ! new NullPointerException // crash it harder
    child ! "get"
    expectMsg(0)

    watch(child)
    child ! new IllegalArgumentException
    expectMsgPF() { case Terminated(`child`) => ()}

    supervisor ! Props[Child] // create new child
    val child2 = expectMsgType[ActorRef]
    watch(child2)
    child2 ! "get" // verify it is alive
    expectMsg(0)

    child2 ! new Exception("CRASH") // escalate failure
    expectMsgPF() {
      case t @ Terminated(`child2`) if t.existenceConfirmed => ()
    }
    val supervisor2 = system.actorOf(Props[Supervisor2], "supervisor2")

    supervisor2 ! Props[Child]
    val child3 = expectMsgType[ActorRef]

    child3 ! 23
    child3 ! "get"
    expectMsg(23)

    child3 ! new Exception("CRASH")
    child3 ! "get"
    expectMsg(0)
  }
}
