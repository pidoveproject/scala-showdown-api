package io.github.projectpidove.showdown.tyrian

import io.github.projectpidove.showdown.protocol.{LoginResponse, ProtocolError}
import tyrian.http.{HttpError, Response}
import zio.json.*

enum TyrianLoginResponse:
  case LogUser(response: LoginResponse)
  case LogGuest(assertion: String)
  case Error(error: ProtocolError)

object TyrianLoginResponse:

  def fromUserLoginResponse(response: Response): TyrianLoginResponse =
    response
      .body
      .tail
      .fromJson[LoginResponse]
      .fold(
        msg => Error(ProtocolError.InvalidInput(response.body, msg)),
        response => LogUser(response)
      )
    
  def fromGuestLoginResponse(response: Response): TyrianLoginResponse =
    LogGuest(response.body)

  def fromError(error: HttpError): TyrianLoginResponse = error match
    case HttpError.BadRequest(msg) => Error(ProtocolError.AuthentificationFailed(msg))
    case HttpError.Timeout => Error(ProtocolError.Miscellaneous("timeout"))
    case HttpError.NetworkError => Error(ProtocolError.Miscellaneous("network error"))