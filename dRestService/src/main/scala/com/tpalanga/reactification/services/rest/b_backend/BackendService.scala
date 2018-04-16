package com.tpalanga.reactification.services.rest.b_backend

import com.tpalanga.reification.common.Protocol.{DocumentId, TicketId, TicketOrder, UserDetails}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future

class BackendService extends LazyLogging {

  def createPrintTicket(ticketOrder: TicketOrder): Future[DocumentId] = {
    logger.info(s"createPrintTicket - ${ticketOrder.tickets}, ${ticketOrder.userDetails}, ${ticketOrder.transactionKey}")
    Future.successful(DocumentId("tickets/000578.pdf"))
  }
}
