package api.web

import akka.actor._
import akka.event.LoggingReceive
import core.notebook.Job.JobComplete
import core.notebook.Notebook
import core.notebook.Notebook.{UnregisterEvent, RegisterEvent}
import org.json4s.JsonAST.{JString, JObject}
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.atmosphere._
import org.scalatra.scalate.ScalateSupport
import org.scalatra.{SessionSupport, ScalatraServlet}
import org.scalatra.json.{JacksonJsonSupport, JValueResult}
import org.slf4j.LoggerFactory

class NotebookStatusController(actorSystem: ActorSystem, notebooks: ActorRef) extends ScalatraServlet
with ScalateSupport with JValueResult
with JacksonJsonSupport with SessionSupport
with AtmosphereSupport {

  val logger = LoggerFactory.getLogger(getClass)

  protected implicit val jsonFormats: Formats = DefaultFormats

  import scala.concurrent.ExecutionContext.Implicits.global

  var clientNotebook = Map[String, String]()


  before() {
    contentType = formats("json")
  }

  atmosphere("/notebook-status") {
    new AtmosphereClient {

      class BridgeActor extends Actor {
        def receive = LoggingReceive {
          case jc: JobComplete => broadcast(JObject(List(("type", JString("job_complete")))), Everyone)
          case rm: (String, RegisterEvent) => notebooks ! rm
          case ur: (String, UnregisterEvent) => notebooks ! ur
          case other => logger.warn("unknown_message, {}", other)
        }
      }

      var bridge: ActorRef = _


      def receive = {
        case Connected => {
          logger.debug("client_async_connect, {}", uuid)
          bridge = actorSystem.actorOf(Props(new BridgeActor()), uuid)
        }
        case Disconnected(disconnector, Some(error)) => {
          logger.debug("disconnect_with_error, {}, {}", Array(uuid, error))
          bridge !(clientNotebook(uuid), Notebook.UnregisterEvent())
          clientNotebook -= uuid
          bridge ! PoisonPill
        }
        case Disconnected(disconnector, None) => {
          logger.debug("disconnect, {}", uuid)
          bridge !(clientNotebook(uuid), Notebook.UnregisterEvent())
          clientNotebook -= uuid
          bridge ! PoisonPill
        }
        case Error(Some(error)) => logger.info("disconnect_on_error, {}", error)
        case TextMessage(text) => logger.debug("unknown_text_msg, {}", text)
        case JsonMessage(JObject(List(("type", JString("register_notebook")), ("id", JString(notebookId))))) => {
          // Register for notebook event
          logger.info("register_notebook_event, {}", uuid)
          clientNotebook += uuid -> notebookId
          bridge !(notebookId, Notebook.RegisterEvent())
        }
        case JsonMessage(json) => logger.warn("unknown_atmo_message, {}", json)
        case other => logger.warn("unknown, {}", other)
      }


    }
  }


}