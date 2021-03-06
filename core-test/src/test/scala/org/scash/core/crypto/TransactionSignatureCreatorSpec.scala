package org.scash.core.crypto

import org.scash.core.script.PreExecutionScriptProgram
import org.scash.core.script.interpreter.ScriptInterpreter
import org.scash.core.script.result._
import org.scash.core.util.BitcoinSLogger
import org.scalacheck.{Prop, Properties}
import org.scash.testkit.gen.TransactionGenerators

/**
 * Created by chris on 7/25/16.
 */
class TransactionSignatureCreatorSpec extends Properties("TransactionSignatureCreatorSpec") {
  private val logger = BitcoinSLogger.logger

  property("Must generate a valid signature for a p2pk transaction") =
    Prop.forAll(TransactionGenerators.signedP2PKTransaction) {
      case (txSignatureComponent: TxSigComponent, _) =>
        //run it through the interpreter
        val program = PreExecutionScriptProgram(txSignatureComponent)
        val result = ScriptInterpreter.run(program)
        result == ScriptOk
    }

  property("generate valid signatures for a multisignature transaction") =
    Prop.forAllNoShrink(TransactionGenerators.signedMultiSigTransaction) {
      case (txSignatureComponent: TxSigComponent, _) =>
        //run it through the interpreter
        val program = PreExecutionScriptProgram(txSignatureComponent)
        val result = ScriptInterpreter.run(program)
        result == ScriptOk
    }

  property("generate a valid signature for a p2sh transaction") =
    Prop.forAll(TransactionGenerators.signedP2SHTransaction) {
      case (txSignatureComponent: TxSigComponent, _) =>
        //run it through the interpreter
        val program = PreExecutionScriptProgram(txSignatureComponent)
        val result = ScriptInterpreter.run(program)
        //can be ScriptErrorPushSize if the redeemScript is larger than 520 bytes or can have non
        //push only elements in the ScriptSig
        Seq(ScriptOk, ScriptErrorPushSize).contains(result)
    }

  property("generate a valid signature for a valid and spendable cltv transaction") =
    Prop.forAllNoShrink(TransactionGenerators.spendableCLTVTransaction :| "cltv_spendable") {
      case (txSignatureComponent: TxSigComponent, _) =>
        val program = PreExecutionScriptProgram(txSignatureComponent)
        val result = ScriptInterpreter.run(program)
        result == ScriptOk
    }

  property("fail to verify a transaction with a locktime that has not yet been met") =
    Prop.forAllNoShrink(TransactionGenerators.unspendableCLTVTransaction :| "cltv_unspendable") {
      case (txSignatureComponent: TxSigComponent, _) =>
        val program = PreExecutionScriptProgram(txSignatureComponent)
        val result = ScriptInterpreter.run(program)
        Seq(ScriptErrorUnsatisfiedLocktime, ScriptErrorPushSize).contains(result)
    }

  property("generate a valid signature for a valid and spendable csv transaction") =
    Prop.forAllNoShrink(TransactionGenerators.spendableCSVTransaction :| "spendable csv") {
      case (txSignatureComponent: TxSigComponent, _) =>
        //run it through the interpreter
        val program = PreExecutionScriptProgram(txSignatureComponent)
        val result = ScriptInterpreter.run(program)
        Seq(ScriptOk, ScriptErrorPushSize).contains(result)
    }
  property("fail to verify a transaction with a relative locktime that has not been satisfied yet") =
    Prop.forAllNoShrink(TransactionGenerators.unspendableCSVTransaction :| "unspendable csv") {
      case (txSignatureComponent: TxSigComponent, _) =>
        //run it through the interpreter
        val program = PreExecutionScriptProgram(txSignatureComponent)
        val result = ScriptInterpreter.run(program)
        Seq(ScriptErrorUnsatisfiedLocktime, ScriptErrorPushSize).contains(result)

    }
  /*
  property("generate a valid signature for a escrow timeout transaction") =
    Prop.forAll(TransactionGenerators.spendableEscrowTimeoutTransaction) { txSigComponent: TxSigComponent =>
      val program = PreExecutionScriptProgram(txSigComponent)
      val result = ScriptInterpreter.run(program)
      result == ScriptOk
    }
*/
  property("fail to evaluate a locktime escrow timeout transaction") = {
    Prop.forAll(TransactionGenerators.unspendableEscrowTimeoutTransaction) { txSigComponent: TxSigComponent =>
      val program = PreExecutionScriptProgram(txSigComponent)
      val result = ScriptInterpreter.run(program)
      result != ScriptOk
    }
  }

}
