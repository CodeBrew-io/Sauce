package io.codebrew
package eval

import java.security._
import java.io.FilePermission
import java.util.PropertyPermission
import java.lang.reflect.ReflectPermission

object EvalSecurity {
  def start: Unit = {
    Policy.setPolicy(new EvalSecurityPolicy)
    System.setSecurityManager(new SecurityManager)
  }
}

class EvalSecurityPolicy extends Policy {
  private val websitePermissions = new Permissions
  websitePermissions.add(new AllPermission)

  private val scriptPermissions = new Permissions

  override def getPermissions( sourceCode: CodeSource ) = {
    if(sourceCode.getLocation == null) {
      scriptPermissions
    }
    else {
      websitePermissions
    }
  }

  override def getPermissions( domain: ProtectionDomain ) = {
    if(domain.getCodeSource.getLocation == null) {
      scriptPermissions
    }
    else {
      websitePermissions
    }
  }
}