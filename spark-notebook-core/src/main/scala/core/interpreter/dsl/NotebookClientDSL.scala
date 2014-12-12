package core.interpreter.dsl

import java.text.SimpleDateFormat

import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SchemaRDD
import org.json4s.JsonAST.{JDouble, JString, JInt, JValue}
import org.json4s.JsonDSL.WithDouble._
import org.json4s.jackson.JsonMethods._

class NotebookClientDSL {

  var json: String = _

  def result(): String = {
    val result = if (json == null) "" else json
    json = ""
    return result
  }

  case class Simple(name: String, v: Long) {
    def display() = {
      json = compact(render(("name" -> name) ~ ("value" -> v)))
    }
  }

  case class Display() {
    var name = "display"
    var rdd: RDD[String] = _
    var limit = 100

    def name(name: String): Display = {
      this.name = name
      this
    }

    def limit(limit: Int): Display = {
      this.limit = limit
      this
    }

    def data(rdd: RDD[String]): Display = {
      this.rdd = rdd
      this
    }

    def plot() = {
      json = compact(render(("type" -> "list") ~ ("data" -> (("header" -> "value ") ~ ("value" -> rdd.take(limit).toList)))))
    }

  }

  case class Table() {

    var name = "display"
    var rdd: SchemaRDD = _
    var limit = 100

    def name(name: String): Table = {
      this.name = name
      this
    }

    def limit(limit: Int): Table = {
      this.limit = limit
      this
    }

    def data(rdd: SchemaRDD): Table = {
      this.rdd = rdd
      this
    }

    def plot() = {
      val header = rdd.schema.fields.map(f => Map(("name" -> f.name), ("type" -> f.dataType.toString)))
      val values = rdd.take(limit).map(row => row.map(v => v.toString).toList).toList
      json = compact(render(("type" -> "table") ~ ("data" -> (("header" -> header) ~ ("value" -> values)))))
    }
  }

  case class Histogram(name: String, rdd: RDD[Double], nbBucket: Integer = 10) {
    def display() = {
      val histogram = rdd.histogram(nbBucket)
      json = compact(render(("type" -> "histogram") ~ ("data" -> (("name" -> name) ~ ("categories" -> histogram._1.toList) ~ ("data" -> histogram._2.toList)))))
    }
  }

  case class Distribution(name: String, rdd: RDD[String], max: Integer = 10) {
    def display() = {
      val distrib = rdd.countByValue()
      val sortedDistrib = distrib.toSeq.sortBy(-_._2).take(max)
      val cat = sortedDistrib.map(t => t._1)
      val count = sortedDistrib.map(t => t._2)
      json = compact(render(("type" -> "distribution") ~ ("data" -> (("name" -> name) ~ ("categories" -> cat) ~ ("count" -> count)))))
    }
  }

  case class Scatter() {
    var name = "scatter"
    var series: List[(String, RDD[(Int, Int)])] = List()

    def name(name: String): Scatter = {
      this.name = name
      this
    }

    def addData(name: String, rdd: RDD[(Int, Int)]): Scatter = {
      series = (name, rdd) :: series
      this
    }

    def plot() = {
      json = compact(render(("type" -> "scatter") ~ ("data" -> (("name" -> name) ~ ("value" -> series.map(serie => ("name" -> serie._1) ~ ("data" -> serie._2.collect().map(r => {
        List(JInt(r._1), JInt(r._2))
      }).toList)))))))
    }

  }

  case class Heatmap() {
    var name = "heatmap"
    var rdd: RDD[(String, Int, Double)] = _

    def name(name: String): Heatmap = {
      this.name = name
      this
    }

    def data(rdd: RDD[(String, Int, Double)]): Heatmap = {
      this.rdd = rdd
      this
    }

    def plot() = {
      val dfin = new SimpleDateFormat("yyyyMMdd")
      val dfout = new SimpleDateFormat("yyyy-MM-dd")
      json = compact(render(("type" -> "heatmap") ~ ("data" -> (("name" -> name) ~ ("value" -> rdd.collect().map(r => {

        List(JString(dfout.format(dfin.parse(r._1))), JInt(r._2), JDouble(r._3))
      }).toList)))))
    }

    // name: String, rdd: RDD[(String, String, Int)]
    /*def display() = {

    }*/

  }

}