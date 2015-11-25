package lu.uni.owl.mutatingowls.generator.axiom;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.RemoveAxiom;

import lu.uni.owl.mutatingowls.Mutant;
import lu.uni.owl.mutatingowlsgenerator.mutant.MutantGenerator;

public class IndividualAxiomGenerator extends EntityAxiomGenerator {

	public List<OWLAxiomChange> axiomsIRT(Mutant mutant, OWLNamedIndividual individual, OWLClassExpression s) {
		List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
		changes.add(new RemoveAxiom(mutant.getOntology(),
				MutantGenerator.factory.getOWLClassAssertionAxiom(s, individual)));
		return changes;
	}

	public List<OWLAxiomChange> axiomsIAP(Mutant mutant, OWLNamedIndividual individual, OWLClassExpression s,
			OWLClassExpression p) {
		List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
		changes.add(new RemoveAxiom(mutant.getOntology(),
				MutantGenerator.factory.getOWLClassAssertionAxiom(s, individual)));
		changes.add(
				new AddAxiom(mutant.getOntology(), MutantGenerator.factory.getOWLClassAssertionAxiom(p, individual)));
		return changes;
	}

	public List<OWLAxiomChange> axiomsIAC(Mutant mutant, OWLNamedIndividual individual, OWLClassExpression s,
			OWLClassExpression c) {
		List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
		changes.add(new RemoveAxiom(mutant.getOntology(),
				MutantGenerator.factory.getOWLClassAssertionAxiom(s, individual)));
		changes.add(
				new AddAxiom(mutant.getOntology(), MutantGenerator.factory.getOWLClassAssertionAxiom(c, individual)));
		return changes;
	}

}
