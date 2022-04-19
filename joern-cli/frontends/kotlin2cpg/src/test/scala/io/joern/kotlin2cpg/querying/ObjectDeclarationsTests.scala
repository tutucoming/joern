package io.joern.kotlin2cpg.querying

import io.joern.kotlin2cpg.TestContext
import io.shiftleft.semanticcpg.language._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class ObjectDeclarationsTests extends AnyFreeSpec with Matchers {
  "CPG for code with simple object declaration" - {
    lazy val cpg = TestContext.buildCpg("""
        |package mypkg
        |
        |object Foo {
        |    val bar = "PLACEHOLDER_1"
        |    var baz = "PLACEHOLDER_2"
        |
        |    fun moo() = println("moo")
        |}
        |
        |fun main() {
        |  Foo.moo()
        |}
        |""".stripMargin)

    "should contain a TYPE_DECL node for the object declaration with the correct properties set" in {
      val List(td) = cpg.typeDecl.name("Foo").l
      td.name shouldBe "Foo"
      td.code shouldBe "Foo"
      td.fullName shouldBe "mypkg.Foo"
      td.inheritsFromTypeFullName shouldBe List("java.lang.Object")
      td.isExternal shouldBe false
      td.lineNumber shouldBe Some(3)
      td.columnNumber shouldBe Some(7)
    }

    "should contain MEMBER node for `bar` with correct properties" in {
      val List(m) = cpg.member("bar").l
      m.name shouldBe "bar"
      m.code shouldBe "bar"
      m.typeFullName shouldBe "java.lang.String"
      m.lineNumber shouldBe Some(4)
      m.columnNumber shouldBe Some(8)
    }

    "should contain MEMBER node for `baz` with correct properties" in {
      val List(m) = cpg.member("baz").l
      m.name shouldBe "baz"
      m.code shouldBe "baz"
      m.typeFullName shouldBe "java.lang.String"
      m.lineNumber shouldBe Some(5)
      m.columnNumber shouldBe Some(8)
    }

    "should contain a CALL node for the call to `moo` with the correct properties set" in {
      val List(c) = cpg.call.code("Foo.moo.*").l
      c.methodFullName shouldBe "mypkg.Foo.moo:void()"
      c.typeFullName shouldBe "void"
    }
  }

  "CPG for code with complex object declaration" - {
    lazy val cpg = TestContext.buildCpg("""
        |package mypkg
        |
        |import android.content.Context
        |import android.content.SharedPreferences
        |
        |object Prefs {
        |    lateinit var sharedpreferences: SharedPreferences
        |    var prefs : Prefs? = null
        |
        |    fun getInstance(context: Context): Prefs {
        |        if (prefs == null) {
        |            sharedpreferences =
        |                context.getSharedPreferences("Prefs", Context.MODE_PRIVATE)
        |            prefs = this
        |        }
        |        return prefs!!
        |    }
        |
        |    var data: String?
        |        get() = sharedpreferences.getString("data","")
        |        set(value) {
        |            sharedpreferences.edit().putString("data", value).apply()
        |        }
        |
        |    var username: String?
        |        get() = sharedpreferences.getString("username","")
        |        set(value) {
        |            sharedpreferences.edit().putString("username", value).apply()
        |        }
        |
        |    var password: String?
        |        get() = sharedpreferences.getString("password","")
        |        set(value) {
        |            sharedpreferences.edit().putString("password", value).apply()
        |        }
        |
        |    var productList: String?
        |        get() = sharedpreferences.getString("productList","")
        |        set(value) {
        |            sharedpreferences.edit().putString("productList", value).apply()
        |        }
        |
        |    fun clearAll(){
        |        sharedpreferences.edit().clear().apply()
        |    }
        |}
        |
        |""".stripMargin)

    "should contain a TYPE_DECL node for the object declaration with the correct properties set" in {
      val List(td) = cpg.typeDecl.name("Prefs").l
      td.name shouldBe "Prefs"
      td.fullName shouldBe "mypkg.Prefs"
      td.inheritsFromTypeFullName shouldBe List("java.lang.Object")
      td.isExternal shouldBe false
      td.lineNumber shouldBe Some(6)
      td.columnNumber shouldBe Some(7)
    }
  }
}
