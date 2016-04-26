package lu.uni.owl.mutatingowls.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.jena.ontology.OntClass;
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
import org.apache.jena.util.iterator.ExtendedIterator;

import lu.uni.owl.mutatingowls.MutatingOWLs;

public class Test {

	private static final String DEFAULT_ONTOLOGY = "pizza";

	private static String ontologyFile;
	private static String testFile;
	private static String coverageFile;
	private static String mutantDirectory;
	private static final String outputPath = MutatingOWLs.OWL_PATH + "/results";

	private OntModel model;

	private Query[] queries = null;
	private QueryExecution[] qes = null;
	private String coverageString;

	private List<ResultSetRewindable> groundResults = new ArrayList<ResultSetRewindable>();

	class MutantResult {

		public boolean equal;
		public double coverage;

		MutantResult(double cov) {
			equal = true;
			coverage = cov;
		}
	}

	public Test() {
		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
		FileManager.get().readModel(model, ontologyFile);
		String[] queryStrings = null;
		try (FileReader fileReader = new FileReader(testFile);
				BufferedReader bufferedReader = new BufferedReader(fileReader);) {
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

		try (FileReader fileReader = new FileReader(coverageFile);
				BufferedReader bufferedReader = new BufferedReader(fileReader);) {
			String lines = "";
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				lines += line + System.lineSeparator();
			}
			bufferedReader.close();
			coverageString = lines;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Test(String queryFile) {
		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
		FileManager.get().readModel(model, ontologyFile);
		String queryString = "";
		try (FileReader fileReader = new FileReader(queryFile);
				BufferedReader bufferedReader = new BufferedReader(fileReader);) {
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

	private double coverage(OntModel model) {
		int total = 0;
		ExtendedIterator<OntClass> classes = model.listClasses();
		while (classes.hasNext()) {
			OntClass thisClass = (OntClass) classes.next();
			if (thisClass.getNameSpace() != null
					&& thisClass.getNameSpace().equals("http://www.uni.lu/dataprotection#"))
				total++;
		}

		Query query = QueryFactory.create(coverageString);
		QueryExecution qes = QueryExecutionFactory.create(query, model);
		ResultSetRewindable rSet = ResultSetFactory.copyResults(qes.execSelect());
		int count = rSet.nextSolution().get("count").asLiteral().getInt();
		qes.close();

		return (double) count / total * 100;
	}

	private MutantResult testMutant(String ontologyFile) {
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
		FileManager.get().readModel(model, ontologyFile);
		MutantResult mr = new MutantResult(coverage(model));
		for (int i = 0; i < queries.length; i++) {
			QueryExecution qe = QueryExecutionFactory.create(queries[i], model);
			ResultSetRewindable results = ResultSetFactory.copyResults(qe.execSelect());
			if (!ResultSetCompare.equalsByTerm(results, groundResults.get(i)))
				mr.equal = false;
			groundResults.get(i).reset();

			qe.close();
			if (!mr.equal)
				return mr;
		}
		model.close();
		return mr;
	}

	/**
	 * With the second argument, it runs a single test. Without the second
	 * argument, it runs mutation testing (using the queries in the file
	 * ONTOLOGYNAME-tests.rq). Without arguments, it runs mutation testing on
	 * the default (pizza) ontology.
	 * 
	 * @param args
	 *            Up to two arguments. The first one is the ontology name (no
	 *            file, without extension). If missing, it falls back to the
	 *            pizza ontology. The second (optional) argument is the query to
	 *            run the test. It must be a file containing a single SPARQL
	 *            query
	 */
	public static void main(String[] args) {
		String ontology = DEFAULT_ONTOLOGY;
		if (args.length > 0 && !args[0].isEmpty()) {
			ontology = args[0];
		}
		ontologyFile = MutatingOWLs.OWL_PATH + "/" + ontology + "-rdf.owl";
		testFile = MutatingOWLs.OWL_PATH + "/" + ontology + "-tests.rq";
		coverageFile = MutatingOWLs.OWL_PATH + "/" + ontology + "-tests-coverage.rq";
		mutantDirectory = MutatingOWLs.OWL_PATH + "/mutants/" + ontology + ".owl" + "/";
		String coverageFile = outputPath + "/" + ontology + "-coverage.txt";
		String mutantFile = outputPath + "/" + ontology + "-mutants.txt";
		if (args.length > 1 && !args[1].isEmpty()) {
			String queryFile = MutatingOWLs.OWL_PATH + "/" + args[1];
			Test testRunner = new Test(queryFile);
			testRunner.runSingleTest();
			System.exit(0);
		}
		Test testRunner = new Test();
		File directory = new File(mutantDirectory);
		File[] subdirs = directory.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
		String covText = "";
		String mutText = "";
		DecimalFormat formatter = new DecimalFormat("#0.00");
		if (subdirs != null)
			for (File dir : subdirs) {
				File[] files = dir.listFiles((FileFilter) FileFileFilter.FILE);
				int numKilled = 0;
				String txt = dir.getName() + ": total " + files.length + "\n";
				covText += txt;
				mutText += txt;
				System.out.print(txt);
				for (int i = 0; i < files.length; i++) {
					File f = files[i];
					MutantResult mr = testRunner.testMutant(f.getPath());
					covText += formatter.format(mr.coverage) + " ";
					if (!mr.equal) {
						txt = ".";
						numKilled++;
					} else {
						txt = "x";
					}
					mutText += txt;
					System.out.print(txt);
					// Here we have mr.coverage
				}
				covText += "\n\n";
				mutText += "\n";
				System.out.println();
				txt = dir.getName() + ": " + numKilled + "/" + files.length + "\n\n";
				mutText += txt;
				System.out.print(txt);
			}
		for (QueryExecution qe : testRunner.qes)
			qe.close();
		try {
			File file = new File(coverageFile);
			file.getParentFile().mkdirs();
			PrintWriter out = new PrintWriter(file);
			out.println(covText);
			out.close();
			out = new PrintWriter(mutantFile);
			out.println(mutText);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
