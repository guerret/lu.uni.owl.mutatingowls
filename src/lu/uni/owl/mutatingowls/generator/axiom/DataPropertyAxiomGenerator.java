package lu.uni.owl.mutatingowls.generator.axiom;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.RemoveAxiom;

import lu.uni.owl.mutatingowls.Mutant;
import lu.uni.owl.mutatingowlsgenerator.mutant.MutantGenerator;

public class DataPropertyAxiomGenerator extends EntityAxiomGenerator {

	public List<OWLAxiomChange> axiomsDAP(Mutant mutant, OWLDataProperty property, OWLClass domain, OWLClass target) {
		List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
		OWLOntology ontology = mutant.getOntology();
		changes.add(new RemoveAxiom(ontology, MutantGenerator.factory.getOWLDataPropertyDomainAxiom(property, domain)));
		changes.add(new AddAxiom(ontology, MutantGenerator.factory.getOWLDataPropertyDomainAxiom(property, target)));
		return changes;
	}

	public List<OWLAxiomChange> axiomsDAC(Mutant mutant, OWLDataProperty property, OWLClass domain, OWLClass target) {
		List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
		OWLOntology ontology = mutant.getOntology();
		changes.add(new RemoveAxiom(ontology, MutantGenerator.factory.getOWLDataPropertyDomainAxiom(property, domain)));
		changes.add(new AddAxiom(ontology, MutantGenerator.factory.getOWLDataPropertyDomainAxiom(property, target)));
		return changes;
	}

	public List<OWLAxiomChange> axiomsDRT(Mutant mutant, OWLDataProperty property, OWLDataRange r) {
		List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
		changes.add(new RemoveAxiom(mutant.getOntology(),
				MutantGenerator.factory.getOWLDataPropertyRangeAxiom(property, r)));
		return changes;
	}

}
