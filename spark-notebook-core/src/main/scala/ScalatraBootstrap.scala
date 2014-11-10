
import javax.servlet.ServletContext

import akka.actor.{ActorSystem, Props}
import api.web.RouterAPI
import core.notebook.Notebooks
import org.scalatra.LifeCycle

class ScalatraBootstrap extends LifeCycle {

  // Get a handle to an ActorSystem and a reference to one of your actors
  val system = ActorSystem()
  val notebooks = system.actorOf(Props[Notebooks], "notebooks")

  override def init(context: ServletContext) {
    context mount(new RouterAPI(system, notebooks), "/*")
  }

  override def destroy(context: ServletContext) {
    system.shutdown()
  }
}