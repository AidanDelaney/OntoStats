package com.karlhammar.ontometrics.plugins.axiomatic;

import java.io.File;
import java.util.logging.Logger;

import org.semanticweb.owlapi.model.OWLOntology;

import com.karlhammar.ontometrics.plugins.api.OntoMetricsPlugin;

public class AnnotationRatioFromOwlAPI  implements OntoMetricsPlugin {
    private Logger logger = Logger.getLogger(getClass().getName());
    private StructuralSingletonOWLAPI ss;

    public String getName() {
        return "Annotation Ratio via OWLApi Plugin";
    }

    /**
     * Initialize the plugin. Required before metrics calculation.
     */
    public void init(File ontologyFile) {
        ss = StructuralSingletonOWLAPI.getSingletonObject(ontologyFile);
    }

    public String getMetricAbbreviation() {
        return "AnnotationRatioOWL";
    }

    /**
     */
    public String getMetricValue(File ontologyFile) {
        if (null == ss) {
            logger.info("getMetricValue called before init()!");
            init(ontologyFile);
        }
        OWLOntology ontology = ss.getOntology();

        float total           = ontology.getAxiomCount();
        float annotationCount = ontology.getAnnotations().size();

        return Float.toString(annotationCount/total);
    }

}
