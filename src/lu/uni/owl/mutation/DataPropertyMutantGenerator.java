package lu.uni.owl.mutation;

import java.util.ArrayList;
import java.util.HashMap;
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
	public HashMap<String, List<MutantGenerator>> generateMutants() {
		HashMap<String, List<MutantGenerator>> ret = new HashMap<String, List<MutantGenerator>>();
		for (OWLDataProperty p : ontology.getDataProperties())
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

	private List<OpData> getOps(OWLDataProperty p) {
		List<OpData> ops = new ArrayList<OpData>();
		ops.add(removeEntity(p));
		ops.add(reassignDataPropertyToSuperclass(p));
		ops.add(reassignDataPropertyToSubclass(p));
		ops.add(removeDataTypes(p));
		ops.add(removeLabels(p));
		ops.add(changeLabelLanguage(p));
		return ops;
	}

	private void reassignDataPropertyDomain(OWLDataProperty property, OWLClass domain, OWLClass target) {
		List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
		changes.add(new RemoveAxiom(ontology.getOntology(), factory.getOWLDataPropertyDomainAxiom(property, domain)));
		changes.add(new AddAxiom(ontology.getOntology(), factory.getOWLDataPropertyDomainAxiom(property, target)));
		manager.applyChanges(changes);
	}

	private OpData reassignDataPropertyToSuperclass(OWLDataProperty property) {
		String opname = "DAP";
		OpData ret = new OpData(opname);
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

	private OpData reassignDataPropertyToSubclass(OWLDataProperty property) {
		String opname = "DAC";
		OpData ret = new OpData(opname);
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

	private OpData removeDataTypes(OWLDataProperty property) {
		String opname = "DRT";
		OpData ret = new OpData(opname);
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
