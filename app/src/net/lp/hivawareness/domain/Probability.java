package net.lp.hivawareness.domain;

public class Probability {
	public final static double worldwide = 0.008; // http://issuu.com/unaids/docs/unaids_globalreport_2010?mode=window&backgroundColor=%23222222
	public final static double scale = 10;
	public final static double male_male = 4; // 6 percent - anal intercourse
	public final static double male_female = 1; // 1 percent - female infected
	public final static double female_male = 2; // 3 percent - male infected
	public final static double female_female = 1; // 1 percent

	
	public static double fromData(Gender gender, Region region) {
		if (Gender.male == gender) {//Males have 50% more chance of being infected.
			return region.getProbability() * 1.2;
			//american male: prob = 0,001141053 * 1.2 = 0,001369264
		} else {
			return region.getProbability() * 0.8;
		}
	}

}
