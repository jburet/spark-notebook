package core.interpreter

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit, TestActorRef}
import core.interpreter.SparkInterpreter.{InterpreterResult, Init}
import core.notebook.Job
import core.notebook.Job.JobComplete
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FlatSpecLike, Matchers}
import akka.util.Timeout
import scala.concurrent.duration._

class TestSparkInterpreter(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with FlatSpecLike
with Matchers with BeforeAndAfter with BeforeAndAfterAll {

  var sint: ActorRef = _

  implicit val timeout = Timeout(10 seconds)

  def this() = this(ActorSystem("MySpec"))

  before {
    sint = system.actorOf(SparkInterpreter.props("notebook-1"))
  }

  after {
    system.stop(sint)
  }

  override def afterAll: Unit = {
    system.shutdown()
  }

  "an interpreter" should "execute simple code" in {
    sint ! Init()
    sint !(TestActorRef(new Job("1", None)), "1", "val out = \"test\"")
    expectMsg(15 second, InterpreterResult("out: String = test\n"))
  }

  "an interpreter with compile error" should "report error" in {
    sint ! Init()
    val job1 = TestActorRef(new Job("1", None))
    sint !(job1, "1", "val out = error_compile")
    expectMsgType[InterpreterResult](15 second).content should include("not found: value error_compile")

  }


  "an interpreter" should "execute a simple spark job" in {
    sint ! Init()
    sint !(TestActorRef(new Job("1", None)), "1", "case class Person(name:String, age:Int)")
    sint !(TestActorRef(new Job("2", None)), "2", "val people = sc.parallelize(Seq(Person(\"moon\", 33), Person(\"jobs\", 51), Person(\"gates\", 51), Person(\"park\", 34)))")
    expectMsgType[InterpreterResult](15 second)
    expectMsgType[InterpreterResult](15 second).content should include("people: org.apache.spark.rdd.RDD[Person]")
  }

}

