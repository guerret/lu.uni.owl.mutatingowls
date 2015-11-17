package lu.uni.owl.mutation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.RemoveAxiom;

public class ClassMutantGenerator extends MutantGenerator {

	public ClassMutantGenerator(Ontology ont) {
		super(ont);
	}

	public ClassMutantGenerator(MutantGenerator mut) {
		super(mut);
	}

	@Override
	public HashMap<String, List<MutantGenerator>> generateMutants() {
		HashMap<String, List<MutantGenerator>> ret = new HashMap<String, List<MutantGenerator>>();
		for (OWLClass c : ontology.getClasses()) {
			List<OpData> ops = getOps(c);
			for (OpData op : ops) {
				String opName = op.getOpName();
				if (!ret.containsKey(opName))
					ret.put(opName, new ArrayList<MutantGenerator>());
				ret.get(opName).addAll(op);
			}
		}
		return ret;
	}

	private List<OpData> getOps(OWLClass c) {
		List<OpData> ops = new ArrayList<OpData>();
		ops.add(removeEntity(c));
		ops.add(removeSubclassAxioms(c));
		ops.add(swapWithParents(c));
		ops.add(removeLabels(c));
		ops.add(changeLabelLanguage(c));
		return ops;
	}

	private OpData removeSubclassAxioms(OWLClass cls) {
		String opname = "CRS";
		OpData ret = new OpData(opname);
		int counter = 0;
		for (OWLClassExpression s : ontology.getSuperClasses(cls)) {
			String parentLabel = s.isAnonymous() ? "anonymousClass" + String.format("%04d", counter++)
					: ontology.getLabel(s.asOWLClass());
			ClassMutantGenerator mutant = (ClassMutantGenerator) copy(this, opname, ontology.getLabel(cls),
					parentLabel);
			if (mutant.ontology != null) {
				manager.applyChange(
						new RemoveAxiom(mutant.ontology.getOntology(), factory.getOWLSubClassOfAxiom(cls, s)));
				ret.add(mutant);
			}
		}
		return ret;
	}

	private void swapClasses(OWLClass cls1, OWLClass cls2) {
		List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
		for (OWLClassExpression s : ontology.getSubClasses(cls1)) {
			OWLClass child = s.asOWLClass();
			changes.add(new RemoveAxiom(ontology.getOntology(), factory.getOWLSubClassOfAxiom(child, cls1)));
			changes.add(new AddAxiom(ontology.getOntology(), factory.getOWLSubClassOfAxiom(child, cls2)));
		}
		for (OWLClassExpression s : ontology.getSuperClasses(cls2)) {
			if (!s.isAnonymous()) {
				OWLClass grandParent = s.asOWLClass();
				changes.add(new RemoveAxiom(ontology.getOntology(), factory.getOWLSubClassOfAxiom(cls2, grandParent)));
				changes.add(new AddAxiom(ontology.getOntology(), factory.getOWLSubClassOfAxiom(cls1, grandParent)));
			}
		}
		changes.add(new RemoveAxiom(ontology.getOntology(), factory.getOWLSubClassOfAxiom(cls1, cls2)));
		changes.add(new AddAxiom(ontology.getOntology(), factory.getOWLSubClassOfAxiom(cls2, cls1)));
		manager.applyChanges(changes);
	}

	private OpData swapWithParents(OWLClass cls) {
		String opname = "CSC";
		OpData ret = new OpData(opname);
		for (OWLClassExpression s : ontology.getSuperClasses(cls)) {
			if (!s.isAnonymous()) {
				OWLClass parent = s.asOWLClass();
				ClassMutantGenerator mutant = new ClassMutantGenerator(
						copy(this, opname, ontology.getLabel(cls), ontology.getLabel(parent)));
				if (mutant.ontology != null) {
					mutant.swapClasses(cls, parent);
					ret.add(mutant);
				}
			}
		}
		return ret;
	}

}
