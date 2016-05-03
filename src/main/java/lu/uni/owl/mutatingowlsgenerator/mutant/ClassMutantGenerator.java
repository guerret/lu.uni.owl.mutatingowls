package lu.uni.owl.mutatingowlsgenerator.mutant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;

import lu.uni.owl.mutatingowls.Mutant;
import lu.uni.owl.mutatingowls.Ontology;
import lu.uni.owl.mutatingowls.OpData;
import lu.uni.owl.mutatingowls.generator.axiom.ClassAxiomGenerator;

public class ClassMutantGenerator extends MutantGenerator implements MutantGenerationInterface {

	private ClassAxiomGenerator axiomGenerator;

	public ClassMutantGenerator(Ontology ont) {
		super(ont);
		axiomGenerator = new ClassAxiomGenerator();
	}

	@Override
	public HashMap<String, List<Mutant>> generateMutants() {
		HashMap<String, List<Mutant>> ret = new HashMap<String, List<Mutant>>();
		for (OWLClass c : sourceOntology.getClasses()) {
			List<OpData> ops = getOps(c);
			for (OpData op : ops) {
				String opName = op.getOpName();
				if (!ret.containsKey(opName))
					ret.put(opName, new ArrayList<Mutant>());
				ret.get(opName).addAll(op);
			}
		}
		return ret;
	}

	@Override
	public List<OpData> getOps(OWLEntity entity) {
		OWLClass c = entity.asOWLClass();
		List<OpData> ops = new ArrayList<OpData>();
		ops.add(removeEntity(c));
		ops.add(addSubclassAxioms(c));
		ops.add(removeSubclassAxioms(c));
		ops.add(swapWithParents(c));
		ops.add(removeLabels(c));
		ops.add(changeLabelLanguage(c));
		ops.add(addDisjointWith(c));
		ops.add(removeDisjointWith(c));
		ops.add(addEquivalentWith(c));
		ops.add(removeEquivalentWith(c));
		return ops;
	}

	private OpData addSubclassAxioms(OWLClass cls) {
		String opname = "CAS";
		OpData ret = new OpData(opname);
		Set<OWLClass> classes = sourceOntology.getClasses();
		classes.remove(cls);
		classes.removeAll(sourceOntology.getSuperClassHierarchy(cls));
		classes.removeAll(sourceOntology.getSubClassHierarchy(cls));
		for (OWLClass c : classes) {
			String subclassLabel = sourceOntology.getLabel(c);
			Mutant mutant = createMutant(opname, sourceOntology.getLabel(cls), subclassLabel);
			mutant.manager.applyChanges(axiomGenerator.axiomsCAS(mutant, cls, c));
			ret.add(mutant);
		}
		return ret;
	}

	private OpData removeSubclassAxioms(OWLClass cls) {
		String opname = "CRS";
		OpData ret = new OpData(opname);
		int counter = 0;
		for (OWLClassExpression s : sourceOntology.getSuperClasses(cls)) {
			String parentLabel = s.isAnonymous() ? "anonymousClass" + String.format("%04d", counter++)
					: sourceOntology.getLabel(s.asOWLClass());
			Mutant mutant = createMutant(opname, sourceOntology.getLabel(cls), parentLabel);
			mutant.manager.applyChanges(axiomGenerator.axiomsCRS(mutant, cls, s));
			ret.add(mutant);
		}
		return ret;
	}

	private OpData swapWithParents(OWLClass cls) {
		String opname = "CSC";
		OpData ret = new OpData(opname);
		for (OWLClassExpression s : sourceOntology.getSuperClasses(cls)) {
			if (!s.isAnonymous()) {
				OWLClass parent = s.asOWLClass();
				Mutant mutant = createMutant(opname, sourceOntology.getLabel(cls), sourceOntology.getLabel(parent));
				mutant.manager.applyChanges(axiomGenerator.axiomsCSC(mutant, cls, parent));
				ret.add(mutant);
			}
		}
		return ret;
	}

	private OpData addDisjointWith(OWLClass cls) {
		String opname = "CAD";
		OpData ret = new OpData(opname);
		Set<OWLClass> classes = sourceOntology.getClasses();
		classes.remove(cls);
		classes.removeAll(sourceOntology.getSuperClassHierarchy(cls));
		classes.removeAll(sourceOntology.getSubClassHierarchy(cls));
		for (OWLDisjointClassesAxiom disjointSet : sourceOntology.getOntology().getDisjointClassesAxioms(cls))
			classes.removeAll(disjointSet.getClassesInSignature());
		for (OWLClass c : classes) {
			String disjointLabel = sourceOntology.getLabel(c);
			Mutant mutant = createMutant(opname, sourceOntology.getLabel(cls), disjointLabel);
			mutant.manager.applyChanges(axiomGenerator.axiomsCAD(mutant, cls, c));
			ret.add(mutant);
		}
		return ret;
	}

	private OpData removeDisjointWith(OWLClass cls) {
		String opname = "CRD";
		OpData ret = new OpData(opname);
		for (OWLDisjointClassesAxiom disjointSet : sourceOntology.getOntology().getDisjointClassesAxioms(cls)) {
			Set<OWLClass> disjoints = disjointSet.getClassesInSignature();
			disjoints.remove(cls);
			for (OWLClass d : disjoints) {
				int counter = 0;
				String disjointLabel = d.isAnonymous() ? "anonymousClass" + String.format("%04d", counter++)
						: sourceOntology.getLabel(d);
				if (!d.isAnonymous()) {
					Mutant mutant = createMutant(opname, sourceOntology.getLabel(cls), disjointLabel);
					mutant.manager.applyChanges(axiomGenerator.axiomsCRD(mutant, cls, disjointSet, d));
					ret.add(mutant);
				}
			}
		}
		return ret;
	}

	private OpData addEquivalentWith(OWLClass cls) {
		String opname = "CAE";
		OpData ret = new OpData(opname);
		Set<OWLClass> classes = sourceOntology.getClasses();
		classes.remove(cls);
		classes.removeAll(sourceOntology.getSuperClassHierarchy(cls));
		classes.removeAll(sourceOntology.getSubClassHierarchy(cls));
		for (OWLEquivalentClassesAxiom equivalentSet : sourceOntology.getOntology().getEquivalentClassesAxioms(cls))
			classes.removeAll(equivalentSet.getClassesInSignature());
		for (OWLClass c : classes) {
			String equivalentLabel = sourceOntology.getLabel(c);
			Mutant mutant = createMutant(opname, sourceOntology.getLabel(cls), equivalentLabel);
			mutant.manager.applyChanges(axiomGenerator.axiomsCAE(mutant, cls, c));
			ret.add(mutant);
		}
		return ret;
	}

	private OpData removeEquivalentWith(OWLClass cls) {
		String opname = "CRE";
		OpData ret = new OpData(opname);
		for (OWLEquivalentClassesAxiom equivalentSet : sourceOntology.getOntology().getEquivalentClassesAxioms(cls)) {
			Set<OWLClass> equivalents = equivalentSet.getClassesInSignature();
			equivalents.remove(cls);
			for (OWLClass e : equivalents) {
				int counter = 0;
				String equivalentLabel = e.isAnonymous() ? "anonymousClass" + String.format("%04d", counter++)
						: sourceOntology.getLabel(e);
				if (!e.isAnonymous()) {
					Mutant mutant = createMutant(opname, sourceOntology.getLabel(cls), equivalentLabel);
					mutant.manager.applyChanges(axiomGenerator.axiomsCRE(mutant, cls, equivalentSet, e));
					ret.add(mutant);
				}
			}
		}
		return ret;
	}

}
