package fr.xebia.xkeakka.util

import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * @author David Galichet.
 */

class CommandBuilderTests extends WordSpec with ShouldMatchers {

    "A Command Builder" should {
        "launch basic commands" in {
            var input = ""
            var error = ""
            val status = CommandBuilder("/bin/ls", List("-al"), { x => input = x }, { x => error = x }).call()
            error.length() should equal(0)
            status should be(true)
            (input.length() > 0) should be(true)
        }
    }
}