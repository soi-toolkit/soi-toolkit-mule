package org.soitoolkit.tools.generator.plugin.model.impl;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.soitoolkit.tools.generator.plugin.model.enums.MuleVersionEnum;
import org.soitoolkit.tools.generator.plugin.model.enums.TransportEnum;

public class DefaultModelImplTest {

	@Test
	public void testIsGroupIdSuffixedWithArtifactId() {
		DefaultModelImpl dm = new DefaultModelImpl();
		
		String groupId = "se.callista.soitoolkit.test";
		String artifactId = "test";
		String version = null;
		String service = null;
		MuleVersionEnum muleVersion = null;
		List<TransportEnum> transports = null;
		String serviceDescriptor = null;
		List<String> operations = null;
		dm.initModel(groupId, artifactId, version, service, muleVersion, transports, serviceDescriptor, operations);
		assertTrue(dm.isGroupIdSuffixedWithArtifactId());

		groupId = "test";
		artifactId = "test";
		dm.initModel(groupId, artifactId, version, service, muleVersion, transports, serviceDescriptor, operations);
		assertTrue(dm.isGroupIdSuffixedWithArtifactId());

		groupId = "se.callista.soitoolkit.test";
		artifactId = null;
		dm.initModel(groupId, artifactId, version, service, muleVersion, transports, serviceDescriptor, operations);
		assertFalse(dm.isGroupIdSuffixedWithArtifactId());

		groupId = null;
		artifactId = "test";
		dm.initModel(groupId, artifactId, version, service, muleVersion, transports, serviceDescriptor, operations);
		assertFalse(dm.isGroupIdSuffixedWithArtifactId());

		groupId = "se.callista.soitoolkit.test";
		artifactId = "notFound";
		dm.initModel(groupId, artifactId, version, service, muleVersion, transports, serviceDescriptor, operations);
		assertFalse(dm.isGroupIdSuffixedWithArtifactId());

		groupId = "se.callista.soitoolkit.test";
		artifactId = "te";
		dm.initModel(groupId, artifactId, version, service, muleVersion, transports, serviceDescriptor, operations);
		assertFalse(dm.isGroupIdSuffixedWithArtifactId());

		groupId = "short";
		artifactId = "loooooong";
		dm.initModel(groupId, artifactId, version, service, muleVersion, transports, serviceDescriptor, operations);
		assertFalse(dm.isGroupIdSuffixedWithArtifactId());
	}

}
