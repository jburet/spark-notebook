package core.notebook

import akka.actor.{ActorRef, Props, Actor}
import akka.event.{LoggingReceive, Logging}
import akka.util.Timeout
import core.interpreter.SparkInterpreter
import core.interpreter.SparkInterpreter.{InterpreterResult, Init}
import core.notebook.Notebook._
import core.storage.FileStorageActor

/**
 * Notebook actor.
 * Represent an open notebook.
 * - Manage storage of code
 * - Responsible of exchange with front-end
 * - Execute the code and format the result for front end
 * - Manage interpreters used by notebook
 */
class Notebook(val id: String, val storage: ActorRef) extends Actor {

  import scala.concurrent.ExecutionContext.Implicits.global

  import _root_.akka.pattern.ask

  implicit val defaultTimeout = Timeout(10)

  val log = Logging(context.system, this)
  val sint = context.actorOf(SparkInterpreter.props("notebook_" + id), "notebook_" + id)
  sint ! Init()
  var content: String = ""
  var result: String = ""
  var jobs = Map[String, ActorRef]()


  override def receive = LoggingReceive {
    case InitNotebook => {
      sint ! Init()
      // Load content
      storage ? FileStorageActor.ReadContent(id) onSuccess {
        case c: String => content = c
      }
      storage ? FileStorageActor.ReadResult(id) onSuccess {
        case c: String => result = c
      }
    }
    // Return content of notebook
    case _: GetContent => sender ! Content(content, result)
    // Save content from client
    case c: Content => {
      storage ! FileStorageActor.WriteContent(id, content)
      content = c.content
    }
    // Play current content of notebook
    case (job: ActorRef, jid: String) => {
      sint !(job, jid, content)
    }
    case ir: InterpreterResult => {
      result = ir.content
      storage ! FileStorageActor.WriteResult(id, result)
    }

    // Unknown message
    case other => log.warning("unknown_message, " + other.toString)
  }

  override def postStop() {
    context.stop(sint)
  }
}

object Notebook {
  def props(id: String, storage: ActorRef) = Props(new Notebook(id, storage))

  case class InitNotebook()

  case class GetContent()

  case class Content(content: String, result: String)

  object InterpreterStatus extends Enumeration {
    type InterpreterStatus = Value
    val Waiting, Running = Value
  }

}