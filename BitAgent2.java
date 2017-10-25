package mosssidewhist;

import java.rmi.server.RemoteCall;
import java.util.*;
import java.util.Map.Entry;

public class BitAgent2 implements MSWAgent  {

	public static final int LEADER = 0;
	public static final int LEFT = 1;
	public static final int RIGHT = 2;
	public static final int THIRTEEN= 0b1111111111111;

	public static final String NAME = "JITS";

	private String leftAgent;
	private String rightAgent;

	private int scores[];
	private int removal[];
	private int currentWinner;

	View current;

	public BitAgent2() {

		current = new View();
		scores = new int[3];
		removal = new int[3];
	}

	@Override
	public void setup(String agentLeft, String agentRight) {
		leftAgent = agentLeft;
		rightAgent = agentRight;
	}


	@Override
	public void seeHand(List<Card> hand, int order) {

		current.resetGame();

		for(int i = 0; i < hand.size(); i++)
		{

			current.addCard(hand.get(i));	
		}

		current.setPosition(order);


		if(LEADER == (order)%3)
		{
			removal[0]=8;
			removal[1]=4;
			removal[2]=4;
		}
		else if(LEADER == (4+order)%3)
		{
			removal[0]=4;
			removal[1]=8;
			removal[2]=4;
		}
		else
		{
			removal[0]=4;
			removal[1]=4;
			removal[2]=8;
		}
	}


	@Override
	public Card[] discard() {
		//TODO

		Card[] discard = new Card[4];


		for(int i = 0; i<4;i++)
		{
			Card remove = removeWorstHand(current.hand);
			int suit =suitVal(remove);
			int val = remove.rank-2;
			current.removeCard(suit, val);

			discard[i] = remove;
		}
		return discard;
	}

	@Override
	public Card playCard() {

		Card toRet = beatable(current, currentWinner);

		current.removeCard(suitVal(toRet), toRet.rank-2);

		return toRet;
	}



	@Override
	public void seeCard(Card card, String agent) {

		if(current.isTableEmpty())
		{
			current.setSuit(suitVal(card));
		}
		else
		{
			if(suitVal(card) != current.suit)
			{
				if(agent.equals(leftAgent))
				{
					current.otherSuits[0][current.suit] = true;
				}
				else if(agent.equals(rightAgent))
				{
					current.otherSuits[1][current.suit] = true;
				}
			}
		}


		if(current.tableSize() != 0)
		{
			for(int i = 0; i < 4; i++)
			{
				for(int j = 0; j < 13; j++)
				{
					if( ((1 << j) & current.hand[i]) != 0)
					{
						if(View.greaterThan(suitVal(card), card.rank-2, i, j, current.suit))
						{
							if(agent.equals(leftAgent))
							{
								currentWinner = 1;
							}
							else if(agent.equals(rightAgent))
							{
								currentWinner = 2;
							}
							else
							{
								currentWinner = 0;
							}

						}
					}
				}
			}
		}
		else
		{
			if(agent.equals(leftAgent))
			{
				currentWinner = 1;
			}
			else if(agent.equals(rightAgent))
			{
				currentWinner = 2;
			}
			else
			{
				currentWinner = 0;
			}
		}


		current.addToTable(suitVal(card), card.rank-2);;


	}

	//Ran once the trick has been won
	//Cards on table removed
	@Override
	public void seeResult(String winner) {

		current.resetTable();

	}

	@Override
	public void seeScore(Map<String, Integer> scoreboard) {
		scores[0] = scoreboard.get(NAME);
		scores[1] = scoreboard.get(leftAgent);
		scores[2] = scoreboard.get(rightAgent);
	}

	@Override
	public String sayName() {

		return NAME;
	}

	/**
	 * Function that returns which Suit the worst Card is in 
	 * @param hand Hand to be passed
	 * @return Suit where the bad card lies
	 */
	private Card removeWorstHand(int hand[])
	{

		int pc [] = new int[3];

		pc[0]= binlog(Integer.lowestOneBit(hand[1]));
		pc[1]= binlog(Integer.lowestOneBit(hand[2]));
		pc[2]= binlog(Integer.lowestOneBit(hand[3]));

		int min = 50;
		int sec = 0;
		for(int i = 0; i < 3; i++)
		{
			if(pc[i]< min && pc[i] != -1)
			{
				sec = i+1;
				min = pc[i];
			}
		}

		return retCard(sec, binlog(Integer.lowestOneBit(hand[sec])));
	} 

	private int suitVal(Card c)
	{
		switch(c.suit)
		{
		case SPADES: return 0;
		case CLUBS: return 1;
		case DIAMONDS: return 2;
		case HEARTS: return 3;
		}
		return 0;
	}


