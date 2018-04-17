package com.tpalanga.reification.test

object ZApiProtocol {
  case class TicketStock(value: Int)

  case class TicketAvailabilities(stock: Map[String, TicketStock])

  case class TicketRequest(ticketType: Int, quantity: Int)
  case class LeaseResponse(leaseId: String)

  case class PaymentConfirmation(transactionKey: String, leaseId: String)
  case class DownloadResponse(downloadLink: String)
}
