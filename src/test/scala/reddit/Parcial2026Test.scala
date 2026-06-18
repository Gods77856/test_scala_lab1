package reddit

import org.scalatest.funsuite.AnyFunSuite

class Parcial2026Test extends AnyFunSuite {
  import Parcial2026._

  test("parseSubscriptions requires every field and fails the whole configuration") {
    val valid = """[{"name":"Scala","url":"https://reddit.com/r/scala/.json","count":10,"before":"abc"}]"""
    val missing = """[{"name":"Scala","url":"https://reddit.com/r/scala/.json","count":10}]"""

    assert(parseSubscriptions(valid).exists(_.head.count == 10))
    assert(parseSubscriptions(missing).isEmpty)
    assert(parseSubscriptions("not json").isEmpty)
  }

  test("buildUrl adds or replaces parameters without duplicating question marks") {
    val plain = Subscription("Scala", "https://reddit.com/r/scala/.json", 10, "abc")
    val existing = Subscription("Scala", "https://reddit.com/r/scala/.json?count=99&sort=hot", 10, "abc")

    assert(buildUrl(plain).contains("https://reddit.com/r/scala/.json?count=10&before=abc"))
    assert(buildUrl(existing).contains("https://reddit.com/r/scala/.json?sort=hot&count=10&before=abc"))
  }

  test("parsePosts extracts title selftext and author functionally") {
    val json = """{"data":{"children":[{"data":{"title":"AI news","selftext":"An LLM","author":"ada"}}]}}"""
    assert(parsePosts(json).contains(List(Post("AI news", "An LLM", "ada"))))
  }

  test("countCensoredWords ignores case and punctuation but not substrings") {
    val post = Post("AI, ai; ChatGPT", "LLM LLMs agent agentic email", "ada")
    assert(countCensoredWords(post) == 7)
  }

  test("downloadAll skips failures and accepts an injected fetch function") {
    val subscriptions = List(
      Subscription("ok", "https://reddit.com/r/ok/.json", 1, "a"),
      Subscription("bad", "https://reddit.com/r/bad/.json", 1, "b")
    )
    val json = """{"data":{"children":[{"data":{"title":"AI","selftext":"ML","author":"ada"}}]}}"""
    val result = downloadAll(subscriptions, url => if (url.contains("/ok/")) Some(json) else None)

    assert(result.length == 1)
    assert(result.head._2.head.author == "ada")
  }
}
