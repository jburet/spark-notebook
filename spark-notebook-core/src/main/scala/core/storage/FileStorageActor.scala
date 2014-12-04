package core.storage

import java.nio.file.{StandardOpenOption, Files, Path, Paths}

import akka.actor.{ActorLogging, Actor}
import akka.event.LoggingReceive
import com.google.common.base.Charsets
import core.storage.FileStorageActor._

class FileStorageActor extends Actor with ActorLogging {

  var baseDir: Path = _

  def receive = LoggingReceive {
    case Init(dir: String) => {
      baseDir = Paths.get(dir)
      if (!baseDir.toFile.exists()) {
        baseDir.toFile.mkdirs()
      }
    }
    case Create(id: String) => {
      val newDir = baseDir.resolve(id).toFile
      if (!newDir.exists()) {
        newDir.mkdirs()
      } else {
        log.warning("notebook_dir_exists, {}", newDir.getAbsolutePath)
      }
    }
    case ListFile() => {
      val lnb = Notebooks(baseDir.toFile.listFiles().filter(f => {
        f.isDirectory
      }).map(f => {
        f.getName
      }))
      sender ! lnb
    }
    case WriteContent(id: String, content: Array[String]) => {
      val newFile = baseDir.resolve(id).resolve("content.scala")
      // Create content each paragraph is separated by a comment line
      val finalContent = content.mkString(
        """
          |/// NEW PARAGRAPH
        """.stripMargin)
      Files.write(newFile, finalContent.getBytes(Charsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    }
    case WriteResult(id: String, content: String) => {
      val newFile = baseDir.resolve(id).resolve("result.txt")
      Files.write(newFile, content.getBytes(Charsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    }
    case ReadContent(id: String) => {
      doReadContent(id, "content.scala", (message: List[String]) => sender ! message.mkString("\n"))

    }
    case ReadResult(id: String) => {
      doReadContent(id, "result.txt", (message: List[String]) => sender ! message.mkString("\n"))
    }
    case ReadAll(id: String) => {
      var c: List[String] = List()
      var r: List[String] = List()
      doReadContent(id, "content.scala", (message: List[String]) => c = message)
      doReadContent(id, "result.txt", (message: List[String]) => r = message)

      sender ! Content(Array(Paragraph("", "", "")))
    }
    case other => log.error("unknown_message, {}, from, {}", Array(other, sender))
  }

  def doReadContent(id: String, file: String, success: List[String] => Unit) = {
    val content = baseDir.resolve(id).resolve(file).toFile
    content.exists() match {
      case true => {
        val s = scala.io.Source.fromFile(content).getLines().foldLeft(List[String]())((acc: List[String], l: String) => {
          var nacc = acc
          if(nacc.isEmpty){
            nacc = nacc ::: List("")
          }
          // If /// NEW PARAGRAPH append a new element to list
          if (l.equals("/// NEW PARAGRAPH")) {
            nacc = acc ::: List("")
          } else {
            // append line to last element
            val nll = nacc.reverse.head + l + "\n"
            nacc = nacc.dropRight(1) ::: List(nll)
          }
          nacc
        })
        success(s)
      }
      case false => success(List[String]())
    }
  }
}

object FileStorageActor {

  case class Init(baseDir: String)

  case class Create(id: String)

  case class ReadContent(id: String)

  case class ReadResult(id: String)

  case class ReadAll(id: String)

  case class Paragraph(content: String, result: String, data: String)

  case class Content(paragraphs: Array[Paragraph])

  case class WriteContent(id: String, content: Array[String])

  case class WriteResult(id: String, result: String)

  case class ListFile()

  case class Notebooks(names: Array[String])

}