package main;

import java.util.Random;

public class QuoteManager
{
	public static String[] quotes;
	private static Random ran;
	private static int index = 0;
	
	static
	{
		ran = new Random();
		
		quotes = new String[13];
		
		quotes[0] = "\"Victorious warriors win first and then go to war,<br>while defeated warriors go to war first and then seek to win\"  (Sun Tzu)";
		quotes[1] = "\"Let your plans be dark and impenetrable as night,<br>and when you move, fall like a thunderbolt.\"  (Sun Tzu)";
		quotes[2] = "\"Take time to deliberate, but when the time for action comes,<br>stop thinking and go in.\"  (Napoleon Bonaparte)";
		quotes[3] = "\"Never interrupt your enemy when he is making a mistake.\"  (Napoleon Bonaparte)";
		quotes[4] = "\"If your enemy offers you two targets,<br>strike at a third.\"  (Robert Jordan)";
		quotes[5] = "\"The essence of strategy is choosing what not to do.\"  (Michael E. Porter)";
		quotes[6] = "\"Every advantage is temporary.\"  (Katerina Stoykova Klemer)";
		quotes[7] = "\"However beautiful the strategy,<br>you should occasionally look at the results.\"  (Winston Churchill)";
		quotes[8] = "\"You may not be interested in strategy,<br>but strategy is interested in you.\"  (Leon Trotsky)";
		quotes[9] = "\"You can be sure of succeeding in your attacks<br>if you only attack places which are undefended.\"  (Sun Tzu)";
		quotes[10] = "\"You can ensure the safety of your defense<br>if you only hold positions that cannot be attacked.\"  (Sun Tzu)";
		quotes[11] = "\"If his forces are united, separate them.\"  (Sun Tzu)";
		quotes[12] = "\"Attack him where he is unprepared,<br>appear where you are not expected.\"  (Sun Tzu)";
		
		randomize();
	}
	
	public static String getQuote()
	{
		return quotes[index];
	}
	
	public static void randomize()
	{
		index = ran.nextInt(quotes.length);
	}
}
