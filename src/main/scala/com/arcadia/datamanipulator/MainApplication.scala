package com.arcadia.datamanipulator

import org.apache.log4j._
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Use this to test the app locally, from sbt:
  * sbt "run inputFile.txt outputFile.txt"
  *  (+ select CountingLocalApp when prompted)
  */
object MainApplicationLocal extends App {
//  val (inputFile, outputFile) = (args(0), args(1))
  val conf = new SparkConf()
    .setMaster("local")
    .setAppName("my awesome app")

  val sc = new SparkContext(conf)
  SparkPi.calculatePi(sc)
//  Runner.run(conf, inputFile, outputFile)
}

/**
  * Use this when submitting the app to a cluster with spark-submit
  * */
object MainApplication extends App {
  // spark-submit command should supply all necessary config elements
//  Runner.run(new SparkConf(), inputFile, outputFile)
  val sc = new SparkContext(new SparkConf())
  SparkPi.calculatePi(sc)
}

object MainWordCount extends App {

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

object Runner {
  def run(conf: SparkConf, inputFile: String, outputFile: String): Unit = {
    val sc = new SparkContext(conf)
    val rdd = sc.textFile(inputFile)
    val counts = WordCount.withStopWordsFiltered(rdd)
    counts.saveAsTextFile(outputFile)
  }
}
