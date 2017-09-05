package com.nachinius.free

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.nachinius.free.KeyValue.{ActorState, actorCompiler, program}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers, WordSpecLike}

class KeyValueTest  extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "actor compilor" must {
    "run" in {
      val a= new ActorState(system)
      val p=program.foldMap(actorCompiler).run(a).value
      assert(p._2==Some(14))
    }
  }

  "impure future compiler" must{
    "work with futures" in {
      import system.dispatcher
      import cats.instances.future._

      val p=program.foldMap(KeyValue.impureFutureCompiler)
      p.map(x=>assert(x==Some(14)))
    }
  }
}
