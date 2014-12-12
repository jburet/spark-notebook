
import javax.servlet.ServletContext

import akka.actor.{ActorSystem, Props}
import api.web.{NotebookStatusController, RouterAPI}
import core.notebook.Notebooks
import org.scalatra.LifeCycle

class ScalatraBootstrap extends LifeCycle {

  // Get a handle to an ActorSystem and a reference to one of your actors
  val system = ActorSystem()
  val notebooks = system.actorOf(Props[Notebooks], "notebooks")

  override def init(context: ServletContext) {
    context mount(new NotebookStatusController(system, notebooks), "/async/*")
    context mount(new RouterAPI(system, notebooks), "/api-v1/*")
  }

  override def destroy(context: ServletContext) {
    system.shutdown()
  }
}