package lu.uni.owl.mutation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MutantGeneration {

	protected static final String ONTOLOGY = "dataprotection";

	protected static final String OWL_PATH = System.getProperty("user.dir") + "/resources";
	protected static final String OWL_FILE = ONTOLOGY + ".owl";
	protected static final String MUTANT_PATH = OWL_PATH + "/mutants/" + ONTOLOGY;

	private Ontology ontology;

	private MutantGeneration() {
		ontology = new Ontology(OWL_PATH, OWL_FILE);
	}

	private HashMap<String, List<MutantGenerator>> generateMutants() {
		HashMap<String, List<MutantGenerator>> ret = new HashMap<String, List<MutantGenerator>>();
		List<HashMap<String, List<MutantGenerator>>> generators = new ArrayList<HashMap<String, List<MutantGenerator>>>();
		generators.add(new ClassMutantGenerator(ontology).generateMutants());
		generators.add(new ObjectPropertyMutantGenerator(ontology).generateMutants());
		generators.add(new DataPropertyMutantGenerator(ontology).generateMutants());
		generators.add(new IndividualMutantGenerator(ontology).generateMutants());
		for (HashMap<String, List<MutantGenerator>> generator : generators) {
			for (String key : generator.keySet()) {
				if (ret.containsKey(key))
					ret.get(key).addAll(generator.get(key));
				else
					ret.put(key, generator.get(key));
			}
		}
		return ret;
	}

	public static void main(String[] args) {
		MutantGeneration mutantGeneration = new MutantGeneration();
		HashMap<String, List<MutantGenerator>> mutants = mutantGeneration.generateMutants();
		ArrayList<String> keys = new ArrayList<String>(mutants.keySet());
		Collections.sort(keys);
		for (String operator : keys) {
			List<MutantGenerator> mutantSet = mutants.get(operator);
			System.out.println(operator + ": " + mutantSet.size());
			String path = MUTANT_PATH + "/" + operator;
			for (MutantGenerator mutant : mutantSet)
				mutant.save(path, mutant.ontology.getVersionIRI() + ".owl");
		}
	}

}
