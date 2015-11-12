package lu.uni.owl.mutation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import com.google.common.base.Optional;

public abstract class MutantGenerator {

	protected OWLOntologyManager manager;
	protected OWLDataFactory factory;
	protected Ontology ontology;

	public MutantGenerator(Ontology ont) {
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		ontology = ont;
	}

	public abstract List<MutantGenerator> generateMutants();

	public MutantGenerator(MutantGenerator mut) {
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		ontology = mut.ontology;
	}

	public void save(String path, String fileName) {
		File file = new File(path + File.separator + fileName);
		ontology.save(file);
	}

	public MutantGenerator copy(MutantGenerator source, String mutation, String cls1, String cls2) {
		try {
			OWLOntologyID ontologyID = ontology.getOntologyID();
			Optional<IRI> versionIRI = Optional.fromNullable(IRI.create(mutation + "_" + cls1 + "_" + cls2));
			OWLOntology owlMutant = manager.createOntology(new OWLOntologyID(ontologyID.getOntologyIRI(), versionIRI));
			manager.addAxioms(owlMutant, ontology.getOntology().getAxioms());
			if (source instanceof ClassMutantGenerator)
				return new ClassMutantGenerator(new Ontology(owlMutant));
			if (source instanceof ObjectPropertyMutantGenerator)
				return new ObjectPropertyMutantGenerator(new Ontology(owlMutant));
			if (source instanceof DataPropertyMutantGenerator)
				return new DataPropertyMutantGenerator(new Ontology(owlMutant));
			if (source instanceof IndividualMutantGenerator)
				return new IndividualMutantGenerator(new Ontology(owlMutant));
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected List<MutantGenerator> removeEntity(OWLEntity e) {
		List<MutantGenerator> ret = new ArrayList<MutantGenerator>();
		MutantGenerator mutant = copy(this, "ERE", ontology.getLabel(e), "deleted");
		OWLEntityRemover remover = new OWLEntityRemover(Collections.singleton(mutant.ontology.getOntology()));
		e.accept(remover);
		manager.applyChanges(remover.getChanges());
		ret.add(mutant);
		return ret;
	}

	protected List<MutantGenerator> removeLabels(OWLEntity entity) {
		List<MutantGenerator> ret = new ArrayList<MutantGenerator>();
		for (OWLAnnotation a : ontology.getLabels(entity)) {
			MutantGenerator mutant = copy(this, "ERL", ontology.getLabel(entity),
					((OWLLiteral) a.getValue()).getLang());
			manager.applyChange(new RemoveAxiom(mutant.ontology.getOntology(),
					factory.getOWLAnnotationAssertionAxiom(entity.getIRI(), a)));
			ret.add(mutant);
		}
		return ret;
	}

	protected List<MutantGenerator> changeLabelLanguage(OWLEntity entity) {
		List<MutantGenerator> ret = new ArrayList<MutantGenerator>();
		for (OWLAnnotation a : ontology.getLabels(entity)) {
			OWLLiteral label = (OWLLiteral) a.getValue();
			MutantGenerator mutant = copy(this, "ECL", ontology.getLabel(entity), label.getLang());
			List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
			OWLLiteral lbl = factory.getOWLLiteral(label.getLiteral(), "xx");
			OWLAnnotation newLabel = factory
					.getOWLAnnotation(factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()), lbl);
			changes.add(new RemoveAxiom(mutant.ontology.getOntology(),
					factory.getOWLAnnotationAssertionAxiom(entity.getIRI(), a)));
			changes.add(new AddAxiom(mutant.ontology.getOntology(),
					factory.getOWLAnnotationAssertionAxiom(entity.getIRI(), newLabel)));
			manager.applyChanges(changes);
			ret.add(mutant);
		}
		return ret;
	}

}
