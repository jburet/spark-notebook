package core.storage

import org.scalatest.{Matchers, FlatSpec}

class TestParagraphParser extends FlatSpec with Matchers {

  "three paragraph with header" should "return array of size 3" in {
    val data = """
                 |// PARAGRAPH 1
                 |test
                 |l2
                 |// PARAGRAPH 2
                 |test2
                 |l2
                 |// PARAGRAPH 3
                 |test3
                 |l2
                 | """.stripMargin
    val pp = ParagraphParser(data)

    //pp should be(Array())
    println(pp)
    pp.isSuccess should be(true)
    pp.get.length should be(3)
  }

  "three paragraph without header" should "return array of size 3" in {
    val data = """// PARAGRAPH 1
                 |test
                 |l2
                 |// PARAGRAPH 2
                 |test2
                 |l2
                 |// PARAGRAPH 3
                 |test3
                 |l2
                 | """.stripMargin
    val pp = ParagraphParser(data)

    //pp should be(Array())
    println(pp)
    pp.isSuccess should be(true)
    pp.get.length should be(3)
  }

  "3 line without last \n" should "return 3 lines" in {
    val data = "\n// PARAGRAPH 0\n        sc\nsc\nsc"
    val pp = ParagraphParser(data)
    println(pp)
    pp.isSuccess should be(true)
    // one paragraph
    pp.get.length should be(1)
    // with 3 line
    pp.get(0).split("\n").length should be(3)
  }

  "empty notebook" should "return 1 paragraph" in {
    val data = "\n// PARAGRAPH 0\n        "
    val pp = ParagraphParser(data)
    println(pp)
    pp.isSuccess should be(true)
    // one paragraph
    pp.get.length should be(1)
    // with 3 line
    pp.get(0).split("\n").length should be(1)
  }

  "empty file" should "return empty seq" in {
    val data = ""
    val pp = ParagraphParser(data)
    println(pp)
    pp.isSuccess should be(true)
    pp.get.length should be(0)
  }

  "empty paragraph" should "return seq with empty paragraph" in {
    val data = "// PARAGRAPH 0\n"
    val pp = ParagraphParser(data)
    println(pp)
    pp.isSuccess should be(true)
    pp.get.length should be(1)
  }

}
