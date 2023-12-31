package de.medizininformatik_initiative.processes.test.data.generator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import dev.dsf.fhir.service.ReferenceCleaner;
import dev.dsf.fhir.service.ReferenceCleanerImpl;
import dev.dsf.fhir.service.ReferenceExtractor;
import dev.dsf.fhir.service.ReferenceExtractorImpl;

public class BundleGenerator
{
	private static final Logger logger = LoggerFactory.getLogger(BundleGenerator.class);

	private final FhirContext fhirContext = FhirContext.forR4();
	private final ReferenceExtractor extractor = new ReferenceExtractorImpl();
	private final ReferenceCleaner cleaner = new ReferenceCleanerImpl(extractor);

	private Bundle bundle;

	private Bundle readAndCleanBundle(Path bundleTemplateFile)
	{
		try (InputStream in = Files.newInputStream(bundleTemplateFile))
		{
			Bundle bundle = newXmlParser().parseResource(Bundle.class, in);

			// FIXME hapi parser can't handle embedded resources and creates them while parsing bundles
			return cleaner.cleanReferenceResourcesIfBundle(bundle);
		}
		catch (IOException e)
		{
			logger.error("Error while reading bundle from " + bundleTemplateFile.toString(), e);
			throw new RuntimeException(e);
		}
	}

	private void writeBundle(Path bundleFile, Bundle bundle)
	{
		try (OutputStream out = Files.newOutputStream(bundleFile);
				OutputStreamWriter writer = new OutputStreamWriter(out))
		{
			newXmlParser().encodeResourceToWriter(bundle, writer);
		}
		catch (IOException e)
		{
			logger.error("Error while writing bundle to " + bundleFile.toString(), e);
			throw new RuntimeException(e);
		}
	}

	private IParser newXmlParser()
	{
		IParser parser = fhirContext.newXmlParser();
		parser.setStripVersionsFromReferences(false);
		parser.setOverrideResourceIdWithBundleEntryFullUrl(false);
		parser.setPrettyPrint(true);
		return parser;
	}

	public void createDockerTestBundles(
			Map<String, CertificateGenerator.CertificateFiles> clientCertificateFilesByCommonName)
	{
		createDockerTestBundle(clientCertificateFilesByCommonName);
	}

	private void createDockerTestBundle(
			Map<String, CertificateGenerator.CertificateFiles> clientCertificateFilesByCommonName)
	{
		Path bundleTemplateFile = Paths.get("src/main/resources/bundle-templates/bundle.xml");

		bundle = readAndCleanBundle(bundleTemplateFile);

		Organization organizationHrp = (Organization) bundle.getEntry().get(0).getResource();
		Extension organizationHrpThumbprintExtension = organizationHrp
				.getExtensionByUrl("http://dsf.dev/fhir/StructureDefinition/extension-certificate-thumbprint");
		organizationHrpThumbprintExtension.setValue(new StringType(
				clientCertificateFilesByCommonName.get("hrp-client").getCertificateSha512ThumbprintHex()));

		Organization organizationDms = (Organization) bundle.getEntry().get(1).getResource();
		Extension organizationDmsThumbprintExtension = organizationDms
				.getExtensionByUrl("http://dsf.dev/fhir/StructureDefinition/extension-certificate-thumbprint");
		organizationDmsThumbprintExtension.setValue(new StringType(
				clientCertificateFilesByCommonName.get("dms-client").getCertificateSha512ThumbprintHex()));

		Organization organizationDic1 = (Organization) bundle.getEntry().get(2).getResource();
		Extension organizationDic1thumbprintExtension = organizationDic1
				.getExtensionByUrl("http://dsf.dev/fhir/StructureDefinition/extension-certificate-thumbprint");
		organizationDic1thumbprintExtension.setValue(new StringType(
				clientCertificateFilesByCommonName.get("dic1-client").getCertificateSha512ThumbprintHex()));

		Organization organizationDic2 = (Organization) bundle.getEntry().get(3).getResource();
		Extension organizationDic2thumbprintExtension = organizationDic2
				.getExtensionByUrl("http://dsf.dev/fhir/StructureDefinition/extension-certificate-thumbprint");
		organizationDic2thumbprintExtension.setValue(new StringType(
				clientCertificateFilesByCommonName.get("dic2-client").getCertificateSha512ThumbprintHex()));

		writeBundle(Paths.get("bundle/bundle.xml"), bundle);
	}

	public void copyDockerTestBundles()
	{
		Path hrpBundleFile = Paths.get("docker/hrp/fhir/conf/bundle.xml");
		logger.info("Copying fhir bundle to {}", hrpBundleFile);
		writeBundle(hrpBundleFile, bundle);

		Path dmsBundleFile = Paths.get("docker/dms/fhir/conf/bundle.xml");
		logger.info("Copying fhir bundle to {}", dmsBundleFile);
		writeBundle(dmsBundleFile, bundle);

		Path dic1BundleFile = Paths.get("docker/dic1/fhir/conf/bundle.xml");
		logger.info("Copying fhir bundle to {}", dic1BundleFile);
		writeBundle(dic1BundleFile, bundle);

		Path dic2BundleFile = Paths.get("docker/dic2/fhir/conf/bundle.xml");
		logger.info("Copying fhir bundle to {}", dic2BundleFile);
		writeBundle(dic2BundleFile, bundle);
	}
}
