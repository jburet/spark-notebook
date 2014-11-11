package core.notebook

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, ImplicitSender, TestKit}
import core.notebook.Job.JobComplete
import org.scalatest.{Matchers, FlatSpecLike}
import akka.util.Timeout
import scala.concurrent.duration._

class TestJob(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with FlatSpecLike with Matchers {

  implicit val timeout = Timeout(10 seconds)

  def this() = this(ActorSystem("MySpec"))

}
