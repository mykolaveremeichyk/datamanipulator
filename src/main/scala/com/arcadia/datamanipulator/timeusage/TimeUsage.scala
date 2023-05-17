package com.arcadia.datamanipulator.timeusage

import org.apache.spark.sql.functions.{avg, col, round, when}
import org.apache.spark.sql.{Column, DataFrame, SparkSession}

import scala.annotation.tailrec

object TimeUsage extends TimeUsageInterface {
  val spark: SparkSession =
    SparkSession
      .builder()
      .appName("Time Usage")
      .getOrCreate()



  // For implicit conversions like converting RDDs to DataFrames

  import spark.implicits.StringToColumn

  /** Main function */
  def main(args: Array[String]): Unit = {
    timeUsageByLifePeriod()
    spark.close()
  }

  def timeUsageByLifePeriod(): Unit = {
    val (columns, initDf) = read("src/main/resources/timeusage/atussum.csv")
    val (primaryNeedsColumns, workColumns, otherColumns) = classifiedColumns(columns)

    println(columns.size)
    println("**** CLASSIFIED FIELDS ****")
    println(primaryNeedsColumns)
    println(workColumns)
    println(otherColumns)
    println(primaryNeedsColumns.size + workColumns.size + otherColumns.size)
    println("**** CLASSIFIED FIELDS - END ****")

    val summaryDf = timeUsageSummary(primaryNeedsColumns, workColumns, otherColumns, initDf)
    summaryDf.show()
    val finalDf = timeUsageGrouped(summaryDf)
    finalDf.show()
  }

  /** @return The read DataFrame along with its column names. */
  def read(path: String): (List[String], DataFrame) = {
    val df = spark.read.options(Map("header" -> "true", "inferSchema" -> "true")).csv(path)
    (df.schema.fields.map(_.name).toList, df)
  }

  /** @return The initial data frame columns partitioned in three groups: primary needs (sleeping, eating, etc.),
   *          work and other (leisure activities)
   * @see https://www.kaggle.com/bls/american-time-use-survey
   *
   *      The dataset contains the daily time (in minutes) people spent in various activities. For instance, the column
   *      “t010101” contains the time spent sleeping, the column “t110101” contains the time spent eating and drinking, etc.
   *
   *      This method groups related columns together:
   *      1. “primary needs” activities (sleeping, eating, etc.). These are the columns starting with “t01”, “t03”, “t11”,
   *         “t1801” and “t1803”.
   *         2. working activities. These are the columns starting with “t05” and “t1805”.
   *         3. other activities (leisure). These are the columns starting with “t02”, “t04”, “t06”, “t07”, “t08”, “t09”,
   *         “t10”, “t12”, “t13”, “t14”, “t15”, “t16” and “t18” (those which are not part of the previous groups only).
   */
  def classifiedColumns(columnNames: List[String]): (List[Column], List[Column], List[Column]) = {
    val primary = List[Column]()
    val working = List[Column]()
    val leisure = List[Column]()

    classifyColumnsRec(columnNames, primary, working, leisure)
  }

  @tailrec
  def classifyColumnsRec(columnNames: List[String], primary: List[Column], working: List[Column], leisure: List[Column]): (List[Column], List[Column], List[Column]) =
    columnNames match {
      case Nil => (primary, working, leisure)

      case x :: xs =>
        if (x.startsWith("t01") || x.startsWith("t03") || x.startsWith("t11") || x.startsWith("t1801") || x.startsWith("t1803"))
          classifyColumnsRec(xs, col(x) :: primary, working, leisure)
        else if (x.startsWith("t05") || x.startsWith("t1805"))
          classifyColumnsRec(xs, primary, col(x) :: working, leisure)
        else if (x.startsWith("t02") || x.startsWith("t04") || x.startsWith("t06") || x.startsWith("t07") || x.startsWith("t08") || x.startsWith("t09") ||
          x.startsWith("t10") || x.startsWith("t12") || x.startsWith("t13") || x.startsWith("t14") || x.startsWith("t15") || x.startsWith("t16") || x.startsWith("t18"))
          classifyColumnsRec(xs, primary, working, col(x) :: leisure)
        else
          classifyColumnsRec(xs, primary, working, leisure)
    }

