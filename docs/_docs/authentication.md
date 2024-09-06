---
title: Authentication
---

# Authentication

The authentication process is similar accross all [client implementations](clients/index.md).

## Getting the ChallStr

Once connected, the server sends to the client a [[GlobalMessage.ChallStr|io.github.pidoveproject.showdown.protocol.server.GlobalMessage.ChallStr]] containing a session token needed to begin the authentication process.

## Login process

Once the [[ChallStr|io.github.pidoveproject.showdown.ChallStr]] is received, it can be passed to whether [[ShowdownClient#login|io.github.pidoveproject.showdown.client.ShowdownClient.login]] to log to a registered account or [[ShowdownClient#loginGuest|io.github.pidoveproject.showdown.client.ShowdownClient.loginGuest]].

These methods will return a response containing a token (often called "assertion") used to confirm the login process using [[ShowdownConnection#confirmLogin|io.github.pidoveproject.showdown.client.ShowdownConnection.confirmLogin]].

## Example

A minimal example can be found in [the ping-pong bot example](https://github.com/pidoveproject/scala-showdown-api/blob/4930ebbfaf724019316c33648d8220e90cbcd870/examples/sync-ping-pong/src/io/github/pidoveproject/showdown/example/PingPongBot.scala#L20).