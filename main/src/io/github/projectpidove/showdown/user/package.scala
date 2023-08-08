package io.github.projectpidove.showdown.user

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

opaque type AvatarName = String :| Not[Blank]
object AvatarName extends RefinedTypeOpsImpl[String, Not[Blank], AvatarName]

opaque type UserList = List[Username] :| Pure
object UserList extends RefinedTypeOpsImpl[List[Username], Pure, UserList]:

  def from(names: Username*): UserList = List(names*).assume

  val empty: UserList = List.empty.assume