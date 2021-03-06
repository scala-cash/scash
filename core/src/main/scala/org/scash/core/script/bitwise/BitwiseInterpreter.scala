package org.scash.core.script.bitwise

/**
 *   Copyright (c) 2016-2018 Chris Stewart (MIT License)
 *   Copyright (c) 2018 Flores Lorca (MIT License)
 */
import org.scash.core.script
import org.scash.core.script.constant._
import org.scash.core.script.control.{ ControlOperationsInterpreter, OP_VERIFY }
import org.scash.core.script.result._
import org.scash.core.script._
import org.scash.core.util.BitcoinSLogger
import scodec.bits.ByteVector

sealed abstract class BitwiseInterpreter {
  private def logger = BitcoinSLogger.logger

  /** Returns 1 if the inputs are exactly equal, 0 otherwise. */
  def opEqual(program: ScriptProgram): ScriptProgram = {
    require(program.script.headOption.contains(OP_EQUAL), "Script operation must be OP_EQUAL")
    if (program.stack.size < 2) {
      ScriptProgram(program, ScriptErrorInvalidStackOperation)
    } else {
      val h  = program.stack.head
      val h1 = program.stack.tail.head
      val result = (h, h1) match {
        case (OP_0, ScriptNumber.zero) | (ScriptNumber.zero, OP_0) =>
          OP_0.underlying == ScriptNumber.zero.toLong
        case (OP_FALSE, ScriptNumber.zero) | (ScriptNumber.zero, OP_FALSE) =>
          OP_FALSE.underlying == ScriptNumber.zero.toLong
        case (OP_TRUE, ScriptNumber.one) | (ScriptNumber.one, OP_TRUE) =>
          OP_TRUE.underlying == ScriptNumber.one.toLong
        case (OP_1, ScriptNumber.one) | (ScriptNumber.one, OP_1) =>
          OP_1.underlying == ScriptNumber.one.toLong
        case _ => h.bytes == h1.bytes
      }
      val scriptBoolean = if (result) OP_TRUE else OP_FALSE
      ScriptProgram(program, scriptBoolean :: program.stack.tail.tail, program.script.tail)
    }
  }

  /** Same as [[OP_EQUAL]], but runs [[OP_VERIFY]] afterward. */
  def opEqualVerify(program: ScriptProgram): ScriptProgram = {
    require(program.script.headOption.contains(OP_EQUALVERIFY), "Script operation must be OP_EQUALVERIFY")
    if (program.stack.size > 1) {
      //first replace OP_EQUALVERIFY with OP_EQUAL and OP_VERIFY
      val simpleScript              = OP_EQUAL :: OP_VERIFY :: program.script.tail
      val newProgram: ScriptProgram = opEqual(ScriptProgram(program, program.stack, simpleScript))
      ControlOperationsInterpreter.opVerify(newProgram) match {
        case p: PreExecutionScriptProgram => p
        case p: ExecutedScriptProgram =>
          if (p.error.isDefined) ScriptProgram(p, ScriptErrorEqualVerify)
          else p
        case p: ExecutionInProgressScriptProgram => p
      }
    } else {
      logger.error("OP_EQUALVERIFY requires at least 2 elements on the stack")
      ScriptProgram(program, ScriptErrorInvalidStackOperation)
    }
  }

  /**
   * Bitwise operands added on may 2018 HF
   * [[https://github.com/bitcoincashorg/bitcoincash.org/blob/master/spec/may-2018-reenabled-opcodes.md#bitwise-logic]]
   */
  private def opBitWise(program: => ScriptProgram)(f: (ByteVector, ByteVector) => ByteVector) =
    script
      .checkBinary(program)
      .map { p =>
        val v1 = p.stack(0).bytes
        val v2 = p.stack(1).bytes
        if (v1.size != v2.size) {
          logger.error("Inputs must be the same size")
          ScriptProgram(p, ScriptErrorInvalidOperandSize)
        } else {
          val r = ScriptConstant(f(v1, v2))
          ScriptProgram(p, r +: p.stack.drop(2), p.script.tail)
        }
      }
      .merge

  /** [[OP_AND]] Boolean and between each bit in the operands**/
  def opAnd(program: ScriptProgram): ScriptProgram = opBitWise(program)(_ & _)

  /** [[OP_OR]] Boolean or between each bit in the operands**/
  def opOr(program: ScriptProgram): ScriptProgram = opBitWise(program)(_ | _)

  /** [[OP_XOR]] Boolean xor between each bit in the operands**/
  def opXor(program: ScriptProgram): ScriptProgram = opBitWise(program)(_ ^ _)

}

object BitwiseInterpreter extends BitwiseInterpreter
