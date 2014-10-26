package api.web

import akka.actor.Props
import spray.routing._

class Router extends HttpServiceActor {

  val notebookActor = context.actorOf(Props[NotebookV1Api], name = "notebookRouter")

  def receive = runRoute {
    path("notebooks") {
      get {
        complete("FIXME Return notebook list")
      }
    } ~
      pathPrefix("notebook") {
        ctx => notebookActor ! ctx
      }
  }
}