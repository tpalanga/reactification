package com.tpalanga.reification.common

import spray.json.{DefaultJsonProtocol, JsString, JsValue, RootJsonFormat}

object Protocol {
  case class TicketType(id: Int)
  case class TicketStock(value: Int)
  case class TicketAvailabilities(stock: Map[TicketType, TicketStock])

  case class TicketId(serialNo: String)
  case class DocumentId(filePath: String)

  case class TicketRequest(ticketType: Int, quantity: Int)

  case class LeaseResponse(leaseId: String)

  case class DownloadResponse(downloadLink: String)

  case class LeaseError(msg: String)

  case class PaymentConfirmation(transactionKey: String, leaseId: String)

  case class LeaseId(value: String)

  sealed trait LeaseConfirmation
  case class Accepted(leaseId: LeaseId) extends LeaseConfirmation
  case class Denied(msg: String) extends LeaseConfirmation

  case class UserDetails(email: String, name: String, phone: String)

  case class TicketOrder(tickets: Seq[TicketId], userDetails: UserDetails, transactionKey: String)

  object JsonFormats extends DefaultJsonProtocol {
    implicit val ticketTypeFormat: RootJsonFormat[TicketType] = new RootJsonFormat[TicketType] {
      override def write(obj: TicketType): JsValue = JsString(obj.id.toString)

      override def read(json: JsValue): TicketType = json match {
        case JsString(id) => TicketType(id.toInt)
        case x => throw new Exception(s"Could not parse TicketType from $x")
      }
    }
    implicit val documentIdFormat: RootJsonFormat[DocumentId] = jsonFormat1(DocumentId)
    implicit val ticketIdFormat: RootJsonFormat[TicketId] = jsonFormat1(TicketId)
    implicit val ticketStockFormat: RootJsonFormat[TicketStock] = jsonFormat1(TicketStock)
    implicit val ticketAvailabilitiesFormat: RootJsonFormat[TicketAvailabilities] = jsonFormat1(TicketAvailabilities)
    implicit val ticketRequestFormat: RootJsonFormat[TicketRequest] = jsonFormat2(TicketRequest)
    implicit val leaseResponseFormat: RootJsonFormat[LeaseResponse] = jsonFormat1(LeaseResponse)
    implicit val leaseErrorFormat: RootJsonFormat[LeaseError] = jsonFormat1(LeaseError)
    implicit val paymentConfirmationFormat: RootJsonFormat[PaymentConfirmation] = jsonFormat2(PaymentConfirmation)
    implicit val downloadResponseFormat: RootJsonFormat[DownloadResponse] = jsonFormat1(DownloadResponse)
    implicit val userDetailsFormat: RootJsonFormat[UserDetails] = jsonFormat3(UserDetails)
    implicit val ticketOrderFormat: RootJsonFormat[TicketOrder] = jsonFormat3(TicketOrder)
  }

}
