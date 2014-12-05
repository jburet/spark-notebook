package core.storage

import java.nio.file.{StandardOpenOption, Files, Path, Paths}

import akka.actor.{ActorLogging, Actor}
import akka.event.LoggingReceive
import com.google.common.base.Charsets
import core.storage.FileStorageActor._

import scala.util.{Failure, Success}

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
      // FIXME ADD A HEADER in API
      val header = ""
      // Create content each paragraph is separated by a comment line
      val finalContent = (header +: content.map(c => if (c!= null && (c.length == 0 || !c.takeRight(1).charAt(0).equals('\n'))) {
        c + "\n"
      } else {
        c
      })).mkString("// PARAGRAPH 0\n")
      Files.write(newFile, finalContent.getBytes(Charsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    }
    case WriteResult(id: String, resContent: Array[String]) => {
      val newFile = baseDir.resolve(id).resolve("result.txt")
      // FIXME ADD A HEADER in API
      val header = ""
      // Create content each paragraph is separated by a comment line
      val finalContent = (header +: resContent.map(c => if (c!= null && (c.length == 0 || !c.takeRight(1).charAt(0).equals('\n'))) {
        c + "\n"
      } else {
        c
      })).mkString("// PARAGRAPH 0\n")
      Files.write(newFile, finalContent.getBytes(Charsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    }
    case ReadContent(id: String) => {
      doReadContent(id, "content.scala", (message: List[String]) => sender ! message)

    }
    case ReadResult(id: String) => {
      doReadContent(id, "result.txt", (message: List[String]) => sender ! message)
    }
    case ReadAll(id: String) => {
      var c: List[String] = List()
      var r: List[String] = List()
      doReadContent(id, "content.scala", (message: List[String]) => c = message)
      doReadContent(id, "result.txt", (message: List[String]) => r = message)
      val ps = c.zipAll(r, "", "").map(p => {
        Paragraph(p._1, p._2, "")
      }).toArray
      val content = Content(ps)
      sender ! content
    }
    case other => log.error("unknown_message, {}, from, {}", Array(other, sender))
  }

  def doReadContent(id: String, file: String, success: List[String] => Unit) = {
    val content = baseDir.resolve(id).resolve(file).toFile
    log.info("parse_content, {}", content)
    content.exists() match {
      case true => {
        val s = scala.io.Source.fromFile(content).getLines().mkString("\n")
        ParagraphParser(s) match {
          case Success(ps) => {
            success(ps.toList)
          }
          case Failure(e) => {
            log.error(e, "cannot_load_file, {}", id)
            success(List[String]())
          }
        }

      }
      case false => {
        log.error("cannot_load_file, {}", id)
        success(List[String]())
      }
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

  case class WriteResult(id: String, result: Array[String])

  case class ListFile()

  case class Notebooks(names: Array[String])

}