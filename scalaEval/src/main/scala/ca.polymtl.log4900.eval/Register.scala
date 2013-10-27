package ca.polymtl.log4900
package eval

import api._

import com.twitter.finagle._
import builder.ClientBuilder
import thrift.ThriftClientFramedCodec

object Register {
	def ready(evalPort: Int): Unit = {
		val lookupHostname = Option(System.getProperty("io.codebrew.lookupHostname")).getOrElse("localhost")
		val lookupPort = (for {
			env <- Option(System.getProperty("io.codebrew.lookupPort"))
			port <- Try(env.toInt).toOption
		} yield (port)).getOrElse(lookup.Config.port)

		println(s"lookup is ${lookupHostname}:${lookupPort}")
		val client = new lookup.Lookup.FinagledClient(
			ClientBuilder()
				.hosts(s"${lookupHostname}:${lookupPort}")
				.codec(ThriftClientFramedCodec())
				.hostConnectionLimit(1)
				.build()
		)

		val evalHostname = Option(System.getProperty("io.codebrew.scalaEvalHostname")).getOrElse("localhost")
		client.register(lookup.ServiceInfo(eval.Config.name, evalHostname, evalPort))
	}
}