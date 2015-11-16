package lu.uni.owl.mutation;

import java.util.ArrayList;

public class OpData extends ArrayList<MutantGenerator> {

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