	private Card beatable(View X, int currentWin)
	{

		if(current.tableSize() ==1)
		{
			//play unbeatable if not found(greedy),
			//play strategic

			boolean out = false;

			//next player
			//out of current hand but has spades

			if(X.suit != 0 && X.otherSuits[1][X.suit] == true && X.otherSuits[1][0] == false)
			{
				//Check if out of cards of the required deck
				if(X.hand[X.suit] == 0 || X.suit == 0)
				{

					//Check if have any spades if not throw a trashy card of any deck but spades
					if(X.hand[0] == 0)
					{
						//TODO REMOVE WORST CARD strategically

						Card toRet = Simulator.simulateBest(X, currentWin, scores, removal);
						if(toRet == null)
						{
							return removeWorstHand(X.hand);
						}
						else
						{
							//TODO
							return toRet;
							//return removeWorstHand(X.hand);
						}
					}

					//check if possible to beat with spades
					int start = binlog(Integer.highestOneBit(X.getTableSet(0) | ((~X.getgraveSet(0) & THIRTEEN)&(~X.getHandSet(0) & THIRTEEN))));

					int found = -1;
					for(int i = start; i < 13; i++)
					{
						if( ((1 << i) & X.getHandSet(0)) != 0)
						{
							found = i;
							break;
						}
					}

					//If beatable play the spade
					if(found != -1)
					{
						return retCard(0, found);
					}
					//else throw a trashy card of any deck but spades
					else
					{
						//TODO
						//return removeWorstHand(X.hand);

						Card toRet = Simulator.simulateBest(X, currentWin, scores, removal);
						if(toRet == null)
						{
							return removeWorstHand(X.hand);
						}
						else
						{
							//TODO
							return toRet;
							//return removeWorstHand(X.hand);
						}

					}
				}
				//Throw a bad card of the deck since it can't win
				else
				{
					return retCard(X.suit, binlog(Integer.lowestOneBit(X.getHandSet(X.suit))));
				}
			}
			//If others still have the cards
			else
			{
				//If out of cards throw the lowest spade
				if(X.getHandSet(X.suit) == 0)
				{
					//Check if have any spades if not throw a trashy card of any deck but spades
					if(X.getHandSet(0) == 0)
					{
						//TODO
						return removeWorstHand(X.hand);
					}
					else
					{
						return retCard(0, binlog(Integer.lowestOneBit(X.getHandSet(0))));
					}
				}
				//checks if beatable
				else
				{

					int start = binlog(Integer.highestOneBit(X.getTableSet(X.suit) | (~X.getgraveSet(X.suit) & ~X.getHandSet(X.suit) & THIRTEEN)));
					int found = -1;
					for(int i = start; i < 13; i++)
					{
						if( ((1 << i) & X.getHandSet(X.suit)) != 0 )
						{
							found = i;
							break;
						}
					}

					//if beatable throw card
					if(found != -1)
					{
						return retCard(X.suit, found);
					}
					//play trash card of the same deck
					else
					{
						return retCard(X.suit, binlog(Integer.lowestOneBit(X.getHandSet(X.suit))));
					}
				}
			}


		}

		else if(current.tableSize() == 2)
		{

			//Select greedily			
			if(X.getHandSet(X.suit) == 0)
			{
				//biggest spade on the table
				int start = X.getSetHighestCard(X.getTableSet(0)) ;
				int found = X.cardGreaterThanInHand(0, start);

				//If beatable play the spade
				if(found != -1)
				{
					return retCard(0, found);
				}

				else
				{
					//Remove random worst card that will reap rewards
					//TODO Play strategic instead
					Card toRet = Simulator.simulateBest(X, currentWin, scores, removal);
					if(toRet == null)
					{
						return removeWorstHand(X.hand);
					}
					else
					{
						//TODO
						return toRet;
						//return removeWorstHand(X.hand);
					}
				}
			}
			//If have cards of suit
			else
			{
				if(X.getTableSet(0) == 0 || X.suit == 0)
				{
					int start = X.getSetHighestCard(X.getTableSet(X.suit));
					int found = X.cardGreaterThanInHand(X.suit, start);

					//if beatable throw card
					if(found != -1)
					{
						return retCard(X.suit, found);
					}
					//play trash card of the same deck
					else
					{
						return retCard(X.suit, binlog(Integer.lowestOneBit(X.getHandSet(X.suit))));
					}
				}
				else
				{
					return retCard(X.suit, binlog(Integer.lowestOneBit(X.hand[X.suit])));
				}
			}
		}

		else //Nothing on table
		{
			//play unbeatable if not found, (greedy)
			//play strategic

			//GREEDY
			for(int k = 1; k < 4; k++)
			{
				if(!((X.otherSuits[0][k] == true && X.otherSuits[0][0] == false) || (X.otherSuits[1][k] == true && X.otherSuits[1][0] == false)))
				{
					//System.out.println("CHECK2");
					//look for unbeatable card if found play
					//else play trash

					//highest card
					int start = binlog(Integer.highestOneBit(~X.grave[k] & ~X.hand[k] & THIRTEEN));
					int found = -1;
					for(int i = start; i < 13; i++)
					{
						if( ((1 << i) & X.hand[k]) != 0)
						{
							//System.out.println(i);
							found = i;
							break;
						}
					}
					//If beatable play
					if(found != -1)
					{
						//System.out.println(retCard(k, found).toString()+ "THIS?");
						return retCard(k, found);
					}
				}
			}

			//try spade
			int start = binlog(Integer.highestOneBit(~X.grave[0] & ~X.hand[0] & THIRTEEN));
			int found = -1;
			for(int i = start; i < 13; i++)
			{
				if( ((1 << i) & X.hand[0]) != 0)
				{
					found = i;
					break;
				}
			}
			//If beatable play
			if(found != -1)
			{
				return retCard(0, found);
			}
			//play strategic card
			else
			{
				Card toRet = Simulator.simulateBest(X, currentWin, scores, removal);
				if(toRet == null)
				{
					return removeWorstHand(X.hand);
				}
				else
				{
					//TODO
					return toRet;
					//return removeWorstHand(X.hand);
				}
			}

		}
	}


