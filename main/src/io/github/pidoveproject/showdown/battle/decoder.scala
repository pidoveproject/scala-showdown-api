package io.github.pidoveproject.showdown.battle

import io.github.pidoveproject.showdown.protocol.MessageDecoder
import zio.json.JsonDecoder

given MessageDecoder[PlayerNumber] = MessageDecoder.string.mapEither(PlayerNumber.fromString)

given JsonDecoder[PlayerNumber] = JsonDecoder.string.mapOrFail(PlayerNumber.fromString(_).left.map(_.getMessage))

given MessageDecoder[PokemonSlot] = MessageDecoder.char.mapEither(PokemonSlot.fromCode)

given MessageDecoder[Health] = MessageDecoder.string.mapEither(Health.fromString)
