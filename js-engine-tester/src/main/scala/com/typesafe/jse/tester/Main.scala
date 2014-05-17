package com.typesafe.jse.tester

import akka.actor.ActorSystem
import akka.pattern.ask

import com.typesafe.jse.{Trireme, Rhino, CommonNode, Engine, Node}
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import java.io.File
import com.typesafe.jse.Engine.JsExecutionResult
import scala.collection.immutable

object Main {
  def main(args: Array[String]) {
    implicit val system = ActorSystem("jse-system")
    implicit val timeout = Timeout(5.seconds)

    system.scheduler.scheduleOnce(7.seconds) {
      system.shutdown()
      System.exit(1)
    }

    val engine = system.actorOf(Trireme.props(), "engine")
    val f = new File(Main.getClass.getResource("test.js").toURI)
    for (
      result <- (engine ? Engine.ExecuteJs(f, immutable.Seq("999"), timeout.duration)).mapTo[JsExecutionResult]
    ) yield {
      println(s"output\n======\n${new String(result.output.toArray, "UTF-8")}\n")
      println(s"error\n=====\n${new String(result.error.toArray, "UTF-8")}\n")

      try {
        system.shutdown()
        System.exit(0)
      } catch {
        case _: Throwable =>
      }

    }

  }
}
