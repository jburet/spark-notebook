package core.storage

import org.parboiled2.RuleFrame.{ZeroOrMore}
import org.parboiled2._

import scala.util.{Try, Success, Failure}

object ParagraphParser extends {

  def apply(input: ParserInput): Try[Seq[String]] = {

    val parser = new ParagraphParser(input)
    parser.document.run() match {
      case Success(result) => {
        Success(result)
      }
      case Failure(e: ParseError) => println("Expression is not valid: " + parser.formatError(e, showTraces = true)); Failure(e)
      case Failure(e) => println("Unexpected error during parsing run: " + e); Failure(e)
    }

  }
}

class ParagraphParser(val input: ParserInput) extends Parser {

  def NON_CAPTURING_CRLF = CharPredicate('\n')

  def document = rule {
    //noEmptyDocument | nonValidDocument
    noEmptyDocument
  }

  def nonValidDocument = rule {
    oneOrMore(capture(paragraph))
  }

  def noEmptyDocument = rule {
    header ~ separator ~ oneOrMore(capture(paragraph)).separatedBy(separator)
  }

  def paragraph = rule {
    zeroOrMore(line) ~ optional(lastLine)
  }

  def line = rule {
    !paragraphBegin ~ zeroOrMore(CharPredicate.Printable) ~ NON_CAPTURING_CRLF
  }

  def lastLine = rule {
    !paragraphBegin ~ zeroOrMore(CharPredicate.Printable) ~ EOI
  }

  def separator = rule {
    paragraphBegin ~ oneOrMore(CharPredicate.Digit) ~ NON_CAPTURING_CRLF
  }

  def paragraphBegin = rule {
    "// PARAGRAPH "
  }

  def header = rule {
    zeroOrMore(line)
  }


}

case class Line(s: String)

case class Sep(s: String)