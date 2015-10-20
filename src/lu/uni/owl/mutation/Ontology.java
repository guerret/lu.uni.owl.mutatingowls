package lu.uni.owl.mutation;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.search.EntitySearcher;

public class Ontology {

	private OWLOntologyManager manager;
	private OWLOntology ontology;

	public Ontology(String path, String fileName) {
		manager = OWLManager.createOWLOntologyManager();
		manager.getOWLDataFactory();
		ontology = load(path + File.separator + fileName);
		System.out.println("Number of axioms: " + ontology.getAxiomCount());
		System.out.println("IRI: " + ontology.getOntologyID().getOntologyIRI().get());
	}

	public Ontology(OWLOntology owlMutant) {
		manager = OWLManager.createOWLOntologyManager();
		manager.getOWLDataFactory();
		ontology = owlMutant;
	}

	public OWLOntology getOntology() {
		return ontology;
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

	public void save(File file) {
		OWLDocumentFormat format = new OWLXMLDocumentFormat();
		try {
			manager.saveOntology(ontology, format, IRI.create(file.toURI()));
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		}
	}

	public String getLabel(OWLEntity cls) {
		for (OWLAnnotation a : EntitySearcher.getAnnotations(cls, ontology)) {
			if (a.getProperty().isLabel()) {
				OWLLiteral label = (OWLLiteral) a.getValue();
				return label.getLiteral();
			}
		}
		return null;
	}

	public Set<OWLAnnotation> getLabels(OWLEntity e) {
		Set<OWLAnnotation> ret = new HashSet<OWLAnnotation>();
		for (OWLAnnotation a : EntitySearcher.getAnnotations(e, ontology)) {
			if (a.getProperty().isLabel())
				ret.add(a);
		}
		return ret;
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

	public Set<OWLClass> getClasses() {
		return ontology.getClassesInSignature();
	}

	public String[] getClassLabels() {
		return getLabels(ontology.getClassesInSignature());
	}

	public Set<OWLObjectProperty> getObjectProperties() {
		return ontology.getObjectPropertiesInSignature();
	}

	public String[] getObjectPropertyLabels() {
		return getObjectPropertyLabels(ontology.getObjectPropertiesInSignature());
	}

	public Set<OWLNamedIndividual> getIndividuals() {
		return ontology.getIndividualsInSignature();
	}

	public Set<OWLDataProperty> getDataProperties() {
		return ontology.getDataPropertiesInSignature();
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

	public Collection<OWLClassExpression> getSuperClasses(OWLClass cls) {
		return EntitySearcher.getSuperClasses(cls, ontology);
	}

	public Collection<OWLClassExpression> getIndividualTypes(OWLNamedIndividual individual) {
		return EntitySearcher.getTypes(individual, ontology);
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

	public Collection<OWLClassExpression> getObjectPropertyDomains(OWLObjectProperty property) {
		return EntitySearcher.getDomains(property, ontology);
	}

	public Collection<OWLClassExpression> getRanges(OWLObjectProperty property) {
		return EntitySearcher.getRanges(property, ontology);
	}

	public Collection<OWLClassExpression> getDataPropertyDomains(OWLDataProperty property) {
		return EntitySearcher.getDomains(property, ontology);
	}

}
