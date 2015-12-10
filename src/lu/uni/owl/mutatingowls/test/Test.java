package lu.uni.owl.mutatingowls.test;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;

import lu.uni.owl.mutatingowls.MutatingOWLs;

public class Test {

	private static final String DEFAULT_ONTOLOGY = "dataprotection-rdf.owl";

	public static void main(String[] args) {
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
		String inputFileName = MutatingOWLs.OWL_PATH + "/" + DEFAULT_ONTOLOGY;
		FileManager.get().readModel(model, inputFileName);
		String uri = model.listOntologies().next().getURI() + "#";

		String queryString = "prefix : <" + uri + "> "
				+ "prefix rdfs: <" + RDFS.getURI() + "> "
				+ "prefix owl: <" + OWL.getURI() + "> "
				+ "select ?subclass ?superclass "
				+ "where {"
				+ "?subclass rdfs:subClassOf :OWLClass_a2ccd29b_a341_4659_919a_977a6a81d4d1 ." 
				+ "}";
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();

		ResultSetFormatter.out(System.out, results, query);
		qe.close();
	}

}
