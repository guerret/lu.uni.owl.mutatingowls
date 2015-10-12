package lu.uni.owl.mutation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.search.EntitySearcher;

import com.google.common.base.Optional;

public class Ontology {

	private OWLOntology ontology;
	private OWLOntologyManager manager;

	public Ontology(String path, String fileName) {
		manager = OWLManager.createOWLOntologyManager();
		ontology = load(path + File.separator + fileName);
		System.out.println("Number of axioms: " + ontology.getAxiomCount());
		System.out.println("IRI: " + ontology.getOntologyID().getOntologyIRI().get());
	}

	public Ontology(OWLOntology owlMutant) {
		manager = OWLManager.createOWLOntologyManager();
		ontology = owlMutant;
	}

	public OWLOntologyID getOntologyID() {
		return ontology.getOntologyID();
	}

	public IRI getVersionIRI() {
		return ontology.getOntologyID().getVersionIRI().get();
	}

	private OWLOntology load(String fileName) {
		try {
			File file = new File(fileName);
			return manager.loadOntologyFromOntologyDocument(file);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void save(String path, String fileName) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		File file = new File(path + File.separator + fileName);
		OWLDocumentFormat format = new OWLXMLDocumentFormat();
		try {
			manager.saveOntology(ontology, format, IRI.create(file.toURI()));
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		}
	}

	private String getLabel(OWLEntity cls) {
		for (OWLAnnotation a : EntitySearcher.getAnnotations(cls, ontology)) {
			if (a.getProperty().isLabel())
				return ((OWLLiteral) a.getValue()).getLiteral();
		}
		return null;
	}

	public String[] getLabels(Set<? extends OWLEntity> entities) {
		String[] labels = new String[entities.size()];
		int i = 0;
		for (OWLEntity c : entities)
			labels[i++] = getLabel(c);
		return labels;
	}

	public String[] getObjectPropertyLabels(Set<OWLObjectProperty> properties) {
		String[] labels = new String[properties.size()];
		int i = 0;
		for (OWLObjectProperty p : properties)
			labels[i++] = getLabel(p);
		return labels;
	}

	public String[] getClassLabels() {
		return getLabels(ontology.getClassesInSignature());
	}

	public String[] getObjectPropertyLabels() {
		return getObjectPropertyLabels(ontology.getObjectPropertiesInSignature());
	}

	public OWLClass findClassesByLabel(String label) {
		for (OWLClass c : ontology.getClassesInSignature())
			if (getLabel(c).equals(label))
				return c;
		return null;
	}

	public OWLObjectProperty findObjectPropertiesByLabel(String label) {
		for (OWLObjectProperty p : ontology.getObjectPropertiesInSignature())
			if (getLabel(p).equals(label))
				return p;
		return null;
	}

	public Set<OWLClass> getSubClasses(OWLClass cls) {
		Set<OWLClass> ret = new HashSet<OWLClass>();
		for (OWLClassExpression s : EntitySearcher.getSubClasses(cls, ontology)) {
			OWLClass c = (OWLClass) s;
			ret.add(c);
			ret.addAll(getSubClasses(c));
		}
		return ret;
	}

	public Ontology copy(String mutation, String cls1, String cls2) {
		Ontology mutant = null;
		try {
			OWLOntologyID ontologyID = ontology.getOntologyID();
			Optional<IRI> versionIRI = Optional.fromNullable(IRI.create(mutation + "_" + cls1 + "_" + cls2));
			OWLOntology owlMutant = manager.createOntology(new OWLOntologyID(ontologyID.getOntologyIRI(), versionIRI));
			manager.addAxioms(owlMutant, ontology.getAxioms());
			mutant = new Ontology(owlMutant);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		return mutant;
	}

	private void swapClasses(OWLClass cls1, OWLClass cls2) {
		OWLDataFactory factory = manager.getOWLDataFactory();
		List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
		for (OWLClassExpression s : EntitySearcher.getSubClasses(cls1, ontology)) {
			OWLClass child = s.asOWLClass();
			changes.add(new RemoveAxiom(ontology, factory.getOWLSubClassOfAxiom(child, cls1)));
			changes.add(new AddAxiom(ontology, factory.getOWLSubClassOfAxiom(child, cls2)));
		}
		for (OWLClassExpression s : EntitySearcher.getSuperClasses(cls2, ontology)) {
			if (!s.isAnonymous()) {
				OWLClass grandParent = s.asOWLClass();
				changes.add(new RemoveAxiom(ontology, factory.getOWLSubClassOfAxiom(cls2, grandParent)));
				changes.add(new AddAxiom(ontology, factory.getOWLSubClassOfAxiom(cls1, grandParent)));
			}
		}
		changes.add(new RemoveAxiom(ontology, factory.getOWLSubClassOfAxiom(cls1, cls2)));
		changes.add(new AddAxiom(ontology, factory.getOWLSubClassOfAxiom(cls2, cls1)));
		manager.applyChanges(changes);
	}

	public ArrayList<Ontology> swapWithParents(OWLClass cls) {
		ArrayList<Ontology> ret = new ArrayList<Ontology>();
		for (OWLClassExpression s : EntitySearcher.getSuperClasses(cls, ontology)) {
			if (!s.isAnonymous()) {
				OWLClass parent = s.asOWLClass();
				Ontology mutant = copy("swap", getLabel(cls), getLabel(parent));
				mutant.swapClasses(cls, parent);
				ret.add(mutant);
			}
		}
		return ret;
	}

	private void changeDomainToRange(OWLObjectProperty property, OWLClass domain) {
		OWLDataFactory factory = manager.getOWLDataFactory();
		List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
		changes.add(new RemoveAxiom(ontology, factory.getOWLObjectPropertyDomainAxiom(property, domain)));
		changes.add(new AddAxiom(ontology, factory.getOWLObjectPropertyRangeAxiom(property, domain)));
		manager.applyChanges(changes);
	}

	public ArrayList<Ontology> domainToRange(OWLObjectProperty property) {
		ArrayList<Ontology> ret = new ArrayList<Ontology>();
		for (OWLClassExpression d : EntitySearcher.getDomains(property, ontology)) {
			if (!d.isAnonymous()) {
				OWLClass domain = d.asOWLClass();
				Ontology mutant = copy("d2r", getLabel(property), getLabel(domain));
				mutant.changeDomainToRange(property, domain);
				ret.add(mutant);
			}
		}
		return ret;
	}

	private void changeRangeToDomain(OWLObjectProperty property, OWLClass range) {
		OWLDataFactory factory = manager.getOWLDataFactory();
		List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
		changes.add(new RemoveAxiom(ontology, factory.getOWLObjectPropertyRangeAxiom(property, range)));
		changes.add(new AddAxiom(ontology, factory.getOWLObjectPropertyDomainAxiom(property, range)));
		manager.applyChanges(changes);
	}

	public ArrayList<Ontology> rangeToDomain(OWLObjectProperty property) {
		ArrayList<Ontology> ret = new ArrayList<Ontology>();
		for (OWLClassExpression r : EntitySearcher.getRanges(property, ontology)) {
			if (!r.isAnonymous()) {
				OWLClass range = r.asOWLClass();
				Ontology mutant = copy("r2d", getLabel(property), getLabel(range));
				mutant.changeRangeToDomain(property, range);
				ret.add(mutant);
			}
		}
		return ret;
	}

}
