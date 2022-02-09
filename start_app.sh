export SPARK_HOME=/Users/hadoop/Apps/spark-2.3.1-bin-hadoop2.7

spark-submit --master yarn-client --num-executors 2 --executor-memory 1G target/scala-2.11/Kafka-Spark-assembly-0.1-SNAPSHOT.jar
