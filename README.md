# 🧠 Reddit Scala Lab - Base de Estudio para Paradigmas Funcionales

Bienvenido a tu base de estudio personalizada para aprender **Scala** y **programación funcional** bajo la metodología del tutor de paradigmas.

---

## 🎯 Objetivo

Esta es una **base de código (skeleton)** donde practicarás tres ejercicios prototípicos basados exactamente en la estructura de evaluación de la cátedra:

1. **Ejercicio 1:** Extensión de Configuración y Filtrado
2. **Ejercicio 2:** Nueva Heurística de Minería de Texto  
3. **Ejercicio 3:** Tolerancia a Fallos en el Parseo

Cada ejercicio está preparado con espacios en blanco (TODOs) para que **implementes manualmente** mientras aprendes los conceptos.

---

## 📁 Estructura

```
test_lab1/
├── README.md                          ← Estás aquí
├── EJERCICIOS.md                      ← **COMIENZA AQUÍ**: Guía detallada de los 3 ejercicios
├── GUT.md                             ← Referencia técnica: opciones, tuplas, funciones, JSON4s
│
├── build.sbt                          ← Configuración del proyecto (dependencias)
│
├── src/main/scala/                   ← Código Scala a implementar
│   ├── FileIO.scala           [EJERCICIO 1 - PASO 1] Leer JSON con minScore
│   ├── RedditParser.scala     [EJERCICIO 3] Tolerancia a fallos en parseo
│   ├── Statistics.scala       [EJERCICIO 2] Heurística de minería de texto
│   ├── TextProcessing.scala   [Utilidades] Funciones auxiliares
│   └── Main.scala             [EJERCICIO 1 - PASO 2] Aplicar filtro de minScore
│
└── src/main/resources/
    └── subscriptions.json     ← Datos de prueba (JSON con config de suscripciones)
```

---

## ✅ Cómo Empezar (5 pasos)

### 1. Lee el Documento de Ejercicios

Abre **[EJERCICIOS.md](./EJERCICIOS.md)** y lee:
- **Conceptos Clave** (sección al inicio) - Entiende inmutabilidad, Option, tuplas
- **Ejercicio 1** completamente (incluye pseudocódigo)
- Luego los ejercicios 2 y 3

### 2. Consulta la Referencia Técnica

Si necesitas recordar cómo funciona `flatMap`, `groupBy`, JSON4s, abre **[GUT.md](./GUT.md)**. Es tu mano derecha.

### 3. Implementa Ejercicio 1 (Paso 1)

**Archivo:** [src/main/scala/FileIO.scala](./src/main/scala/FileIO.scala)

- Busca la función `readSubscriptions()`
- Implementa la lógica de lectura del archivo JSON
- Retorna `Some(List[Subscription])` con el nuevo campo `minScore`
- Nota: `minScore` puede venir como número o como cadena; extraerlo con `extractOpt[Int]` o con `extractOpt[String].map(_.toInt)` y usar `0` por defecto.

### 4. Implementa Ejercicio 1 (Paso 2)

**Archivo:** [src/main/scala/Main.scala](./src/main/scala/Main.scala)

- Busca el TODO `EJERCICIO 1 - Paso 2`
- Implementa el `flatMap` que descarga feeds
- Aplica el filtro: `posts.filter(post => post._5 >= minScore)`

### 5. Implementa Ejercicio 2

**Archivo:** [src/main/scala/Statistics.scala](./src/main/scala/Statistics.scala)

- Busca la función `mentionsTop()`
- Arma el pipeline funcional de 7 pasos
- Integra en `generateSubredditReport()` para imprimir

### 6. Implementa Ejercicio 3

**Archivo:** [src/main/scala/RedditParser.scala](./src/main/scala/RedditParser.scala)

- Busca la función `parsePosts()`
- Implementa tolerancia a fallos: extrae `title` con fallback a `"Sin Título"`
- Mantén el for-comprehension limpio

---

## 🔨 Compilar y Ejecutar

### Requisitos
```bash
# Verifica que tengas sbt instalado
sbt --version
```

### Compilar
```bash
cd test_lab1
sbt compile
```

### Ejecutar
```bash
sbt run
```

### Compilar y Ejecutar Juntos
```bash
sbt "compile; run"
```

