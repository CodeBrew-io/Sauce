package io.codebrew.simpleinsight
package html

package object html {
	import Instrument._
	
	implicit def generic[T] = new Html[T] { 
		def show(a: T) = Code(a.toString)
	}	
}