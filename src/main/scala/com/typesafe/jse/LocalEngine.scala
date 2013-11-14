package com.typesafe.jse

import akka.actor._
import scala.collection.mutable.ListBuffer
import com.typesafe.jse.Engine.ExecuteJs
import akka.contrib.process.Process
import akka.contrib.process.Process.Started

/**
 * Provides an Actor on behalf of a JavaScript Engine. Engines are represented as operating system processes and are
 * communicated with by launching with arguments and returning a status code.
 * @param stdArgs a sequence of standard command line arguments used to launch the engine from the command line.
 */
class LocalEngine(stdArgs: Seq[String]) extends Engine {

  expectOnce {
    case ExecuteJs(f, args, timeout) =>
      val requester = sender
      val lb = ListBuffer[String]()
      lb ++= stdArgs
      lb += f.getCanonicalPath
      lb ++= args
      context.actorOf(Process.props(lb, self)(context.system))
      expectOnce {
        case Started(i, o, e) => new EngineIOHandler(o, e, requester, timeout)
      }
  }
}

/**
 * Used to manage a local instance of Node.js with CommonJs support. common-node is assumed to be on the path.
 */
object CommonNode {
  def props()(implicit system: ActorSystem): Props = {
    val args = Seq("common-node")
    Props(classOf[LocalEngine], args)
  }
}

/**
 * Used to manage a local instance of Node.js. Node is assumed to be on the path.
 */
object Node {
  def props()(implicit system: ActorSystem): Props = {
    val args = Seq("node")
    Props(classOf[LocalEngine], args)
  }
}

/**
 * Used to manage a local instance of PhantomJS. PhantomJS is assumed to be on the path.
 */
object PhantomJs {
  def props()(implicit system: ActorSystem): Props = {
    val args = Seq("phantomjs")
    Props(classOf[LocalEngine], args)
  }
}
