package lu.uni.owl.mutation;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.RemoveAxiom;

public class IndividualMutantGenerator extends MutantGenerator {

	public IndividualMutantGenerator(Ontology ont) {
		super(ont);
	}

	public IndividualMutantGenerator(MutantGenerator mut) {
		super(mut);
	}

	@Override
	public List<MutantGenerator> generateMutants() {
		List<MutantGenerator> ret = new ArrayList<MutantGenerator>();
		for (OWLNamedIndividual i : ontology.getIndividuals()) {
			ret.addAll(removeTypes(i));
			ret.addAll(assignToParents(i));
			ret.addAll(removeLabels(i));
			ret.addAll(changeLabelLanguage(i));
		}
		return ret;
	}

	private List<MutantGenerator> removeTypes(OWLNamedIndividual individual) {
		List<MutantGenerator> ret = new ArrayList<MutantGenerator>();
		int counter = 0;
		for (OWLClassExpression s : ontology.getIndividualTypes(individual)) {
			String typeLabel = s.isAnonymous() ? "anonymousClass" + String.format("%04d", counter++)
					: ontology.getLabel(s.asOWLClass());
			IndividualMutantGenerator mutant = (IndividualMutantGenerator) copy(this, "rtype",
					ontology.getLabel(individual), typeLabel);
			manager.applyChange(
					new RemoveAxiom(mutant.ontology.getOntology(), factory.getOWLClassAssertionAxiom(s, individual)));
			ret.add(mutant);
		}
		return ret;
	}

	private List<MutantGenerator> assignToParents(OWLNamedIndividual individual) {
		List<MutantGenerator> ret = new ArrayList<MutantGenerator>();
		int counter = 0;
		for (OWLClassExpression s : ontology.getIndividualTypes(individual))
			if (!s.isAnonymous()) {
				for (OWLClassExpression p : ontology.getSuperClasses(s.asOWLClass())) {
					String parentLabel = s.isAnonymous() ? "anonymousClass" + String.format("%04d", counter++)
							: ontology.getLabel(p.asOWLClass());
					IndividualMutantGenerator mutant = (IndividualMutantGenerator) copy(this, "atype",
							ontology.getLabel(individual), parentLabel);
					List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
					changes.add(new RemoveAxiom(mutant.ontology.getOntology(),
							factory.getOWLClassAssertionAxiom(s, individual)));
					changes.add(new AddAxiom(mutant.ontology.getOntology(),
							factory.getOWLClassAssertionAxiom(p, individual)));
					manager.applyChanges(changes);
					ret.add(mutant);
				}
			}
		return ret;
	}

}
