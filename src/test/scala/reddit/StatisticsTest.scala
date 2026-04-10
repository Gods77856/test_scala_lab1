package reddit

import org.scalatest.funsuite.AnyFunSuite
import reddit.RedditParser.Post

class StatisticsTest extends AnyFunSuite {

  // Fixture: Posts de ejemplo para pruebas
  val samplePosts: List[Post] = List(
    ("scala", "Learning Scala", "u/spez is here u/spez again", "2025-03-15 14:30", 100, "https://reddit.com/1"),
    ("scala", "Monads Explained", "check u/torvalds_right and u/spez", "2025-03-15 15:00", 85, "https://reddit.com/2"),
    ("java", "Java vs Scala", "u/spez agrees", "2025-03-15 15:30", 120, "https://reddit.com/3"),
    ("scala", "Pattern Matching", "u/functionalFan posted u/spez", "2025-03-15 16:00", 95, "https://reddit.com/4")
  )

  test("mentionsTop: should be callable without errors") {
    val result = Statistics.mentionsTop(samplePosts, 3)
    // Result is a list of tuples with (mention, count)
    assert(result.isInstanceOf[List[(String, Int)]])
  }

  test("mentionsTop: should respect limit parameter") {
    val result = Statistics.mentionsTop(samplePosts, 2)
    assert(result.length <= 2)
  }

  test("mentionsTop: should order by frequency descending") {
    val result = Statistics.mentionsTop(samplePosts, 10)
    // Verify that list is sorted in descending order
    val frequencies = result.map(_._2)
    assert(frequencies == frequencies.sorted.reverse)
  }

  test("mentionsTop: should handle empty list") {
    val result = Statistics.mentionsTop(List.empty, 3)
    assert(result.isEmpty)
  }

  test("mentionsTop: should extract and count mentions correctly") {
    val result = Statistics.mentionsTop(samplePosts, 10)
    // From the sample posts, u/spez appears 5 times (2+1+1+1), u/torvalds_right 1 time, u/functionalFan 1 time
    assert(result.nonEmpty, "Should find mentions")
    assert(result.head._1 == "u/spez", s"Most frequent should be u/spez but got ${result.headOption}")
    assert(result.head._2 == 5, s"u/spez should appear 5 times but got ${result.head._2}")
  }

  test("basicStats: should calculate stats correctly") {
    val result = Statistics.basicStats(samplePosts)
    assert(result.contains("Total:"))
    assert(result.contains("Avg Score:"))
  }

  test("basicStats: should handle empty list") {
    val result = Statistics.basicStats(List.empty)
    assert(result == "No posts available")
  }

  test("groupBySubreddit: should group posts by subreddit") {
    val result = Statistics.groupBySubreddit(samplePosts)
    assert(result.keys.toList.contains("scala"))
    assert(result.keys.toList.contains("java"))
    assert(result("scala").length == 3)
    assert(result("java").length == 1)
  }

  test("generateSubredditReport: should generate report") {
    val report = Statistics.generateSubredditReport("scala", samplePosts.filter(_._1 == "scala"))
    assert(report.contains("scala"))
    assert(report.contains("Total posts:"))
    assert(report.contains("Avg Score:"))
  }
}
