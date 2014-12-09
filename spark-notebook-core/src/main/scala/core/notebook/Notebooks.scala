package core.notebook

import java.util.UUID

import akka.actor.{ActorLogging, Props, ActorRef, Actor}
import akka.event.LoggingReceive
import akka.util.Timeout
import core.notebook.Notebooks._
import core.storage.FileStorageActor
import scala.concurrent.duration._


class Notebooks extends Actor with ActorLogging {

  import _root_.akka.pattern.ask

  implicit val defaultTimeout = Timeout(3600 second)

  import scala.concurrent.ExecutionContext.Implicits.global

  var openNotebooks = Map[String, ActorRef]()
  val storage = context.actorOf(Props[FileStorageActor], "fs-storage")
  storage ! FileStorageActor.Init("notebook")


  def receive = LoggingReceive {
    case command: FileStorageActor.ListFile => {
      storage forward command
    }
    case CreateNotebook() => {
      // Generate unique id for notebook
      val uid = UUID.randomUUID().toString
      // Store
      val nb = context.actorOf(Props(classOf[NotebookActor], uid, storage), "notebook_"+uid)
      storage ! FileStorageActor.Create(uid)
      openNotebooks += uid -> nb
      nb ! NotebookActor.InitNotebook()
      sender ! uid
    }
    case Open(id) => {
      openNotebooks.get(id) match {
        case None => {
          // Load from storage
          val nb = context.actorOf(Props(classOf[NotebookActor], id, storage), "notebook_"+id)
          openNotebooks += id -> nb
          nb ! NotebookActor.InitNotebook()
          sender ! nb
        }
        case Some(nb: ActorRef) => {
          sender ! nb
        }
      }

    }
    case Submit(id) => {
      val jobid = UUID.randomUUID().toString

      val n = openNotebooks(id)
      val job = context.actorOf(Job.props(jobid, Some(n)), "job-" + jobid)
      n !(job, jobid)
      sender ! jobid
    }

    case (notebookId: String, register: NotebookActor.RegisterEvent) => {
      openNotebooks(notebookId) forward register
    }
    case (notebookId: String, ur: NotebookActor.UnregisterEvent) => {
      openNotebooks(notebookId) forward ur
    }
  }


}

object Notebooks {


  case class CreateNotebook()

  case class NotebookNotFound()

  case class Open(id: String)

  case class Submit(jobid: String)

  case class ListJobs()

  case class JobStatus()

}