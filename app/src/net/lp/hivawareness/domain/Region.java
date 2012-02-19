package net.lp.hivawareness.domain;

public enum Region {
	american (1 + 0.07 + 1.4 + 0.24 / 950), european (2.3 / 700), asian ( 0.02), australian (0.02/35), african (23 / 1000);
	
	private final double mProb;
	
	Region (double probability){
		mProb = probability;
	}
	
	public double getProbability(){
		return mProb;
	}
}
