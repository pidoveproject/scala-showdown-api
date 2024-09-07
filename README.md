# Pokemon Showdown API for Scala

![License](https://img.shields.io/github/license/pidove-project/scala-showdown-api?color=red)
![Commit activity](https://img.shields.io/github/commit-activity/m/pidove-project/scala-showdown-api?color=orange)
[![CI](https://github.com/pidove-project/scala-showdown-api/actions/workflows/ci.yml/badge.svg)](https://github.com/pidove-project/scala-showdown-api/actions/workflows/ci.yml)
___

This is a wrapper of [Pokemon Showdown](https://pokemonshowdown.com/)'s
[API](https://github.com/smogon/pokemon-showdown/blob/master/PROTOCOL.md) for [Scala](https://scala-lang.org). It
allows developers to easily interact with Pokemon Showdown (either official or unofficial instances) to make clients,
bots and other tools.

## Table of contents

- [Importing the library](#Importing-the-library)
- [Usage](#Usage)
- [Contributing](#Contributing)
- [Useful links](#Useful-links)

## Importing the library

SBT:

```scala
libraryDependencies += "io.github.projectpidove" %% "scala-showdown-api" % "version"
```

Mill:

```scala
ivy"io.github.projectpidove::scala-showdown-api:version"
```

## Usage

The way to connect to Showdown depends on the module you are using. Check yours [here](https://pidoveproject.github.io/scala-showdown-api/docs/clients/index.html).

Information about methods and data types can be found in the [API Reference](https://pidove-project.github.io/scala-showdown-api)

## Contributing

To contribute to the project, you can
either [create a new issue](https://github.com/pidove-project/scala-showdown-api/issues/new/choose)
if you discovered a bug or have a feature to request, or contributing to the code by submitting
a [pull request](https://github.com/pidove-project/scala-showdown-api/pulls) and/or
solving [open issues](https://github.com/pidove-project/scala-showdown-api/issues)

### Building the project

If you want to contribute to the code or just compile the project yourself, you need to build the project:

```scala
mill <module>.compile
```

Where `<module>` is either:
- `main`: API's core logic and datatypes
- `cats`: Cats integration
- `tyrian`: Tyrian integration

### Building the documentation

You need to execute the following command:

```scala
mill docs.docJar
```

The documentation's target directory is `out/docs/docJar.dest/`

## Useful links

- [Documentation](https://pidoveproject.github.io/scala-showdown-api/docs)
- [API Reference](https://pidoveproject.github.io/scala-showdown-api)
- [Pokemon Showdown's protocol](https://github.com/smogon/pokemon-showdown/blob/master/PROTOCOL.md)
- [Pokemon Showdown's simulator protocol](https://github.com/smogon/pokemon-showdown/blob/master/sim/SIM-PROTOCOL.md)
- [Showdown's coding Discord server](https://discord.gg/Cbs4dKz)
