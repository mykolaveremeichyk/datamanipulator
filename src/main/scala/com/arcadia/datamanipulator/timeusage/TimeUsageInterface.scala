package com.arcadia.datamanipulator.timeusage

import org.apache.spark.sql._

/**
 * The interface used by the grading infrastructure. Do not change signatures
 * or your submission will fail with a NoSuchMethodError.
 */
trait TimeUsageInterface {

  import org.apache.spark.sql.DataFrame

  def classifiedColumns(columnNames: List[String]): (List[Column], List[Column], List[Column])

  def timeUsageGrouped(summed: DataFrame): DataFrame

}
