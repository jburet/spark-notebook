package core.notebook

import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import core.notebook.Notebook.Content
import org.scalatest.{Matchers, FlatSpec}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class TestNotebook extends FlatSpec with Matchers {

  implicit val system = ActorSystem("test-notebook")
  implicit val timeout = Timeout(5 seconds)

  import scala.concurrent.ExecutionContext.Implicits.global


  "a notebook" should "save content" in {
    val nb = TestActorRef(new Notebook("test-notebook", null))
    nb ! Content("test content", "")
    nb.underlyingActor.content should be("test content")
  }

  "a notebook" should "return content " in {
    val nb = TestActorRef(new Notebook("test-notebook", null))
    nb.underlyingActor.content = "test content"
    nb ? Notebook.GetContent() onComplete {
      case Success(content: Content) => content.content should be("test content")
      case Failure(_) => fail("return failure")
    }

  }
}
