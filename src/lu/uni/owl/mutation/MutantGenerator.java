package lu.uni.owl.mutation;

import java.io.File;
import java.util.List;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;

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
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		return null;
	}

}
