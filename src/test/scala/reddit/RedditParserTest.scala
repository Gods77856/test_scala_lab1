package reddit

import org.scalatest.funsuite.AnyFunSuite

class RedditParserTest extends AnyFunSuite {

  test("parsePosts: should parse valid Reddit JSON") {
    val json = """
    {
      "data": {
        "children": [
          {
            "data": {
              "subreddit": "scala",
              "title": "Learning Scala",
              "selftext": "This is a great post about Scala",
              "created_utc": 1234567890,
              "score": 100,
              "url": "https://reddit.com/r/scala/post1"
            }
          }
        ]
      }
    }
    """
    val result = RedditParser.parsePosts(json)
    assert(result.isDefined)
    assert(result.get.nonEmpty)
    assert(result.get.head._1 == "scala")
    assert(result.get.head._2 == "Learning Scala")
    assert(result.get.head._5 == 100) // score
  }

  test("parsePosts: should handle missing title with fallback") {
    val json = """
    {
      "data": {
        "children": [
          {
            "data": {
              "subreddit": "scala",
              "selftext": "Post without title",
              "created_utc": 1234567890,
              "score": 50,
              "url": "https://reddit.com/r/scala/post2"
            }
          }
        ]
      }
    }
    """
    val result = RedditParser.parsePosts(json)
    assert(result.isDefined)
    assert(result.get.nonEmpty)
    assert(result.get.head._2 == "Sin Título") // Should use fallback value
  }

  test("parsePosts: should handle multiple posts") {
    val json = """
    {
      "data": {
        "children": [
          {
            "data": {
              "subreddit": "scala",
              "title": "Post 1",
              "selftext": "Content 1",
              "created_utc": 1234567890,
              "score": 100,
              "url": "https://reddit.com/1"
            }
          },
          {
            "data": {
              "subreddit": "java",
              "title": "Post 2",
              "selftext": "Content 2",
              "created_utc": 1234567900,
              "score": 150,
              "url": "https://reddit.com/2"
            }
          }
        ]
      }
    }
    """
    val result = RedditParser.parsePosts(json)
    assert(result.isDefined)
    assert(result.get.length == 2)
  }

  test("parsePosts: should return None for invalid JSON") {
    val invalidJson = "{ invalid json }"
    val result = RedditParser.parsePosts(invalidJson)
    assert(result.isEmpty)
  }

  test("parsePosts: should handle posts with special characters") {
    val json = """
    {
      "data": {
        "children": [
          {
            "data": {
              "subreddit": "scala",
              "title": "Test with émojis 🎉",
              "selftext": "u/spez said something",
              "created_utc": 1234567890,
              "score": 75,
              "url": "https://reddit.com/special"
            }
          }
        ]
      }
    }
    """
    val result = RedditParser.parsePosts(json)
    assert(result.isDefined)
    assert(result.get.nonEmpty)
  }

  test("parsePosts: should preserve score field") {
    val json = """
    {
      "data": {
        "children": [
          {
            "data": {
              "subreddit": "test",
              "title": "Test Score",
              "selftext": "Testing score field",
              "created_utc": 1234567890,
              "score": 42,
              "url": "https://reddit.com/score"
            }
          }
        ]
      }
    }
    """
    val result = RedditParser.parsePosts(json)
    assert(result.isDefined)
    assert(result.get.head._5 == 42) // score should be 42
  }
}
