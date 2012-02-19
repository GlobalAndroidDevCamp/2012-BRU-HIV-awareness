package net.lp.hivawareness.domain;

public class Probability {
<<<<<<< HEAD
	public final static double worldwide = 0.008; // http://issuu.com/unaids/docs/unaids_globalreport_2010?mode=window&backgroundColor=%23222222
=======
	public final static double worldwide = 35 / 6500d; // 8 billion people and 35
														// million infected
														// humans in 2010
>>>>>>> c9ee8c3e4625e43b112a3eb037ac2bb584a96583
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
