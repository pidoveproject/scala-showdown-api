package io.github.projectpidove.showdown.tyrian

type TyrianShowdownEvent[F[_]] = TyrianConnectEvent[F] | TyrianLoginResponse | TyrianServerEvent