package lu.uni.owl.mutatingowls;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import lu.uni.owl.mutatingowlsgenerator.mutant.ClassMutantGenerator;
import lu.uni.owl.mutatingowlsgenerator.mutant.DataPropertyMutantGenerator;
import lu.uni.owl.mutatingowlsgenerator.mutant.IndividualMutantGenerator;
import lu.uni.owl.mutatingowlsgenerator.mutant.ObjectPropertyMutantGenerator;

public class MutatingOWLs {

	protected static final String DEFAULT_ONTOLOGY = "dataprotection.owl";
	public static final String OWL_PATH = System.getProperty("user.dir") + "/resources";

	private Ontology ontology;
	private String ontologyFile;
	public static String mutantPath;

	private MutatingOWLs(String ontologyName) {
		try {
			ontologyFile = OWL_PATH + "/" + ontologyName + ".owl";
			ontology = new Ontology(new File(ontologyFile).toURI().toURL());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		mutantPath = OWL_PATH + "/mutants/" + ontologyName + ".owl";
	}

	private HashMap<String, List<Mutant>> generateMutants() {
		HashMap<String, List<Mutant>> ret = new HashMap<String, List<Mutant>>();
		List<HashMap<String, List<Mutant>>> generators = new ArrayList<HashMap<String, List<Mutant>>>();
		generators.add(new ClassMutantGenerator(ontology).generateMutants());
		generators.add(new ObjectPropertyMutantGenerator(ontology).generateMutants());
		generators.add(new DataPropertyMutantGenerator(ontology).generateMutants());
		generators.add(new IndividualMutantGenerator(ontology).generateMutants());
		for (HashMap<String, List<Mutant>> generator : generators) {
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
		HashMap<String, List<Mutant>> mutants = mutantGeneration.generateMutants();
		ArrayList<String> keys = new ArrayList<String>(mutants.keySet());
		Collections.sort(keys);
		for (String operator : keys) {
			List<Mutant> mutantSet = mutants.get(operator);
			System.out.println(operator + ": " + mutantSet.size());
			for (Mutant mutant : mutantSet)
				mutant.save();
		}
	}

}
