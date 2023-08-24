package de.medizininformatik_initiative.processes.test.data.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.medizininformatik_initiative.processes.test.data.generator.CertificateGenerator.CertificateFiles;

public class EnvGenerator
{
	private static final Logger logger = LoggerFactory.getLogger(EnvGenerator.class);

	private static final String BUNDLE_USER_THUMBPRINT = "BUNDLE_USER_THUMBPRINT";
	private static final String WEBBROSER_TEST_USER_THUMBPRINT = "WEBBROSER_TEST_USER_THUMBPRINT";

	private static final class EnvEntry
	{
		final String userThumbprintsVariableName;
		final Stream<String> userThumbprints;

		EnvEntry(String userThumbprintsVariableName, Stream<String> userThumbprints)
		{
			this.userThumbprintsVariableName = userThumbprintsVariableName;
			this.userThumbprints = userThumbprints;
		}
	}

	public void generateAndWriteDockerTestFhirEnvFiles(Map<String, CertificateFiles> clientCertificateFilesByCommonName)
	{
		Stream<String> webbroserTestUserThumbprints = filterAndMapToThumbprint(clientCertificateFilesByCommonName,
				"Webbrowser Test User");

		Stream<String> dic1UserThumbprints = filterAndMapToThumbprint(clientCertificateFilesByCommonName,
				"dic1-client");

		Stream<String> dic2UserThumbprints = filterAndMapToThumbprint(clientCertificateFilesByCommonName,
				"dic2-client");

		Stream<String> dmsUserThumbprints = filterAndMapToThumbprint(clientCertificateFilesByCommonName, "dms-client");

		Stream<String> hrpUserThumbprints = filterAndMapToThumbprint(clientCertificateFilesByCommonName, "hrp-client");

		List<EnvEntry> entries = List.of(new EnvEntry(WEBBROSER_TEST_USER_THUMBPRINT, webbroserTestUserThumbprints),
				new EnvEntry("DIC1_" + BUNDLE_USER_THUMBPRINT, dic1UserThumbprints),
				new EnvEntry("DIC2_" + BUNDLE_USER_THUMBPRINT, dic2UserThumbprints),
				new EnvEntry("DMS_" + BUNDLE_USER_THUMBPRINT, dmsUserThumbprints),
				new EnvEntry("HRP_" + BUNDLE_USER_THUMBPRINT, hrpUserThumbprints));

		writeEnvFile(Paths.get("docker/.env"), entries);
	}

	private Stream<String> filterAndMapToThumbprint(Map<String, CertificateFiles> clientCertificateFilesByCommonName,
			String... commonNames)
	{
		return clientCertificateFilesByCommonName.entrySet().stream()
				.filter(entry -> Arrays.asList(commonNames).contains(entry.getKey()))
				.sorted(Comparator.comparing(e -> Arrays.asList(commonNames).indexOf(e.getKey()))).map(Entry::getValue)
				.map(CertificateFiles::getCertificateSha512ThumbprintHex);
	}

	private void writeEnvFile(Path target, List<? extends EnvEntry> entries)
	{
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < entries.size(); i++)
		{
			EnvEntry entry = entries.get(i);

			builder.append(entry.userThumbprintsVariableName);
			builder.append('=');
			builder.append(entry.userThumbprints.collect(Collectors.joining(",")));
			builder.append('\n');
		}

		try
		{
			logger.info("Writing .env file to {}", target.toString());
			Files.writeString(target, builder.toString());
		}
		catch (IOException e)
		{
			logger.error("Error while writing .env file to " + target.toString(), e);
			throw new RuntimeException(e);
		}
	}
}
