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
				ret.addAll(removeEntity(p));
				ret.addAll(removeDomains(p));
				ret.addAll(removeRanges(p));
				ret.addAll(domainToRange(p));
				ret.addAll(rangeToDomain(p));
				ret.addAll(reassignObjectPropertyToSuperclass(p));
				ret.addAll(reassignObjectPropertyToSubclass(p));
				ret.addAll(removeLabels(p));
				ret.addAll(changeLabelLanguage(p));
			}
		return ret;
	}

	private List<MutantGenerator> removeDomains(OWLObjectProperty property) {
		String opname = "OND";
		List<MutantGenerator> ret = new ArrayList<MutantGenerator>();
		for (OWLClassExpression d : ontology.getObjectPropertyDomains(property)) {
			if (!d.isAnonymous()) {
				OWLClass domain = d.asOWLClass();
				ObjectPropertyMutantGenerator mutant = new ObjectPropertyMutantGenerator(
						copy(this, opname, ontology.getLabel(property), ontology.getLabel(domain)));
				manager.applyChange(new RemoveAxiom(mutant.ontology.getOntology(),
						factory.getOWLObjectPropertyDomainAxiom(property, domain)));
				ret.add(mutant);
			}
		}
		return ret;
	}

	private List<MutantGenerator> removeRanges(OWLObjectProperty property) {
		String opname = "ONR";
		List<MutantGenerator> ret = new ArrayList<MutantGenerator>();
		for (OWLClassExpression r : ontology.getRanges(property)) {
			if (!r.isAnonymous()) {
				OWLClass domain = r.asOWLClass();
				ObjectPropertyMutantGenerator mutant = new ObjectPropertyMutantGenerator(
						copy(this, opname, ontology.getLabel(property), ontology.getLabel(domain)));
				manager.applyChange(new RemoveAxiom(mutant.ontology.getOntology(),
						factory.getOWLObjectPropertyRangeAxiom(property, domain)));
				ret.add(mutant);
			}
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

	private List<MutantGenerator> domainToRange(OWLObjectProperty property) {
		String opname = "ODR";
		List<MutantGenerator> ret = new ArrayList<MutantGenerator>();
		for (OWLClassExpression d : ontology.getObjectPropertyDomains(property)) {
			if (!d.isAnonymous()) {
				OWLClass domain = d.asOWLClass();
				ObjectPropertyMutantGenerator mutant = new ObjectPropertyMutantGenerator(
						copy(this, opname, ontology.getLabel(property), ontology.getLabel(domain)));
				mutant.changeDomainToRange(property, domain);
				ret.add(mutant);
			}
		}
		return ret;
	}

	private List<MutantGenerator> rangeToDomain(OWLObjectProperty property) {
		String opname = "ORD";
		List<MutantGenerator> ret = new ArrayList<MutantGenerator>();
		for (OWLClassExpression r : ontology.getRanges(property)) {
			if (!r.isAnonymous()) {
				OWLClass range = r.asOWLClass();
				ObjectPropertyMutantGenerator mutant = new ObjectPropertyMutantGenerator(
						copy(this, opname, ontology.getLabel(property), ontology.getLabel(range)));
				mutant.changeRangeToDomain(property, range);
				ret.add(mutant);
			}
		}
		return ret;
	}

	private void reassignObjectPropertyDomain(OWLObjectProperty property, OWLClass domain, OWLClass target) {
		List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
		changes.add(new RemoveAxiom(ontology.getOntology(), factory.getOWLObjectPropertyDomainAxiom(property, domain)));
		changes.add(new AddAxiom(ontology.getOntology(), factory.getOWLObjectPropertyDomainAxiom(property, target)));
		manager.applyChanges(changes);
	}

	private List<MutantGenerator> reassignObjectPropertyToSuperclass(OWLObjectProperty property) {
		String opname = "OAP";
		List<MutantGenerator> ret = new ArrayList<MutantGenerator>();
		for (OWLClassExpression d : ontology.getObjectPropertyDomains(property)) {
			if (!d.isAnonymous()) {
				OWLClass cls = d.asOWLClass();
				for (OWLClassExpression s : ontology.getSuperClasses(cls)) {
					if (!s.isAnonymous()) {
						OWLClass c = s.asOWLClass();
						ObjectPropertyMutantGenerator mutant = new ObjectPropertyMutantGenerator(
								copy(this, opname, ontology.getLabel(property), ontology.getLabel(c)));
						mutant.reassignObjectPropertyDomain(property, cls, c);
						ret.add(mutant);
					}
				}
			}
		}
		return ret;
	}

	private List<MutantGenerator> reassignObjectPropertyToSubclass(OWLObjectProperty property) {
		String opname = "OAC";
		List<MutantGenerator> ret = new ArrayList<MutantGenerator>();
		for (OWLClassExpression d : ontology.getObjectPropertyDomains(property)) {
			if (!d.isAnonymous()) {
				OWLClass cls = d.asOWLClass();
				for (OWLClassExpression s : ontology.getSubClasses(cls)) {
					if (!s.isAnonymous()) {
						OWLClass c = s.asOWLClass();
						ObjectPropertyMutantGenerator mutant = new ObjectPropertyMutantGenerator(
								copy(this, opname, ontology.getLabel(property), ontology.getLabel(c)));
						mutant.reassignObjectPropertyDomain(property, cls, c);
						ret.add(mutant);
					}
				}
			}
		}
		return ret;
	}

}
