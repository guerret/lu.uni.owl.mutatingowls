package lu.uni.owl.mutation;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.RemoveAxiom;

public class DataPropertyMutantGenerator extends MutantGenerator {

	public DataPropertyMutantGenerator(Ontology ont) {
		super(ont);
	}

	public DataPropertyMutantGenerator(MutantGenerator mut) {
		super(mut);
	}

	@Override
	public List<MutantGenerator> generateMutants() {
		List<MutantGenerator> ret = new ArrayList<MutantGenerator>();
		for (OWLDataProperty p : ontology.getDataProperties())
			if (!p.isTopEntity()) {
				ret.addAll(removeEntity(p));
				ret.addAll(reassignDataPropertyToSuperclass(p));
				ret.addAll(reassignDataPropertyToSubclass(p));
				ret.addAll(removeDataTypes(p));
				ret.addAll(removeLabels(p));
				ret.addAll(changeLabelLanguage(p));
			}
		return ret;
	}

	private void reassignDataPropertyDomain(OWLDataProperty property, OWLClass domain, OWLClass target) {
		List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
		changes.add(new RemoveAxiom(ontology.getOntology(), factory.getOWLDataPropertyDomainAxiom(property, domain)));
		changes.add(new AddAxiom(ontology.getOntology(), factory.getOWLDataPropertyDomainAxiom(property, target)));
		manager.applyChanges(changes);
	}

	private List<MutantGenerator> reassignDataPropertyToSuperclass(OWLDataProperty property) {
		String opname = "DAP";
		List<MutantGenerator> ret = new ArrayList<MutantGenerator>();
		for (OWLClassExpression d : ontology.getDataPropertyDomains(property)) {
			if (!d.isAnonymous()) {
				OWLClass cls = d.asOWLClass();
				for (OWLClassExpression s : ontology.getSuperClasses(cls)) {
					if (!s.isAnonymous()) {
						OWLClass c = s.asOWLClass();
						DataPropertyMutantGenerator mutant = new DataPropertyMutantGenerator(
								copy(this, opname, ontology.getLabel(property), ontology.getLabel(c)));
						mutant.reassignDataPropertyDomain(property, cls, c);
						ret.add(mutant);
					}
				}
			}
		}
		return ret;
	}

	private List<MutantGenerator> reassignDataPropertyToSubclass(OWLDataProperty property) {
		String opname = "DAC";
		List<MutantGenerator> ret = new ArrayList<MutantGenerator>();
		for (OWLClassExpression d : ontology.getDataPropertyDomains(property)) {
			if (!d.isAnonymous()) {
				OWLClass cls = d.asOWLClass();
				for (OWLClassExpression s : ontology.getSubClasses(cls)) {
					if (!s.isAnonymous()) {
						OWLClass c = s.asOWLClass();
						DataPropertyMutantGenerator mutant = new DataPropertyMutantGenerator(
								copy(this, opname, ontology.getLabel(property), ontology.getLabel(c)));
						mutant.reassignDataPropertyDomain(property, cls, c);
						ret.add(mutant);
					}
				}
			}
		}
		return ret;
	}

	private List<MutantGenerator> removeDataTypes(OWLDataProperty property) {
		String opname = "DRT";
		List<MutantGenerator> ret = new ArrayList<MutantGenerator>();
		for (OWLDataRange r : ontology.getDataPropertyRanges(property)) {
			OWLDatatype type = r.asOWLDatatype();
			if (!type.isTopDatatype()) {
				String typeLabel = ontology.getLabel(type).replace(':', '_');
				DataPropertyMutantGenerator mutant = new DataPropertyMutantGenerator(
						copy(this, opname, ontology.getLabel(property), typeLabel));
				List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
				factory.getOWLDataPropertyRangeAxiom(property, factory.getRDFPlainLiteral());
				changes.add(new RemoveAxiom(mutant.ontology.getOntology(),
						factory.getOWLDataPropertyRangeAxiom(property, r)));
				manager.applyChanges(changes);
				ret.add(mutant);
			}
		}
		return ret;
	}

}