  /** @return a projection of the initial DataFrame such that all columns containing hours spent on primary needs
   *          are summed together in a single column (and same for work and leisure). The “teage” column is also
   *          projected to three values: "young", "active", "elder".
   * @param primaryNeedsColumns List of columns containing time spent on “primary needs”
   * @param workColumns         List of columns containing time spent working
   * @param otherColumns        List of columns containing time spent doing other activities
   * @param df                  DataFrame whose schema matches the given column lists
   *
   *                            This methods builds an intermediate DataFrame that sums up all the columns of each group of activity into
   *                            a single column.
   *
   *                            The resulting DataFrame should have the following columns:
   *                            - working: value computed from the “telfs” column of the given DataFrame:
   *   - "working" if 1 <= telfs < 3
   *   - "not working" otherwise
   *     - sex: value computed from the “tesex” column of the given DataFrame:
   *   - "male" if tesex = 1, "female" otherwise
   *     - age: value computed from the “teage” column of the given DataFrame:
   *   - "young" if 15 <= teage <= 22,
   *   - "active" if 23 <= teage <= 55,
   *   - "elder" otherwise
   *     - primaryNeeds: sum of all the `primaryNeedsColumns`, in hours
   *     - work: sum of all the `workColumns`, in hours
   *     - other: sum of all the `otherColumns`, in hours
   *
   * Finally, the resulting DataFrame should exclude people that are not employable (ie telfs = 5).
   *
   *                            Note that the initial DataFrame contains time in ''minutes''. You have to convert it into ''hours''.
   */
  def timeUsageSummary(
                        primaryNeedsColumns: List[Column],
                        workColumns: List[Column],
                        otherColumns: List[Column],
                        df: DataFrame
                      ): DataFrame = {
    // Transform the data from the initial dataset into data that make
    // more sense for our use case
    // Hint: you can use the `when` and `otherwise` Spark functions
    // Hint: don’t forget to give your columns the expected name with the `as` method
    val workingStatusProjection: Column = when(col("telfs") >= 1 && col("telfs") < 3, "working")
      .otherwise("not working").as("working")
    println("1:")
    println(workingStatusProjection)

    val sexProjection: Column = when(col("tesex").equalTo(1), "male")
      .otherwise("female").as("sex")
    println("2:")
    println(sexProjection)

    val ageProjection: Column = when(col("teage") >= 15 && col("teage") < 22, "young")
      .when(col("teage") >= 23 && col("teage") < 55, "active")
      .otherwise("elder").as("age")
    println("3:")
    println(ageProjection)

    // Create columns that sum columns of the initial dataset
    // Hint: you want to create a complex column expression that sums other columns
    //       by using the `+` operator between them
    // Hint: don’t forget to convert the value to hours
    val primaryNeedsProjection: Column = (primaryNeedsColumns.reduce(_ + _) / 60).as("primaryNeeds")
    println("4:")
    println(primaryNeedsProjection)

    val workProjection: Column = (workColumns.reduce(_ + _) / 60).as("work")
    println("5:")
    println(workProjection)

    val otherProjection: Column = (otherColumns.reduce(_ + _) / 60).as("other")
    println("6:")
    println(otherProjection)
    df
      .select(workingStatusProjection, sexProjection, ageProjection, primaryNeedsProjection, workProjection, otherProjection)
      .where($"telfs" <= 4) // Discard people who are not in labor force
  }


  /** @return the average daily time (in hours) spent in primary needs, working or leisure, grouped by the different
   *          ages of life (young, active or elder), sex and working status.
   * @param summed DataFrame returned by `timeUsageSumByClass`
   *
   *               The resulting DataFrame should have the following columns:
   *               - working: the “working” column of the `summed` DataFrame,
   *               - sex: the “sex” column of the `summed` DataFrame,
   *               - age: the “age” column of the `summed` DataFrame,
   *               - primaryNeeds: the average value of the “primaryNeeds” columns of all the people that have the same working
   *                 status, sex and age, rounded with a scale of 1 (using the `round` function),
   *               - work: the average value of the “work” columns of all the people that have the same working status, sex
   *                 and age, rounded with a scale of 1 (using the `round` function),
   *               - other: the average value of the “other” columns all the people that have the same working status, sex and
   *                 age, rounded with a scale of 1 (using the `round` function).
   *
   *               Finally, the resulting DataFrame should be sorted by working status, sex and age.
   */
  def timeUsageGrouped(summed: DataFrame): DataFrame = {
    summed.groupBy(col("working"), col("sex"), col("age"))
      .agg(round(avg("primaryNeeds"), 1).as("primaryNeeds"),
        round(avg("work"), 1).as("work"),
        round(avg("other"), 1).as("other")
      )
      .sort("working", "sex", "age")
  }
}