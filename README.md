# Reddit Scala Lab - Dashboard de Parcial

Este repositorio es una base de estudio y consulta rápida para el parcial de Lab 1. El código Scala en `src/main/scala` contiene soluciones de referencia compilables y los archivos `.md` explican cómo adaptarlas.

El objetivo no es solo practicar: también es poder abrir el repo durante el parcial, usar `Ctrl+F`, encontrar un patrón parecido al problema nuevo, copiarlo desde las guías y adaptarlo con cambios mínimos. Las fuentes contienen soluciones de referencia compilables; para practicar desde cero, ocultá una función y reconstruíla usando sus tests.

---

## 🎯 Dashboard de Parcial - Lab 1

### 🔍 Search Tags Rápidos

Busca estas etiquetas en el código o en las guías:

- `@JSON_PARSE` -> lectura de JSON local con `json4s` y `Option`.
- `@API_FETCH` -> descarga HTTP segura con `try/catch` y `Option[String]`.
- `@TOLERANCIA_FALLOS` -> diferencia entre campos laxos con `getOrElse` y campos estrictos en `for-comprehension`.
- `@TEXT_MINING` -> pipeline funcional de tokenización, filtros, `groupBy`, conteo y ordenamiento.
- `@FOLD_LEFT` -> acumulación sin estado mutable.
- `@FECHAS_UTC` -> conversión de timestamps UNIX usando `java.time`.
- `@STRICT_FIELDS` -> variante estricta: si falta un campo requerido, se descarta el post.
- `@FILTER_RELEVANT` -> filtro de posts vacíos, con espacios o sin título.
- `@STOPWORDS` -> conteo de palabras con mayúscula excluyendo stopwords.
- `@REPORT_LAB1` -> informe final del ejercicio 6 original.
- `@PARCIAL_2026_SUBSCRIPTIONS` -> `name`, `url`, `count`, `before` estrictos.
- `@URL_BUILDER` -> URL sin `?` ni parámetros duplicados.
- `@PARCIAL_2026_POSTS` -> `Post(title, selftext, author)`.
- `@KEYWORD_MATCH` -> conteo de palabras censuradas sin distinguir mayúsculas.
- `@CUSTOM_REPORT` -> salida del parcial evaluado.

### 🧭 Dónde Ir Según el Problema

| Si el parcial pide... | Mirá primero... |
|---|---|
| Leer un archivo JSON o tolerar campos faltantes | [FileIO.scala](./src/main/scala/FileIO.scala) y [GUT.md](./GUT.md) |
| Parsear una API con campos obligatorios y defaults | [RedditParser.scala](./src/main/scala/RedditParser.scala) |
| Contar menciones, palabras o frecuencias | [Statistics.scala](./src/main/scala/Statistics.scala) |
| Filtrar posts vacíos o sin título | `@FILTER_RELEVANT` en [Statistics.scala](./src/main/scala/Statistics.scala) |
| Contar palabras con mayúscula y stopwords | `@STOPWORDS` en [Statistics.scala](./src/main/scala/Statistics.scala) |
| Sumar scores o acumular resultados | `@FOLD_LEFT` en [Statistics.scala](./src/main/scala/Statistics.scala) |
| Armar el informe final del Lab 1 | `@REPORT_LAB1` en [Statistics.scala](./src/main/scala/Statistics.scala) |
| Entender el razonamiento paso a paso | [EJERCICIOS.md](./EJERCICIOS.md) |
| Defender conceptos del PDF | [GUIA_PARCIAL_LAB1.md](./GUIA_PARCIAL_LAB1.md) |
| Copiar plantillas genéricas | [GUT.md](./GUT.md) |
| Resolver el parcial evaluado 2026 | [PARCIAL_EVALUADO_2026.md](./PARCIAL_EVALUADO_2026.md) y [Parcial2026.scala](./src/main/scala/Parcial2026.scala) |

---

## 📁 Estructura

