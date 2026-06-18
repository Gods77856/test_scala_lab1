package reddit

/**
 * Statistics.scala
 *
 * Módulo para análisis y estadísticas sobre posts de Reddit.
 * Incluye funciones para extraer información significativa.
 */
object Statistics {

  import RedditParser.Post

  val Stopwords: Set[String] = Set(
    "the", "about", "above", "after", "again", "against", "all", "am", "an",
    "and", "any", "are", "aren't", "as", "at", "be", "because", "been",
    "before", "being", "below", "between", "both", "but", "by", "can't",
    "cannot", "could", "couldn't", "did", "didn't", "do", "does", "doesn't",
    "doing", "don't", "down", "during", "each", "few", "for", "from", "further",
    "had", "hadn't", "has", "hasn't", "have", "haven't", "having", "he",
    "he'd", "he'll", "he's", "her", "here", "here's", "hers", "herself",
    "him", "himself", "his", "how", "how's", "i", "i'd", "i'll", "i'm",
    "i've", "if", "in", "into", "is", "isn't", "it", "it's", "its",
    "itself", "let's", "me", "more", "most", "mustn't", "my", "myself",
    "no", "nor", "not", "of", "off", "on", "once", "only", "or", "other",
    "ought", "our", "ours", "ourselves", "out", "over", "own", "same",
    "shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't",
    "so", "some", "such", "than", "that", "that's", "their", "theirs",
    "them", "themselves", "then", "there", "there's", "these", "they",
    "they'd", "they'll", "re", "they've", "this", "those", "through",
    "to", "too", "under", "until", "up", "very", "was", "wasn't", "we",
    "we'd", "we'll", "we're", "we've", "were", "weren't", "what", "what's",
    "when", "when's", "where", "where's", "which", "while", "who", "who's",
    "whom", "why", "why's", "with", "won't", "would", "wouldn't", "you",
    "you'd", "you'll", "you're", "you've", "your", "yours", "yourself",
    "yourselves"
  )

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
      f"Total: ${posts.length} posts | Avg Score: $avgScore%.2f"
    }
  }

  /**
   * Consigna Lab 1 - Patrón de acumulación funcional.
   *
   * Usar foldLeft cuando la consigna pida acumular un total sin estado mutable.
   */
  def sumarScoresTotales(posts: List[Post]): Int = {
    // @FOLD_LEFT
    posts.foldLeft(0) { (total, post) => total + post._5 }
  }

  /**
   * Lab 1 - Ejercicio 3: filtra posts vacíos o irrelevantes.
   */
  def isRelevantPost(post: Post): Boolean = {
    // @FILTER_RELEVANT
    val title = post._2.trim
    title.nonEmpty && title != "Sin Título" && post._3.trim.nonEmpty
  }

  def filterRelevantPosts(posts: List[Post]): List[Post] = {
    posts.filter(isRelevantPost)
  }

  /**
   * Lab 1 - Ejercicio 5: palabras frecuentes que empiezan con mayúscula
   * y no son stopwords. Se agrupa en minúscula para contar equivalencias.
   */
  def capitalizedWordsTop(posts: List[Post], limit: Int): List[(String, Int)] = {
    // @STOPWORDS @TEXT_MINING
    posts
      .flatMap(post => TextProcessing.tokenize(s"${post._2} ${post._3}"))
      .filter(_.headOption.exists(_.isUpper))
      .map(_.toLowerCase)
      .filterNot(Stopwords.contains)
      .groupBy(identity)
      .map { case (word, occurrences) => (word, occurrences.length) }
      .toList
      .sortBy { case (word, count) => (-count, word) }
      .take(limit)
  }

  def capitalizedWordsBySubreddit(posts: List[Post], limit: Int): Map[String, List[(String, Int)]] = {
    groupBySubreddit(posts).map { case (subreddit, subredditPosts) =>
      subreddit -> capitalizedWordsTop(subredditPosts, limit)
    }
  }

  /**
   * Lab 1 - Ejercicio 6: primeros posts para el informe final.
   */
  def firstPostsSummary(posts: List[Post], limit: Int): List[(String, String, String)] = {
    posts.take(limit).map(post => (post._2, post._4, post._6))
  }

  /**
   * EJERCICIO 2 - Nueva Heurística: Extrae las menciones a usuarios más frecuentes
   *
   * Definición: Una mención es una palabra que comienza exactamente con "u/"
   * Ejemplo: "u/spez", "u/torvalds_right" son menciones válidas
   *
   * Pasos del pipeline funcional:
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
    // @TEXT_MINING
    posts
      .flatMap(post => TextProcessing.tokenize(post._3))
      .map(_.toLowerCase)
      .filter(mention => mention.startsWith("u/") && mention.length > 2)
      .groupBy(identity)
      .map { case (mention, occurrences) => (mention, occurrences.length) }
      .toList
      .sortBy { case (mention, count) => (-count, mention) }
      .take(limit)
  }

  /**
   * Genera un reporte por subreddit.
   *
   * Referencia: el filtro de minScore se aplica antes, en Main.scala.
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
      report.append(f"Avg Score: $avgScore%.2f\n")
      report.append(s"Max Score: ${posts.map(_._5).max}\n")
      report.append("Top User Mentions:\n")
      mentionsTop(posts, 3).foreach { case (mention, count) =>
        report.append(s"  - $mention: $count\n")
      }
    }
    report.toString()
  }

  /**
   * Lab 1 original - Ejercicio 6: informe pedido por la consigna.
   */
  def generateLab1Report(subreddit: String, posts: List[Post]): String = {
    // @REPORT_LAB1
    val cleanPosts = filterRelevantPosts(posts)
    val report = new StringBuilder
    report.append(s"## $subreddit\n")
    report.append(s"- Total score: ${sumarScoresTotales(cleanPosts)}\n")
    report.append("- Palabras frecuentes:\n")
    capitalizedWordsTop(cleanPosts, 10).foreach { case (word, count) =>
      report.append(s"  - $word: $count\n")
    }
    report.append("- Primeros posts:\n")
    firstPostsSummary(cleanPosts, 5).foreach { case (title, date, url) =>
      report.append(s"  - $title | $date | $url\n")
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
    posts.groupBy(_._1)
  }
}
