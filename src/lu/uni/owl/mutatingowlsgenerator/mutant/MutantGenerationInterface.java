package lu.uni.owl.mutatingowlsgenerator.mutant;

import java.util.HashMap;
import java.util.List;

import org.semanticweb.owlapi.model.OWLEntity;

import lu.uni.owl.mutatingowls.Mutant;
import lu.uni.owl.mutatingowls.OpData;

public interface MutantGenerationInterface {

	public HashMap<String, List<Mutant>> generateMutants();

	public List<OpData> getOps(OWLEntity entity);

}
