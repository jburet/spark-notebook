
import api.web.{NotebookStatusController, RouterAPI}
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

object StartServer { // this is my entry object as specified in sbt project definition
def main(args: Array[String]) {
  val port = if(System.getenv("PORT") != null) System.getenv("PORT").toInt else 8080

  val server = new Server(port)
  val context = new WebAppContext()
  context setContextPath "/"
  context.setResourceBase("src/main/webapp")
  context.addEventListener(new ScalatraListener)
  context.addServlet(classOf[RouterAPI], "/")
  context.addServlet(classOf[NotebookStatusController], "/async")

  server.setHandler(context)

  server.start
  server.join
}
}