package lu.uni.owl.mutation;

import java.util.ArrayList;
import java.util.HashMap;
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
	public HashMap<String, List<MutantGenerator>> generateMutants() {
		HashMap<String, List<MutantGenerator>> ret = new HashMap<String, List<MutantGenerator>>();
		for (OWLObjectProperty p : ontology.getObjectProperties())
			if (!p.isTopEntity()) {
				List<OpData> ops = getOps(p);
				for (OpData op : ops) {
					String opName = op.getOpName();
					if (!ret.containsKey(opName))
						ret.put(opName, new ArrayList<MutantGenerator>());
					ret.get(opName).addAll(op);
				}
			}
		return ret;
	}

	private List<OpData> getOps(OWLObjectProperty p) {
		List<OpData> ops = new ArrayList<OpData>();
		ops.add(removeEntity(p));
		ops.add(removeDomains(p));
		ops.add(removeRanges(p));
		ops.add(domainToRange(p));
		ops.add(rangeToDomain(p));
		ops.add(reassignObjectPropertyDomainToSuperclass(p));
		ops.add(reassignObjectPropertyDomainToSubclass(p));
		ops.add(reassignObjectPropertyRangeToSuperclass(p));
		ops.add(reassignObjectPropertyRangeToSubclass(p));
		ops.add(removeLabels(p));
		ops.add(changeLabelLanguage(p));
		return ops;
	}

	private OpData removeDomains(OWLObjectProperty property) {
		String opname = "OND";
		OpData ret = new OpData(opname);
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

	private OpData removeRanges(OWLObjectProperty property) {
		String opname = "ONR";
		OpData ret = new OpData(opname);
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

	private OpData domainToRange(OWLObjectProperty property) {
		String opname = "ODR";
		OpData ret = new OpData(opname);
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

	private OpData rangeToDomain(OWLObjectProperty property) {
		String opname = "ORD";
		OpData ret = new OpData(opname);
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

	private OpData reassignObjectPropertyDomainToSuperclass(OWLObjectProperty property) {
		String opname = "ODP";
		OpData ret = new OpData(opname);
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

	private OpData reassignObjectPropertyDomainToSubclass(OWLObjectProperty property) {
		String opname = "ODC";
		OpData ret = new OpData(opname);
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

	private void reassignObjectPropertyRange(OWLObjectProperty property, OWLClass domain, OWLClass target) {
		List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
		changes.add(new RemoveAxiom(ontology.getOntology(), factory.getOWLObjectPropertyRangeAxiom(property, domain)));
		changes.add(new AddAxiom(ontology.getOntology(), factory.getOWLObjectPropertyRangeAxiom(property, target)));
		manager.applyChanges(changes);
	}

	private OpData reassignObjectPropertyRangeToSuperclass(OWLObjectProperty property) {
		String opname = "ORP";
		OpData ret = new OpData(opname);
		for (OWLClassExpression d : ontology.getRanges(property)) {
			if (!d.isAnonymous()) {
				OWLClass cls = d.asOWLClass();
				for (OWLClassExpression s : ontology.getSuperClasses(cls)) {
					if (!s.isAnonymous()) {
						OWLClass c = s.asOWLClass();
						ObjectPropertyMutantGenerator mutant = new ObjectPropertyMutantGenerator(
								copy(this, opname, ontology.getLabel(property), ontology.getLabel(c)));
						mutant.reassignObjectPropertyRange(property, cls, c);
						ret.add(mutant);
					}
				}
			}
		}
		return ret;
	}

	private OpData reassignObjectPropertyRangeToSubclass(OWLObjectProperty property) {
		String opname = "ORC";
		OpData ret = new OpData(opname);
		for (OWLClassExpression d : ontology.getRanges(property)) {
			if (!d.isAnonymous()) {
				OWLClass cls = d.asOWLClass();
				for (OWLClassExpression s : ontology.getSubClasses(cls)) {
					if (!s.isAnonymous()) {
						OWLClass c = s.asOWLClass();
						ObjectPropertyMutantGenerator mutant = new ObjectPropertyMutantGenerator(
								copy(this, opname, ontology.getLabel(property), ontology.getLabel(c)));
						mutant.reassignObjectPropertyRange(property, cls, c);
						ret.add(mutant);
					}
				}
			}
		}
		return ret;
	}

}
