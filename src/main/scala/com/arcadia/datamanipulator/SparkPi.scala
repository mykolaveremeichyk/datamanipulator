package com.arcadia.datamanipulator

import org.apache.spark.SparkContext
import scala.math.random

object SparkPi {
  def calculatePi(sc: SparkContext): Unit = {
    println("Hello. I'm PI runner.")
    val slices = 2
    val n = math.min(100000L * slices, Int.MaxValue).toInt // avoid overflow
    val count = sc.parallelize(1 until n, slices).map { i =>
      val x = random * 2 - 1
      val y = random * 2 - 1
      if (x * x + y * y <= 1) 1 else 0
    }.reduce(_ + _)
    println(s"Pi is roughly ${4.0 * count / (n - 1)}")
  }
}
