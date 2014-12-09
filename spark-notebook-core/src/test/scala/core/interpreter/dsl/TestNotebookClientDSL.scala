package core.interpreter.dsl

import org.scalatest.{Matchers, FlatSpec}


class TestNotebookClientDSL extends FlatSpec with Matchers {

  "build with long value" should "return simple value struct" in {
    val dsl = new NotebookClientDSL()
    dsl.SimpleDisplay("test", 0l).build()
    val json = dsl.result()

    json should include("test")
    json should include("0")
  }

}
