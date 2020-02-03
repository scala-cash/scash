package org.scash.core.protocol.script

import org.scalacheck.{Prop, Properties}
import org.scash.testkit.gen.ScriptGenerators

/**
 * Created by chris on 6/24/16.
 */
class P2SHScriptSignatureSpec extends Properties("P2SHScriptSignatureSpec") {

  property("Symmetrical serialization") =
    Prop.forAll(ScriptGenerators.p2shScriptSignature) { p2shScriptSig =>
      P2SHScriptSignature(p2shScriptSig.hex) == p2shScriptSig

    }

}
