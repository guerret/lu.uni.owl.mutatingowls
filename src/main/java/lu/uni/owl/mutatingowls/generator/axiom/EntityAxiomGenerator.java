package lu.uni.owl.mutatingowls.generator.axiom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import lu.uni.owl.mutatingowls.Mutant;
import lu.uni.owl.mutatingowlsgenerator.mutant.MutantGenerator;

public class EntityAxiomGenerator {

	public List<RemoveAxiom> axiomsERE(Mutant mutant, OWLEntity e) {
		OWLEntityRemover remover = new OWLEntityRemover(Collections.singleton(mutant.getOntology()));
		e.accept(remover);
		return remover.getChanges();
	}

	public List<OWLAxiomChange> axiomsERL(Mutant mutant, OWLEntity entity, OWLAnnotation a) {
		List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
		changes.add(new RemoveAxiom(mutant.getOntology(),
				MutantGenerator.factory.getOWLAnnotationAssertionAxiom(entity.getIRI(), a)));
		return changes;
	}

	public List<OWLAxiomChange> axiomsECL(Mutant mutant, OWLEntity entity, OWLAnnotation a) {
		List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
		OWLLiteral label = (OWLLiteral) a.getValue();
		OWLLiteral lbl = MutantGenerator.factory.getOWLLiteral(label.getLiteral(), "xx");
		OWLAnnotation newLabel = MutantGenerator.factory
				.getOWLAnnotation(MutantGenerator.factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()), lbl);
		changes.add(new RemoveAxiom(mutant.getOntology(),
				MutantGenerator.factory.getOWLAnnotationAssertionAxiom(entity.getIRI(), a)));
		changes.add(
				new AddAxiom(mutant.getOntology(), MutantGenerator.factory.getOWLAnnotationAssertionAxiom(entity.getIRI(), newLabel)));
		return changes;
	}

}
