package core.notebook

import akka.actor.{ActorLogging, ActorRef, Props, Actor}
import akka.event.{LoggingReceive, Logging}
import akka.util.Timeout
import core.interpreter.SparkInterpreter
import core.interpreter.SparkInterpreter.{InterpreterResult, Init}
import core.notebook.Job.JobComplete
import core.notebook.NotebookActor._
import core.storage.FileStorageActor._
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
 * Notebook actor.
 * Represent an open notebook.
 * - Manage storage of code
 * - Responsible of exchange with front-end
 * - Execute the code and format the result for front end
 * - Manage interpreters used by notebook
 */
class NotebookActor(val id: String, val storage: ActorRef) extends Actor with ActorLogging {


  import _root_.akka.pattern.ask

  implicit val defaultTimeout = Timeout(3600 second)

  import scala.concurrent.ExecutionContext.Implicits.global

  val sint = context.actorOf(SparkInterpreter.props("notebook_" + id), "interpreter_" + id)
  var jobs = Map[String, ActorRef]()
  var connectedClient = Set[ActorRef]()


  override def receive = LoggingReceive {
    case _: InitNotebook => {
      sint ! Init()
    }
    // Return content of notebook
    case ra: ReadAll => {
      storage forward ra
    }
    // Save content from client
    case c: WriteContent => {
      storage ! c
    }
    // Play current content of notebook
    case (job: ActorRef, jid: String) => {
      storage ? ReadContent(id) onComplete {
        case Success(content: List[String]) => {
          sint ?(job, jid, content) onSuccess {
            case res: List[InterpreterResult] => {
              storage ! WriteStdout(id, res.map(ir => ir.stdout).toArray)
              storage ! WriteResult(id, res.map(ir => ir.jsonContent).toArray)
            }
          }
        }
      }
    }
    case ir: InterpreterResult => {
      storage ! WriteStdout(id, Array(ir.stdout))
    }
    case irs: Array[InterpreterResult] => {
      storage ! WriteStdout(id, irs.map(i => i.stdout))
    }
    case _: RegisterEvent => {
      connectedClient += sender
    }
    case _: UnregisterEvent => {
      connectedClient -= sender
    }
    case jc: JobComplete => {
      connectedClient.foreach { cc =>
        cc ! jc
      }
    }

    // Unknown message
    case other => log.warning("unknown_message, {}, from, {} ", Array(other.toString, sender))
  }

  override def postStop() {
    context.stop(sint)
  }
}

object NotebookActor {
  def props(id: String, storage: ActorRef) = Props(new NotebookActor(id, storage))

  case class InitNotebook()

  case class RegisterEvent()

  case class UnregisterEvent()

  object InterpreterStatus extends Enumeration {
    type InterpreterStatus = Value
    val Waiting, Running = Value
  }

}