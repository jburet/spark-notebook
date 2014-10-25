package api.web

import spray.routing._

class Router extends HttpServiceActor {
  def receive = runRoute {
    path("notebooks") {
      get {
        complete("FIXME Return notebook list")
      }
    } ~
      path("notebook") {
        post {
          complete("FIXME Create a notebook")
        }
      } ~
      path("notebook" / IntNumber) { id =>
        get {
          complete("FIXME return notebook")
        } ~
          put {
            complete("FIXME save notebook")
          } ~
          delete {
            complete("FIXME delete notebook")
          }
      }
  }
}