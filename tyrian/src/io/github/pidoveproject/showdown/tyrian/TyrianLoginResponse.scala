package io.github.pidoveproject.showdown.tyrian

import io.github.pidoveproject.showdown.protocol.{LoginResponse, ProtocolError}
import tyrian.http.{HttpError, Response}
import zio.json.*

/**
 * A Tyrian event related to the current user's login.
 */
enum TyrianLoginResponse:

  /**
   * The user is logging in to a registered account.
   *
   * @param response the login response sent by the server
   */
  case LogUser(response: LoginResponse)

  /**
   * The user is logging in as a guest.
   *
   * @param assertion the token used to finish the login process
   */
  case LogGuest(assertion: String)

  /**
   * An error occurred while logging in.
   */
  case Error(error: ProtocolError)

object TyrianLoginResponse:

  /**
   * Create a [[TyrianLoginResponse]] from Tyrian's HTTP response event.
   *
   * @param response the HTTP response from Tyrian's HTTP API
   * @return a new [[TyrianLoginResponse]] created from the passed HTTP response
   */
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