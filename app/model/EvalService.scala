package model;

import ca.polymtl.log4900.api._

import com.twitter.finagle._
import builder.ClientBuilder
import thrift.ThriftClientFramedCodec

object EvalService {

  private lazy val service = ClientBuilder()
    .hosts(s"${Config.host}:${Config.port}")
    .codec(ThriftClientFramedCodec())
    .hostConnectionLimit(1)
    .build()

  lazy val client = new HelloUser.FinagledClient(service)
}