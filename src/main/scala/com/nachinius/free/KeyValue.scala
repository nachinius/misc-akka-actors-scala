/************************************************************
  * Using free monads simplest example with different compilers:
  *   - With mutable state (provided by the tutorial)
  *   - With immutable state (provided by the tutorial)
  *   - With akka actors without returning values
  *   - With akka actors returning futures
  */
package com.nachinius.free

import akka.actor.{Actor, ActorSystem, Props}
import akka.util.Timeout
import cats.data.State
import cats.free.Free
import cats.free.Free.liftF
import cats.{Id, ~>}

import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}


object KeyValue {

  sealed trait KVStoreADT[A]

  case class Put[T](key: String, value: T) extends KVStoreADT[Unit]

  case class Get[T](key: String) extends KVStoreADT[Option[T]]

  case class Delete(key: String) extends KVStoreADT[Unit]

  type KVStore[A] = Free[KVStoreADT, A]

  // Put returns nothing (i.e. Unit).
  def put[T](key: String, value: T): KVStore[Unit] =
    liftF[KVStoreADT, Unit](Put[T](key, value))

  // Delete returns nothing (i.e. Unit).
  def delete(key: String): KVStore[Unit] =
    liftF(Delete(key))

  // Update composes get and set, and returns nothing.
  def update[T](key: String, f: T => T): KVStore[Unit] =
    for {
      vMaybe <- get[T](key)
      _ <- vMaybe.map(v => put[T](key, f(v))).getOrElse(Free.pure(()))
    } yield ()

  // Get returns a T value.
  def get[T](key: String): KVStore[Option[T]] =
    liftF[KVStoreADT, Option[T]](Get[T](key))

  def program: KVStore[Option[Int]] =
    for {
      _ <- put("wild-cats", 2)
      _ <- update[Int]("wild-cats", (_ + 12))
      _ <- put("tame-cats", 5)
      n <- get[Int]("wild-cats")
      _ <- delete("tame-cats")
    } yield n

  type KVStoreState[A] = State[Map[String, Any], A]
  val pureCompiler: KVStoreADT ~> KVStoreState = new (KVStoreADT ~> KVStoreState) {
    def apply[A](fa: KVStoreADT[A]): KVStoreState[A] =
    fa match {
      case Put(key, value) => State.modify(_.updated(key, value))
      case Get(key) =>
        State.inspect(_.get(key).map(_.asInstanceOf[A]))
      case Delete(key) => State.modify(_ - key)
    }
  }

  type KVStoreActor[A] = State[ActorState, A]
  val actorCompiler: KVStoreADT ~> KVStoreActor = new (KVStoreADT ~> KVStoreActor) {
    override def apply[A](fa: KVStoreADT[A]): KVStoreActor[A] = fa match {
      case Put(key, value) =>
        State.modify({ s: ActorState =>
          s.tell((key, Some(value)))
          s
        })
      case Get(key) =>
        val f = (state: ActorState) => (state, {
          val a = Await.result(state.ask2(key), 1 second).asInstanceOf[A]
          Some(a)
        })
        State(f)
      case Delete(key) =>
        State.modify({ s: ActorState =>
          s.tell((key, None))
          s
        })
    }
  }

  // the program will crash if a key is not found,
  // or if a type is incorrectly specified.
  def impureCompiler: KVStoreADT ~> Id = new (KVStoreADT ~> Id) {
    // a very simple (and imprecise) key-value store
    val kvs = mutable.Map.empty[String, Any]

    def apply[A](fa: KVStoreADT[A]): Id[A] =
      fa match {
        case Put(key, value) =>
          kvs(key) = value
          ()
        case Get(key) =>
          kvs.get(key).map(_.asInstanceOf[A])
        case Delete(key) =>
          kvs.remove(key)
          ()
      }
  }

  def impureFutureCompiler(implicit ec: ExecutionContext): KVStoreADT ~> Future = new (KVStoreADT ~> Future) {
    // a very simple (and imprecise) key-value store
    val kvs = mutable.Map.empty[String, Any]

    def apply[A](fa: KVStoreADT[A]): Future[A] =
      fa match {
        case Put(key, value) =>
          kvs(key) = value
          Future {
            ()
          }
        case Get(key) =>
          Future {
            (kvs.get(key).map(_.asInstanceOf[A]))
          }
        case Delete(key) =>
          kvs.remove(key)
          Future {
            ()
          }
      }
  }

  def actorFutureCompiler(actorFutureState: ActorFutureState): KVStoreADT ~> Future = new (KVStoreADT ~> Future) {
    implicit val context = actorFutureState.system.dispatcher
    override def apply[A](fa: KVStoreADT[A]): Future[A] = fa match {
      case Put(key, value) =>
        actorFutureState.ask2((key, Some(value))).map(_ => ())

      case Get(key) =>
        actorFutureState.ask2(key).asInstanceOf[Future[A]]

      case Delete(key) =>
        actorFutureState.ask2((key, None)).map(_ => ())
    }
  }




  case class ActorState(system: ActorSystem) {
    //    val system = ActorSystem()
    val ref = system.actorOf(Props(new KeyValueActor))

    import akka.pattern.ask

    def terminate() = system.terminate()
    implicit val timeout = Timeout(5 seconds) // needed for `?` below

    def tell(x: Any): Unit =
      ref ! x

    def ask2(x: Any): Future[Any] = {
      ask(ref, x)
    }

    class KeyValueActor extends Actor {
      var state = Map[String, Any]()

      override def receive: Receive = {
        case (k: String) =>
          sender() ! state(k)
        case (k: String, None) =>
          state = state - k
        case (k: String, Some(v)) =>
          state = state.updated(k, v)
      }
    }

  }

  case class ActorFutureState(system: ActorSystem) {
    //    val system = ActorSystem()
    val ref = system.actorOf(Props(new KeyValueActor))

    def terminate() = system.terminate()

    import akka.pattern.ask

    def tell(x: Any): Unit =
      ref ! x
    implicit val timeout = Timeout(5 seconds) // needed for `?` below

    def ask2[A](x: A): Future[Any] = {
      ask(ref, x)
    }

    case class Done()

    class KeyValueActor extends Actor {
      var state = Map[String, Any]()

      override def receive: Receive = {
        case (k: String) =>

          sender() ! state.get(k)
        case (k: String, None) =>
          state = state - k
          sender() ! Done
        case (k: String, Some(v)) =>

          state = state.updated(k, v)
          sender() ! Done

      }
    }

  }


}