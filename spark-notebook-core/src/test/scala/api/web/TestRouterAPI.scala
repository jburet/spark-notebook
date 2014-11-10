package api.web


import akka.actor.{Props, ActorSystem}
import core.notebook.Notebooks
import org.scalatra.test.scalatest._
import org.scalatest.FunSuiteLike


class TestRouterAPI extends ScalatraSuite with FunSuiteLike {
  val system = ActorSystem()
  val notebooks = system.actorOf(Props[Notebooks])
  addServlet(new RouterAPI(system, notebooks), "/*")

  test("list empty notebook list") {
    get("/notebooks") {
      status should equal (200)
      println(body)
    }
  }
}