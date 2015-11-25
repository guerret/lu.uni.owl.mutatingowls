package lu.uni.owl.mutatingowls;

import java.util.ArrayList;

public class OpData extends ArrayList<Mutant> {

	private static final long serialVersionUID = -8394830585122169529L;
	private String opname;

	public OpData(String opname) {
		super();
		this.opname = opname;
	}

	public String getOpName() {
		return opname;
	}

}
