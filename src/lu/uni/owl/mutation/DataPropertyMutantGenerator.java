package lu.uni.owl.mutation;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.RemoveAxiom;

public class DataPropertyMutantGenerator extends MutantGenerator {

	public DataPropertyMutantGenerator(Ontology ont) {
		super(ont);
	}

	public DataPropertyMutantGenerator(MutantGenerator mut) {
		super(mut);
	}

	@Override
	public List<MutantGenerator> generateMutants() {
		List<MutantGenerator> ret = new ArrayList<MutantGenerator>();
		for (OWLDataProperty p : ontology.getDataProperties())
			if (!p.isTopEntity()) {
				ret.addAll(reassignDataProperty(p));
				ret.addAll(removeLabels(p));
				ret.addAll(changeLabelLanguage(p));
			}
		return ret;
	}

	private void reassignDataPropertyDomain(OWLDataProperty property, OWLClass domain, OWLClass target) {
		List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
		changes.add(new RemoveAxiom(ontology.getOntology(), factory.getOWLDataPropertyDomainAxiom(property, domain)));
		changes.add(new AddAxiom(ontology.getOntology(), factory.getOWLDataPropertyDomainAxiom(property, target)));
		manager.applyChanges(changes);
	}

	private List<MutantGenerator> reassignDataProperty(OWLDataProperty property) {
		List<MutantGenerator> ret = new ArrayList<MutantGenerator>();
		for (OWLClassExpression d : ontology.getDataPropertyDomains(property)) {
			if (!d.isAnonymous()) {
				OWLClass cls = d.asOWLClass();
				for (OWLClassExpression s : ontology.getSuperClasses(cls)) {
					if (!s.isAnonymous()) {
						OWLClass c = s.asOWLClass();
						DataPropertyMutantGenerator mutant = new DataPropertyMutantGenerator(
								copy(this, "dp2parent", ontology.getLabel(property), ontology.getLabel(c)));
						mutant.reassignDataPropertyDomain(property, cls, c);
						ret.add(mutant);
					}
				}
				for (OWLClassExpression s : ontology.getSubClasses(cls)) {
					if (!s.isAnonymous()) {
						OWLClass c = s.asOWLClass();
						DataPropertyMutantGenerator mutant = new DataPropertyMutantGenerator(
								copy(this, "dp2child", ontology.getLabel(property), ontology.getLabel(c)));
						mutant.reassignDataPropertyDomain(property, cls, c);
						ret.add(mutant);
					}
				}
			}
		}
		return ret;
	}

}
