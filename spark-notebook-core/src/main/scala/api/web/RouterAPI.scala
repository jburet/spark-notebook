package api.web

import _root_.akka.actor.{ActorRef, ActorSystem}
import _root_.akka.util.Timeout
import core.notebook.Notebook.{Content}
import core.notebook.Notebooks.{JobStatus, ListJobs}
import core.notebook.{Notebook, Notebooks}
import org.json4s.{Formats, DefaultFormats}
import org.scalatra._
import org.scalatra.json._

import scala.concurrent.{Promise, Future, ExecutionContext}
import scala.util.{Try, Success, Failure}

class RouterAPI(actorSystem: ActorSystem, notebooks: ActorRef) extends ScalatraServlet with CorsSupport with FutureSupport with JacksonJsonSupport {

  protected implicit def executor: ExecutionContext = actorSystem.dispatcher

  import _root_.akka.pattern.ask

  implicit val defaultTimeout = Timeout(10)

  protected implicit val jsonFormats: Formats = DefaultFormats

  before() {
    contentType = formats("json")
  }

  options("/*") {
    response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"));
  }

  // Get notebook list
  get("/notebooks") {
    new AsyncResult {
      val is = notebooks ? Notebooks.List()
    }
  }

  // notebook action
  get("/notebook/:id") {
    // FIXME manage timeout

    new AsyncResult() {
      val prom = Promise[Content]()
      notebooks ? Notebooks.Open(params("id")) onComplete {
        case Success(notebook: ActorRef) => {
          notebook ? Notebook.GetContent() onComplete {
            case Success(content: Content) => prom.complete(Try(content))
            case Failure(ex) => prom.failure(ex)
          }
        }
        case Failure(ex) => prom.failure(ex)
      }
      val is = prom.future
    }


  }

  put("/notebook/:id") {
    val content = parsedBody.extract[Content]
    notebooks ? Notebooks.Open(params("id")) onComplete {
      case Success(notebook: ActorRef) => {
        notebook ! content
        Accepted()
      }
      case Failure(ex) => halt(500, ex.getMessage)
    }

  }

  post("/notebook") {
    new AsyncResult {
      val is = notebooks ask Notebooks.Create()
    }

  }

  // notebook action
  post("/notebook/:id/job") {
    new AsyncResult() {
      val prom = Promise[String]()
      notebooks ? Notebooks.Submit(params("id")) onComplete {
        case Success(jid: String) => prom.success(jid)
        case Failure(ex) => prom.failure(ex)
      }
      val is = prom.future
    }
  }

  // notebook action
  get("/jobs") {
    new AsyncResult() {
      val prom = Promise[Seq[String]]()
      notebooks ? ListJobs() onComplete {
        case Success(seqs: Seq[String]) => prom.success(seqs)
        case Failure(ex) => prom.failure(ex)
      }
      val is = prom.future
    }
  }

  // notebook action
  get("/job/:jid") {
    new AsyncResult() {
      val prom = Promise[JobStatus]()
      notebooks ? ListJobs() onComplete {
        case Success(status: JobStatus) => prom.success(status)
        case Failure(ex) => prom.failure(ex)
      }
      val is = prom.future
    }
  }
}