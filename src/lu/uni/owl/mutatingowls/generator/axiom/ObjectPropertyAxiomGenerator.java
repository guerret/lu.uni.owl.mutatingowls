package lu.uni.owl.mutatingowls.generator.axiom;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.RemoveAxiom;

import lu.uni.owl.mutatingowls.Mutant;
import lu.uni.owl.mutatingowlsgenerator.mutant.MutantGenerator;

public class ObjectPropertyAxiomGenerator extends EntityAxiomGenerator {

	public List<OWLAxiomChange> axiomsOND(Mutant mutant, OWLObjectProperty property, OWLClass domain) {
		List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
		changes.add(new RemoveAxiom(mutant.getOntology(),
				MutantGenerator.factory.getOWLObjectPropertyDomainAxiom(property, domain)));
		return changes;
	}

	public List<OWLAxiomChange> axiomsONR(Mutant mutant, OWLObjectProperty property, OWLClass domain) {
		List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
		changes.add(new RemoveAxiom(mutant.getOntology(),
				MutantGenerator.factory.getOWLObjectPropertyRangeAxiom(property, domain)));
		return changes;
	}

	public List<OWLAxiomChange> axiomsODR(Mutant mutant, OWLObjectProperty property, OWLClass domain) {
		List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
		OWLOntology ontology = mutant.getOntology();
		changes.add(
				new RemoveAxiom(ontology, MutantGenerator.factory.getOWLObjectPropertyDomainAxiom(property, domain)));
		changes.add(new AddAxiom(ontology, MutantGenerator.factory.getOWLObjectPropertyRangeAxiom(property, domain)));
		return changes;
	}

	public List<OWLAxiomChange> axiomsORD(Mutant mutant, OWLObjectProperty property, OWLClass range) {
		List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
		OWLOntology ontology = mutant.getOntology();
		changes.add(new RemoveAxiom(ontology, MutantGenerator.factory.getOWLObjectPropertyRangeAxiom(property, range)));
		changes.add(new AddAxiom(ontology, MutantGenerator.factory.getOWLObjectPropertyDomainAxiom(property, range)));
		return changes;
	}

	public List<OWLAxiomChange> axiomsODP(Mutant mutant, OWLObjectProperty property, OWLClass domain, OWLClass target) {
		List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
		OWLOntology ontology = mutant.getOntology();
		changes.add(
				new RemoveAxiom(ontology, MutantGenerator.factory.getOWLObjectPropertyDomainAxiom(property, domain)));
		changes.add(new AddAxiom(ontology, MutantGenerator.factory.getOWLObjectPropertyDomainAxiom(property, target)));
		return changes;
	}

	public List<OWLAxiomChange> axiomsODC(Mutant mutant, OWLObjectProperty property, OWLClass domain, OWLClass target) {
		List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
		OWLOntology ontology = mutant.getOntology();
		changes.add(
				new RemoveAxiom(ontology, MutantGenerator.factory.getOWLObjectPropertyDomainAxiom(property, domain)));
		changes.add(new AddAxiom(ontology, MutantGenerator.factory.getOWLObjectPropertyDomainAxiom(property, target)));
		return changes;
	}

	public List<OWLAxiomChange> axiomsORP(Mutant mutant, OWLObjectProperty property, OWLClass domain, OWLClass target) {
		List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
		OWLOntology ontology = mutant.getOntology();
		changes.add(
				new RemoveAxiom(ontology, MutantGenerator.factory.getOWLObjectPropertyRangeAxiom(property, domain)));
		changes.add(new AddAxiom(ontology, MutantGenerator.factory.getOWLObjectPropertyRangeAxiom(property, target)));
		return changes;
	}

	public List<OWLAxiomChange> axiomsORC(Mutant mutant, OWLObjectProperty property, OWLClass domain, OWLClass target) {
		List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
		OWLOntology ontology = mutant.getOntology();
		changes.add(
				new RemoveAxiom(ontology, MutantGenerator.factory.getOWLObjectPropertyRangeAxiom(property, domain)));
		changes.add(new AddAxiom(ontology, MutantGenerator.factory.getOWLObjectPropertyRangeAxiom(property, target)));
		return changes;
	}

	public List<OWLAxiomChange> axiomsORI(Mutant mutant, OWLObjectProperty property, OWLObjectProperty inverse) {
		List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
		OWLOntology ontology = mutant.getOntology();
		changes.add(new RemoveAxiom(ontology,
				MutantGenerator.factory.getOWLInverseObjectPropertiesAxiom(property, inverse)));
		return changes;
	}

}
