package core.notebook

import java.util.UUID

import akka.actor.{Props, ActorRef, Actor}
import akka.event.LoggingReceive
import akka.util.Timeout
import core.notebook.Notebooks._
import core.storage.MemoryStorageActor


class Notebooks extends Actor {

  implicit val defaultTimeout = Timeout(10)

  var openNotebooks = Map[String, ActorRef]()
  val storage = context.actorOf(Props[MemoryStorageActor], "memory-storage")


  def receive = LoggingReceive {
    case command: List => {
      storage forward command
    }
    case Create() => {
      // Generate unique id for notebook
      val uid = UUID.randomUUID().toString
      // Store
      val nb = context.actorOf(Props(classOf[Notebook], "notebook-" + uid))
      storage !(uid, nb)
      openNotebooks += uid -> nb
      sender ! uid
    }
    case Open(id) => {
      val n = openNotebooks(id)
      sender ! n
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

  case class List()

  case class NotebookList(notebooks: Set[String])

  // Open a notebook
  case class Open(id: String)

  case class Submit(jobid: String)

  case class Create()

  case class NotebookNotFound()

  case class ListJobs()

  case class JobStatus()

}