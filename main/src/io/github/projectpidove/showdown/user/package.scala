package io.github.projectpidove.showdown.user

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

type AvatarName = String :| Not[Blank]
object AvatarName extends RefinedTypeOpsImpl[String, Not[Blank], AvatarName]

type UserList = List[Username] :| Pure
object UserList extends RefinedTypeOpsImpl[List[Username], Blank, UserList]:

  def apply(names: Username*): UserList = List(names*).assume