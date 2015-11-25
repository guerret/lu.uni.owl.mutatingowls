package lu.uni.owl.mutatingowlsgenerator.mutant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import lu.uni.owl.mutatingowls.Mutant;
import lu.uni.owl.mutatingowls.Ontology;
import lu.uni.owl.mutatingowls.OpData;
import lu.uni.owl.mutatingowls.generator.axiom.IndividualAxiomGenerator;

public class IndividualMutantGenerator extends MutantGenerator implements MutantGenerationInterface {

	private IndividualAxiomGenerator axiomGenerator;

	public IndividualMutantGenerator(Ontology ont) {
		super(ont);
		axiomGenerator = new IndividualAxiomGenerator();
	}

	@Override
	public HashMap<String, List<Mutant>> generateMutants() {
		HashMap<String, List<Mutant>> ret = new HashMap<String, List<Mutant>>();
		for (OWLNamedIndividual i : sourceOntology.getIndividuals()) {
			List<OpData> ops = getOps(i);
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
		OWLNamedIndividual i = entity.asOWLNamedIndividual();
		List<OpData> ops = new ArrayList<OpData>();
		ops.add(removeEntity(i));
		ops.add(removeTypes(i));
		ops.add(assignToSuperclass(i));
		ops.add(assignToSubclass(i));
		ops.add(removeLabels(i));
		ops.add(changeLabelLanguage(i));
		return ops;
	}

	private OpData removeTypes(OWLNamedIndividual individual) {
		String opname = "IRT";
		OpData ret = new OpData(opname);
		int counter = 0;
		for (OWLClassExpression s : sourceOntology.getIndividualTypes(individual)) {
			String typeLabel = s.isAnonymous() ? "anonymousClass" + String.format("%04d", counter++)
					: sourceOntology.getLabel(s.asOWLClass());
			Mutant mutant = createMutant(opname, sourceOntology.getLabel(individual), typeLabel);
			sourceOntology.manager.applyChanges(axiomGenerator.axiomsIRT(mutant, individual, s));
			ret.add(mutant);
		}
		return ret;
	}

	private OpData assignToSuperclass(OWLNamedIndividual individual) {
		String opname = "IAP";
		OpData ret = new OpData(opname);
		int counter = 0;
		for (OWLClassExpression s : sourceOntology.getIndividualTypes(individual))
			if (!s.isAnonymous()) {
				for (OWLClassExpression p : sourceOntology.getSuperClasses(s.asOWLClass())) {
					String parentLabel = s.isAnonymous() ? "anonymousClass" + String.format("%04d", counter++)
							: sourceOntology.getLabel(p.asOWLClass());
					Mutant mutant = createMutant(opname, sourceOntology.getLabel(individual), parentLabel);
					List<OWLAxiomChange> changes = axiomGenerator.axiomsIAP(mutant, individual, s, p);
					sourceOntology.manager.applyChanges(changes);
					ret.add(mutant);
				}
			}
		return ret;
	}

	private OpData assignToSubclass(OWLNamedIndividual individual) {
		String opname = "IAC";
		OpData ret = new OpData(opname);
		for (OWLClassExpression s : sourceOntology.getIndividualTypes(individual))
			if (!s.isAnonymous()) {
				for (OWLClassExpression c : sourceOntology.getSubClasses(s.asOWLClass())) {
					String parentLabel = sourceOntology.getLabel(c.asOWLClass());
					Mutant mutant = createMutant(opname, sourceOntology.getLabel(individual), parentLabel);
					List<OWLAxiomChange> changes = axiomGenerator.axiomsIAC(mutant, individual, s, c);
					sourceOntology.manager.applyChanges(changes);
					ret.add(mutant);
				}
			}
		return ret;
	}

}
