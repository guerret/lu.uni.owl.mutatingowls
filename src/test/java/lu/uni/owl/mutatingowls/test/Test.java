package lu.uni.owl.mutatingowls.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
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
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.resultset.ResultSetCompare;
import org.apache.jena.util.FileManager;

import lu.uni.owl.mutatingowls.MutatingOWLs;

public class Test {

	private static final String DEFAULT_ONTOLOGY = "pizza";

	private static String ontologyFile = MutatingOWLs.OWL_PATH + "/" + DEFAULT_ONTOLOGY + ".owl";
	private static String testFile = MutatingOWLs.OWL_PATH + "/" + DEFAULT_ONTOLOGY + "-tests.rq";
	private static String mutantDirectory = MutatingOWLs.OWL_PATH + "/mutants/" + DEFAULT_ONTOLOGY + ".owl" + "/";

	private OntModel model;

	private Query[] queries = null;
	private QueryExecution[] qes = null;

	private List<ResultSetRewindable> groundResults = new ArrayList<ResultSetRewindable>();

	public Test() {
		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
		FileManager.get().readModel(model, ontologyFile);
		String[] queryStrings = null;
		try (FileReader fileReader = new FileReader(testFile);
				BufferedReader bufferedReader = new BufferedReader(fileReader);) {
			// FileReader fileReader = new FileReader(testFile);
			// BufferedReader bufferedReader = new BufferedReader(fileReader);
			String lines = "";
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				lines += line + System.lineSeparator();
			}
			bufferedReader.close();
			queryStrings = lines.split(System.lineSeparator() + System.lineSeparator());
		} catch (IOException e) {
			e.printStackTrace();
		}

		queries = new Query[queryStrings.length];
		qes = new QueryExecution[queryStrings.length];
		for (int i = 0; i < queryStrings.length; i++) {
			queries[i] = QueryFactory.create(queryStrings[i]);
			qes[i] = QueryExecutionFactory.create(queries[i], model);
			ResultSet results = qes[i].execSelect();
			groundResults.add(ResultSetFactory.copyResults(results));
		}
	}

	public Test(String queryFile) {
		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
		FileManager.get().readModel(model, ontologyFile);
		String queryString = "";
		try (FileReader fileReader = new FileReader(queryFile);
				BufferedReader bufferedReader = new BufferedReader(fileReader);) {
			// FileReader fileReader = new FileReader(queryFile);
			// BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				queryString += line + System.lineSeparator();
			}
			bufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		queries = new Query[1];
		queries[0] = QueryFactory.create(queryString);
	}

	private void runSingleTest() {
		QueryExecution qe = QueryExecutionFactory.create(queries[0], model);
		ResultSet results = qe.execSelect();
		ResultSetFormatter.out(System.out, results);

		qe.close();
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

	public static void main(String[] args) {
		if (args.length > 0 && !args[0].isEmpty()) {
			String queryFile = MutatingOWLs.OWL_PATH + "/" + args[0];
			Test testRunner = new Test(queryFile);
			testRunner.runSingleTest();
			System.exit(0);
		}
		Test testRunner = new Test();
		File directory = new File(mutantDirectory);
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
		for (QueryExecution qe : testRunner.qes)
			qe.close();
	}

}
