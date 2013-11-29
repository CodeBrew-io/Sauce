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

	just crazy												$crazy
"""
	def crazy = {
		val hosts = for {
			h <- List("01", "02", "04", "05")
			p <- (0 until 8)
		} yield new InetSocketAddress(s"l4817-$h.lerb.polymtl.ca", 9090 + p)
		
		val dt = 15.seconds
		run(hosts.to[List], dt)
	}

	def run(hosts: List[InetSocketAddress], dt: Duration) = {
		println(hosts)

		val codes = Array(
			"1+1",
			"""|// quicksort for dummy
				|def quicksort(a: List[Int] = List(2, 45, 2, 1, 34, 4, 9)): List[Int] = {
				|	if(a.isEmpty) Nil
				|    else {
				|        val (big, small) = a.tail.partition(_ >= a.head)
				|        quicksort(small) ::: (a.head :: quicksort(big))
				|    }
				|}""".stripMargin,
			"""|trait Animal { def speak: String }
				|class Cat extends Animal { def speak = "miaou" }
				|class Dog extends Animal { def speak = "wouaf" }
				|class Bird extends Animal { def speak = "piou piou" }
				|object Owner { def call(a: Animal) = a.speak }
				|"hey"
				|""".stripMargin,
				"List(1,2,3).reverse.map(_+2)",
				"""println("hello world!")"""
		)
		import scala.util.Random.nextInt
		val randomCode = Stream.continually(codes(nextInt(codes.size)))

		val service = ClientBuilder()
			.hosts(hosts)
		   .codec(ThriftClientFramedCodec())
		   .hostConnectionLimit(1)
		   .build()
		val client = new Eval.FinagledClient(service)

		// Insight
		within(dt) {
			Future.collect(randomCode.take(100 * hosts.size).map{ code =>
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