package io.codebrew
package eval

import io.codebrew.api._
import eval._

import org.specs2._
import time._

import com.twitter.util.Future
import com.twitter.finagle._
import thrift.ThriftClientFramedCodec
import builder.ClientBuilder

import java.net.InetSocketAddress

class Performance extends Specification { def is = s2"""

	* you must start n scalaEval on port starting 9090 manually

	scalaEval should not crash with a minimal load 			$minimal
	it should be two time faster if we use two services		$dual
	just crazy												$crazy
"""
	val port0 = 9090
	args.execute(sequential = true)


	def minimal = {
		val hosts = List(new InetSocketAddress(port0))
		val dt = 30.seconds
		run(hosts, dt)
	}

	def dual = {
		val hosts = List(
			new InetSocketAddress(port0),
			new InetSocketAddress(port0+1)
		)
		val dt = 15.seconds
		run(hosts, dt)
	}

	def crazy = {
		val hosts = (0 until 7).map(p => new InetSocketAddress(port0 + p)).to[List]
		val dt = 15.seconds
		run(hosts, dt)
	}

	def run(hosts: List[InetSocketAddress], dt: Duration) = {

		val code = "1+1"
		
		val service = ClientBuilder()
			.hosts(hosts)
		    .codec(ThriftClientFramedCodec())
		    .hostConnectionLimit(1)
		    .build()
		val client = new Eval.FinagledClient(service)

		// Insight
		within(dt) {
			Future.collect((1 to 100).map{ _ =>
				client.insight(code)
			}.to[Seq])
		}
	}

	def within[T](dt: Duration)(f: Future[T]) = {
		val startTime = Time.now
		f.get
		val t = Time.now - startTime
		println(t)
		(t).inSeconds must be_<(dt.inSeconds)
	}
}