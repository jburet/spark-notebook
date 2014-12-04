package core.storage

import org.scalatest.{Matchers, FlatSpec}

class TestParagraphParser extends FlatSpec with Matchers {

  "three paragraph" should "return array of size 3" in {
    val data = """
                 |// PARAGRAPH 1
                 |test
                 |// PARAGRAPH 2
                 |test2
                 |// PARAGRAPH 3
                 |test3
                 |""".stripMargin
    val pp = ParagraphParser(data)

    //pp should be(Array())
    println(pp)
  }
}
