package core.notebook

import java.util.UUID

import akka.actor.{Props, ActorRef, Actor}
import akka.event.LoggingReceive
import akka.util.Timeout
import core.notebook.Notebooks._
import core.storage.FileStorageActor


class Notebooks extends Actor {

  implicit val defaultTimeout = Timeout(10)

  var openNotebooks = Map[String, ActorRef]()
  val storage = context.actorOf(Props[FileStorageActor], "fs-storage")
  storage ! FileStorageActor.Init("notebook")


  def receive = LoggingReceive {
    case command: FileStorageActor.List => {
      storage forward command
    }
    case CreateNotebook() => {
      // Generate unique id for notebook
      val uid = UUID.randomUUID().toString
      // Store
      val nb = context.actorOf(Props(classOf[Notebook], uid, storage))
      storage ! FileStorageActor.Create(uid)
      openNotebooks += uid -> nb
      sender ! uid
    }
    case Open(id) => {
      openNotebooks.get(id) match {
        case None => {
          // Load from storage
          val nb = context.actorOf(Props(classOf[Notebook], id, storage))
          nb ! Notebook.InitNotebook()
          openNotebooks += id -> nb
          sender ! nb

        }
        case Some(nb: ActorRef) => {
          sender ! nb
        }
      }

    }
    case Submit(id) => {
      val jobid = UUID.randomUUID().toString
      val job = context.actorOf(Job.props(jobid), "job-" + jobid)
      val n = openNotebooks(id)
      n !(job, jobid)
      sender ! jobid
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