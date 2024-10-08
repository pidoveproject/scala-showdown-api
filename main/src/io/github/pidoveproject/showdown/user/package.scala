package io.github.pidoveproject.showdown.user

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

opaque type AvatarName = String :| Not[Blank]
object AvatarName extends RefinedTypeOps[String, Not[Blank], AvatarName]

opaque type UserList = List[User] :| Pure
object UserList extends RefinedTypeOps[List[User], Pure, UserList]:

  def from(names: User*): UserList = List(names*).assume

  val empty: UserList = List.empty.assume

opaque type Username = String :| Not[Blank]
object Username extends RefinedTypeOps[String, Not[Blank], Username]
