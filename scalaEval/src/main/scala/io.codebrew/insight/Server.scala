package io.codebrew
package eval

import api.eval._

import org.apache.thrift._
import transport.TServerSocket
import server.TServer
import server.TThreadPoolServer
import server.TThreadPoolServer.Args

import java.net.InetSocketAddress
import java.io.{File, FileOutputStream}
import java.lang.management.ManagementFactory

import scala.util.Try

object EvalServer {	
	def start(evalPort: Int) {
		val eval = new EvalImpl;
		val processor = new Eval.Processor(eval);
		val serverTransport = new TServerSocket(evalPort);
		val server = new TThreadPoolServer(new Args(serverTransport).processor(processor));

		server.serve()
	}
}


object Main extends App {
	override def main(args: Array[String]) = {
		lazy val fromEnv = for {
		        env <- Option(System.getProperty("io.codebrew.scalaEvalPort"))
		        port <- Try(env.toInt).toOption
		} yield { createRunningPid(); port }

		val port = args.headOption.map(_.toInt) orElse fromEnv getOrElse(Config.port)

		EvalSecurity.start
		EvalServer.start(port)
	}
	private def createRunningPid() = {
	    for {
	            pid <- ManagementFactory.getRuntimeMXBean.getName.split('@').headOption
	            userDir <- Option(System.getProperty("user.dir")) } {
	            val pidFile = new File(userDir, "RUNNING_PID")
	            println(userDir)
	            if (pidFile.exists) {
	                    println("This application is already running (Or delete " +
	                            pidFile.getAbsolutePath + " file).")
	                    System.exit(-1)
	            }
	            new FileOutputStream(pidFile).write(pid.getBytes)
	            Runtime.getRuntime.addShutdownHook(new Thread {
	                    override def run {
	                            pidFile.delete()
	                    }
	            })
	    }
	}
}