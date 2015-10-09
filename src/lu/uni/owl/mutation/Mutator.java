package lu.uni.owl.mutation;

import java.util.ArrayList;

public class Mutator {

	protected static final String OWL_PATH = System.getProperty("user.dir") + "/resources";
	protected static final String OWL_FILE = "dataprotection.owl";

	private Ontology ontology;

	private Mutator() {
		ontology = new Ontology(OWL_PATH, OWL_FILE);
	}

	private void generateMutants() {
		for (String s : ontology.getClassLabels()) {
			ArrayList<Ontology> mutants = ontology.swapWithParents(ontology.findByLabel(s));
			for (Ontology mutant : mutants)
				mutant.save(OWL_PATH, mutant.getVersionIRI() + ".owl");
		}
	}

	public static void main(String[] args) {
		Mutator mutator = new Mutator();
		mutator.generateMutants();
	}

}
