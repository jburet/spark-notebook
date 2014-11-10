package core.storage

import java.nio.file.{Files, Path, Paths}

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
    case List() => {
      val lnb = Notebooks(baseDir.toFile.listFiles().filter(f => {
        f.isDirectory
      }).map(f => {
        f.getName
      }))
      sender ! lnb
    }
    case WriteContent(id: String, content: String) => {
      val newFile = baseDir.resolve(id).resolve("content.scala")
      Files.write(newFile, content.getBytes(Charsets.UTF_8))
    }
    case WriteResult(id: String, content: String) => {
      val newFile = baseDir.resolve(id).resolve("result.txt")
      Files.write(newFile, content.getBytes(Charsets.UTF_8))
    }
    case ReadContent(id: String) => {
      val content = baseDir.resolve(id).resolve("content.scala").toFile
      content.exists() match {
        case true => sender ! scala.io.Source.fromFile(content).mkString
        case false => sender ! ""
      }

    }
    case ReadResult(id: String) => {
      val content = baseDir.resolve(id).resolve("result.txt").toFile
      content.exists() match {
        case true => sender ! scala.io.Source.fromFile(content).mkString
        case false => sender ! ""
      }
    }
    case other => log.error("unknown_message")
  }
}

object FileStorageActor {

  case class Init(baseDir: String)

  case class Create(id: String)

  case class ReadContent(id: String)

  case class ReadResult(id: String)

  case class WriteContent(id: String, content: String)

  case class WriteResult(id: String, result: String)

  case class List()

  case class Notebooks(names: Array[String])

}