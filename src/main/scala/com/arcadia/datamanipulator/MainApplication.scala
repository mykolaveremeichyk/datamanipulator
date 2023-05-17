package com.arcadia.datamanipulator

import com.arcadia.datamanipulator.MainApplication.pi
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
  val pi = SparkPi.calculatePi(sc)
  println(s"PI: $pi")
//  Runner.run(conf, inputFile, outputFile)
}

/**
  * Use this when submitting the app to a cluster with spark-submit
  * */
object MainApplication extends App {
  val sc: SparkContext = new SparkContext(new SparkConf())
  val pi = SparkPi.calculatePi(sc)
  println(s"PI: $pi")
}

object Runner {
  def run(conf: SparkConf, inputFile: String, outputFile: String): Unit = {
    val sc = new SparkContext(conf)
    val rdd = sc.textFile(inputFile)
    val counts = WordCount.withStopWordsFiltered(rdd)
    counts.saveAsTextFile(outputFile)
  }
}
