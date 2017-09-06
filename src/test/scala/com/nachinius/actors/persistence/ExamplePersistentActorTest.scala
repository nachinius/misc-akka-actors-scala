package com.nachinius.actors.persistence

import org.scalatest.WordSpecLike
import akka.actor.{ActorSystem, _}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._

class ExamplePersistentActorTest extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "persistence" must {
    "persistsomething" in {
      val ref = system.actorOf(Props[ExamplePersistentActor])
      (1 to 10).foreach(x => ref ! Cmd(s"$x"))
      //      ref ! "print"
      //      ref ! "def"
      ref ! "print"
      expectMsg("aslish")
//      receiveN(1)
//      ref ! "def"
//      val ref2 = system.actorOf(Props[ExamplePersistentActor])
//      ref2 ! "0"
//      ref2 ! "print"
    }
  }


}
