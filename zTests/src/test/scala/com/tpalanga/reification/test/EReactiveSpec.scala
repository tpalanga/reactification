package com.tpalanga.reification.test

import com.softwaremill.sttp._
import com.softwaremill.sttp.akkahttp._
import com.softwaremill.sttp.json4s._
import org.scalatest.{AsyncFlatSpec, EitherValues, Matchers}

object EReactiveSpec {
  val eventId = "event1234"
  val baseUrl = s"http://localhost:8085/events/tickets/$eventId"
}

class EReactiveSpec extends AsyncFlatSpec with Matchers with EitherValues {

  import EReactiveSpec._
  import ZApiProtocol._

  implicit val backend = AkkaHttpBackend()

  "Reactive" should "process ticket requests and return confirmation message" in {

    val responseAvailabilities = sttp.get(uri"$baseUrl")
      .response(asJson[TicketAvailabilities])
    def responseBook(ticketType: Int, quantity: Int) = sttp
      .body(TicketRequest(ticketType, quantity))
      .post(uri"$baseUrl/book")
      .response(asJson[LeaseResponse])
    def responsePayment(transactionKey: String, leaseId: String) = sttp
      .body(PaymentConfirmation(transactionKey, leaseId))
      .post(uri"$baseUrl/paymentDone")

    for {
      r1 <- responseAvailabilities.send()
      availabilities = r1.body.right.value
      _ = availabilities shouldBe TicketAvailabilities(Map("1" -> TicketStock(100), "2" -> TicketStock(200)))
      r2 <- responseBook(1, 2).send()
      lease = r2.body.right.value
      _ = lease shouldBe a[LeaseResponse]
      leaseId = lease.leaseId
      _ = println(s"leaseId: $leaseId")
      r3 <- responsePayment("PayKey00123", leaseId).send()
      msg = r3.body.right.value
      _ = msg shouldBe "Order confirmed"
      // verify that the document was created and emailed
    } yield {
      backend.close()
      1 shouldBe 1
    }

  }

}
