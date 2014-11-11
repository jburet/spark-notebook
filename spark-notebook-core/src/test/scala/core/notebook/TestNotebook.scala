package core.notebook

import akka.actor.ActorSystem
import org.scalatest.{Matchers, FlatSpec}
import akka.util.Timeout
import scala.concurrent.duration._
class TestNotebook extends FlatSpec with Matchers {

  implicit val system = ActorSystem("test-notebook")
  implicit val timeout = Timeout(5 seconds)

  import scala.concurrent.ExecutionContext.Implicits.global


}
