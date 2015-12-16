package lu.uni.owl.mutatingowls.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.resultset.ResultSetCompare;
import org.apache.jena.util.FileManager;

import lu.uni.owl.mutatingowls.MutatingOWLs;

public class Test {

	private static final String DEFAULT_ONTOLOGY = "pizza";

	private static String ontologyFile = MutatingOWLs.OWL_PATH + "/" + DEFAULT_ONTOLOGY + ".owl";
	private static String testFile = MutatingOWLs.OWL_PATH + "/" + DEFAULT_ONTOLOGY + "-tests.rq";

	private Query[] queries = null;

	private List<ResultSetRewindable> groundResults = new ArrayList<ResultSetRewindable>();

	public Test() {
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
		FileManager.get().readModel(model, ontologyFile);
		String[] queryStrings = null;
		try {
			FileReader fileReader = new FileReader(testFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String lines = "";
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				lines += line + System.lineSeparator();
			}
			bufferedReader.close();
			queryStrings = lines.split(System.lineSeparator() + System.lineSeparator());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		queries = new Query[queryStrings.length];
		QueryExecution[] qes = new QueryExecution[queryStrings.length];
		for (int i = 0; i < queryStrings.length; i++) {
			queries[i] = QueryFactory.create(queryStrings[i]);
			QueryExecution qe = QueryExecutionFactory.create(queries[i], model);
			qes[i] = qe;
			ResultSet results = qe.execSelect();
			groundResults.add(ResultSetFactory.copyResults(results));

			// qe.close();
		}
	}

	public static void main(String[] args) {
		// String uri = model.listOntologies().next().getURI() + "#";
		// String prefixes = "prefix : <" + uri + "> "
		// + "prefix rdf: <" + RDF.getURI() + "> "
		// + "prefix rdfs: <" + RDFS.getURI() + "> "
		// + "prefix owl: <" + OWL.getURI() + "> ";
		// List<String> queryStrings = new ArrayList<String>();
		Test testRunner = new Test();
		String mutantDir = MutatingOWLs.OWL_PATH + "/mutants/" + DEFAULT_ONTOLOGY + ".owl" + "/";
		File directory = new File(mutantDir);
		File[] subdirs = directory.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
		for (File dir : subdirs) {
			File[] files = dir.listFiles((FileFilter) FileFileFilter.FILE);
			int numKilled = 0;
			System.out.println(dir.getName() + ": total " + files.length);
			for (int i = 0; i < files.length; i++) {
				File f = files[i];
				boolean equal = testRunner.testMutant(f.getPath(), testFile);
				if (!equal) {
					System.out.print(".");
					numKilled++;
				} else
					System.out.print("x");
			}
			System.out.println();
			System.out.println(dir.getName() + ": " + numKilled + "/" + files.length);
		}

	}

	private boolean testMutant(String ontologyFile, String testFile) {
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
		FileManager.get().readModel(model, ontologyFile);
		boolean equal = true;
		for (int i = 0; i < queries.length; i++) {
			QueryExecution qe = QueryExecutionFactory.create(queries[i], model);
			ResultSetRewindable results = ResultSetFactory.copyResults(qe.execSelect());
			if (!ResultSetCompare.equalsByTerm(results, groundResults.get(i)))
				equal = false;
			groundResults.get(i).reset();

			qe.close();
			if (!equal)
				return false;
		}
		model.close();
		return equal;
	}

}
