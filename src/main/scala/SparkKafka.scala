import org.apache.spark._
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext._
import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.streaming._
import org.apache.spark.storage.StorageLevel

import org.apache.kafka.common.serialization.StringDeserializer

import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent

object SparkKafka {

  def main(args: Array[String]) {

    // Configue Elasticseach connection
    val conf = new SparkConf().
      setAppName("kafka").
      setMaster("local[*]")

    val sc = new SparkContext(conf) 
    val ssc = new StreamingContext(sc, Seconds(5))

    sc.setLogLevel("ERROR")
	
    
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "group1",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val topics = Array("test1")

    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )

    // Output must be idempotent
    val out = stream.map(record => record.value())
    //out.print(10)
    out.foreachRDD(r => {
      r.foreach(println)
    })
    out.saveAsTextFiles("/user/hadoop/kafka/data")

    // Commit offsets to a special Kafka topic to ensure recovery from a failure
    stream.foreachRDD { rdd =>
      val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      stream.asInstanceOf[CanCommitOffsets].commitAsync(offsetRanges)
    }

    ssc.start()
    ssc.awaitTermination()
  }  
}

