package lu.uni.owl.mutatingowlsgenerator.mutant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;

import lu.uni.owl.mutatingowls.Mutant;
import lu.uni.owl.mutatingowls.Ontology;
import lu.uni.owl.mutatingowls.OpData;
import lu.uni.owl.mutatingowls.generator.axiom.DataPropertyAxiomGenerator;

public class DataPropertyMutantGenerator extends MutantGenerator implements MutantGenerationInterface {

	private DataPropertyAxiomGenerator axiomGenerator;

	public DataPropertyMutantGenerator(Ontology ont) {
		super(ont);
		axiomGenerator = new DataPropertyAxiomGenerator();
	}

	@Override
	public HashMap<String, List<Mutant>> generateMutants() {
		HashMap<String, List<Mutant>> ret = new HashMap<String, List<Mutant>>();
		for (OWLDataProperty p : sourceOntology.getDataProperties())
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
		OWLDataProperty p = entity.asOWLDataProperty();
		List<OpData> ops = new ArrayList<OpData>();
		ops.add(removeEntity(p));
		ops.add(reassignDataPropertyToSuperclass(p));
		ops.add(reassignDataPropertyToSubclass(p));
		ops.add(removeDataTypes(p));
		ops.add(removeLabels(p));
		ops.add(changeLabelLanguage(p));
		return ops;
	}

	private OpData reassignDataPropertyToSuperclass(OWLDataProperty property) {
		String opname = "DAP";
		OpData ret = new OpData(opname);
		for (OWLClassExpression d : sourceOntology.getDataPropertyDomains(property)) {
			if (!d.isAnonymous()) {
				OWLClass cls = d.asOWLClass();
				for (OWLClassExpression s : sourceOntology.getSuperClasses(cls)) {
					if (!s.isAnonymous()) {
						OWLClass c = s.asOWLClass();
						Mutant mutant = createMutant(opname, sourceOntology.getLabel(property),
								sourceOntology.getLabel(cls) + "-" + sourceOntology.getLabel(c));
						mutant.manager.applyChanges(axiomGenerator.axiomsDAP(mutant, property, cls, c));
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
		for (OWLClassExpression d : sourceOntology.getDataPropertyDomains(property)) {
			if (!d.isAnonymous()) {
				OWLClass cls = d.asOWLClass();
				for (OWLClassExpression s : sourceOntology.getSubClasses(cls)) {
					if (!s.isAnonymous()) {
						OWLClass c = s.asOWLClass();
						Mutant mutant = createMutant(opname, sourceOntology.getLabel(property),
								sourceOntology.getLabel(cls) + "-" + sourceOntology.getLabel(c));
						mutant.manager.applyChanges(axiomGenerator.axiomsDAC(mutant, property, cls, c));
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
		for (OWLDataRange r : sourceOntology.getDataPropertyRanges(property)) {
			OWLDatatype type = r.asOWLDatatype();
			if (!type.isTopDatatype()) {
				String typeLabel = sourceOntology.getLabel(type).replace(':', '_');
				Mutant mutant = createMutant(opname, sourceOntology.getLabel(property), typeLabel);
				mutant.manager.applyChanges(axiomGenerator.axiomsDRT(mutant, property, r));
				ret.add(mutant);
			}
		}
		return ret;
	}

}
