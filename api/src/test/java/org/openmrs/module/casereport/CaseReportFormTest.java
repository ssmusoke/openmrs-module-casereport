/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.casereport;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class CaseReportFormTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	private CaseReportService service;
	
	/**
	 * @see CaseReportForm#getIdentifierType()
	 * @verifies generate the report form for the specified patient
	 */
	@Test
	public void shouldGenerateTheReportFormForTheSpecifiedPatient() throws Exception {
		executeDataSet("moduleTestData-initial.xml");
		executeDataSet("moduleTestData-other.xml");
		PatientService ps = Context.getPatientService();
		Patient patient = ps.getPatient(2);
		patient.setDead(true);
		patient.setCauseOfDeath(Context.getConceptService().getConcept(22));
		Date deathDate = new Date();
		patient.setDeathDate(deathDate);
		ps.savePatient(patient);
		CaseReport caseReport = service.getCaseReport(1);
		assertEquals(patient, caseReport.getPatient());
		assertNull(caseReport.getReportForm());
		CaseReportForm reportForm = new CaseReportForm(caseReport);
		assertNotNull(reportForm);
		assertEquals(patient.getPersonName().getGivenName(), reportForm.getGivenName());
		assertEquals(patient.getPersonName().getMiddleName(), reportForm.getMiddleName());
		assertEquals(patient.getPersonName().getFamilyName(), reportForm.getFamilyName());
		assertEquals(patient.getPersonName().getFullName(), reportForm.getFullName());
		PatientIdentifier pid = patient.getPatientIdentifier();
		assertEquals(pid.getUuid(), reportForm.getPatientIdentifier().getUuid());
		assertEquals(pid.getIdentifier(), reportForm.getPatientIdentifier().getValue());
		assertEquals(pid.getIdentifierType().getUuid(), reportForm.getIdentifierType().getUuid());
		assertEquals(pid.getIdentifierType().getName(), reportForm.getIdentifierType().getValue());
		assertEquals(patient.getGender(), reportForm.getGender());
		assertEquals(0, reportForm.getBirthdate().indexOf("1975-04-08T00:00:00.000"));
		assertEquals(CaseReportConstants.DATE_FORMATTER.format(deathDate), reportForm.getDeathdate());
		assertEquals(patient.isDead(), reportForm.getDead());
		assertEquals(2, reportForm.getTriggers().size());
		assertTrue(CaseReportUtil.collContainsItemWithValue(reportForm.getTriggers(), "HIV Switched To Second Line"));
		assertTrue(CaseReportUtil.collContainsItemWithValue(reportForm.getTriggers(), "New HIV Case"));
		assertEquals(3, reportForm.getMostRecentViralLoads().size());
		assertEquals(3, reportForm.getMostRecentCd4Counts().size());
		assertEquals(3, reportForm.getMostRecentHivTests().size());
		assertEquals("77fb7f47-b80a-4056-9285-bd798be13c63", reportForm.getMostRecentViralLoad().getUuid());
		assertEquals("7dfb7f47-c80a-4056-9285-bd767be13c63", reportForm.getMostRecentCd4Count().getUuid());
		assertEquals("4afb7f47-d80a-4056-9285-bd767be13c63", reportForm.getMostRecentHivTest().getUuid());
		assertEquals(2, reportForm.getCurrentHivMedications().size());
		assertEquals("WHO HIV stage 2", reportForm.getCurrentHivWhoStage().getValue());
		assertEquals("Regimen failure", reportForm.getMostRecentArvStopReason().getValue());
		assertEquals(0, reportForm.getLastVisitDate().getValue().toString().indexOf("2016-06-15T00:00:00.000"));
		assertEquals(patient.getCauseOfDeath().getUuid(), reportForm.getCauseOfDeath().getUuid());
		assertEquals(patient.getCauseOfDeath().getName().getName(), reportForm.getCauseOfDeath().getValue());
	}
}
