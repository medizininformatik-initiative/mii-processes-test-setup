package de.medizininformatik_initiative.processes.test.data.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hsheilbronn.mi.utils.crypto.CertificateHelper;
import de.hsheilbronn.mi.utils.crypto.io.PemIo;

public class RsaKeyPairGenerator
{
	private static final Logger logger = LoggerFactory.getLogger(RsaKeyPairGenerator.class);

	private static final BouncyCastleProvider PROVIDER = new BouncyCastleProvider();

	private KeyPair pair;

	public void createRsaKeyPair()
	{
		Path dmsPrivateKeyFile = Paths.get("rsa/dms_private-key.pem");
		Path dmsPublicKeyFile = Paths.get("rsa/dms_public-key.pem");

		if (Files.isReadable(dmsPrivateKeyFile) && Files.isReadable(dmsPublicKeyFile))
		{
			try
			{
				logger.info("Reading DMS private-key from {}", dmsPrivateKeyFile.toString());
				PrivateKey privateKey = PemIo.readPrivateKeyFromPem(dmsPrivateKeyFile);

				logger.info("Reading DMS public-key from {}", dmsPublicKeyFile.toString());
				RSAPublicKey publicKey = PemIo.readPublicKeyFromPem(dmsPublicKeyFile);

				pair = new KeyPair(publicKey, privateKey);
			}
			catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException | PKCSException e)
			{
				logger.error("Error while reading rsa key-pair from " + dmsPrivateKeyFile.toString() + " and "
						+ dmsPublicKeyFile.toString(), e);
				throw new RuntimeException(e);
			}
		}
		else
		{
			try
			{
				logger.info("Generating 4096 Bit RSA key-pair");
				pair = CertificateHelper.createRsaKeyPair4096Bit();

				logger.info("Writing DMS private-key to {}", dmsPrivateKeyFile.toString());
				PemIo.writeNotEncryptedPrivateKeyToOpenSslClassicPem(PROVIDER, dmsPrivateKeyFile, pair.getPrivate());

				logger.info("Writing DMS public-key to {}", dmsPublicKeyFile.toString());
				PemIo.writePublicKeyToPem((RSAPublicKey) pair.getPublic(), dmsPublicKeyFile);
			}
			catch (NoSuchAlgorithmException | IOException | OperatorCreationException e)
			{
				logger.error("Error while creating or writing rsa key-pair to " + dmsPrivateKeyFile.toString() + " and "
						+ dmsPublicKeyFile.toString(), e);
				throw new RuntimeException(e);
			}
		}
	}

	public void copyDockerTestRsaKeyPair()
	{
		Path dmsPrivateKeyFile = Paths.get("docker/secrets/dms_private_key.pem");

		Path dmsPublicKeyFile = Paths.get("docker/secrets/dms_public_key.pem");

		try
		{
			logger.info("Copying DMS private-key to {}", dmsPrivateKeyFile.toString());
			PemIo.writeNotEncryptedPrivateKeyToOpenSslClassicPem(PROVIDER, dmsPrivateKeyFile, pair.getPrivate());

			logger.info("Copying DMS public-key to {}", dmsPublicKeyFile.toString());
			PemIo.writePublicKeyToPem((RSAPublicKey) pair.getPublic(), dmsPublicKeyFile);
		}
		catch (IOException | OperatorCreationException e)
		{
			logger.error("Error copying key-pair", e);
			throw new RuntimeException(e);
		}
	}
}
