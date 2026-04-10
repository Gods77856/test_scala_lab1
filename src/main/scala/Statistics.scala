package reddit

/**
 * Statistics.scala
 *
 * Módulo para análisis y estadísticas sobre posts de Reddit.
 * Incluye funciones para extraer información significativa.
 */
object Statistics {

  import RedditParser.Post

  /**
   * Calcula estadísticas básicas sobre los posts.
   *
   * @param posts Lista de posts a analizar
   * @return String con reporte de estadísticas (opcional, puede usarse para debug)
   */
  def basicStats(posts: List[Post]): String = {
    if (posts.isEmpty) "No posts available"
    else {
      val avgScore = posts.map(_._5).sum.toDouble / posts.length
      s"Total: ${posts.length} posts | Avg Score: ${avgScore.formatted("%.2f")}"
    }
  }

  /**
   * EJERCICIO 2 - Nueva Heurística: Extrae las menciones a usuarios más frecuentes
   *
   * Definición: Una mención es una palabra que comienza exactamente con "u/"
   * Ejemplo: "u/spez", "u/torvalds_right" son menciones válidas
   *
   * Pasos (implementar como un pipeline funcional):
   * 1. `flatMap` sobre posts para extraer el selftext (_3) de cada post
   * 2. Tokenizar el selftext (usar TextProcessing.tokenize o split)
   * 3. `filter` solo palabras que comiencen con "u/"
   * 4. `filter` nuevamente para descartar "u/" sueltos (length > 2)
   * 5. `groupBy(identity)` para agrupar menciones iguales
   * 6. `map` para contar: (mención, cantidad)
   * 7. `toList`, `sortBy(-_._2)` para ordenar por cantidad descendente
   * 8. `take(limit)` para obtener top N
   *
   * Estructura esperada de resultado:
   * List(
   *   ("u/spez", 5),
   *   ("u/torvalds", 3),
   *   ("u/guido", 2)
   * )
   *
   * @param posts Lista de posts a analizar
   * @param limit Cantidad de top menciones a retornar
   * @return List[(String, Int)] siendo cada tupla (usuario, cantidad)
   */
  def mentionsTop(posts: List[Post], limit: Int): List[(String, Int)] = {
    // TODO: EJERCICIO 2 - Implementar pipeline funcional de 7 pasos
    //
    // Pipeline recomendado (SIN MUTACIÓN):
    // 1. flatMap: extrae selftext (_3) de cada post y tokeniza
    // 2. filter: mantén solo palabras que comienzan con "u/"
    // 3. filter: descarta "u/" sueltos (length > 2)
    // 4. groupBy(identity): agrupa menciones iguales
    // 5. map: transforma a (mención, cantidad_de_ocurrencias)
    // 6. toList + sortBy(-_._2): ordena descendentemente por cantidad
    // 7. take(limit): obtén top N
    //
    // Referencia: Ver EJERCICIOS.md Ejercicio 2
    // Hint: TextProcessing.tokenize() divide texto en palabras
    //       groupBy() devuelve Map, necesitas .toList para convertir
    //       sortBy(-_._2) ordena descendente (el - invierte el orden)
    
    posts
      .flatMap(post => TextProcessing.tokenize(post._3))
      .filter(_.startsWith("u/"))
      .filter(_.length > 2)
      .groupBy(identity)
      .map { case (mention, occurrences) => (mention, occurrences.length) }
      .toList
      .sortBy(-_._2)
      .take(limit)
  }

  /**
   * Genera un reporte por subreddit.
   *
   * EJERCICIO 1 - Será aquí donde aplicarás el filtro de minScore.
   *
   * @param subreddit Nombre del subreddit
   * @param posts Posts de ese subreddit
   * @return String con reporte formateado
   */
  def generateSubredditReport(subreddit: String, posts: List[Post]): String = {
    val report = new StringBuilder
    
    report.append(s"=== Subreddit: $subreddit ===\n")
    report.append(s"Total posts: ${posts.length}\n")
    
    if (posts.nonEmpty) {
      val avgScore = posts.map(_._5).sum.toDouble / posts.length
      val maxScore = posts.map(_._5).max
      
      report.append(s"Avg Score: ${avgScore.formatted("%.2f")}\n")
      report.append(s"Max Score: $maxScore\n")
      
      // TODO: Aquí irá la llamada a mentionsTop(posts, 3) en EJERCICIO 2
      // Por ahora solo se comenta:
      val topMentions = mentionsTop(posts, 3)
      report.append("\nTop User Mentions:\n")
      topMentions.foreach { case (user, count) =>
        report.append(s"  - $user: $count\n")
      }
    }
    
    report.toString()
  }

  /**
   * Agrupa posts por subreddit.
   *
   * @param posts Lista de todos los posts
   * @return Map[String, List[Post]] agrupado por subreddit
   */
  def groupBySubreddit(posts: List[Post]): Map[String, List[Post]] = {
    posts.groupBy(_._1) // _1 es el subreddit
  }
}
