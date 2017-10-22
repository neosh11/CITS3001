package mosssidewhist;

import java.util.*;

public class BitAgentTester {

	public static void main(String args[])
	{

		BitAgent X = new BitAgent();
		ProjectAgent Y = new ProjectAgent();
		List<Card> hand = Arrays.asList(Card.NINE_S, Card.SEVEN_S, Card.FIVE_S, Card.FOUR_S,Card.THREE_S, Card.TWO_S, Card.SIX_S, Card.EIGHT_S, Card.TEN_S, Card.JACK_S, Card.QUEEN_S, Card.KING_S, Card.EIGHT_C, Card.SEVEN_C, Card.SIX_C, Card.FIVE_C, Card.JACK_H, Card.FOUR_H, Card.THREE_H, Card.TWO_H);
		
		X.setup("JIM", "BHEEM");
		Y.seeHand(hand, 0);
		X.seeHand(hand, 0);
		
		X.printCards();
		long start= System.nanoTime();
		X.discard();
		System.out.println("X: "+((double)(System.nanoTime()-start))/(Math.pow(10, 9)));
		
		start= System.nanoTime();
		Y.discard();
		System.out.println("Y: "+((double)(System.nanoTime()-start))/(Math.pow(10, 9)));
		
		X.printCards();
		X.playCard();
		X.printCards();
		X.playCard();
		X.printCards();
		X.playCard();
		X.printCards();
		X.playCard();
		X.printCards();
		X.printGrave();
		System.out.println(X.otherSuits[0][0]);
		System.out.println(X.otherSuits[1][0]);
		
		X.seeCard(Card.ACE_S, "JIM");
		for(Card c: X.table)
		{
			System.out.print(c.toString()+" ");
		}
		System.out.println();
		System.out.println(X.otherSuits[0][0]);
		System.out.println(X.otherSuits[1][0]);
		X.seeResult("");
		X.printGrave();
		
		
	}
	

}
