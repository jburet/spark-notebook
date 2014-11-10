package core.storage

import akka.actor.{ActorRef, Actor}
import akka.event.LoggingReceive
import core.notebook.Notebooks
import core.notebook.Notebooks._
import core.notebook.Notebooks.Open



class MemoryStorageActor() extends Actor {

  var notebooks: Map[String, ActorRef] = Map()

  def receive = LoggingReceive{
    case _: Notebooks.List => {
      sender ! NotebookList(notebooks.keySet)
    }
    case (command: Open, sender: ActorRef) => {
      sender ! notebooks.get(command.id)
    }
    case((id: String, notebook: ActorRef)) => {
      notebooks += id -> notebook
    }
  }
}
