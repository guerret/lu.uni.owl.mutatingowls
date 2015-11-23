package lu.uni.owl.mutatingowls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
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
		// ops.add(removeEntity(c));
		// ops.add(removeSubclassAxioms(c));
		// ops.add(swapWithParents(c));
		// ops.add(removeLabels(c));
		// ops.add(changeLabelLanguage(c));
		ops.add(removeDisjointWith(c));
		ops.add(removeEquivalentWith(c));
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

	private OpData removeDisjointWith(OWLClass cls) {
		String opname = "CRD";
		OpData ret = new OpData(opname);
		for (OWLDisjointClassesAxiom disjointSet : ontology.getOntology().getDisjointClassesAxioms(cls)) {
			Set<OWLClass> disjoints = disjointSet.getClassesInSignature();
			disjoints.remove(cls);
			for (OWLClass d : disjoints) {
				int counter = 0;
				String disjointLabel = d.isAnonymous() ? "anonymousClass" + String.format("%04d", counter++)
						: ontology.getLabel(d);
				if (!d.isAnonymous()) {
					ClassMutantGenerator mutant = new ClassMutantGenerator(
							copy(this, opname, ontology.getLabel(cls), disjointLabel));
					if (mutant.ontology != null) {
						mutant.manager.applyChange(new RemoveAxiom(mutant.ontology.getOntology(), disjointSet));
						List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
						changes.add(new RemoveAxiom(mutant.ontology.getOntology(),
								mutant.factory.getOWLDisjointClassesAxiom(disjoints)));
						if (disjoints.size() > 1) {
							Set<OWLClass> newSet = disjointSet.getClassesInSignature();
							newSet.remove(d);
							changes.add(new AddAxiom(mutant.ontology.getOntology(),
									factory.getOWLDisjointClassesAxiom(newSet)));
						}
						mutant.manager.applyChanges(changes);
						ret.add(mutant);
					}
				}
			}
		}
		return ret;
	}

	private OpData removeEquivalentWith(OWLClass cls) {
		String opname = "CRE";
		OpData ret = new OpData(opname);
		for (OWLEquivalentClassesAxiom equivalentSet : ontology.getOntology().getEquivalentClassesAxioms(cls)) {
			Set<OWLClass> equivalents = equivalentSet.getClassesInSignature();
			equivalents.remove(cls);
			for (OWLClass e : equivalents) {
				int counter = 0;
				String equivalentLabel = e.isAnonymous() ? "anonymousClass" + String.format("%04d", counter++)
						: ontology.getLabel(e);
				if (!e.isAnonymous()) {
					ClassMutantGenerator mutant = new ClassMutantGenerator(
							copy(this, opname, ontology.getLabel(cls), equivalentLabel));
					if (mutant.ontology != null) {
						mutant.manager.applyChange(new RemoveAxiom(mutant.ontology.getOntology(), equivalentSet));
						List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
						changes.add(new RemoveAxiom(mutant.ontology.getOntology(),
								mutant.factory.getOWLEquivalentClassesAxiom(equivalents)));
						if (equivalents.size() > 1) {
							Set<OWLClass> newSet = equivalentSet.getClassesInSignature();
							newSet.remove(e);
							changes.add(new AddAxiom(mutant.ontology.getOntology(),
									factory.getOWLEquivalentClassesAxiom(newSet)));
						}
						mutant.manager.applyChanges(changes);
						ret.add(mutant);
					}
				}
			}
		}
		return ret;
	}

}
