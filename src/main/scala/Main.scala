package reddit

import reddit.RedditParser.Post
import reddit.FileIO.Subscription

/**
 * Main.scala
 *
 * Punto de entrada del programa.
 * Orquesta el flujo: cargar suscripciones -> descargar feeds -> parsear posts -> generar reportes.
 */
object Main extends App {

  println("╔════════════════════════════════════════╗")
  println("║     Reddit Scala Lab - Paradigmas     ║")
  println("╚════════════════════════════════════════╝")
  println()

  /**
   * EJERCICIO 1 - Paso 2: Aplicar filtro de minScore
   *
   * Esta es la función principal que orquesta todo.
   * Aquí es donde debes APLICAR EL NUEVO COMPORTAMIENTO funcional:
   * 
   * El flujo actual (sin filtro):
   * 1. Lee suscripciones (incluye minScore)
   * 2. Para cada suscripción, descarga el feed
   * 3. Parsea los posts
   * 4. Los acumula todos en allPosts
   *
   * Lo que debes cambiar (EJERCICIO 1):
   * 3.5 DESPUÉS de parsear, FILTRA los posts donde score < minScore
   *
   * Hint: Usa .filter() con una lambda que compare el score del post con minScore.
   *       Recordá que _5 es el score (Int).
   */
  
  // Cargamos las suscripciones
  val subscriptionsResult = FileIO.loadSubscriptionsFromResources()

  subscriptionsResult match {
    case Some(subscriptions) =>
      println(s"✓ Loaded ${subscriptions.length} subscriptions")
      println()

      val allPosts: List[Post] = subscriptions.flatMap { case (name, url, minScore) =>
        FileIO.downloadFeed(url)
          .flatMap(RedditParser.parsePosts)
          .map { posts =>
            val filtered = posts.filter(_._5 >= minScore)
            println(s"  Parsed ${filtered.length}/${posts.length} posts from $name")
            filtered
          }
          .getOrElse {
            println(s"  Could not load posts from $name")
            List.empty[Post]
          }
      }
      
      println()
      println(s"Total posts collected: ${allPosts.length}")
      println()

      // Generar reportes por subreddit
      if (allPosts.nonEmpty) {
        val postsBySubreddit = Statistics.groupBySubreddit(allPosts)
        
        postsBySubreddit.foreach { case (subreddit, posts) =>
          val report = Statistics.generateSubredditReport(subreddit, posts)
          println(report)
          println()
        }
      } else {
        println("No posts to display")
      }

    case None =>
      println("✗ Failed to load subscriptions")
  }

  println("╔════════════════════════════════════════╗")
  println("║           Program finished            ║")
  println("╚════════════════════════════════════════╝")
}
