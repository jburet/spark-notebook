package api.web

import akka.actor.{ActorRef, Props, Actor}
import akka.pattern.{ask}
import akka.util.Timeout
import core.interpreter.SparkInterpreter.InterpreterResult
import core.notebook.Notebooks.Open
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import core.notebook.{OpenNotebook, Notebooks}
import spray.routing.HttpService
import scala.util.{Failure, Success}
import ExecutionContext.Implicits.global

class NotebookV1Api extends Actor with NotebookService {

  def actorRefFactory = context

  def receive = runRoute(notebookRoute)
}

trait NotebookService extends HttpService {

  val notebooks = actorRefFactory.actorOf(Props[Notebooks])
  implicit val timeout = Timeout(15 seconds)
  val notebookRoute =
    post {
      complete("notebook created")
    } ~
      pathPrefix(Segment) { id =>
        pathEnd {
          get {
            onComplete(notebooks ? Open(id)) {
              case Success(Some(notebook: ActorRef)) => {
                onComplete(notebook ? OpenNotebook.Content()) {
                  case Success(content: String) => complete(content)
                  case Failure(t) => complete(t)
                  case other => complete("notebook_content, receive other thing: " + other.toString)
                }
              }
              case Failure(t) => complete(t)
              case other => complete("notebook_open, receive other thing: " + other.toString)
            }
          } ~
            put {
              complete("put")
            } ~
            post {
              complete("post")
            }
        } ~
          path("play") {
            get {
              onComplete(notebooks ? Open(id)) {
                case Success(Some(notebook: ActorRef)) => {
                  notebook ! OpenNotebook.Play()
                  complete("notebook played")
                }
                case Failure(t) => complete(t)
                case other => complete("notebook_open, receive other thing: " + other.toString)
              }
            }
          } ~
          path("result") {
            get {
              onComplete(notebooks ? Open(id)) {
                case Success(Some(notebook: ActorRef)) => {
                  onComplete(notebook ? OpenNotebook.Result()) {
                    case Success(ir: InterpreterResult) => complete(ir.toString())
                    case Failure(t) => complete(t)
                    case other => complete("notebook_result, receive other thing: " + other.toString)
                  }

                }
                case Failure(t) => complete(t)
                case other => complete("notebook_open, receive other thing: " + other.toString)
              }
            }
          }
      }
}