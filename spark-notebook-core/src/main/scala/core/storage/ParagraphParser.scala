package core.storage

import org.parboiled2.RuleFrame.{ZeroOrMore}
import org.parboiled2._

import scala.util.{Try, Success, Failure}

object ParagraphParser extends {

  def apply(input: ParserInput): Try[Unit] = {

    val parser = new ParagraphParser(input)
    parser.paragraph.run() match {
      case Success(result) => {

        println(result);
        Success(result);
      }
      case Failure(e: ParseError) => println("Expression is not valid: " + parser.formatError(e, showTraces = true)); Failure(e)
      case Failure(e) => println("Unexpected error during parsing run: " + e); Failure(e)
    }

  }
}

class ParagraphParser(val input: ParserInput) extends Parser {

  def NON_CAPTURING_CRLF = rule("\n\r" | "\n")


  def paragraph = rule {
    oneOrMore(separator | line).separatedBy(NON_CAPTURING_CRLF)
  }

  def line = rule {
    capture(zeroOrMore(CharPredicate.Printable)) ~> (Line(_))
  }

  def separator = rule {
    "// PARAGRAPH " ~ capture(oneOrMore(CharPredicate.Digit)) ~> (Sep(_))
  }


}

case class Line(s: String)

case class Sep(s: String)