package lu.uni.owl.mutation;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.RemoveAxiom;

public class ObjectPropertyMutantGenerator extends MutantGenerator {

	public ObjectPropertyMutantGenerator(Ontology ont) {
		super(ont);
	}

	public ObjectPropertyMutantGenerator(MutantGenerator mut) {
		super(mut);
	}

	@Override
	public List<MutantGenerator> generateMutants() {
		List<MutantGenerator> ret = new ArrayList<MutantGenerator>();
		for (OWLObjectProperty p : ontology.getObjectProperties())
			if (!p.isTopEntity()) {
				ret.addAll(swapDomainRange(p));
				ret.addAll(removeLabels(p));
				ret.addAll(changeLabelLanguage(p));
			}
		return ret;
	}

	private void changeDomainToRange(OWLObjectProperty property, OWLClass domain) {
		List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
		changes.add(new RemoveAxiom(ontology.getOntology(), factory.getOWLObjectPropertyDomainAxiom(property, domain)));
		changes.add(new AddAxiom(ontology.getOntology(), factory.getOWLObjectPropertyRangeAxiom(property, domain)));
		manager.applyChanges(changes);
	}

	private void changeRangeToDomain(OWLObjectProperty property, OWLClass range) {
		List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
		changes.add(new RemoveAxiom(ontology.getOntology(), factory.getOWLObjectPropertyRangeAxiom(property, range)));
		changes.add(new AddAxiom(ontology.getOntology(), factory.getOWLObjectPropertyDomainAxiom(property, range)));
		manager.applyChanges(changes);
	}

	private List<MutantGenerator> swapDomainRange(OWLObjectProperty property) {
		List<MutantGenerator> ret = new ArrayList<MutantGenerator>();
		for (OWLClassExpression d : ontology.getObjectPropertyDomains(property)) {
			if (!d.isAnonymous()) {
				OWLClass domain = d.asOWLClass();
				ObjectPropertyMutantGenerator mutant = new ObjectPropertyMutantGenerator(
						copy(this, "d2r", ontology.getLabel(property), ontology.getLabel(domain)));
				mutant.changeDomainToRange(property, domain);
				ret.add(mutant);
			}
		}
		for (OWLClassExpression r : ontology.getRanges(property)) {
			if (!r.isAnonymous()) {
				OWLClass range = r.asOWLClass();
				ObjectPropertyMutantGenerator mutant = new ObjectPropertyMutantGenerator(
						copy(this, "r2d", ontology.getLabel(property), ontology.getLabel(range)));
				mutant.changeRangeToDomain(property, range);
				ret.add(mutant);
			}
		}
		return ret;
	}

}
