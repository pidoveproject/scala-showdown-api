package io.github.projectpidove.showdown.battle

import io.github.projectpidove.showdown.protocol.{MessageDecoder, ProtocolError}
import MessageDecoder.*

given MessageDecoder[PlayerNumber] = MessageDecoder.string.mapEither(PlayerNumber.fromString)

given MessageDecoder[PokemonSlot] = MessageDecoder.char.mapEither(PokemonSlot.fromCode)

given MessageDecoder[Health] = MessageDecoder.string.mapEither(Health.fromString)