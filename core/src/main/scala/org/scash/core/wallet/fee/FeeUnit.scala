package org.scash.core.wallet.fee

import org.scash.core.currency.{ CurrencyUnit, Satoshis }
import org.scash.core.protocol.transaction.Transaction

/**
 * This is meant to be an abstract type that represents different fee unit measurements for
 * blockchains
 */
sealed abstract class FeeUnit {
  def currencyUnit: CurrencyUnit
  def *(tx: Transaction): CurrencyUnit    = calc(tx)
  def calc(tx: Transaction): CurrencyUnit = Satoshis(tx.size * toLong)
  def toLong: Long                        = currencyUnit.satoshis.toLong
}

/**
 * Meant to represent the different fee unit types for the bitcoin protocol
 * @see [[https://en.bitcoin.it/wiki/Weight_units]]
 */
sealed abstract class BitcoinFeeUnit extends FeeUnit

case class SatoshisPerByte(currencyUnit: CurrencyUnit) extends BitcoinFeeUnit {

  def toSatPerKb: SatoshisPerKiloByte =
    SatoshisPerKiloByte(currencyUnit.satoshis * Satoshis(1000))
}

case class SatoshisPerKiloByte(currencyUnit: CurrencyUnit) extends BitcoinFeeUnit {

  def toSatPerByte: SatoshisPerByte = {
    val conversionOpt = (currencyUnit.toBigDecimal * 0.001).toBigIntExact
    conversionOpt match {
      case Some(conversion) =>
        val sat = Satoshis(conversion)
        SatoshisPerByte(sat)

      case None =>
        throw new RuntimeException(s"Failed to convert sat/kb -> sat/byte for ${currencyUnit}")
    }

  }
}
