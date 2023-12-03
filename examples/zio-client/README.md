# ZIO Client example

This is a minimalist CLI client for Pokémon Showdown using the ZIO module.

## Run the project

To run the project, use the following command:

```shell
./millw examples.zio-client.run
```

Note: use `./millw.bat` on Windows.

## Usage

Here is the list of the commands provided by the client:

- `debug`: toggle debug mode/show received messages from the server.
- `login`: login to an account
- `show teams`: show the teams of the current battle (aka battle of the currently used room).
- `show active`: show the active pokémon of the current battle.
- `stop`: close the connection and stop the client.
- `use [room]`: join and select the given `room`. Return to lobby/global room if empty.

Every other messages (including Showdown's commands starting with a `/`) are sent to the server.
This usually results in a chat message sent to the current room or a user command (e.g `/pm`).

### Battle

You can see the [`show teams` and `show active` commands](#usage) to show battle information.

When a choice request from the server is received (usually at the end of each turn), you can display it to the console using `choice`.
You can use either `/move <slot>` or `/switch <slot>` to choose an action. Use `/undo` to cancel it.

See also [Showdown's action choice documentation](https://github.com/smogon/pokemon-showdown/blob/master/sim/SIM-PROTOCOL.md#possible-choices).

