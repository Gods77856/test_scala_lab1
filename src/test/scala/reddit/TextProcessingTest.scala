package reddit

import org.scalatest.funsuite.AnyFunSuite

class TextProcessingTest extends AnyFunSuite {

  test("tokenize: should split text into words") {
    val text = "hello world scala programming"
    val result = TextProcessing.tokenize(text)
    assert(result == List("hello", "world", "scala", "programming"))
  }

  test("tokenize: should handle punctuation") {
    val text = "Hello, world! How are you?"
    val result = TextProcessing.tokenize(text)
    assert(result == List("Hello", "world", "How", "are", "you"))
  }

  test("tokenize: should handle empty string") {
    val text = ""
    val result = TextProcessing.tokenize(text)
    assert(result.isEmpty)
  }

  test("tokenize: should handle mentions with u/") {
    val text = "u/spez is the founder"
    val result = TextProcessing.tokenize(text)
    // Mentions like u/spez should be preserved as single tokens
    assert(result.contains("u/spez"))
    assert(result.contains("is"))
    assert(result.contains("the"))
    assert(result.contains("founder"))
  }

  test("formatDateFromUTC: should format timestamp correctly") {
    val timestamp = 1234567890L
    val result = TextProcessing.formatDateFromUTC(timestamp)
    assert(result.length == 16) // Format: "YYYY-MM-DD HH:mm"
    assert(result.contains("-"))
    assert(result.contains(":"))
  }

  test("formatDateFromUTC: should handle epoch") {
    val timestamp = 0L
    val result = TextProcessing.formatDateFromUTC(timestamp)
    // Should not throw exception
    assert(result.nonEmpty)
  }
}
