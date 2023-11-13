package io.github.projectpidove.showdown.tyrian

/**
 * A Tyrian event related to the Showdown client.
 * 
 * @tparam F the effect type of the Tyrian app
 */
type TyrianShowdownEvent[F[_]] = TyrianConnectEvent[F] | TyrianLoginResponse | TyrianServerEvent