package core.notebook

import akka.actor.{ActorRef, Props, Actor}
import akka.event.{LoggingReceive, Logging}
import core.notebook.Job.{JobComplete}


class Job(id: String, nb: Option[ActorRef]) extends Actor {
  val log = Logging(context.system, this)

  override def receive = LoggingReceive {
    case jc: JobComplete => nb match {
      case None =>
      case Some(ar: ActorRef) => ar ! jc
    }

    case other => log.warning("unknonw_message", other)
  }
}

object Job {
  def props(id: String, nb: Option[ActorRef]) = Props(new Job(id, nb))

  case class JobComplete(jobId: String)

}