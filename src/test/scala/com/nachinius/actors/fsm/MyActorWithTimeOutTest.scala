package com.nachinius.actors.fsm

import akka.actor.{ActorSystem, _}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class MyActorWithTimeOutTest() extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "An Echo actor" must {

    "walk the path" in {
      val echo = system.actorOf(Props[MyActorWithTimeOut])
      echo ! "Hello"
      echo ! "continue"
      expectMsg("receive continue")
      echo ! "last"
      expectMsg("receive last")
    }
    "back to start when no message" in {
      val echo = system.actorOf(Props[MyActorWithTimeOut])
      echo ! "Hello"
      expectNoMsg(100 milliseconds)
      expectMsg("back to start")
    }
    "walk the path twice" in {
      val echo = system.actorOf(Props[MyActorWithTimeOut])
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