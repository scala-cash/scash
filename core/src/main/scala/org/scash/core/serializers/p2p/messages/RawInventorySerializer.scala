package org.scash.core.serializers.p2p.messages

import org.scash.core.crypto.DoubleSha256Digest
import org.scash.core.serializers.RawBitcoinSerializer
import org.scash.core.p2p.TypeIdentifier
import org.scash.core.p2p.Inventory
import scodec.bits.ByteVector

/**
 * Serializes/deserializes a inventory
 * @see https://bitcoin.org/en/developer-reference#term-inventory
 */
trait RawInventorySerializer extends RawBitcoinSerializer[Inventory] {

  override def read(bytes: ByteVector): Inventory = {
    val typeIdentifier = TypeIdentifier(bytes.take(4))
    val hash           = DoubleSha256Digest(bytes.slice(4, bytes.size))
    Inventory(typeIdentifier, hash)
  }

  override def write(inventory: Inventory): ByteVector =
    inventory.typeIdentifier.bytes ++ inventory.hash.bytes
}

object RawInventorySerializer extends RawInventorySerializer
