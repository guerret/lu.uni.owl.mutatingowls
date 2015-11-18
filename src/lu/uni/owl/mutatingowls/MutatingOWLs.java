package lu.uni.owl.mutatingowls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MutatingOWLs {

	protected static final String DEFAULT_ONTOLOGY = "dataprotection.owl";
	protected static final String OWL_PATH = System.getProperty("user.dir") + "/resources";

	private Ontology ontology;
	private String mutantPath;

	private MutatingOWLs(String ontologyName) {
		ontology = new Ontology(OWL_PATH, ontologyName);
		mutantPath = OWL_PATH + "/mutants/" + ontologyName;
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
		String ontologyName = DEFAULT_ONTOLOGY;
		if (args.length > 0)
			ontologyName = args[0];
		MutatingOWLs mutantGeneration = new MutatingOWLs(ontologyName);
		HashMap<String, List<MutantGenerator>> mutants = mutantGeneration.generateMutants();
		ArrayList<String> keys = new ArrayList<String>(mutants.keySet());
		Collections.sort(keys);
		for (String operator : keys) {
			List<MutantGenerator> mutantSet = mutants.get(operator);
			System.out.println(operator + ": " + mutantSet.size());
			String path = mutantGeneration.mutantPath + "/" + operator;
			for (MutantGenerator mutant : mutantSet)
				mutant.save(path, mutant.ontology.getVersionIRI() + ".owl");
		}
	}

}
