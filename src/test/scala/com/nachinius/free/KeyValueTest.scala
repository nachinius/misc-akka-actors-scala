package com.nachinius.free

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.nachinius.free.KeyValue.{ActorState, actorCompiler, program, _}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers, WordSpecLike}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationLong

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

      val p: Future[Option[Int]] =program.foldMap(KeyValue.impureFutureCompiler)
      assert(Await.result(p,1 second)==Some(14))
    }
  }

  "actor future compiler" must{
    "work with futures" in {
      import system.dispatcher
      import cats.instances.future._

      val compiler = actorFutureCompiler(ActorFutureState(system))
      val p=program.foldMap(compiler)
      assert(Await.result(p,1 second)==Some(14))

    }
  }
}
