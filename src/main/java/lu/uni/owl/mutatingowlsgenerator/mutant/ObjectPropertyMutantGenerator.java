package lu.uni.owl.mutatingowlsgenerator.mutant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import lu.uni.owl.mutatingowls.Mutant;
import lu.uni.owl.mutatingowls.Ontology;
import lu.uni.owl.mutatingowls.OpData;
import lu.uni.owl.mutatingowls.generator.axiom.ObjectPropertyAxiomGenerator;

public class ObjectPropertyMutantGenerator extends MutantGenerator implements MutantGenerationInterface {

	private ObjectPropertyAxiomGenerator axiomGenerator;

	public ObjectPropertyMutantGenerator(Ontology ont) {
		super(ont);
		axiomGenerator = new ObjectPropertyAxiomGenerator();
	}

	@Override
	public HashMap<String, List<Mutant>> generateMutants() {
		HashMap<String, List<Mutant>> ret = new HashMap<String, List<Mutant>>();
		for (OWLObjectProperty p : sourceOntology.getObjectProperties())
			if (!p.isTopEntity()) {
				List<OpData> ops = getOps(p);
				for (OpData op : ops) {
					String opName = op.getOpName();
					if (!ret.containsKey(opName))
						ret.put(opName, new ArrayList<Mutant>());
					ret.get(opName).addAll(op);
				}
			}
		return ret;
	}

	@Override
	public List<OpData> getOps(OWLEntity entity) {
		OWLObjectProperty p = entity.asOWLObjectProperty();
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
		ops.add(removeInverseProperty(p));
		ops.add(removeLabels(p));
		ops.add(changeLabelLanguage(p));
		return ops;
	}

	private OpData removeDomains(OWLObjectProperty property) {
		String opname = "OND";
		OpData ret = new OpData(opname);
		for (OWLClassExpression d : sourceOntology.getObjectPropertyDomains(property)) {
			if (!d.isAnonymous()) {
				OWLClass domain = d.asOWLClass();
				Mutant mutant = createMutant(opname, sourceOntology.getLabel(property),
						sourceOntology.getLabel(domain));
				sourceOntology.manager.applyChanges(axiomGenerator.axiomsOND(mutant, property, domain));
				ret.add(mutant);
			}
		}
		return ret;
	}

	private OpData removeRanges(OWLObjectProperty property) {
		String opname = "ONR";
		OpData ret = new OpData(opname);
		for (OWLClassExpression r : sourceOntology.getRanges(property)) {
			if (!r.isAnonymous()) {
				OWLClass domain = r.asOWLClass();
				Mutant mutant = createMutant(opname, sourceOntology.getLabel(property),
						sourceOntology.getLabel(domain));
				List<OWLAxiomChange> changes = axiomGenerator.axiomsONR(mutant, property, domain);
				sourceOntology.manager.applyChanges(changes);
				ret.add(mutant);
			}
		}
		return ret;
	}

	private OpData domainToRange(OWLObjectProperty property) {
		String opname = "ODR";
		OpData ret = new OpData(opname);
		for (OWLClassExpression d : sourceOntology.getObjectPropertyDomains(property)) {
			if (!d.isAnonymous()) {
				OWLClass domain = d.asOWLClass();
				Mutant mutant = createMutant(opname, sourceOntology.getLabel(property),
						sourceOntology.getLabel(domain));
				mutant.manager.applyChanges(axiomGenerator.axiomsODR(mutant, property, domain));
				ret.add(mutant);
			}
		}
		return ret;
	}

	private OpData rangeToDomain(OWLObjectProperty property) {
		String opname = "ORD";
		OpData ret = new OpData(opname);
		for (OWLClassExpression r : sourceOntology.getRanges(property)) {
			if (!r.isAnonymous()) {
				OWLClass range = r.asOWLClass();
				Mutant mutant = createMutant(opname, sourceOntology.getLabel(property), sourceOntology.getLabel(range));
				mutant.manager.applyChanges(axiomGenerator.axiomsORD(mutant, property, range));
				ret.add(mutant);
			}
		}
		return ret;
	}

	private OpData reassignObjectPropertyDomainToSuperclass(OWLObjectProperty property) {
		String opname = "ODP";
		OpData ret = new OpData(opname);
		for (OWLClassExpression d : sourceOntology.getObjectPropertyDomains(property)) {
			if (!d.isAnonymous()) {
				OWLClass cls = d.asOWLClass();
				for (OWLClassExpression s : sourceOntology.getSuperClasses(cls)) {
					if (!s.isAnonymous()) {
						OWLClass c = s.asOWLClass();
						Mutant mutant = createMutant(opname, sourceOntology.getLabel(property),
								sourceOntology.getLabel(cls) + "-" + sourceOntology.getLabel(c));
						mutant.manager.applyChanges(axiomGenerator.axiomsODP(mutant, property, cls, c));
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
		for (OWLClassExpression d : sourceOntology.getObjectPropertyDomains(property)) {
			if (!d.isAnonymous()) {
				OWLClass cls = d.asOWLClass();
				for (OWLClassExpression s : sourceOntology.getSubClasses(cls)) {
					if (!s.isAnonymous()) {
						OWLClass c = s.asOWLClass();
						Mutant mutant = createMutant(opname, sourceOntology.getLabel(property),
								sourceOntology.getLabel(cls) + "-" + sourceOntology.getLabel(c));
						mutant.manager.applyChanges(axiomGenerator.axiomsODC(mutant, property, cls, c));
						ret.add(mutant);
					}
				}
			}
		}
		return ret;
	}

	private OpData reassignObjectPropertyRangeToSuperclass(OWLObjectProperty property) {
		String opname = "ORP";
		OpData ret = new OpData(opname);
		for (OWLClassExpression d : sourceOntology.getRanges(property)) {
			if (!d.isAnonymous()) {
				OWLClass cls = d.asOWLClass();
				for (OWLClassExpression s : sourceOntology.getSuperClasses(cls)) {
					if (!s.isAnonymous()) {
						OWLClass c = s.asOWLClass();
						Mutant mutant = createMutant(opname, sourceOntology.getLabel(property),
								sourceOntology.getLabel(cls) + "-" + sourceOntology.getLabel(c));
						mutant.manager.applyChanges(axiomGenerator.axiomsORP(mutant, property, cls, c));
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
		for (OWLClassExpression d : sourceOntology.getRanges(property)) {
			if (!d.isAnonymous()) {
				OWLClass cls = d.asOWLClass();
				for (OWLClassExpression s : sourceOntology.getSubClasses(cls)) {
					if (!s.isAnonymous()) {
						OWLClass c = s.asOWLClass();
						Mutant mutant = createMutant(opname, sourceOntology.getLabel(property),
								sourceOntology.getLabel(cls) + "-" + sourceOntology.getLabel(c));
						mutant.manager.applyChanges(axiomGenerator.axiomsORC(mutant, property, cls, c));
						ret.add(mutant);
					}
				}
			}
		}
		return ret;
	}

	private OpData removeInverseProperty(OWLObjectProperty property) {
		String opname = "ORI";
		OpData ret = new OpData(opname);
		OWLObjectPropertyExpression inverse = property.getInverseProperty();
		if (!inverse.isAnonymous()) {
			OWLObjectProperty i = inverse.asOWLObjectProperty();
			Mutant mutant = createMutant(opname, sourceOntology.getLabel(property), sourceOntology.getLabel(i));
			mutant.manager.applyChanges(axiomGenerator.axiomsORI(mutant, property, i));
			ret.add(mutant);
		}
		return ret;
	}

}