	protected static Card retCard(int s, int val)
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
	//https://stackoverflow.com/questions/3305059/how-do-you-calculate-log-base-2-in-java-for-integers
	//It is slightly faster than Integer.numberOfLeadingZeros() (20-30%) 
	//and almost 10 times faster (jdk 1.6 x64) than a Math.log() based implementation

	/**
	 * 
	 * @param bits Set which to check on
	 * @return location of the bit
	 */
	public static int binlog( int bits ) // returns 0 for bits=0
	{
		if(bits == 0) return -1;
		int log = 0;
		if( ( bits & 0xffff0000 ) != 0 ) { bits >>>= 16; log = 16; }
		if( bits >= 256 ) { bits >>>= 8; log += 8; }
		if( bits >= 16  ) { bits >>>= 4; log += 4; }
		if( bits >= 4   ) { bits >>>= 2; log += 2; }
		return log + ( bits >>> 1 );
	}



	/******************
	 * 
	 * DEBUGGING PURPOSES ONLY
	 *
	 */


	public void printCards()
	{
		for(int i = 0; i < 4; i++)
		{
			for(int j = 0; j < 13; j++)
			{
				if((current.hand[i] & (1 << j)) != 0)
				{
					System.out.print(retCard(i, j).toString() +" ");
				}
			}
		}
		System.out.println();
	}

	public void printGrave()
	{
		for(int i = 0; i < 4; i++)
		{
			for(int j = 0; j < 13; j++)
			{
				if((current.grave[i] & (1 << j)) != 0)
				{
					System.out.print(retCard(i, j).toString() +" ");
				}
			}
		}
		System.out.println();
	}


}







class View
{

	protected int hand[];
	protected int grave[];
	protected int position;

	//FALSE IF THEY HAVE THE CARD
	protected boolean otherSuits[][];
	protected int table[];
	protected int suit;
	/**
	 * Default constructor
	 */
	View()
	{
		hand = new int[4];
		grave = new int[4];
		otherSuits = new boolean[2][4];
		table = new int[4];
		suit = -1;
	}

	/**
	 * Creates an image of an existing View
	 * @param parent Object to be cloned
	 */
	View(View parent)
	{
		this.hand = parent.hand.clone();
		grave = parent.grave.clone();
		otherSuits = new boolean[2][4];
		for(int i = 0; i < 4; i++)
		{
			otherSuits[0][i] = parent.otherSuits[0][i];
			otherSuits[1][i] = parent.otherSuits[1][i];
		}

		table = parent.table.clone();
		suit = parent.suit;
	}

	/**
	 * Resets the entire game
	 * Should be done once all cards have been played
	 */
	void resetGame()
	{
		hand = new int[4];
		grave = new int[4];
		otherSuits = new boolean[2][4];
		table = new int[4];
		suit = -1;
	}

	void resetTable()
	{
		table = new int[4];
	}

	void removeCard(int suit, int value)
	{
		hand[suit] = hand[suit]&(~(1<< value));
		addToGrave(suit, value);
	}

	/**
	 * 
	 * @param c Card to be added to the hand
	 */
	void addCard(Card c)
	{
		switch(c.suit)
		{
		case SPADES: hand[0] = (hand[0]|(1 << c.rank-2)); break;
		case CLUBS: hand[1] = (hand[1]|(1 << c.rank-2)); break;
		case DIAMONDS: hand[2] = (hand[2]|(1 << c.rank-2)); break;
		case HEARTS: hand[3] = (hand[3]|(1 << c.rank-2)); break;
		}
	}

	/**
	 * 
	 * @param suit Suit to be added [0,3]
	 * @param card Value to be added [0,12]s
	 */
	void addToGrave(int suit, int card)
	{
		grave[suit] = grave[suit] | (1 << card);

		for(int i =0; i < 4; i++)
		{
			if((grave[i]|hand[i]) == BitAgent2.THIRTEEN)
			{
				otherSuits[1][i] = true;
				otherSuits[0][i] = true;
			}
		}
	}

	/**
	 * 
	 * @param suit Suit to be added [0,3]
	 * @param card Value to be added [0,12]
	 */
	void addToTable(int suit, int card)
	{
		table[suit] = table[suit] | (1 << card);
		addToGrave(suit, card);
	}

	/**
	 * Sets position on the table
	 * @param p the position of the Agent
	 */
	void setPosition(int p)
	{
		position = p;
	}

	/**
	 * The Card that must be played, trumps can be played if unavailable
	 * @param s the default suit of the round
	 */
	void setSuit(int s)
	{
		suit = s;
	}


