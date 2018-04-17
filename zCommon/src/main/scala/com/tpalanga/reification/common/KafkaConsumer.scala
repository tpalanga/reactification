package com.tpalanga.reification.common

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

import scala.concurrent.{ExecutionContext, Future}

class KafkaConsumer(handler: String => Future[Any])
                   (implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext) extends LazyLogging {
  val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
    .withBootstrapServers("localhost:9092")
    .withGroupId("group1")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  Consumer.committableSource(consumerSettings, Subscriptions.topics(KafkaCommon.kafkaOrdersTopic))
    .mapAsync(1) { msg =>
      Future {
        logger.info(s"=== Received: ${msg.record.value}")
      }.flatMap(_ => handler(msg.record.value))
        .map(_ => msg)
    }
    .mapAsync(1) { msg =>
      msg.committableOffset.commitScaladsl()
    }
    .runWith(Sink.ignore)
}
