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
   * Hint: Usa .filter() con una lambda
   *       val filteredPosts = posts.filter(post => post._5 >= minScore)
   *       donde _5 es el score (Int)
   */
  
  // Cargamos las suscripciones
  val subscriptionsResult = FileIO.loadSubscriptionsFromResources()

  subscriptionsResult match {
    case Some(subscriptions) =>
      println(s"✓ Loaded ${subscriptions.length} subscriptions")
      println()

      // TODO - EJERCICIO 1 - PASO 2: Descargar feeds y aplicar filtro de minScore
      //
      // Estructura esperada:
      // val allPosts = subscriptions.flatMap { case (name, url, minScore) =>
      //   descarga el feed -> parsea los posts -> FILTRA por minScore
      // }
      //
      // Pasos clave:
      // 1. Usa flatMap para iterar sobre subscriptions
      // 2. Descarga feed: FileIO.downloadFeed(url)
      // 3. Parsea posts: RedditParser.parsePosts(jsonString)
      // 4. FILTRA: posts.filter(post => post._5 >= minScore)
      //    (post._5 es el score - recuerda el índice!)
      // 5. Imprime cuántos posts quedaron después del filtro
      // 6. Si algún paso falla, retorna List.empty
      //
      // Nota: post._5 es el score (Int), minScore es un Int
      // Referencia: Ver EJERCICIOS.md Ejercicio 1 - Paso 2
      
      val allPosts: List[RedditParser.Post] = List.empty  // Reemplaza con tu implementación
      
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
