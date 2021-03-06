package lu.uni.owl.mutatingowls;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyAlreadyExistsException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;

import com.google.common.base.Optional;

public class Mutant extends Ontology {

	private String operator;

	public Mutant(Ontology source, String operator, String details) {
		super();
		this.operator = operator;
		String version = operator + "_" + details;
		OWLOntologyID ontologyID = source.getOntologyID();
		Optional<IRI> versionIRI = Optional.fromNullable(IRI.create(version));
		try {
			ontology = manager.createOntology(new OWLOntologyID(ontologyID.getOntologyIRI(), versionIRI));
			manager.addAxioms(ontology, source.getOntology().getAxioms());
		} catch (OWLOntologyAlreadyExistsException e) {
			System.err.println("Mutant already exists: " + version);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}

	public void save() {
		String path = MutatingOWLs.mutantPath + "/" + this.operator;
		String fileName = getVersionIRI() + ".owl";
		super.save(path, fileName);
	}

}
