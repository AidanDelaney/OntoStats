package com.karlhammar.ontometrics.plugins;

import java.io.File;
import java.util.logging.Logger;

import com.karlhammar.ontometrics.plugins.api.OntoMetricsPlugin;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.profiles.OWL2QLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileReport;

public class QLProfile implements OntoMetricsPlugin {
	
	private Logger logger = Logger.getLogger(getClass().getName());
	
	public String getName() {
		return "QL Profile Plugin";
	}

	public void init() {
		
	}

	public String getMetricAbbreviation() {
		return "QLProfile";
	}

	public String getMetricValue(File ontologyFile) {
		try {
			OWL2QLProfile o2ql = new OWL2QLProfile();
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = manager.loadOntologyFromOntologyDocument(ontologyFile);
			OWLProfileReport report = o2ql.checkOntology(ontology);
			return new Boolean(report.isInProfile()).toString();
		} 
		catch (OWLOntologyCreationException e) {
			logger.severe(e.getMessage());
			return null;
		}
	}

}