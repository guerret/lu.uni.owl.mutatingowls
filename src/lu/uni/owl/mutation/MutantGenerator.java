package lu.uni.owl.mutation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import org.semanticweb.owlapi.model.OWLOntologyAlreadyExistsException;
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

	public abstract HashMap<String, List<MutantGenerator>> generateMutants();

	public MutantGenerator(MutantGenerator mut) {
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		if (mut != null)
			ontology = mut.ontology;
	}

	public void save(String path, String fileName) {
		File file = new File(path + File.separator + fileName);
		ontology.save(file);
	}

	public MutantGenerator copy(MutantGenerator source, String mutation, String cls1, String cls2) {
		OWLOntologyID ontologyID = null;
		Optional<IRI> versionIRI = null;
		try {
			ontologyID = ontology.getOntologyID();
			versionIRI = Optional.fromNullable(IRI.create(mutation + "_" + cls1 + "_" + cls2));
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
		} catch (OWLOntologyAlreadyExistsException e) {
			System.err.println("Mutant already exists: " + versionIRI.get());
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected OpData removeEntity(OWLEntity e) {
		String opname = "ERE";
		OpData ret = new OpData(opname);
		MutantGenerator mutant = copy(this, opname, ontology.getLabel(e), "deleted");
		if (mutant.ontology != null) {
			OWLEntityRemover remover = new OWLEntityRemover(Collections.singleton(mutant.ontology.getOntology()));
			e.accept(remover);
			manager.applyChanges(remover.getChanges());
			ret.add(mutant);
		}
		return ret;
	}

	protected OpData removeLabels(OWLEntity entity) {
		String opname = "ERL";
		OpData ret = new OpData(opname);
		for (OWLAnnotation a : ontology.getLabels(entity)) {
			MutantGenerator mutant = copy(this, opname, ontology.getLabel(entity),
					((OWLLiteral) a.getValue()).getLang());
			if (mutant.ontology != null) {
				manager.applyChange(new RemoveAxiom(mutant.ontology.getOntology(),
						factory.getOWLAnnotationAssertionAxiom(entity.getIRI(), a)));
				ret.add(mutant);
			}
		}
		return ret;
	}

	protected OpData changeLabelLanguage(OWLEntity entity) {
		String opname = "ECL";
		OpData ret = new OpData(opname);
		for (OWLAnnotation a : ontology.getLabels(entity)) {
			OWLLiteral label = (OWLLiteral) a.getValue();
			MutantGenerator mutant = copy(this, opname, ontology.getLabel(entity), label.getLang());
			if (mutant.ontology != null) {
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
		}
		return ret;
	}

}
