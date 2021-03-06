/*******************************************************************************
 * Copyright (c) 2016 Sebastian Stenzel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the accompanying LICENSE.txt.
 *
 * Contributors:
 *     Sebastian Stenzel - initial API and implementation
 *******************************************************************************/
package org.cryptomator.cryptolib;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

final class CipherSupplier {

	public static final CipherSupplier AES_CTR = new CipherSupplier("AES/CTR/NoPadding");

	private final String cipherAlgorithm;
	private final ThreadLocal<Cipher> threadLocal;

	public CipherSupplier(String cipherAlgorithm) {
		this.cipherAlgorithm = cipherAlgorithm;
		this.threadLocal = new Provider();
	}

	private class Provider extends ThreadLocal<Cipher> {
		@Override
		protected Cipher initialValue() {
			try {
				return Cipher.getInstance(cipherAlgorithm);
			} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
				throw new IllegalArgumentException("Invalid cipher algorithm or padding.", e);
			}
		}
	}

	public Cipher forEncryption(SecretKey key, AlgorithmParameterSpec params) {
		return forMode(Cipher.ENCRYPT_MODE, key, params);
	}

	public Cipher forDecryption(SecretKey key, AlgorithmParameterSpec params) {
		return forMode(Cipher.DECRYPT_MODE, key, params);
	}

	private Cipher forMode(int ciphermode, SecretKey key, AlgorithmParameterSpec params) {
		final Cipher cipher = threadLocal.get();
		try {
			cipher.init(ciphermode, key, params);
			return cipher;
		} catch (InvalidKeyException e) {
			throw new IllegalArgumentException("Invalid key.", e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new IllegalArgumentException("IV not appropriate for " + cipher.getAlgorithm() + ".", e);
		}
	}
}