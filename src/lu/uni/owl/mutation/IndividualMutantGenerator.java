package lu.uni.owl.mutation;

import java.util.ArrayList;
import java.util.HashMap;
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
	public HashMap<String, List<MutantGenerator>> generateMutants() {
		HashMap<String, List<MutantGenerator>> ret = new HashMap<String, List<MutantGenerator>>();
		for (OWLNamedIndividual i : ontology.getIndividuals()) {
			List<OpData> ops = getOps(i);
			for (OpData op : ops) {
				String opName = op.getOpName();
				if (!ret.containsKey(opName))
					ret.put(opName, new ArrayList<MutantGenerator>());
				ret.get(opName).addAll(op);
			}
		}
		return ret;
	}

	private List<OpData> getOps(OWLNamedIndividual i) {
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
		for (OWLClassExpression s : ontology.getIndividualTypes(individual)) {
			String typeLabel = s.isAnonymous() ? "anonymousClass" + String.format("%04d", counter++)
					: ontology.getLabel(s.asOWLClass());
			IndividualMutantGenerator mutant = (IndividualMutantGenerator) copy(this, opname,
					ontology.getLabel(individual), typeLabel);
			manager.applyChange(
					new RemoveAxiom(mutant.ontology.getOntology(), factory.getOWLClassAssertionAxiom(s, individual)));
			ret.add(mutant);
		}
		return ret;
	}

	private OpData assignToSuperclass(OWLNamedIndividual individual) {
		String opname = "IAP";
		OpData ret = new OpData(opname);
		int counter = 0;
		for (OWLClassExpression s : ontology.getIndividualTypes(individual))
			if (!s.isAnonymous()) {
				for (OWLClassExpression p : ontology.getSuperClasses(s.asOWLClass())) {
					String parentLabel = s.isAnonymous() ? "anonymousClass" + String.format("%04d", counter++)
							: ontology.getLabel(p.asOWLClass());
					IndividualMutantGenerator mutant = (IndividualMutantGenerator) copy(this, opname,
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

	private OpData assignToSubclass(OWLNamedIndividual individual) {
		String opname = "IAC";
		OpData ret = new OpData(opname);
		for (OWLClassExpression s : ontology.getIndividualTypes(individual))
			if (!s.isAnonymous()) {
				for (OWLClassExpression p : ontology.getSubClasses(s.asOWLClass())) {
					String parentLabel = ontology.getLabel(p.asOWLClass());
					IndividualMutantGenerator mutant = (IndividualMutantGenerator) copy(this, opname,
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
