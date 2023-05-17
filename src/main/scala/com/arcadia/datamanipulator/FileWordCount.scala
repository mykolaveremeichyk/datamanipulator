package com.arcadia.datamanipulator

import org.apache.log4j._
import org.apache.spark.{SparkConf, SparkContext}

object FileWordCount extends App {

  Logger.getLogger("org").setLevel(Level.ERROR)

  println("HELLO WORLD!!!")
  val conf = new SparkConf()
  val sc = new SparkContext(conf)
  val lines = sc.textFile("/opt/spark-data/u.data")
  println("RDD initialized")
  val numLines = lines.count()
  println(s"Hello world! The u.data file has $numLines lines.")

  sc.stop()
}
