package io.github.projectpidove.showdown

import io.github.projectpidove.showdown.protocol.{MessageDecoder, ProtocolError}

given challStrDecoder: MessageDecoder[ChallStr] =
  MessageDecoder.string
    .repeatUntilEnd
    .mapEither(list => ChallStr.either(list.mkString("|")).left.map(x => ProtocolError.InvalidInput(x, "Invalid Challstr")))
