package lu.uni.owl.mutation;

import java.util.ArrayList;
import java.util.List;

public class MutantGeneration {

	protected static final String OWL_PATH = System.getProperty("user.dir") + "/resources";
	protected static final String OWL_FILE = "dataprotection.owl";

	private Ontology ontology;

	private MutantGeneration() {
		ontology = new Ontology(OWL_PATH, OWL_FILE);
	}

	private List<MutantGenerator> generateMutants() {
		List<MutantGenerator> ret = new ArrayList<MutantGenerator>();
		// ret.addAll(new ClassMutantGenerator(ontology).generateMutants());
		// ret.addAll(new
		// ObjectPropertyMutantGenerator(ontology).generateMutants());
		// ret.addAll(new
		// DataPropertyMutantGenerator(ontology).generateMutants());
		ret.addAll(new LabelMutantGenerator(ontology).generateMutants());
		return ret;
	}

	public static void main(String[] args) {
		MutantGeneration mutantGeneration = new MutantGeneration();
		List<MutantGenerator> mutants = mutantGeneration.generateMutants();
		for (MutantGenerator mutant : mutants)
			mutant.save(OWL_PATH, mutant.ontology.getVersionIRI() + ".owl");
	}

}
