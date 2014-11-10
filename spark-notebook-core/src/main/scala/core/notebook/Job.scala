package core.notebook

import akka.actor.{ActorRef, Props, Actor}
import akka.event.{LoggingReceive, Logging}
import core.interpreter.SparkInterpreter.InterpreterResult
import core.notebook.Job.{JobSuccess, RegisterForComplete}


class Job(id: String) extends Actor {
  val log = Logging(context.system, this)

  var callback: Option[ActorRef] = None

  override def receive = LoggingReceive{
    case _: RegisterForComplete => callback = Some(sender)
    case js: JobSuccess => callback match {
      case Some(actor) => actor ! js
      case None =>
    }
    case other => println(other)
  }
}

object Job {
  def props(id: String) = Props(new Job(id))

  case class RegisterForComplete()

  case class JobSuccess(jobId: String)

}