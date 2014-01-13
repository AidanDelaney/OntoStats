package org.ontologyengineering.ontometrics.plugins;

import java.util.Arrays;
import java.util.List;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.impls.sail.SailGraph;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.pipes.PipeFunction;

public class Filter {
    public enum FilterType {
    ATOM_ONLY,
    CONJUNCTION,
    DISJUNCTION,
    COMPLEMENT,
    ALLOF,
    SOMEOF,
    TOP,
    UNRESTRICTED};

    private static List<String> filters = Arrays.asList(TestUtils.owlnsComplementOf, TestUtils.owlnsIntersectionOf, TestUtils.owlnsUnionOf, TestUtils.owlSomeValuesFrom, TestUtils.owlAllValuesFrom);
    private GremlinPipeline pipeline;

    public Filter(SailGraph sg, FilterType type) {
        Vertex classVertex = sg.getVertex(TestUtils.owlnsClass);
        switch (type) {
            case ATOM_ONLY:
                this.pipeline = getAtomicPipeline(classVertex);
                break;
            case CONJUNCTION:
                this.pipeline = getConjunctionPipeline(classVertex);
                break;
            case DISJUNCTION:
                this.pipeline = getDisjunctionPipeline(classVertex);
                break;
            case SOMEOF:
                this.pipeline = getSomeValuesFromPipeline(classVertex);
                break;
            case ALLOF:
                this.pipeline = getAllValuesFromPipeline(classVertex);
                break;
            case COMPLEMENT:
                this.pipeline = getComplementPipeline(classVertex);
                break;
            case TOP:
                this.pipeline = getTopPipeline(classVertex);
                break;
        }
    }

    private static GremlinPipeline<Vertex, Vertex> getPipelineVerifyIsOfRDFType(Vertex v) {
        return new GremlinPipeline()._().as("ver").out(TestUtils.rdfnsType).retain(Arrays.asList(v)).back("ver");
    }

    private static GremlinPipeline<Vertex, Vertex> getAtomicPipeline(Vertex owlClassVertex) {
        return new GremlinPipeline<Vertex, Vertex>().and(
                new GremlinPipeline<Vertex, Vertex>().add(getPipelineVerifyIsOfRDFType(owlClassVertex))
                , new GremlinPipeline<Vertex, Vertex>().filter(
                    new PipeFunction<Vertex, Boolean>() {
                        @Override
                        public Boolean compute(Vertex v) {
                            boolean containsBannedEdge = false;
                            for(Edge e: v.getEdges(Direction.OUT)) {
                                if(filters.contains(e.getLabel())) containsBannedEdge = true;
                            }
                            return !containsBannedEdge;
                        }
                    }
                )
        );
    }

    private GremlinPipeline getConjunctionPipeline(Vertex owlClassVertex) {
        return new GremlinPipeline().and(
                new GremlinPipeline().add(getPipelineVerifyIsOfRDFType(owlClassVertex))
                , new GremlinPipeline().outE(TestUtils.owlnsIntersectionOf).inV().outE(TestUtils.rdfnsFirst).inV().add(getAtomicPipeline(owlClassVertex))
                , new GremlinPipeline().outE(TestUtils.owlnsIntersectionOf).inV().outE(TestUtils.rdfnsRest).inV().outE(TestUtils.rdfnsFirst).inV().add(getAtomicPipeline(owlClassVertex))
                , new GremlinPipeline().outE(TestUtils.owlnsIntersectionOf).inV().outE(TestUtils.rdfnsRest).inV().outE(TestUtils.rdfnsRest).inV().has("id", TestUtils.rdfnsNil)
        );
    }

    private GremlinPipeline getDisjunctionPipeline(Vertex owlClassVertex) {
        return new GremlinPipeline().and(
                new GremlinPipeline().add(getPipelineVerifyIsOfRDFType(owlClassVertex))
                , new GremlinPipeline().outE(TestUtils.owlnsUnionOf).inV().outE(TestUtils.rdfnsFirst).inV().add(getAtomicPipeline(owlClassVertex))
                , new GremlinPipeline().outE(TestUtils.owlnsUnionOf).inV().outE(TestUtils.rdfnsRest).inV().outE(TestUtils.rdfnsFirst).inV().add(getAtomicPipeline(owlClassVertex))
                , new GremlinPipeline().outE(TestUtils.owlnsUnionOf).inV().outE(TestUtils.rdfnsRest).inV().outE(TestUtils.rdfnsRest).inV().has("id", TestUtils.rdfnsNil)
        );
    }

    private GremlinPipeline getSomeValuesFromPipeline(Vertex owlClassVertex) {
        return new GremlinPipeline().outE(TestUtils.owlSomeValuesFrom).inV().add(getAtomicPipeline(owlClassVertex));
    }

    private GremlinPipeline getAllValuesFromPipeline(Vertex owlClassVertex) {
        return new GremlinPipeline().outE(TestUtils.owlAllValuesFrom).inV().add(getAtomicPipeline(owlClassVertex));
    }

    private GremlinPipeline getComplementPipeline(Vertex owlClassVertex) {
        return new GremlinPipeline().outE(TestUtils.owlnsComplementOf).add(getAtomicPipeline(owlClassVertex));
    }

    private GremlinPipeline getTopPipeline(Vertex owlClassVertex) {
        return new GremlinPipeline().has("id", TestUtils.owlnsThing);
    }

    public GremlinPipeline getPipeline() {
        return pipeline;
    }
};