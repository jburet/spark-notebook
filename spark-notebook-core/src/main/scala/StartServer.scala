
import api.web.{NotebookStatusController, RouterAPI}
import com.typesafe.config.ConfigFactory
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

object StartServer {
  def main(args: Array[String]) {

    val config = ConfigFactory.load("notebook")


    val port = config.getInt("server.port")

    val server = new Server(port)
    val context = new WebAppContext()
    context setContextPath "/"
    context.setResourceBase("src/main/webapp")
    context.addEventListener(new ScalatraListener)

    server.setHandler(context)

    server.start
    server.join
  }
}