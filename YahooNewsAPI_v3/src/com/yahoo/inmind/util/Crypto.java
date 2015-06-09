package com.yahoo.inmind.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import android.content.res.AssetManager;
import android.util.Log;

import com.yahoo.inmind.reader.App;


public class Crypto {
	private Key mPublickey;
	private Key mPrivatekey;
	boolean bUseApache = false;
	
	public Crypto()
	{
		
	}
	
	public void readRSAKeys(String path)
	{
		if (!path.endsWith("/"))
			path = path + "/";
		readPrivateKey(path + "/private.key");
		readPublicKey(path + "/public.key");
	}
	
	public void readPrivateKey(String path)
	{
		try{
			// Read Private Key.
			File filePrivateKey = new File(path);
			FileInputStream fis = new FileInputStream(path);
			byte[] encodedPrivateKey = new byte[(int) filePrivateKey.length()];
			fis.read(encodedPrivateKey);
			fis.close();
			 
			// Generate KeyPair.
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			 
			PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
			encodedPrivateKey);
			PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
			mPrivatekey = privateKey;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void readPublicKey(String path)
	{
		try{			
			// Read Public Key.
			File filePublicKey = new File(path);
			byte[] encodedPublicKey = new byte[(int) filePublicKey.length()];
						
			AssetManager am = App.get().getAssets();
			InputStream is = am.open("public.key");	
			if( encodedPublicKey.length > 0 ){
				is.read(encodedPublicKey);	
			}else{
				int len;
			    int size = 1024;
				if (is instanceof ByteArrayInputStream) {
			      size = is.available();
			      encodedPublicKey = new byte[size];
			      len = is.read(encodedPublicKey, 0, size);
			    } else {
			      ByteArrayOutputStream bos = new ByteArrayOutputStream();
			      encodedPublicKey = new byte[size];
			      while ((len = is.read(encodedPublicKey, 0, size)) != -1)
			        bos.write(encodedPublicKey, 0, len);
			      encodedPublicKey = bos.toByteArray();
			    }
			}
			is.close();

			// Generate KeyPair.
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
			PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
			 
			mPublickey = publicKey;			
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public SecretKeySpec genAESKey(){
		SecretKeySpec sks = null;
		try {
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			sr.setSeed("why can't".getBytes());
			KeyGenerator kg = KeyGenerator.getInstance("AES");
			kg.init(128, sr);
			sks = new SecretKeySpec((kg.generateKey()).getEncoded(), "AES");
		} catch (Exception e) {
			Log.e(App.TAG, "AES secret key spec error");
		}
		return sks;
	}
	
	public void genRSAKeysAndExport(String path){
		File folder = new File(path);
		if (!folder.exists() || !folder.isDirectory()){
			Log.e(App.TAG, this.getClass().getSimpleName() + ": cannot find the folder @ " + path);
			return;
		}
		if (!path.endsWith("/"))
			path = path + "/";
		Key publicKey = null;
		Key privateKey = null;
		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA/ECB/PKCS1Padding");
			kpg.initialize(1024);
			KeyPair kp = kpg.genKeyPair();			
			publicKey = kp.getPublic();
			privateKey = kp.getPrivate();	

			// Store Public Key.
			X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
			FileOutputStream fos = new FileOutputStream(path + "/public.key");
			fos.write(x509EncodedKeySpec.getEncoded());
			fos.close();
			
			// Store Private Key.
			PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
			privateKey.getEncoded());
			fos = new FileOutputStream(path + "/private.key");
			fos.write(pkcs8EncodedKeySpec.getEncoded());
			fos.close();
		} catch (Exception e) {
			Log.e(App.TAG, "RSA key pair error");
		}
	}
	
	public byte[] encryptRSA(String in)
	{
		if (in == null || mPublickey == null)
			return null;

		byte[] encodedBytes = null;
		try {
			Cipher	         cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
	        cipher.init(Cipher.ENCRYPT_MODE, mPublickey);
	        encodedBytes = cipher.doFinal(in.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}	
		if (encodedBytes == null)
			return null;

		return encodedBytes;
	}
	
	public byte[] encryptRSA(byte[] in)
	{
		if (in == null || mPublickey == null)
			return null;

		byte[] encodedBytes = null;
		try {
			Cipher	         cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
	        cipher.init(Cipher.ENCRYPT_MODE, mPublickey);
	        encodedBytes = cipher.doFinal(in);
		} catch (Exception e) {
			e.printStackTrace();
		}	
		if (encodedBytes == null)
			return null;

		return encodedBytes;
	}
	
	public SecretKeySpec getAESKey(String key){
		if (key == null)
			return null;
		
		byte[] keyBuf = null;
		if (bUseApache)
			keyBuf = Base64.decodeBase64(key);
		else
			keyBuf = android.util.Base64.decode(key, android.util.Base64.DEFAULT);
		
		SecretKeySpec sks = null;
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(keyBuf));
			sks = (SecretKeySpec) ois.readObject();
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e){
			e.printStackTrace();
		} finally{
			try {
				if (ois != null)
					ois.close();
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
		return sks;
	}
	
	public String encryptAES(String in, SecretKeySpec sks)
	{
		if (in == null || sks == null)
			return null;
		byte[] encodedBytes = null;
		
		try {
			Cipher c = Cipher.getInstance("AES");
			c.init(Cipher.ENCRYPT_MODE, sks);
			encodedBytes = c.doFinal(in.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}	
		
		if (encodedBytes == null)
			return null;
		
		if (bUseApache)
			return Base64.encodeBase64String(encodedBytes);
		else
			return android.util.Base64.encodeToString(encodedBytes, android.util.Base64.DEFAULT);
	}
	
	public String decryptAES(String in, SecretKeySpec sks)
	{
		if (sks == null || in == null)
			return null;
		byte[] encodedBytes = null;
		if (bUseApache)
			encodedBytes = Base64.decodeBase64(in);
		else
			encodedBytes = android.util.Base64.decode(in, android.util.Base64.DEFAULT);
		try {
			Cipher c = Cipher.getInstance("AES");
			c.init(Cipher.DECRYPT_MODE, sks);
			byte[] decodedBytes = c.doFinal(encodedBytes);
			return new String(decodedBytes);
		} catch (Exception e) {
			Log.e(App.TAG, "RSA decryption error");
		}	
		return null;
	}
	
	public byte[] decryptRSA(byte[] in)
	{
		if (mPrivatekey == null || in == null)
			return null;
		
		try {
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, mPrivatekey);

	        byte[] decodedBytes = cipher.doFinal(in);
			return decodedBytes;
		} catch (Exception e) {
			e.printStackTrace();
		}	
		return null;
	}
}
