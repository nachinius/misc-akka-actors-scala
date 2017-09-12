package com.nachinius.streams

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, PartitionHub, RunnableGraph, Source, _}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{Assertion, BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class MyPartitionHubTest(_system: ActorSystem) extends TestKit(_system)
  with ImplicitSender with FlatSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("MySpec"))

  behavior of "PartitionHubTest"

  it should "rundouble" in {
    val hub = new MyPartitionHub
    val (a, b, c) = hub.example

    hub.rundouble(1)(c)
    hub.rundouble(2)(c)
    val z: Future[Done] =hub.rundouble(3)(c)

    implicit val ec = _system.dispatcher
    _system.scheduler.scheduleOnce(3.second, () => {
      hub.rundouble(444)(c)
      ()
    })

    implicit val se = _system.dispatcher
    Await.ready(z,10.second)
  }

}

