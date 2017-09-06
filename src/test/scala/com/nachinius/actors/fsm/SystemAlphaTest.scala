package com.nachinius.actors.fsm

import akka.actor.{ActorSystem, _}
import akka.testkit.{ImplicitSender, TestKit}
import com.nachinius.actors.persistence.{Cmd, ExamplePersistentActorTest}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

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
      expectMsg("got 2")
      expectMsg("3 o 4")
      alpha ! "4"
      expectMsg("24")
      alpha ! "where"
      expectMsg("nowhere")
    }

  }

}
