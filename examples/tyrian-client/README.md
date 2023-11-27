# API demo using Tyrian

This is a minimal Pokemon Showdown client written using the Tyrian module.
[Tyrian](https://tyrian.indigoengine.io) is a Scala library used to make Web graphical user interfaces.

## Run the project

You first need to build the project using the following command in the repository's root (not in `tyrian-client`):

```shell
./millw examples.tyrian-client.fastLinkJS
```

Note: use `./millw.bat` on Windows.

Then, you need to open the `index.html` file in this directory. Due to the official server's security policy, you might
need to launch your browser using the `--disable-web-security` (on Chrome and variants) flag and set a user directory
using `--user-data-dir` (on Chrome and variants).

## Usage

When you start the client, you firstly need to enter your account's name and password. Note that this demo only supports
connection to a registered account.

Once connected, you can:
- Connect to rooms
- Send chat messages and private messages
- Challenge someone by PM-ing `/challenge <user>, <format>`
- Battle (basic features)

## Gallery

- [Chat demo (before CSS was added)](https://youtu.be/5Y5f-AaLLxk)
- [Battle demo](https://youtu.be/cYA8XPWYVRo)