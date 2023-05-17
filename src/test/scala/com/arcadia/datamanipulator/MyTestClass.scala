package com.arcadia.datamanipulator

import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession

class MyTestClass extends munit.FunSuite {
  lazy val spark: SparkContext = {
    SparkSession
      .builder()
      .master("local")
      .appName("spark test example")
      .getOrCreate().sparkContext
  }

  test("hello") {
    val o = MainApplication
    val result = SparkPi.calculatePi(spark)
    assertEquals(result, 3.140975357048768)
  }
}
