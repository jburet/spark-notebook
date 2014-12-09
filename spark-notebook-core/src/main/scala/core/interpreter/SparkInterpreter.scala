package core.interpreter

import java.io.{ByteArrayOutputStream, PrintStream, PrintWriter}

import akka.actor.{ActorLogging, ActorRef, Props, Actor}
import akka.event.{LoggingReceive}
import core.interpreter.SparkInterpreter.{Stop, InterpreterResult, Init}
import core.interpreter.dsl.NotebookClientDSL
import core.notebook.Job.JobComplete
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.repl.{SparkIMain, SparkILoop}
import org.apache.spark.ui.jobs.JobProgressListener

import scala.reflect.io.File
import scala.tools.nsc.interpreter.Results.{Incomplete, Error, Success}
import scala.tools.nsc.{Interpreter, Settings}
import scala.tools.nsc.interpreter.{Results}

object SparkInterpreter {

  def props(appname: String) = Props(new SparkInterpreter(appname))

  case class Init()

  case class Stop()

  case class InterpreterResult(stdout: String, jsonContent: String)

}

class SparkInterpreter(appname: String) extends Actor with ActorLogging {

  var out: ByteArrayOutputStream = _
  var interpreter: SparkILoop = _
  var binder: scala.collection.mutable.Map[String, AnyRef] = _
  var settings: Settings = _
  var intp: SparkIMain = _
  var sc: SparkContext = _
  var sparkListener: JobProgressListener = _
  val dsl = new NotebookClientDSL()

  def init() = {
    out = new ByteArrayOutputStream()
    interpreter = new SparkILoop(None, new PrintWriter(out), None)
    binder = scala.collection.mutable.Map[String, AnyRef]()
    settings = new Settings

    settings.usejavacp.value = true
    settings.classpath.value += File.pathSeparator + System.getProperty("java.class.path")
    val in = new Interpreter(settings) {
      override protected def parentClassLoader = getClass.getClassLoader
    }
    in.setContextClassLoader()

    interpreter.settings_$eq(settings)
    interpreter.createInterpreter
    interpreter.loadFiles(settings)
    intp = interpreter.intp

    intp.setContextClassLoader
    intp.initializeSynchronous

    sc = createSparkContext
    sparkListener = new JobProgressListener(sc.getConf)

    intp.interpret("var _binder = scala.collection.mutable.Map[String, AnyRef]()")
    val optionBinder = getValue("_binder")
    val optionDSL = getValue("dsl")
    optionBinder match {
      case Some(b: scala.collection.mutable.Map[String, AnyRef]) => {
        binder = b
        binder.put("sc", sc)
        binder.put("out", new PrintStream(out))
        binder.put("dsl", dsl)
        intp.interpret("@transient val sc = _binder.get(\"sc\").get.asInstanceOf[org.apache.spark.SparkContext]")
        intp.interpret("@transient val dsl = _binder.get(\"dsl\").get.asInstanceOf[core.interpreter.dsl.NotebookClientDSL]")
      }
      case _ => scala.Console.err.println("Invalid binder")
    }
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

  /**
   * Interpret multiple line
   */
  def interpret(lines: Array[String]): Results.Result = {
    this synchronized {
      sc.setJobGroup(jobGroup, appname, false)
      val res = _interpret(lines)
      sc.clearJobGroup
      return res
    }
  }

  private def _interpret(lines: Array[String]): Results.Result = {
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
    return Success
  }


  def close() = {
    sc.stop
    //interpreter.closeInterpreter
  }

  /* ACTOR METHOD */

  def receive = LoggingReceive({
    case _: Init => init()
    case (job: ActorRef, id: String, line: String) => {
      out.reset()
      interpret(line)
      sender ! InterpreterResult(out.toString(), dsl.result())
      job ! JobComplete(id)
      out.reset()
    }
    case (job: ActorRef, id: String, line: List[String]) => {
      sender ! line.map(code => {
        out.reset()
        interpret(code)
        val stdout = out.toString()
        val result = dsl.result()
        log.debug("interpreter_result, {}, {}", Array(stdout, result))
        InterpreterResult(stdout, result)
      })
      job ! JobComplete(id)
      out.reset()
    }
    case _: Stop => close()
    case other => log.warning("invalid_message, " + other)
  })

  override def postStop(): Unit = {
    close()
  }
}