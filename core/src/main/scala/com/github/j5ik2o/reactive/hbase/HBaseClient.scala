package com.github.j5ik2o.reactive.hbase

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{Get, HTable, Put, Result}

import scala.concurrent.{ExecutionContext, Future}

abstract class HBaseClient[M[_]] (configuration: Configuration = HBaseConfiguration.create(), tableName: String){

  private val table = new HTable(configuration, tableName)

  def get(getRequest: Get)(implicit ec: ExecutionContext): Future[Result] = Future{ table.get(getRequest) }

  def put(putRequest: Put)(implicit ec: ExecutionContext): Future[Unit] = Future { table.put(putRequest) }


}
