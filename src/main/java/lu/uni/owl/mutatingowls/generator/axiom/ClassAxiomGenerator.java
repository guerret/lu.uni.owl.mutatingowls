package lu.uni.owl.mutatingowls.generator.axiom;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.RemoveAxiom;

import lu.uni.owl.mutatingowls.Mutant;
import lu.uni.owl.mutatingowlsgenerator.mutant.MutantGenerator;

public class ClassAxiomGenerator extends EntityAxiomGenerator {

	public List<OWLAxiomChange> axiomsCRS(Mutant mutant, OWLClass cls, OWLClassExpression s) {
		List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
		changes.add(new RemoveAxiom(mutant.getOntology(), MutantGenerator.factory.getOWLSubClassOfAxiom(cls, s)));
		return changes;
	}

	public List<OWLAxiomChange> axiomsCSC(Mutant mutant, OWLClass cls1, OWLClass cls2) {
		List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
		OWLOntology ontology = mutant.getOntology();
		for (OWLClassExpression s : mutant.getSubClasses(cls1)) {
			OWLClass child = s.asOWLClass();
			changes.add(new RemoveAxiom(ontology, MutantGenerator.factory.getOWLSubClassOfAxiom(child, cls1)));
			changes.add(new AddAxiom(ontology, MutantGenerator.factory.getOWLSubClassOfAxiom(child, cls2)));
		}
		for (OWLClassExpression s : mutant.getSuperClasses(cls2)) {
			if (!s.isAnonymous()) {
				OWLClass grandParent = s.asOWLClass();
				changes.add(
						new RemoveAxiom(ontology, MutantGenerator.factory.getOWLSubClassOfAxiom(cls2, grandParent)));
				changes.add(new AddAxiom(ontology, MutantGenerator.factory.getOWLSubClassOfAxiom(cls1, grandParent)));
			}
		}
		changes.add(new RemoveAxiom(ontology, MutantGenerator.factory.getOWLSubClassOfAxiom(cls1, cls2)));
		changes.add(new AddAxiom(ontology, MutantGenerator.factory.getOWLSubClassOfAxiom(cls2, cls1)));
		return changes;
	}

	public List<OWLAxiomChange> axiomsCRD(Mutant mutant, OWLClass cls, OWLDisjointClassesAxiom disjointSet,
			OWLClass d) {
		List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
		OWLOntology ontology = mutant.getOntology();
		changes.add(new RemoveAxiom(ontology, disjointSet));
		Set<OWLClass> disjoints = disjointSet.getClassesInSignature();
		if (disjoints.size() > 2) {
			disjoints.remove(d);
			changes.add(new AddAxiom(ontology, MutantGenerator.factory.getOWLDisjointClassesAxiom(disjoints)));
		}
		return changes;
	}

	public List<OWLAxiomChange> axiomsCRE(Mutant mutant, OWLClass cls, OWLEquivalentClassesAxiom equivalentSet,
			OWLClass e) {
		List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
		OWLOntology ontology = mutant.getOntology();
		changes.add(new RemoveAxiom(ontology, equivalentSet));
		Set<OWLClass> equivalents = equivalentSet.getClassesInSignature();
		if (equivalents.size() > 2) {
			equivalents.remove(e);
			changes.add(new AddAxiom(ontology, MutantGenerator.factory.getOWLEquivalentClassesAxiom(equivalents)));
		}
		return changes;
	}

}
