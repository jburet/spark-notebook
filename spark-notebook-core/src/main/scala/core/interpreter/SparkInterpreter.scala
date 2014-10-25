package core.interpreter

import java.io.{PrintStream, PrintWriter}

import akka.actor.Actor
import akka.event.Logging
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import org.apache.spark.{SparkConf, SparkEnv, SparkContext}
import org.apache.spark.repl.{SparkIMain, SparkILoop}
import org.apache.spark.ui.jobs.JobProgressListener

import scala.collection.mutable
import scala.reflect.io.File
import scala.tools.nsc.interpreter.Results.{Incomplete, Error, Success}
import scala.tools.nsc.{Interpreter, Settings}
import scala.tools.nsc.interpreter.{Results}

class SparkInterpreter(appname: String) extends Actor {

  val log = Logging(context.system, this)

  private val out = new ByteOutputStream()
  private val interpreter = new SparkILoop(None, new PrintWriter(out), None)
  private var binder = scala.collection.mutable.Map[String, AnyRef]()
  val settings: Settings = new Settings

  settings.usejavacp.value = true
  settings.classpath.value += File.pathSeparator + System.getProperty("java.class.path")
  val in = new Interpreter(settings) {
    override protected def parentClassLoader = getClass.getClassLoader
  }
  in.setContextClassLoader()

  interpreter.settings_$eq(settings)
  interpreter.createInterpreter
  interpreter.loadFiles(settings)
  private val intp = interpreter.intp

  intp.setContextClassLoader
  intp.initializeSynchronous

  private val sc = createSparkContext
  private val sparkListener = new JobProgressListener(sc.getConf)

  intp.interpret("var _binder = scala.collection.mutable.Map[String, AnyRef]()")
  val optionBinder = getValue("_binder")
  optionBinder match {
    case Some(b: scala.collection.mutable.Map[String, AnyRef]) => {
      binder = b
      binder.put("sc", sc)
      binder.put("out", new PrintStream(out))
      intp.interpret("@transient val sc = _binder.get(\"sc\").get.asInstanceOf[org.apache.spark.SparkContext]")
    }
    case _ => scala.Console.err.println("Invalid binder")
  }


  private def createSparkContext(): SparkContext = {
    val execUri: String = System.getenv("SPARK_EXECUTOR_URI")
    val jars: Array[String] = SparkILoop.getAddedJars
    val conf: SparkConf = new SparkConf().setMaster(getMaster).setAppName(appname).setJars(jars).set("spark.repl.class.uri", interpreter.intp.classServer.uri)
    if (execUri != null) {
      conf.set("spark.executor.uri", execUri)
    }
    if (System.getenv("SPARK_HOME") != null) {
      conf.setSparkHome(System.getenv("SPARK_HOME"))
    }
    conf.set("spark.scheduler.mode", "FAIR")
    val sparkContext: SparkContext = new SparkContext(conf)
    // FIXME sc.listenerBus.addListener(sparkListener)
    return sparkContext
  }

  private def getMaster: String = {
    val envMaster: String = System.getenv.get("MASTER")
    if (envMaster != null) return envMaster
    val propMaster: String = System.getProperty("spark.master")
    if (propMaster != null) return propMaster
    return "local[*]"
  }

  private def getValue(name: String): AnyRef = {
    return intp.valueOfTerm(name)
  }

  private final val jobGroup: String = appname + "-" + this.hashCode

  /**
   * Interpret a single line
   */
  def interpret(line: String): Results.Result = {
    if (line == null || line.trim.length == 0) {
      return Success
    }
    return interpret(line.split("\n"))
  }

  def interpret(lines: Array[String]): Results.Result = {
    this synchronized {
      sc.setJobGroup(jobGroup, appname, false)
      val res = _interpret(lines)
      sc.clearJobGroup
      return res
    }
  }

  private def _interpret(lines: Array[String]): Results.Result = {
    try {
      // FIXME Pb if two interpretation in parallel. How to have one scala.Console by interpreter
      //scala.Console.setOut(binder.get("out").get.asInstanceOf[PrintStream])
      var incomplete: String = ""
      for (s <- lines) {
        try {
          val res: Results.Result = intp.interpret(incomplete + s)
          res match {
            case Success => {
              incomplete = ""
            }
            case Error => {
              sc.clearJobGroup()
              return res
            }
            case Incomplete => {
              incomplete += s + "\n"
            }
          }
        }
        catch {
          case e: Exception => {
            sc.clearJobGroup
            return Error
          }
        }

      }
    } finally {
     // scala.Console.setOut(System.out)
    }
    return Success
  }


  def close {
    sc.stop
    //interpreter.closeInterpreter
  }

  def receive: Receive = {
    case lines: Array[String] => {
      out.reset()
      interpret(lines)
      sender ! out.toString
      out.reset()
    }
    case line: String => {
      out.reset()
      interpret(line)
      sender ! out.toString
      out.reset()
    }
    case _ => log.warning("invalid_message")
  }
}