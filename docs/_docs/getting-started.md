---
title: Getting started
---

# Getting started

## Dependency

Include Scala Showdown API in your project using your preferred build tool.

SBT: 

```scala 
libraryDependencies += "io.github.pidoveproject" %% "scala-showdown-api" % "version"
```

Mill:

```scala 
ivy"io.github.pidoveproject::scala-showdown-api:version"
```

JVM and [JS](https://scala-js.org/) platforms are supported.

## Connecting to the server

Scala Showdown API provides different implementations under the same interfaces: [[ShowdownClient|io.github.pidoveproject.showdown.client.ShowdownClient]] and [[ShowdownConnection|io.github.pidoveproject.showdown.client.ShowdownConnection]].

Check them [here](clients/index.md).

## Authentication

You can find details about authentication to an account [here](authentication.md).