```text
test_lab1/
├── README.md                     <- Dashboard rápido
├── EJERCICIOS.md                 <- Mapa de resolución y ejercicios explicados
├── GUIA_PARCIAL_LAB1.md          <- Cobertura de PDFs y defensa conceptual
├── GUT.md                        <- Plantillas copiables y referencia técnica
├── PARCIAL_EVALUADO_2026.md      <- Auditoría y solución exacta del parcial
├── build.sbt                     <- Dependencias Scala, json4s, scalaj-http, ScalaTest
├── src/main/scala/
│   ├── FileIO.scala              <- @JSON_PARSE, @API_FETCH
│   ├── RedditParser.scala        <- @TOLERANCIA_FALLOS, @FECHAS_UTC
│   ├── Statistics.scala          <- @TEXT_MINING, @FOLD_LEFT
│   ├── TextProcessing.scala      <- tokenización y fechas UTC
│   ├── Parcial2026.scala         <- referencia compilable del parcial evaluado
│   └── Main.scala                <- flujo completo: leer, descargar, parsear, filtrar
├── src/main/resources/
│   └── subscriptions.json        <- datos de prueba
└── src/test/scala/reddit/        <- tests de aceptación del laboratorio
```

---

## 🚦 Flujo de Uso Recomendado

1. Abrí [EJERCICIOS.md](./EJERCICIOS.md) para reconocer qué patrón pide la consigna.
2. Abrí [GUIA_PARCIAL_LAB1.md](./GUIA_PARCIAL_LAB1.md) si necesitás defender por qué la solución es funcional.
3. Abrí [GUT.md](./GUT.md) y copiá la plantilla más parecida.
4. Buscá el tag correspondiente en `src/main/scala/` para copiar la implementación de referencia.
5. Adaptá nombres de campos, tipos de tupla y filtros.
6. Corré `sbt test`; los tests son el oráculo para saber si tu implementación quedó bien.

---

## 🔨 Comandos Útiles

```bash
sbt compile
sbt test
sbt run
```

El repositorio de referencia debe compilar y dejar todos los tests en verde. Después de adaptar una plantilla, corré nuevamente `sbt test`.

Modo watch:

```bash
sbt "~test"
```

---

## ✅ Checklist Antes del Parcial

- [ ] Sé ubicar `@JSON_PARSE` para leer JSON y convertirlo a tuplas.
- [ ] Sé decidir qué campos van con `getOrElse` y cuáles van dentro del `for-comprehension`.
- [ ] Sé explicar por qué `flatMap` sirve para aplanar listas u opciones.
- [ ] Sé escribir el pipeline `flatMap -> filter -> groupBy -> map -> sortBy -> take`.
- [ ] Sé filtrar posts irrelevantes con `@FILTER_RELEVANT`.
- [ ] Sé contar palabras con mayúscula excluyendo stopwords con `@STOPWORDS`.
- [ ] Sé usar `foldLeft` para acumular sin `var`.
- [ ] Sé armar el informe del Lab 1 con `@REPORT_LAB1`.
- [ ] Sé acceder a las posiciones de `Post`: `_1` subreddit, `_2` title, `_3` selftext, `_4` date, `_5` score, `_6` url.
- [ ] Después de adaptar una plantilla, `sbt test` sigue en verde.

---

## Errores Comunes

### `value _5 is not a member of Option[...]`

Intentaste acceder a una tupla que todavía está envuelta en `Option`.

```scala
// Incorrecto
val score = postOpt._5

// Correcto
val scoreOpt = postOpt.map(_._5)
```

### `NumberFormatException`

Convertiste un `String` con `.toInt` directo. Para parcial, preferí conversión segura:

```scala
import scala.util.Try

val n = Try(raw.trim.toInt).toOption.getOrElse(0)
```

### El `for-comprehension` descarta elementos

Eso es esperado si alguno de los campos obligatorios devuelve `None`. Si un campo debe tener default, extraelo fuera del `for`.

---

## Recursos

- [Scala Collections](https://docs.scala-lang.org/overviews/collections-2.13/overview.html)
- [Option API](https://www.scala-lang.org/api/2.13.x/scala/Option.html)
- [json4s Quick Start](https://github.com/json4s/json4s#quick-start)

Buena suerte. Este repo está pensado para que puedas pensar menos en la sintaxis accidental y más en reconocer el patrón correcto bajo presión.
