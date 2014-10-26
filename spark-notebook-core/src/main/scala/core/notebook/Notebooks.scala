package core.notebook

import akka.actor.{ActorRef, Actor}
import core.notebook.Notebooks.{Open, Create}

class Notebooks extends Actor {

  val notebooks = scala.collection.mutable.Map[String, Option[ActorRef]]()

  def receive = {
    case List() => {

    }
    case Create() => {
      notebooks += "test" -> Some(context.actorOf(OpenNotebook.props("test")))
      sender ! notebooks("test")
    }
    case Open(id: String) => {
      notebooks.get("test") match {
        case Some(notebook) => sender ! notebook
        case None => {
          notebooks += "test" -> Some(context.actorOf(OpenNotebook.props("test")))
          sender ! notebooks("test")
        }
      }
    }
  }


}

object Notebooks {

  case class List()

  case class Open(id: String)

  case class Create()

}