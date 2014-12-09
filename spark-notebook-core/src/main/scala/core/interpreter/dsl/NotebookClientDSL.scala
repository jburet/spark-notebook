package core.interpreter.dsl


import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SchemaRDD
import org.json4s.JsonAST.{JArray, JString}
import org.json4s.JsonDSL.WithDouble._
import org.json4s.jackson.JsonMethods._

class NotebookClientDSL {

  var json: String = _

  def result(): String = {
    val result = if (json == null) "" else json
    json = ""
    return result
  }

  case class SimpleDisplay(name: String, v: Long) {

    def build() = {
      json = compact(render(("name" -> name) ~ ("value" -> v)))
    }
  }

  case class SchemaRDDDisplay(name: String, rdd: SchemaRDD, limit: Int = 100) {
    def build() = {
      val header = rdd.schema.fields.map(f => Map(("name" -> f.name), ("type" -> f.dataType.toString)))
      val values = rdd.take(limit).map(row => row.map(v => v.toString).toList).toList
      json = compact(render(("type" -> "table") ~ ("data" -> (("header" -> header) ~ ("value" -> values)))))
    }
  }

  case class HistogramDisplay(name: String, histogram: (Array[Double], Array[Long])) {
    def build() = {
      json = compact(render(("type" -> "histogram") ~ ("data" -> (("name" -> name) ~ ("categories" -> histogram._1.toList) ~ ("data" -> histogram._2.toList)))))
    }
  }

  case class DistributionDisplay(name: String, rdd: RDD[String], max: Integer = 10) {
    def build() = {
      val distrib = rdd.countByValue()
      val sortedDistrib = distrib.toSeq.sortBy(-_._2).take(max)
      val cat = sortedDistrib.map(t => t._1)
      val count = sortedDistrib.map(t => t._2)
      json = compact(render(("type" -> "distribution") ~ ("data" -> (("name" -> name) ~ ("categories" -> cat) ~ ("count" -> count)))))
    }
  }

}