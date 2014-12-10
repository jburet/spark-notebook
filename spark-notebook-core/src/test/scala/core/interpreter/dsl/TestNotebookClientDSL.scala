package core.interpreter.dsl

import org.scalatest.{Matchers, FlatSpec}


class TestNotebookClientDSL extends FlatSpec with Matchers {

  "build with long value" should "return simple value struct" in {
    val dsl = new NotebookClientDSL()
    dsl.Simple("test", 0l).display()
    val json = dsl.result()

    json should include("test")
    json should include("0")
  }

}
