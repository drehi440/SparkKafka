package com.rehi.sparkRun
import java.util.HashMap

import org.apache.kafka.clients.producer.{ KafkaProducer, ProducerConfig, ProducerRecord }
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._

object KafkaWordCount extends App {
/*  if (args.length < 4) {
    System.err.println("Usage: KafkaWordCount <zkQuorum><group> <topics> <numThreads>")
    System.exit(1)
  }*/

  val Array(zkQuorum, group, topics, numThreads) = Array("","","test1","2")
  val sparkConf = new SparkConf().setAppName("KafkaWordCount").setMaster("local[*]")
  val ssc = new StreamingContext(sparkConf, Seconds(2))
  ssc.checkpoint("checkpoint")

  val topicMap = topics.split(",").map((_, numThreads.toInt)).toMap
  val lines = KafkaUtils.createStream(ssc, zkQuorum, group, topicMap).map(_._2)
  val words = lines.flatMap(_.split(" "))
  val wordCounts = words.map(x => (x, 1L))
    .reduceByKeyAndWindow(_ + _, _ - _, Minutes(10), Seconds(2), 2)
  wordCounts.print()

  ssc.start()
  ssc.awaitTermination()
}