package io.github.pidoveproject.showdown

import zio.json.internal.{RetractReader, Write}
import zio.json.*
import zio.json.ast.Json

import scala.util.Left

object json:

  private def blankNoneDecoder: JsonDecoder[None.type] =
    JsonDecoder.string.mapOrFail:
      case data if data.isBlank => Right(None)
      case _                    => Left("This String is not blank")

  given someOrEmptyDecoder[A: JsonDecoder]: JsonDecoder[Option[A]] =
    blankNoneDecoder <> JsonDecoder.option[A]

  given nonEmptyListOrEmpty[A](using decoder: JsonDecoder[List[A]]): JsonDecoder[List[A]] with

    override def unsafeDecodeMissing(trace: List[JsonError]): List[A] = List.empty

    override def unsafeDecode(trace: List[JsonError], in: RetractReader): List[A] = decoder.unsafeDecode(trace, in)

    override def unsafeFromJsonAST(trace: List[JsonError], json: Json): List[A] = decoder.unsafeFromJsonAST(trace, json)


  given someOrEmptyEncoder[A](using encoder: JsonEncoder[A]): JsonEncoder[Option[A]] =
    new JsonEncoder[Option[A]]:

      def unsafeEncode(oa: Option[A], indent: Option[Int], out: Write): Unit = oa match
        case None    => out.write("\"\"")
        case Some(a) => encoder.unsafeEncode(a, indent, out)

      override def isNothing(oa: Option[A]): Boolean =
        oa match
          case None    => false
          case Some(a) => encoder.isNothing(a)

      override final def toJsonAST(oa: Option[A]): Either[String, Json] =
        oa match
          case None    => Right(Json.Str(""))
          case Some(a) => encoder.toJsonAST(a)
