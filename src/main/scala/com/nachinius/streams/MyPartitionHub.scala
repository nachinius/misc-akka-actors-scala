package com.nachinius.streams
// after akka documentation on partition hub

class MyPartitionHub {
  import akka.stream.scaladsl._

  import scala.concurrent.duration._
  import akka.actor.ActorSystem
  import akka.NotUsed
  import akka.stream.ActorMaterializer

  implicit val system=ActorSystem()
  implicit val mat=ActorMaterializer()
  implicit val dispatcher=system.dispatcher
  // A simple producer that publishes a new "message-" every second

  def example = {
    val producer = Source.tick(1.second, 1.second, "message")
      .zipWith(Source(1 to 10))((a, b) => s"$a-$b")

    // Attach a PartitionHub Sink to the producer. This will materialize to a
    // corresponding Source.
    // (We need to use toMat and Keep.right since by default the materialized
    // value to the left is used)
    val runnableGraph: RunnableGraph[Source[String, NotUsed]] =
    producer.toMat(PartitionHub.sink(
      (size, elem) => math.abs(elem.hashCode) % size,
      startAfterNrOfConsumers = 2, bufferSize = 256))(Keep.right)

    // By running/materializing the producer, we get back a Source, which
    // gives us access to the elements published by the producer.
    val fromProducer: Source[String, NotUsed] = runnableGraph.run()

    (producer, runnableGraph, fromProducer)
  }
  def rundouble(i:Int)(fromproducer: Source[String, NotUsed]) ={
    fromproducer.runForeach(msg => println(s"consumer $i" + msg))
  }

}
