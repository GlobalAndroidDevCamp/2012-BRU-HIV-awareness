package net.lp.hivawareness.domain;

public enum Region {
	american (1 + 0.07 + 1.4 + 0.24 / 950), europe (2.3 / 700), asian ( 4000), australia (35), africa ( 1000);
	
	private final double mProb;
	
	Region (double probability){
		mProb = probability;
	}
	
	public double getProbability(){
		return mProb;
	}
}
