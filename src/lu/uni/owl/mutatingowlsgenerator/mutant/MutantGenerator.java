package lu.uni.owl.mutatingowlsgenerator.mutant;

import java.util.List;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;

import lu.uni.owl.mutatingowls.Mutant;
import lu.uni.owl.mutatingowls.Ontology;
import lu.uni.owl.mutatingowls.OpData;
import lu.uni.owl.mutatingowls.generator.axiom.EntityAxiomGenerator;

public abstract class MutantGenerator {

	protected Ontology sourceOntology;
	public static OWLDataFactory factory = null;
	private EntityAxiomGenerator axiomGenerator;

	public MutantGenerator(Ontology ont) {
		sourceOntology = ont;
		if (factory == null)
			factory = sourceOntology.manager.getOWLDataFactory();
		axiomGenerator = new EntityAxiomGenerator();
	}

	public Mutant createMutant(String operator, String cls1, String cls2) {
		return new Mutant(sourceOntology, operator, cls1 + "_" + cls2);
	}

	protected OpData removeEntity(OWLEntity e) {
		String opname = "ERE";
		OpData ret = new OpData(opname);
		Mutant mutant = createMutant(opname, sourceOntology.getLabel(e), "deleted");
		sourceOntology.manager.applyChanges(axiomGenerator.axiomsERE(mutant, e));
		ret.add(mutant);
		return ret;
	}

	protected OpData removeLabels(OWLEntity entity) {
		String opname = "ERL";
		OpData ret = new OpData(opname);
		for (OWLAnnotation a : sourceOntology.getLabels(entity)) {
			Mutant mutant = createMutant(opname, sourceOntology.getLabel(entity),
					((OWLLiteral) a.getValue()).getLang());
			sourceOntology.manager.applyChanges(axiomGenerator.axiomsERL(mutant, entity, a));
			ret.add(mutant);
		}
		return ret;
	}

	protected OpData changeLabelLanguage(OWLEntity entity) {
		String opname = "ECL";
		OpData ret = new OpData(opname);
		for (OWLAnnotation a : sourceOntology.getLabels(entity)) {
			Mutant mutant = createMutant(opname, sourceOntology.getLabel(entity),
					((OWLLiteral) a.getValue()).getLang());
			List<OWLAxiomChange> changes = axiomGenerator.axiomsECL(mutant, entity, a);
			sourceOntology.manager.applyChanges(changes);
			ret.add(mutant);
		}
		return ret;
	}

}