	/**
	 * Checks if the table is empty
	 * @return true if empty, false otherwise
	 */
	boolean isTableEmpty()
	{
		if(table[0]+table[1]+table[2]+table[3] == 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * This returns the number of Cards on the table
	 * @return number of Cards on the table
	 */
	int tableSize()
	{
		return Integer.bitCount(table[0]) + Integer.bitCount(table[1]) + Integer.bitCount(table[2]) + Integer.bitCount(table[3]);
	}

	/**
	 * This returns the number of Cards the Agent has
	 * @return number of Cards in Agent's hand
	 */
	int handSize()
	{
		return Integer.bitCount(hand[0]) + Integer.bitCount(hand[1]) + Integer.bitCount(hand[2]) + Integer.bitCount(hand[3]);
	}

	int getHandSet(int suit)
	{
		return hand[suit];
	}

	int getgraveSet(int suit)
	{
		return grave[suit];
	}

	int getTableSet(int suit)
	{
		return table[suit];
	}

	int getSetHighestCard(int i)
	{
		return BitAgent2.binlog(Integer.highestOneBit(i));
	}

	int cardGreaterThanInHand(int suit, int value)
	{
		for(int i = value; i < 13; i++)
		{
			if( ((1 << i) & hand[suit]) != 0)
			{
				return i;
			}
		}
		return -1;

	}



	//*************************
	protected static boolean greaterThan(int suit1, int card1, int suit2, int card2, int suit)
	{

		if(suit1 == suit && suit1 !=0)
		{
			if(suit2 == suit)
			{
				return card2 > card1 ? false : true;
			}
			else if(suit2 == 0)
			{
				return false;
			}
			else
			{
				return true;
			}
		}
		else if(suit1 == 0)
		{
			if(suit2 == 0)
			{
				return card2 > card1 ? false : true;
			}
			else
			{
				return true;
			}

		}
		else
			return true;
	}

	protected static boolean legal(int hand[] ,int table[], int suit1, int card1, int suit)
	{

		if(table[0]+table[1]+table[2]+table[3] == 0)
		{
			return true;
		}

		if(suit1 == suit || hand[suit] == 0)
			return true;
		else
			return false;
	}
}

class Simulator
{
	private static Random random = new Random();
	public static final int THIRTEEN= 0b1111111111111;


	static void constructBoth(int leftOvers[], int left[], int leftVal, int right[], int rightVal, boolean otherSuits[][], boolean leader)
	{
		int leftOversClone[] = leftOvers.clone();
		int count = 0;
		int olcount, orcount;
		int lcount, rcount, addToLead;
		lcount = rcount = addToLead =olcount =orcount= 0;
		boolean retry = false;

		if(leader)
		{
			addToLead = 4;
		}

		boolean distributed = false;

		while(!distributed)
		{
			for(int i = 0; i < 4; i++)
			{

				//Distribute

				for(int k = 0; k < 13; k++)
				{
					if((leftOvers[i] & (1 << k)) != 0)
					{
						if(random.nextInt(10) < 5)
						{
							if(random.nextBoolean() && otherSuits[0][i] == false && (lcount < leftVal+ addToLead))
							{
								left[i] = left[i] | (1 << k);
								leftOvers[i] = leftOvers[i] & ~(1 << k) & THIRTEEN;
								lcount ++;
							}
							else if(rcount < rightVal && otherSuits[1][i] == false)
							{
								right[i] = right[i] | (1 << k);
								leftOvers[i] = leftOvers[i] & ~(1 << k) & THIRTEEN;
								rcount++;
							}
						}
					}
					if(leftOvers[i] == 0)
						break;

				}

			}
			if(lcount == rcount || (olcount == lcount && orcount == rcount))
			{
				count++;
				if(count >20)
				{
					retry = true;
					distributed = true;

				}
			}
			if(leftOvers[0] + leftOvers[1] + leftOvers[2] + leftOvers[3] == 0)
			{
				distributed = true;
			}
			else if(rcount == rightVal)
			{
				if(leader)
				{
					for(int l = 0; l < 4; l++)
					{
						left[l] = left[l] | leftOvers[l];
					}
					distributed = true;
				}

			}
			olcount = lcount;
			orcount = rcount;
		}

		if(retry)
		{
			for(int i = 0; i< 4; i++ )
			{
				left[i] = right[i] = 0;
			}
			constructBoth(leftOversClone, left, leftVal, right, rightVal, otherSuits, leader);
		}

	}



	static Card simulateBest(View X, int currWinner, int scores[], int removes[])
	{
		//https://stackoverflow.com/questions/81346/most-efficient-way-to-increment-a-map-value-in-java
		//MutableInt method and the Trove method are significantly faster, in that only they give a performance boost of more than 10%
		//This is the MutableInt method
		class MutableInt {
			int value = 1;
			public void increment() {++value;}
			public int get() {return value;}
		}

		HashMap<Card,MutableInt> map = new HashMap<Card, MutableInt>();

		for(int i = 0; i < 100; i++)
		{
			int ar[][] = Simulator.simulate(X, currWinner, scores, removes);
			if(ar == null)
			{
				continue;
			}
			for(int p = 0; p < 20; p++)
			{
				if(ar[p][0] == -1)
				{
					break;
				}
				Card tempC = BitAgent2.retCard(ar[p][0], ar[p][1]);
				MutableInt count = map.get(tempC);
				if (count == null) {
					map.put(tempC, new MutableInt());
				}
				else {
					count.increment();
				}
			}
		}


		Card toReturn = null;
		int maximum = 0;
		for (Entry<Card, MutableInt> entry : map.entrySet()) { 
			if (entry.getValue().get()> maximum) {
				toReturn = entry.getKey(); 
				maximum = entry.getValue().get();
			}
		}
		if(toReturn == null)
		{
			int[] ar =removeWorstHand(X.hand);
			return BitAgent2.retCard(ar[0], ar[1]);
		}
		return toReturn;

	}

	//int simulate(int hand[], int grave[], int otherSuits[][], int table[])
	static int [][] simulate(View X, int currWinner, int scores[], int removes[])
	{
		int [][] goodCards = null;
		int number = 0;

		int leftOvers[] = new int[4];
		int leftP[] = new int[4];
		int rightP[] = new int[4];

		int tSize = X.tableSize();

		int depth = 0;

		for(int i = 0; i < 4; i++)
		{
			leftOvers[i] = (~X.grave[i] & ~X.hand[i] & THIRTEEN);
		}

		//0 for agent 1 for left, 2 for right

		int leftCount = 0;
		int rightCount = 0;

		if(tSize == 0)
		{
			leftCount = X.handSize();
			rightCount = X.handSize();
		}
		else if(tSize == 1)
		{
			leftCount = X.handSize();
			rightCount = X.handSize()-1;
		}
		else
		{
			leftCount = X.handSize()-1;
			rightCount = X.handSize()-1;
		}

		//left is leader
		int templeftovers[] = leftOvers.clone();
		if(removes[1] == 8)
		{
			constructBoth(templeftovers, leftP, leftCount, rightP, rightCount, X.otherSuits, true);
			for(int l = 0; l  < 4; l ++)
			{
				int ar[] = removeWorstHand(leftP);
				leftP[ar[0]] = leftP[ar[0]] & ~ (1<< ar[1]);
			}
		}
		//right is leader
		else if(removes[2] == 8)
		{
			constructBoth(templeftovers, rightP, rightCount, leftP, leftCount, X.otherSuits, true);
			for(int l = 0; l  < 4; l ++)
			{
				int ar[] = removeWorstHand(rightP);
				rightP[ar[0]] = rightP[ar[0]] & ~ (1<< ar[1]);
			}
		}
		else
		{
			constructBoth(templeftovers, leftP, leftCount, rightP, rightCount, X.otherSuits, false);
		}

		depth = 0;
		//		try {
		//			Thread.sleep(10000);
		//		} catch (InterruptedException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}

		//get current winner


		//Simulate a minimax assuming players play greedy if possible

		//Return card which on play gives best result

		//int min = 100;
		int max = -1000;
		int choice [] = null;
		int wins[] = new int[3];
		//Maximize winner
		//TODO ADD HEURISTIC THAT IS MAXIMIZED
		for(int i = 0; i < 4; i++)
		{
			for(int j = 0; j < 13; j++)
			{
				if( ((1 << j) & X.hand[i]) != 0)
				{

					//check if legal, if not continue
					if(!View.legal(X.hand, X.table, i, j, X.suit))
					{
						continue;
					}

					View tempV = new View(X);
					int winner = currWinner;
					int turn = 0;
					tempV.suit = X.suit;
					int tempLeft[] = leftP.clone();
					int tempRight[] = rightP.clone();

					int tempScore[] = scores.clone();




					//Simulate playing this card


					//pick the card with minimum return back to win time,
					//if a card with return = 1 found play it baby

					//How to simulate game??


					boolean agentSituation[] = new boolean[4];
					boolean found = false;
					boolean relativeOthers[][];
					int ar[];

					while(!found)
					{

						fixKnowledge(tempV.hand, agentSituation);

						if(tempV.tableSize() == 0)
						{
							switch(turn)
							{
							case 0:

								if(depth == 0)
								{
									//play (i,j)

									tempV.addToTable(i, j);
									tempV.removeCard(i, j);
									tempV.suit = i;
								}

								else
								{
									ar = beatable(tempV.hand, tempV.grave, tempV.suit, tempV.table, tempV.otherSuits);
									tempV.suit = ar[0];
									tempV.addToTable(ar[0], ar[1]);
									tempV.removeCard(ar[0], ar[1]);
								}

								winner = 0;
								turn = (4+ turn)%3;
								fixKnowledge(tempV.hand, agentSituation);
								break;

							case 1:

								relativeOthers = new boolean[2][4];
								relativeOthers[0] = tempV.otherSuits[1];
								relativeOthers[1] = agentSituation;

								ar = beatable(tempLeft, tempV.grave, tempV.suit, tempV.table, relativeOthers);
								tempV.suit = ar[0];
								tempV.addToTable(ar[0], ar[1]);
								tempLeft[ar[0]] = tempLeft[ar[0]] & (~(1<< ar[1]));
								tempV.addToGrave(ar[0], ar[1]);

								winner = 1;
								turn = (4+ turn)%3;

								fixKnowledge(tempLeft, tempV.otherSuits[0]);

								break;

							case 2:

								relativeOthers = new boolean[2][4];
								relativeOthers[1] = tempV.otherSuits[0];
								relativeOthers[0] = agentSituation;

								ar = beatable(tempRight, tempV.grave, tempV.suit, tempV.table, relativeOthers);
								tempV.suit = ar[0];
								tempV.addToTable(ar[0], ar[1]);
								tempRight[ar[0]] = tempRight[ar[0]] & (~(1<< ar[1]));
								tempV.addToGrave(ar[0], ar[1]);

								winner = 2;
								turn = (4+ turn)%3;
								fixKnowledge(tempRight, tempV.otherSuits[1]);
								break;
							}

						}
						else if(tempV.tableSize() == 1)
						{

							switch(turn)
							{
							case 0:

								if(depth == 0)
								{
									//play (i,j)
									tempV.addToTable(i, j);
									tempV.removeCard(i, j);

									if(View.greaterThan(i, j, tempV.suit, BitAgent2.binlog( Integer.highestOneBit(tempV.table[tempV.suit] )), tempV.suit))
									{
										winner = 0;
									}
								}

								else
								{
									ar = beatable(tempV.hand, tempV.grave, tempV.suit, tempV.table, tempV.otherSuits);

									tempV.addToTable(ar[0], ar[1]);
									tempV.removeCard(ar[0], ar[1]);
									if(View.greaterThan(ar[0], ar[1], tempV.suit, BitAgent2.binlog( Integer.highestOneBit(tempV.table[tempV.suit] )), tempV.suit))
									{
										winner = 0;
									}
								}

								fixKnowledge(tempV.hand, agentSituation);

								//winner = 2;
								turn = (4+ turn)%3;


								break;

							case 1:

								relativeOthers = new boolean[2][4];
								relativeOthers[0] = tempV.otherSuits[1];
								relativeOthers[1] = agentSituation;

								ar = beatable(tempLeft, tempV.grave, tempV.suit, tempV.table, relativeOthers);

								tempV.addToTable(ar[0], ar[1]);
								tempLeft[ar[0]] = tempLeft[ar[0]] & (~(1<< ar[1]));
								tempV.addToGrave(ar[0], ar[1]);

								if(View.greaterThan(ar[0], ar[1], tempV.suit, BitAgent2.binlog( Integer.highestOneBit(tempV.table[tempV.suit] )), tempV.suit))
								{
									winner = 1;
								}

								turn = (4+ turn)%3;

								fixKnowledge(tempLeft, tempV.otherSuits[0]);

								break;

							case 2:

								relativeOthers = new boolean[2][4];
								relativeOthers[1] = tempV.otherSuits[0];
								relativeOthers[0] = agentSituation;

								ar = beatable(tempRight, tempV.grave, tempV.suit, tempV.table, relativeOthers);

								tempV.addToTable(ar[0], ar[1]);
								tempRight[ar[0]] = tempRight[ar[0]] & (~(1<< ar[1]));
								tempV.addToGrave(ar[0], ar[1]);

								if(View.greaterThan(ar[0], ar[1], tempV.suit, BitAgent2.binlog( Integer.highestOneBit(tempV.table[tempV.suit] )), tempV.suit))
								{
									winner = 2;
								}
								turn = (4+ turn)%3;

								fixKnowledge(tempRight, tempV.otherSuits[1]);
								break;
							}


						}
						else if (tempV.tableSize() == 2)
						{

							switch(turn)
							{
							case 0:

								if(depth == 0)
								{
									//play (i,j)
									tempV.addToTable(i, j);
									tempV.removeCard(i, j);

									if(View.greaterThan(i, j, tempV.suit, BitAgent2.binlog( Integer.highestOneBit(tempV.table[tempV.suit] )), tempV.suit))
									{
										winner = 0;
									}
									else if(View.greaterThan(i, j, 0, BitAgent2.binlog( Integer.highestOneBit(tempV.table[0] )), tempV.suit))
									{
										winner = 0;
									}

								}

								else
								{
									ar = beatable(tempV.hand, tempV.grave, tempV.suit, tempV.table, tempV.otherSuits);


									tempV.addToTable(ar[0], ar[1]);
									tempV.removeCard(ar[0], ar[1]);
									if(View.greaterThan(ar[0], ar[1], tempV.suit, BitAgent2.binlog( Integer.highestOneBit(tempV.table[tempV.suit] )), tempV.suit))
									{
										winner = 0;
									}
									else if(View.greaterThan(ar[0], ar[1], 0, BitAgent2.binlog( Integer.highestOneBit(tempV.table[0] )), tempV.suit))
									{
										winner = 0;
									}
								}

								turn = (4+ turn)%3;

								fixKnowledge(tempV.hand, agentSituation);
								break;

							case 1:

								relativeOthers = new boolean[2][4];
								relativeOthers[0] = tempV.otherSuits[1];
								relativeOthers[1] = agentSituation;

								ar = beatable(tempLeft, tempV.grave, tempV.suit, tempV.table, relativeOthers);

								tempV.addToTable(ar[0], ar[1]);
								tempLeft[ar[0]] = tempLeft[ar[0]] & (~(1<< ar[1]));
								tempV.addToGrave(ar[0], ar[1]);

								if(View.greaterThan(ar[0], ar[1], tempV.suit, BitAgent2.binlog( Integer.highestOneBit(tempV.table[tempV.suit] )), tempV.suit))
								{
									winner = 1;
								}
								else if(View.greaterThan(ar[0], ar[1], 0, BitAgent2.binlog( Integer.highestOneBit(tempV.table[0] )), tempV.suit))
								{
									winner = 1;
								}


								turn = (4+ turn)%3;								

								fixKnowledge(tempLeft, tempV.otherSuits[0]);

								break;

							case 2:
								relativeOthers = new boolean[2][4];
								relativeOthers[1] = tempV.otherSuits[0];
								relativeOthers[0] = agentSituation;

								ar = beatable(tempRight, tempV.grave, tempV.suit, tempV.table, relativeOthers);

								tempV.addToTable(ar[0], ar[1]);
								tempRight[ar[0]] = tempRight[ar[0]] & (~(1<< ar[1]));
								tempV.addToGrave(ar[0], ar[1]);

								if(View.greaterThan(ar[0], ar[1], tempV.suit, BitAgent2.binlog( Integer.highestOneBit(tempV.table[tempV.suit] )), tempV.suit))
								{
									winner = 2;
								}
								else if(View.greaterThan(ar[0], ar[1], 0, BitAgent2.binlog( Integer.highestOneBit(tempV.table[0] )), tempV.suit))
								{
									winner = 2;
								}

								turn = (4+ turn)%3;

								fixKnowledge(tempRight, tempV.otherSuits[1]);
								break;
							}

						}
						else
						{

							//clear table
							if(winner == 0)
							{
								tempScore[0]++;
								found = true;
								break;

							}
							else if(winner == 1)
							{
								tempScore[1]++;
							}
							else
							{
								tempScore[2]++;
							}

							tempV.resetTable();
							turn = winner;
						}
						if(depth > 5)
						{
							found = true;
							break;
						}
						if(tempV.handSize() == 0 || tempLeft[0]+tempLeft[1]+tempLeft[2]+tempLeft[3] == 0 || tempRight[0] +tempRight[1]+tempRight[2]+tempRight[3] ==0)
						{
							break;

						}

						depth++;

					}		



					//While ends here
					if(depth < 10)
					{

						int maxim = tempScore[1] -removes[1] > tempScore[2] -removes[2]? tempScore[1] -removes[1] : tempScore[2] -removes[2];

						if(tempScore[0] - removes[0] - maxim > max)
						{
							goodCards = new int[20][2];
							for(int kk = 0; kk < 20; kk++)
							{
								goodCards[kk][0] = -1;
							}
							number = 0;
							max = tempScore[0] - removes[0] - maxim;
							goodCards[number] = new int [] {i,j};
							number++;
							choice = new int [] {i,j};
						}
						else if(tempScore[0] - removes[0] - maxim == max)
						{
							goodCards[number] = new int [] {i,j};
							number++;
						}

					}	
				}
			}
		}

		return goodCards;
	}

	public static void printCards(int hand[])
	{
		for(int i = 0; i < 4; i++)
		{
			for(int j = 0; j < 13; j++)
			{
				if((hand[i] & (1 << j)) != 0)
				{
					System.out.print(BitAgent2.retCard(i, j).toString() +" ");
				}
			}
		}
		System.out.println();
	}



















	private static int[] beatable(int[] hand, int [] grave, int curSuit, int[] tableB, boolean otherSuits[][])
	{
		int sizeT = Integer.bitCount(tableB[0])+Integer.bitCount(tableB[1])+Integer.bitCount(tableB[2])+Integer.bitCount(tableB[3]);
		//Both cards on table
		if(sizeT == 2)
		{
			//play a card bigger than both

			if(hand[curSuit] == 0)
			{

				int start = BitAgent2.binlog( Integer.highestOneBit(tableB[0]) );

				int found = -1;
				for(int i = start; i < 13; i++)
				{
					if( ((1 << i) & hand[0]) != 0)
					{
						found = i;
						break;
					}
				}

				//If beatable play the spade
				if(found != -1)
				{
					return new int[] {0, found};
				}

				else
				{
					//TODO REMOVE WORST CARD
					return removeWorstHand(hand);
				}
			}

			else
			{

				if(tableB[0] == 0 || curSuit == 0)
				{
					int start = BitAgent2.binlog(Integer.highestOneBit(tableB[curSuit]));
					int found = -1;
					for(int i = start; i < 13; i++)
					{
						if( ((1 << i) & hand[curSuit]) != 0 )
						{
							found = i;
							break;
						}
					}

					//if beatable throw card
					if(found != -1)
					{
						return new int[] {curSuit, found};
					}
					//play trash card of the same deck
					else
					{
						return new int[] {curSuit, BitAgent2.binlog(Integer.lowestOneBit(hand[curSuit]))};
					}
				}
				else
				{
					return new int[] {curSuit, BitAgent2.binlog(Integer.lowestOneBit(hand[curSuit]))};
				}
			}
		}

		if(sizeT==0)
		{

			for(int k = 1; k < 4; k++)
			{
				if(!((otherSuits[0][k] == true && otherSuits[0][0] == false) || (otherSuits[1][k] == true && otherSuits[1][0] == false)))
				{
					//look for unbeatable card if found play
					//else play trash

					//highest card
					int start = BitAgent2.binlog(Integer.highestOneBit(((~grave[k] &0b1111111111111)&(~hand[k] &0b1111111111111))));
					int found = -1;
					for(int i = start; i < 13; i++)
					{
						if( ((1 << i) & hand[k]) != 0)
						{
							found = i;
							break;
						}
					}
					//If beatable play
					if(found != -1)
					{
						return new int[] {k, found};
					}
				}
			}

			//try spade
			int start = BitAgent2.binlog(Integer.highestOneBit(((~grave[0] &0b1111111111111)&(~hand[0] &0b1111111111111))));
			int found = -1;
			for(int i = start; i < 13; i++)
			{
				if( ((1 << i) & hand[0]) != 0)
				{
					found = i;
					break;
				}
			}
			//If beatable play
			if(found != -1)
			{
				return new int[] {0, found};
			}
			//play shitty card
			else
			{
				//TODO REMOVE WORST CARD
				return removeWorstHand(hand);
			}
		}

		//out stands for out and has spades

		boolean out = false;

		//next player
		//out of current hand but has spades
		if(curSuit != 0 && otherSuits[1][curSuit] == true && otherSuits[1][0] == false)
		{
			out = true;
		}

		if(out)
		{
			//Check if out of cards of the required deck
			if(hand[curSuit] == 0 || curSuit == 0)
			{

				//Check if have any spades if not throw a trashy card of any deck but spades
				if(hand[0] == 0)
				{
					//TODO REMOVE WORST CARD
					return removeWorstHand(hand);
				}

				//check if possible to beat with spades
				int start = BitAgent2.binlog(Integer.highestOneBit( tableB[0] | ((~grave[0] &0b1111111111111)&(~hand[0] &0b1111111111111))));

				int found = -1;
				for(int i = start; i < 13; i++)
				{
					if( ((1 << i) & hand[0]) != 0)
					{
						found = i;
						break;
					}
				}

				//If beatable play the spade
				if(found != -1)
				{
					return new int[] {0, found};
				}
				//else throw a trashy card of any deck but spades
				else
				{
					//TOD
					return removeWorstHand(hand);

				}
			}
			//Throw a bad card of the deck since it can't win
			else
			{
				return new int[] {curSuit, BitAgent2.binlog(Integer.lowestOneBit(hand[curSuit]))};
			}
		}
		//If others still have the cards
		else
		{
			//If out of cards throw the lowest spade
			if(hand[curSuit] == 0)
			{
				//Check if have any spades if not throw a trashy card of any deck but spades
				if(hand[0] == 0)
				{
					//TODO
					return removeWorstHand(hand);
				}
				else
				{
					return new int[] {0, BitAgent2.binlog(Integer.lowestOneBit(hand[0]))};
				}
			}
			//checks if beatable
			else
			{

				int start = BitAgent2.binlog(Integer.highestOneBit(tableB[curSuit] | ((~grave[curSuit] &0b1111111111111)&(~hand[curSuit] &0b1111111111111))));
				int found = -1;
				for(int i = start; i < 13; i++)
				{
					if( ((1 << i) & hand[curSuit]) != 0 )
					{
						found = i;
						break;
					}
				}

				//if beatable throw card
				if(found != -1)
				{
					return new int[] {curSuit, found};
				}
				//play trash card of the same deck
				else
				{
					return new int[] {curSuit, BitAgent2.binlog(Integer.lowestOneBit(hand[curSuit]))};
				}
			}
		}

	}


	private static int [] removeWorstHand(int hand[])
	{

		int pc [] = new int[3];

		pc[0]= BitAgent2.binlog(Integer.lowestOneBit(hand[1]));
		pc[1]= BitAgent2.binlog(Integer.lowestOneBit(hand[2]));
		pc[2]= BitAgent2.binlog(Integer.lowestOneBit(hand[3]));

		int min = 50;
		int sec = 0;
		for(int i = 0; i < 3; i++)
		{
			if(pc[i]< min && pc[i] != -1)
			{
				sec = i+1;
				min = pc[i];
			}
		}

		return new int [] {sec, BitAgent2.binlog(Integer.lowestOneBit(hand[sec]))};
	} 

	private static void fixKnowledge(int handP[], boolean arr[])
	{
		for(int t = 0; t < 4; t++)
		{
			if(handP[t] == 0)
			{
				arr[t] = true;
			}
		}
	}
}



//static int [] constructHand(int leftOvers[], int player, boolean otherSuits[][], int size)
//{
//
//
//	int hand[] = new int[4];
//	int assigned = 0;
//	if(player == 1)
//	{
//		while(assigned != size)
//		{
//
//			int total = 0;
//			for(int i = 0; i < 4; i++)
//			{
//				if(otherSuits[0][i] == false)
//				{
//					if(assigned < size)
//					{
//						int set = (int)(Math.random()*((double)(THIRTEEN + 1)));
//
//						if((set & leftOvers[i]) != 0)
//						{
//							if(Integer.bitCount((set & leftOvers[i])) + assigned <= size)
//							{
//								assigned += Integer.bitCount((set & leftOvers[i]));
//								hand[i] = hand[i] | (set & leftOvers[i]);
//								leftOvers[i] = leftOvers[i]& ~set & THIRTEEN;
//							}
//							else
//							{
//								for(int k = 0; k < 13; k++)
//								{
//									if((set & (1<<k)) != 0)
//									{
//										int in = (int)(Math.random()*((double)2));
//										if(in == 0)
//										{
//											hand[i] = hand[i] | (1<<k);
//											leftOvers[i] = leftOvers[i] & ~(1<<k);
//											assigned++;
//										}
//										if(assigned== size)
//											break;
//									}
//
//								}
//							}
//						}
//					}
//					else
//					{
//						break;
//					}
//					total+= Integer.bitCount(leftOvers[i]);
//				}
//			}
//
//			if(assigned+total < size)
//			{
//				return null;
//			}
//
//		}
//
//	}
//	else if(player == 2)
//	{
//		while(assigned != size)
//		{
//			int total = 0;
//
//			for(int i = 0; i < 4; i++)
//			{
//				if(otherSuits[1][i] == false)
//				{
//					if(assigned < size)
//					{
//
//						int set = (int)(Math.random()*((double)(0b10000000000000)));
//						//							System.out.println("YOLO "+ (set & leftOvers[i]));
//						if((set & leftOvers[i]) != 0)
//						{
//							//								System.out.println("XT"+Integer.toBinaryString(set & leftOvers[i]));
//							if(Integer.bitCount((set & leftOvers[i])) + assigned <= size)
//							{
//								assigned += Integer.bitCount((set & leftOvers[i]));
//								hand[i] = hand[i] | (set & leftOvers[i]);
//								leftOvers[i] = leftOvers[i]& ~set & THIRTEEN;
//							}
//							else
//							{
//								for(int k = 0; k < 13; k++)
//								{
//									if((set & (1<<k)) != 0)
//									{
//										int in = (int)(Math.random()*((double)2));
//										if(in == 0)
//										{
//											hand[i] = hand[i] | (1<<k);
//											leftOvers[i] = leftOvers[i] & ~(1<<k) &THIRTEEN ;
//											assigned++;
//										}
//										if(assigned== size)
//											break;
//									}
//
//								}
//							}
//						}
//
//					}
//
//					total+= Integer.bitCount(leftOvers[i]);	
//				}
//
//			}
//			if(assigned+total < size)
//			{
//				return null;
//			}
//		}
//	}
//
//	return hand;
//}