### Modo Watch (recompila automáticamente)
```bash
sbt "~run"
```

### Testear
```bash
sbt test
```

---

## 📚 Flujo de Aprendizaje Recomendado

```
Día 1 - Conceptos
├─ Lee EJERCICIOS.md (Conceptos Clave)
├─ Lee GUT.md (Referencia de Option, Tuplas, Funciones)
└─ Haz EJ1 Paso 1 (familiarizarse con Option y JSON4s)

Día 2 - Composición Funcional
├─ Repasa "rechazo a mutación" en EJERCICIOS.md
├─ Haz EJ1 Paso 2 (flatMap, filter)
└─ Haz EJ2 (pipeline de alto orden: map, filter, groupBy, sort)

Día 3 - Manejo de Errores
├─ Lee "Tolerancia a Fallos" en EJERCICIOS.md
├─ Haz EJ3 (Option dentro de for-comprehension)
└─ Integra todo in Main.scala
```

---

## 💡 Consejos Importantes

### ❌ NO hagas esto:

```scala
var count = 0
for (word <- words) {
  if (word.startsWith("u/")) {
    count += 1
  }
}
```

### ✅ Haz esto en su lugar:

```scala
words
  .filter(_.startsWith("u/"))
  .length
```

### Si ves un `var` en tu código, DETENTE

La mutación es el enemigo en programación funcional. Siempre hay una forma más elegante con `map`, `filter`, `fold`, etc.

---

## 🚀 Checklist Antes de Entregar

Antes de ir al examen, verifica:

- [ ] Ejercicio 1 - Paso 1 compila sin errores
- [ ] Ejercicio 1 - Paso 2 imprime cantidad de posts filtrados
- [ ] Ejercicio 2 imprime top 3 menciones por subreddit
- [ ] Ejercicio 3 maneja posts sin titulo (imprime "Sin Título")
- [ ] Código sin `var` (solo `val`)
- [ ] Código sin loops imperativos (solo `map`, `filter`, `flatMap`, etc.)
- [ ] Compilación con `sbt compile` sin warnings

---

## 📞 Guía de Errores Comunes

### Error: `value _5 is not a member of Option[...]`

**Causa:** Intentaste acceder a tupla dentro de Option

**Solución:**
```scala
// ❌ Incorrecto
val score = post._5  // Si post es Option[Post]

// ✅ Correcto
val score = post.map(_._5)
```

### Error: `type mismatch; found : String required: Int`

**Causa:** Tipos dispares

**Solución:** Convierte explícitamente:
```scala
val minScore = s.getOrElse("minScore", "0").toInt  // String a Int
```

### Error: `No viable alternative`

**Causa:** Sintaxis incorrecta (paréntesis, llaves, punto y coma)

**Solución:** Revisa paréntesis y llaves. Scala no requiere `;` pero aparecen en for-comprehension

---

## 📖 Recursos Externos

- [Scala 2.13 Collections](https://docs.scala-lang.org/overviews/collections-2.13/overview.html)
- [Option Documentation](https://www.scala-lang.org/api/2.13.x/scala/Option.html)
- [JSON4s Quick Start](https://github.com/json4s/json4s#quick-start)

---

## 🏁 Próximos Pasos

Una vez que termines los 3 ejercicios:

1. Prueba con diferentes URLs de Reddit
2. Modifica `subscriptions.json` para agregar más subreddits
3. Intenta agregar nuevas heurísticas (más filtros, estadísticas)
4. Prepárate mentalmente: el examen seguirá exactamente esta estructura

---

## 🌐 Compartir en GitHub (Para Tutores/Profesores)

Si estás preparando este laboratorio para compartir con otros estudiantes:

1. **Lee la guía completa:** [GITHUB_DEPLOYMENT.md](./GITHUB_DEPLOYMENT.md)
2. **Crear repositorio:** Ve a https://github.com/new
3. **Conectar local:** `git remote add origin https://github.com/USERNAME/repo-name.git`
4. **Pushear:** `git push -u origin main`

**Resultado:** Tu laboratorio estará disponible en GitHub para que otros clonen y practiquen.

---

**¡Buena suerte! 🚀**

Si tienes dudas, refiere a:
- **EJERCICIOS.md** para explicación conceptual
- **GUT.md** para referencia técnica rápida
- Código comentado en cada archivo `.scala`
