package org.bitcoins.core.gen

import org.bitcoins.core.crypto._
import org.bitcoins.core.script.ScriptSettings
import org.bitcoins.core.script.crypto.HashType
import org.bitcoins.core.util.CryptoUtil
import org.scalacheck.Gen

/**
  * Created by chris on 6/22/16.
  */
trait CryptoGenerators {


  def privateKey : Gen[ECPrivateKey] = for {
    i <- Gen.choose(1,2)
  } yield ECPrivateKey()

  /**
    * Generate a sequence of private keys
    * @param num maximum number of keys to generate
    * @return
    */
  def privateKeySeq(num : Int): Gen[Seq[ECPrivateKey]] = Gen.listOfN(num,privateKey)

  /**
    * Generates a sequence of private keys, and determines an amount of 'required' private keys
    * that a transaction needs to be signed with
    * @param num the maximum number of keys to generate
    * @return
    */
  def privateKeySeqWithRequiredSigs(num: Int): Gen[(Seq[ECPrivateKey], Int)] = {
    val privateKeys = privateKeySeq(num)
    for {
      keys <- privateKeys
      requiredSigs <- Gen.choose(0,keys.size-1)
    } yield (keys,requiredSigs)
  }

  /**
    * Generates a random number of private keys less than the max public keys setting in [[ScriptSettings]]
    * also generates a random 'requiredSigs' number that a transaction needs to be signed with
    * @return
    */
  def privateKeySeqWithRequiredSigs: Gen[(Seq[ECPrivateKey], Int)] = for {
    num <- Gen.choose(0,ScriptSettings.maxPublicKeysPerMultiSig)
    keysAndRequiredSigs <- privateKeySeqWithRequiredSigs(num)
  } yield keysAndRequiredSigs

  /**
    * Generates a random public key
    * @return
    */
  def publicKey : Gen[ECPublicKey] = for {
    privKey <- privateKey
  } yield privKey.publicKey

  /**
    * Generates a random digital signature
    * @return
    */
  def digitalSignatures : Gen[ECDigitalSignature] = for {
    privKey <- privateKey
    hash <- CryptoGenerators.doubleSha256Digest
  } yield privKey.sign(hash)

  /**
    * Generates a random [[DoubleSha256Digest]]
    * @return
    */
  def doubleSha256Digest : Gen[DoubleSha256Digest] = for {
    hex <- StringGenerators.hexString
    digest = CryptoUtil.doubleSHA256(hex)
  } yield digest

  /**
    * Generates a sequence of [[DoubleSha256Digest]]
    * @param num the number of digets to generate
    * @return
    */
  def doubleSha256DigestSeq(num : Int): Gen[Seq[DoubleSha256Digest]] = Gen.listOfN(num,doubleSha256Digest)


  /** Generates a random [[org.bitcoins.core.crypto.Sha256Hash160Digest]] */
  def sha256Hash160Digest: Gen[Sha256Hash160Digest] = for {
    pubKey <- publicKey
    hash = CryptoUtil.sha256Hash160(pubKey.bytes)
  } yield hash

  /** Generates a random [[HashType]] */
  def hashType: Gen[HashType] = Gen.oneOf(HashType.sigHashAll, HashType.sigHashNone, HashType.sigHashSingle,
    HashType.sigHashAnyoneCanPay, HashType.sigHashSingleAnyoneCanPay, HashType.sigHashNoneAnyoneCanPay,
    HashType.sigHashAllAnyoneCanPay)

}

object CryptoGenerators extends CryptoGenerators