package core.notebook

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, ImplicitSender, TestKit}
import core.interpreter.SparkInterpreter.InterpreterResult
import core.notebook.Job.{JobSuccess, RegisterForComplete}
import org.scalatest.{Matchers, FlatSpecLike}
import akka.util.Timeout
import scala.concurrent.duration._

class TestJob(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with FlatSpecLike with Matchers {

  implicit val timeout = Timeout(10 seconds)

  def this() = this(ActorSystem("MySpec"))

  "a job" should "accept other actor for send callback" in {
    val job = TestActorRef(new Job("test-job"))
    job ! RegisterForComplete()
    job.underlyingActor.callback.get should be(this.testActor)
  }

  "a job" should "transfer interpreter result to callback if exist" in {
    val job = TestActorRef(new Job("test-job"))
    job ! RegisterForComplete()
    job ! JobSuccess("test-job")
    expectMsg(JobSuccess("test-job"))
  }
}
