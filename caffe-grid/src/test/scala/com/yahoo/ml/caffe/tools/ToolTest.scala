// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.ml.caffe.tools

import com.yahoo.ml.caffe.Config

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.{FunSuite, BeforeAndAfterAll}
import org.slf4j.{Logger, LoggerFactory}
import org.testng.Assert._

class ToolTest extends FunSuite with BeforeAndAfterAll {
  val log: Logger = LoggerFactory.getLogger(this.getClass)
  var sc : SparkContext = null

  override def beforeAll() = {
    sc = new SparkContext(new SparkConf().setAppName("caffe-on-spark").setMaster("local"))
  }

  override def afterAll() = {
    sc.stop()
  }

  test("Binary2Sequence") {
    val ROOT_PATH = {
      val fullPath = getClass.getClassLoader.getResource("log4j.properties").getPath
      fullPath.substring(0, fullPath.indexOf("caffe-grid/"))
    }
    val args = Array(
      "-imageRoot", "file:"+ROOT_PATH+"data/images",
      "-labelFile", "file:"+ROOT_PATH+"data/images/labels.txt",
      "-output", "file:"+ROOT_PATH+"caffe-grid/target/seq_files"
    )
    val conf = new Config(sc, args)

    val path = new Path(conf.outputPath)
    val fs = path.getFileSystem(new Configuration)
    if (fs.exists(path)) fs.delete(path, true)

    val rdd = new Binary2Sequence(sc, conf).makeRDD()

    //check file size
    assertEquals(rdd.count(), sc.textFile(conf.labelFile).count())
  }
}
