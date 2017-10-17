package mosssidewhist;

import java.util.*;
import java.util.Map;

public class BitAgent implements MSWAgent  {

	public static final int LEADER = 0;
	public static final int LEFT = 1;
	public static final int RIGHT = 2;

	private int hand[];
	
	private ArrayList<Integer> priorityH;

	private ArrayList<Card> table;

	public BitAgent() {

		priorityH = new ArrayList<Integer>();
		table = new ArrayList<Card>();
	}

	@Override
	public void setup(String agentLeft, String agentRight) {
		// TODO Auto-generated method stub

	}

	@Override
	public void seeHand(List<Card> hand, int order) {
		for(int i = 0; i < hand.size(); i++)
		{
			Card c = hand.get(i);
			this.hand = new int[4];
			
			switch(c.suit)
			{
			case SPADES: this.hand[0] = (this.hand[0]|(1 << c.rank-2)); break;
			case CLUBS: this.hand[1] = (this.hand[1]|(1 << c.rank-2)); break;
			case DIAMONDS: this.hand[2] = (this.hand[2]|(1 << c.rank-2)); break;
			case HEARTS: this.hand[3] = (this.hand[3]|(1 << c.rank-2)); break;
			}
		}
	}

	@Override
	public Card[] discard() {
		
		Card[] discard = new Card[4];
		for(int i = 0; i<4;i++)
		{
			int indexHand = minimumHandIndex(hand);
			int remove = Integer.lowestOneBit(hand[indexHand]);
			hand[indexHand] = hand[indexHand]&(~(1<< remove));
			discard[i] = retCard(indexHand, remove);;
		}

		return discard;

	}

	@Override
	public Card playCard() {
		
		
	}



	@Override
	public void seeCard(Card card, String agent) {

		table.add(card);
	}

	//Ran once the trick has been won
	//Cards on table removed
	@Override
	public void seeResult(String winner) {
		table.clear();
	}

	@Override
	public void seeScore(Map<String, Integer> scoreboard) {
		// TODO Auto-generated method stub

	}

	@Override
	public String sayName() {

		return "47";
	}

	private int goodCard(ArrayList<Card> hand, Suit s)
	{
		
		int index = 0;
		int max = -1000;

		for(int d = 0; d < priorityH.size(); d++)
		{
			if(priorityH.get(d) > max && hand.get(d).equals(s))
			{
				max = priorityH.get(d);
				index = d;
			}
		}
		return index;
	}
	
	
	private int trashCard(ArrayList<Card> hand, Suit s)
	{
		
		int index = 0;
		int min = 1000;

		for(int d = 0; d < priorityH.size(); d++)
		{
			if(priorityH.get(d) < min && hand.get(d).equals(s))
			{
				min = priorityH.get(d);
				index = d;
			}
		}
		return index;
	}

	private int minimumHandIndex(int hand[])
	{
		
		int pc [] = new int[3];
		
		pc[0]= Integer.bitCount(hand[1]);
		pc[1]= Integer.bitCount(hand[2]);
		pc[2]= Integer.bitCount(hand[3]);

		int min = 50;
		int sec = 0;
		for(int i = 0; i < 3; i++)
		{
			if(pc[i]< min && pc[i] != 0)
			{
				sec = i+1;
				min = pc[i];
			}
		}
		
		return sec;
	}
	
	private Card retCard(int s, int val)
	{
		
		Card x = null;
		switch(s)
		{
		case 0:
			switch(val+2)
			{
			case 14: x = Card.ACE_S; break;
			case 13: x = Card.KING_S; break;
			case 12: x = Card.QUEEN_S; break;
			case 11: x = Card.JACK_S; break;
			case 10: x = Card.TEN_S; break;
			case 9: x = Card.NINE_S; break;
			case 8: x = Card.EIGHT_S; break;
			case 7: x = Card.SEVEN_S; break;
			case 6: x = Card.SIX_S; break;
			case 5: x = Card.FIVE_S; break;
			case 4: x = Card.FOUR_S; break;
			case 3: x = Card.THREE_S; break;
			case 2: x = Card.TWO_S; break;
			} break;
		case 1:
			switch(val+2)
			{
			case 14: x = Card.ACE_C; break;
			case 13: x = Card.KING_C; break;
			case 12: x = Card.QUEEN_C; break;
			case 11: x = Card.JACK_C; break;
			case 10: x = Card.TEN_C; break;
			case 9: x = Card.NINE_C; break;
			case 8: x = Card.EIGHT_C; break;
			case 7: x = Card.SEVEN_C; break;
			case 6: x = Card.SIX_C; break;
			case 5: x = Card.FIVE_C; break;
			case 4: x = Card.FOUR_C; break;
			case 3: x = Card.THREE_C; break;
			case 2: x = Card.TWO_C; break;
			} break;
		case 2:
			switch(val+2)
			{
			case 14: x = Card.ACE_D; break;
			case 13: x = Card.KING_D; break;
			case 12: x = Card.QUEEN_D; break;
			case 11: x = Card.JACK_D; break;
			case 10: x = Card.TEN_D; break;
			case 9: x = Card.NINE_D; break;
			case 8: x = Card.EIGHT_D; break;
			case 7: x = Card.SEVEN_D; break;
			case 6: x = Card.SIX_D; break;
			case 5: x = Card.FIVE_D; break;
			case 4: x = Card.FOUR_D; break;
			case 3: x = Card.THREE_D; break;
			case 2: x = Card.TWO_D; break;
			} break;
		case 3:
			switch(val+2)
			{
			case 14: x = Card.ACE_H; break;
			case 13: x = Card.KING_H; break;
			case 12: x = Card.QUEEN_H; break;
			case 11: x = Card.JACK_H; break;
			case 10: x = Card.TEN_H; break;
			case 9: x = Card.NINE_H; break;
			case 8: x = Card.EIGHT_H; break;
			case 7: x = Card.SEVEN_H; break;
			case 6: x = Card.SIX_H; break;
			case 5: x = Card.FIVE_H; break;
			case 4: x = Card.FOUR_H; break;
			case 3: x = Card.THREE_H; break;
			case 2: x = Card.TWO_H; break;
			} break;
		}
		return x;
	}

}
