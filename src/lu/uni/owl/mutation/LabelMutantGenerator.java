package lu.uni.owl.mutation;

import java.util.ArrayList;
import java.util.List;

public class LabelMutantGenerator extends MutantGenerator {

	public LabelMutantGenerator(Ontology ont) {
		super(ont);
	}

	public LabelMutantGenerator(MutantGenerator mut) {
		super(mut);
	}

	@Override
	public List<MutantGenerator> generateMutants() {
		List<MutantGenerator> ret = new ArrayList<MutantGenerator>();
		return ret;
	}

}
