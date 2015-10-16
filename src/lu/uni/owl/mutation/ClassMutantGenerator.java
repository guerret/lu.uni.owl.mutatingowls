package lu.uni.owl.mutation;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.RemoveAxiom;

public class ClassMutantGenerator extends MutantGenerator {

	public ClassMutantGenerator(Ontology ont) {
		super(ont);
	}

	public ClassMutantGenerator(MutantGenerator mut) {
		super(mut);
	}

	@Override
	public List<MutantGenerator> generateMutants() {
		List<MutantGenerator> ret = new ArrayList<MutantGenerator>();
		for (OWLClass c : ontology.getClasses()) {
			ret.addAll(swapWithParents(c));
			ret.addAll(removeParents(c));
			ret.addAll(changeLang(c));
		}
		return ret;
	}

	public List<MutantGenerator> removeParents(OWLClass cls) {
		List<MutantGenerator> ret = new ArrayList<MutantGenerator>();
		int counter = 0;
		for (OWLClassExpression s : ontology.getSuperClasses(cls)) {
			String parentLabel = s.isAnonymous() ? "anonymousClass" + String.format("%04d", counter++)
					: ontology.getLabel(s.asOWLClass());
			ClassMutantGenerator mutant = (ClassMutantGenerator) copy(this, "remove", ontology.getLabel(cls),
					parentLabel);
			manager.applyChange(new RemoveAxiom(mutant.ontology.getOntology(), factory.getOWLSubClassOfAxiom(cls, s)));
			ret.add(mutant);
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

	public List<MutantGenerator> swapWithParents(OWLClass cls) {
		List<MutantGenerator> ret = new ArrayList<MutantGenerator>();
		for (OWLClassExpression s : ontology.getSuperClasses(cls)) {
			if (!s.isAnonymous()) {
				OWLClass parent = s.asOWLClass();
				ClassMutantGenerator mutant = new ClassMutantGenerator(
						copy(this, "swap", ontology.getLabel(cls), ontology.getLabel(parent)));
				mutant.swapClasses(cls, parent);
				ret.add(mutant);
			}
		}
		return ret;
	}

	private List<MutantGenerator> changeLang(OWLEntity c) {
		List<MutantGenerator> ret = new ArrayList<MutantGenerator>();
		return ret;
	}

}
