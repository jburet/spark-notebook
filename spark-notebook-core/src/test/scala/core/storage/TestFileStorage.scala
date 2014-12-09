package core.storage

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{TestActorRef, ImplicitSender, TestKit}
import akka.util.Timeout
import core.interpreter.SparkInterpreter.InterpreterResult
import core.storage.FileStorageActor._
import scala.concurrent.duration._
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, FlatSpecLike}

class TestFileStorage(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with FlatSpecLike with Matchers
with BeforeAndAfterAll with BeforeAndAfter {

  implicit val timeout = Timeout(10 seconds)

  def this() = this(ActorSystem("MySpec"))

  val baseDir = "notebook-storage-unit-test"

  def delete(file: File) {
    if (file.isDirectory)
      Option(file.listFiles).map(_.toList).getOrElse(Nil).foreach(delete(_))
    file.delete
  }

  override def afterAll: Unit = {
    system.shutdown()
  }

  var storage: ActorRef = _

  before {
    storage = TestActorRef[FileStorageActor]
    storage ! Init(baseDir)
  }

  after {
    system.stop(storage)
    delete(Paths.get(baseDir).toFile)
  }

  "add new notebook" should "create new directory" in {
    storage ! Create("test")
    // The directory for test should be create
    val test = Paths.get(baseDir, "test")
    test.toFile.exists() should be(true)
    test.toFile.isDirectory should be(true)
  }

  "add content in empty notebook" should "create new content file" in {
    val testContent = "val test = \"test\"\nsc"
    storage ! Create("test")
    storage ! WriteContent("test", Array(testContent))
    val test = Paths.get(baseDir, "test", "content.scala")
    test.toFile.exists() should be(true)
    test.toFile.isFile should be(true)
    val source = scala.io.Source.fromFile(test.toAbsolutePath.toString)

    val lines = source.mkString
    // header + first sep
    source.reset().getLines().length should be(4)
    source.close()
    lines should include(testContent)

  }

  "add content in existing notebook" should "update content file" in {
    val testContent = "val test = \"test\"\nsc"
    val testContent2 = "val test2 = \"test_updated\"\nsc"
    storage ! Create("test")
    storage ! WriteContent("test", Array(testContent))
    storage ! WriteContent("test", Array(testContent2))
    val test = Paths.get(baseDir, "test", "content.scala")
    test.toFile.exists() should be(true)
    test.toFile.isFile should be(true)
    val source = scala.io.Source.fromFile(test.toAbsolutePath.toString)

    val lines = source.mkString
    source.reset().getLines().length should be(4)
    source.close()

    lines should include(testContent2)
  }

  "add new paragraph content in existing notebook" should "update content file" in {
    val testContent = "val test = \"test\"\nsc"
    val testContent2 = "val test2 = \"test_updated\"\nsc"
    storage ! Create("test")
    storage ! WriteContent("test", Array(testContent, testContent2))
    val test = Paths.get(baseDir, "test", "content.scala")
    test.toFile.exists() should be(true)
    test.toFile.isFile should be(true)
    val source = scala.io.Source.fromFile(test.toAbsolutePath.toString)

    val lines = source.mkString
    println(lines)
    source.reset().getLines().length should be(7)
    source.close()
    lines should include(testContent2)
    lines should include(testContent)
    lines should include("// PARAGRAPH")
  }

  "add result in existing notebook" should "create new result file" in {
    val testContent = "result\nresult2"
    storage ! Create("test")
    storage ! WriteStdout("test", Array(testContent))
    val test = Paths.get(baseDir, "test", "result.txt")
    test.toFile.exists() should be(true)
    test.toFile.isFile should be(true)
    val source = scala.io.Source.fromFile(test.toAbsolutePath.toString)

    val lines = source.mkString
    source.reset().getLines().length should be(2)
    source.close()
    lines should include(testContent)
  }

  "add result in existing notebook with result" should "update result file" in {
    val testContent = "result\nresult2"
    val testContent2 = "result\nresult2\nestul3"
    storage ! Create("test")
    storage ! WriteStdout("test", Array(testContent))
    storage ! WriteStdout("test", Array(testContent2))
    val test = Paths.get(baseDir, "test", "result.txt")
    test.toFile.exists() should be(true)
    test.toFile.isFile should be(true)
    val source = scala.io.Source.fromFile(test.toAbsolutePath.toString)

    val lines = source.mkString
    source.reset().getLines().length should be(3)
    source.close()

    lines should include(testContent2)
  }

  "read content" should "return content" in {
    val testContent = "var test = \"test\""
    val testResult = "test: String"
    storage ! Create("test")
    storage ! WriteContent("test", Array(testContent))
    storage ! WriteStdout("test", Array(testResult))
    storage ! ReadContent("test")
    expectMsg(5 second, "var test = \"test\"\n")
  }

  "read result" should "return content" in {
    val testContent = "var test = \"test\""
    val testResult = "test: String"
    storage ! Create("test")
    storage ! WriteContent("test", Array(testContent))
    storage ! WriteStdout("test", Array(testResult))
    storage ! ReadResult("test")
    expectMsg(5 second, "test: String\n")
  }

  "read all on new notebook" should "return empty content" in {
    storage ! ReadAll("test")
    val content = expectMsgType[Content](15 second)
    content.paragraphs.length should be(1)
    content.paragraphs(0).content should be("")
    content.paragraphs(0).result should be("")
  }

  "read content with many paragraph" should "return many paragraph" in {
    val testContent = "var test = \"test\""
    val testContent2 = "var test2 = \"test2\""
    val testResult = "test: String"
    val testResult2 = "test2: String"
    storage ! Create("test")
    storage ! WriteContent("test", Array(testContent, testContent2))
    storage ! ReadContent("test")
    val c = expectMsgType[String](15 second)
    println(c)
  }
}
