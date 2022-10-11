package de.medizininformatik_initiative.process.projectathon.data_sharing.message;

import java.util.List;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.AbstractTaskMessageSend;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Target;
import org.highmed.dsf.fhir.variables.Targets;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.UrlType;

import ca.uhn.fhir.context.FhirContext;
import de.medizininformatik_initiative.process.projectathon.data_sharing.ConstantsDataSharing;
import de.medizininformatik_initiative.process.projectathon.data_sharing.variables.Researchers;

public class SendMergeDataSharing extends AbstractTaskMessageSend
{
	public SendMergeDataSharing(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, OrganizationProvider organizationProvider, FhirContext fhirContext)
	{
		super(clientProvider, taskHelper, readAccessHelper, organizationProvider, fhirContext);
	}

	@Override
	protected Stream<Task.ParameterComponent> getAdditionalInputParameters(DelegateExecution execution)
	{
		String projectIdentifier = (String) execution
				.getVariable(ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_PROJECT_IDENTIFIER);
		Task.ParameterComponent projectIdentifierInput = getProjectIdentifierInput(projectIdentifier);

		String contractLocation = (String) execution
				.getVariable(ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_CONTRACT_LOCATION);
		Task.ParameterComponent contractLocationInput = getContractLocationInput(contractLocation);

		Stream<Task.ParameterComponent> otherInputs = Stream.of(projectIdentifierInput, contractLocationInput);

		List<String> researcherIdentifiers = ((Researchers) execution
				.getVariable(ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_RESEARCHER_IDENTIFIERS)).getEntries();
		Stream<Task.ParameterComponent> researcherIdentifierInputs = getResearcherIdentifierInputs(
				researcherIdentifiers);

		Targets targets = (Targets) execution.getVariable(ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGETS);
		Stream<Task.ParameterComponent> correlationKeyInputs = getCorrelationKeyInputs(targets);

		return Stream.of(otherInputs, researcherIdentifierInputs, correlationKeyInputs).reduce(Stream::concat)
				.orElseThrow(() -> new RuntimeException("Could not concat streams"));
	}

	private Task.ParameterComponent getProjectIdentifierInput(String projectIdentifier)
	{
		Task.ParameterComponent projectIdentifierInput = new Task.ParameterComponent();
		projectIdentifierInput.getType().addCoding().setSystem(ConstantsDataSharing.CODESYSTEM_DATA_SHARING)
				.setCode(ConstantsDataSharing.CODESYSTEM_DATA_SHARING_VALUE_PROJECT_IDENTIFIER);
		projectIdentifierInput.setValue(new Identifier().setSystem(ConstantsDataSharing.NAMINGSYSTEM_PROJECT_IDENTIFIER)
				.setValue(projectIdentifier));

		return projectIdentifierInput;
	}

	private Task.ParameterComponent getContractLocationInput(String contractLocation)
	{
		Task.ParameterComponent contractLocationInput = new Task.ParameterComponent();
		contractLocationInput.getType().addCoding().setSystem(ConstantsDataSharing.CODESYSTEM_DATA_SHARING)
				.setCode(ConstantsDataSharing.CODESYSTEM_DATA_SHARING_VALUE_CONTRACT_LOCATION);
		contractLocationInput.setValue(new UrlType(contractLocation));

		return contractLocationInput;
	}

	private Stream<Task.ParameterComponent> getResearcherIdentifierInputs(List<String> researchers)
	{
		return researchers.stream().map(this::transformToResearcherInput);
	}

	private Task.ParameterComponent transformToResearcherInput(String researcherIdentifier)
	{
		Task.ParameterComponent researcherIdentifierInput = new Task.ParameterComponent();
		researcherIdentifierInput.getType().addCoding().setSystem(ConstantsDataSharing.CODESYSTEM_DATA_SHARING)
				.setCode(ConstantsDataSharing.CODESYSTEM_DATA_SHARING_VALUE_RESEARCHER_IDENTIFIER);
		researcherIdentifierInput.setValue(new Identifier()
				.setSystem(ConstantsDataSharing.NAMINGSYSTEM_RESEARCHER_IDENTIFIER).setValue(researcherIdentifier));

		return researcherIdentifierInput;
	}

	private Stream<Task.ParameterComponent> getCorrelationKeyInputs(Targets targets)
	{
		return targets.getEntries().stream().map(this::transformToTargetInput);
	}

	private Task.ParameterComponent transformToTargetInput(Target target)
	{
		Task.ParameterComponent input = getTaskHelper().createInput(ConstantsDataSharing.CODESYSTEM_DATA_SHARING,
				ConstantsDataSharing.CODESYSTEM_DATA_SHARING_VALUE_MEDIC_CORRELATION_KEY, target.getCorrelationKey());

		input.addExtension().setUrl(ConstantsDataSharing.EXTENSION_URL_MEDIC_IDENTIFIER)
				.setValue(new Reference()
						.setIdentifier(
								new Identifier().setSystem(ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER)
										.setValue(target.getOrganizationIdentifierValue()))
						.setType(ResourceType.Organization.name()));

		return input;
	}
}
