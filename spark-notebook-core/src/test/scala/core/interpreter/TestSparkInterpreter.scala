package core.interpreter

import java.io.{ByteArrayOutputStream}

import akka.actor.ActorSystem
import akka.actor.Status.Success
import akka.testkit.TestActorRef
import org.scalatest.{Matchers, FlatSpec}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

class TestSparkInterpreter extends FlatSpec with Matchers {

  implicit val system = ActorSystem("on-spray-can")
  implicit val timeout = Timeout(5 seconds)

  "an interpreter" should "execute simple code" in {
    val sint = TestActorRef(new SparkInterpreter("notebook"))
    val future = sint ? "val out = \"test\""
    val result = future.value.get.get
    result should be("out: String = test\n")
  }

  "an interpreter with compile error" should "report error" in {
    val sint = TestActorRef(new SparkInterpreter("notebook"))
    val future = sint ? "val out = error_compile"
    val result:String = future.value.get.get.asInstanceOf[String]
    result should include ("not found: value error_compile")
  }


  "an interpreter" should "execute a simple spark job" in {
    val sint = TestActorRef(new SparkInterpreter("notebook"))
    sint ? ("case class Person(name:String, age:Int)");
    sint ? ("val people = sc.parallelize(Seq(Person(\"moon\", 33), Person(\"jobs\", 51), Person(\"gates\", 51), Person(\"park\", 34)))")
  }

  "two interpreter" should "execute some spark job" in {
    val sint1 = TestActorRef(new SparkInterpreter("notebook-1"))
    val sint2 = TestActorRef(new SparkInterpreter("notebook-2"))
    sint1 ? ("case class Person(name:String, age:Int)");
    sint2 ? ("case class Person(name:String, age:Int)");
    sint1 ? ("val people = sc.parallelize(Seq(Person(\"moon\", 33), Person(\"jobs\", 51), Person(\"gates\", 51), Person(\"park\", 34)))")
    sint2 ? ("val people2 = sc.parallelize(Seq(Person(\"moon\", 10), Person(\"jobs\", 11), Person(\"gates\", 12), Person(\"park\", 13)))")
    sint1 ? ("println(people2)")
  }
}

