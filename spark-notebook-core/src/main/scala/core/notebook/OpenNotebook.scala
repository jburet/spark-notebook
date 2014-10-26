package core.notebook

import akka.actor.{Props, Actor}
import akka.event.Logging
import core.interpreter.SparkInterpreter
import core.interpreter.SparkInterpreter.{InterpreterResult, Init}
import core.notebook.OpenNotebook.{Result, Content, Play, StoreLive}

/**
 * Notebook actor.
 * Represent an open notebook.
 * - Manage storage of code
 * - Responsible of exchange with front-end
 * - Execute the code and format the result for front end
 * - Manage interpreters used by notebook
 */
class OpenNotebook(val id: String) extends Actor {

  val log = Logging(context.system, this)
  val sint = context.actorOf(SparkInterpreter.props("notebook_" + id), "notebook_" + id)
  sint ! Init()
  //val storage = context.actorOf(MemoryStorageActor.props(id), id)
  var content: String = "val test = \"test\""
  var lastResult: InterpreterResult = _

  override def receive = {
    case _: Content => sender ! content
    case sl: StoreLive => ???
    case _: Play => sint ! content
    case ir: InterpreterResult => lastResult = ir
    case _: Result => sender ! lastResult
    case other => log.warning("unknown_message, " + other.toString)
  }
}

object OpenNotebook {
  def props(id: String) = Props(new OpenNotebook(id))

  case class Content()

  case class StoreLive(content: String)

  case class Play()

  case class Result()

  case class Load()

  case class Create()


  object InterpreterStatus extends Enumeration {
    type InterpreterStatus = Value
    val Waiting, Running = Value
  }

}