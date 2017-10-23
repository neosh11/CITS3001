package mosssidewhist;

import java.util.*;

public class BitAgent2 implements MSWAgent  {

	public static final int LEADER = 0;
	public static final int LEFT = 1;
	public static final int RIGHT = 2;

	public static final String NAME = "Bit47-2";
	
	private String leftAgent;
	private String rightAgent;
	
	private int scores[];

	View current;
	
	public BitAgent2() {

		current = new View();
		scores = new int[3];
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
			current.addCard(hand.get(i));		
		current.setPosition(order);
	}


	@Override
	public Card[] discard() {
		//TODO
		
		Card[] discard = new Card[4];
		for(int i = 0; i<4;i++)
		{
			int indexHand = removeWorstHand(current.hand);
			int remove = Integer.lowestOneBit(current.hand[indexHand]);	
			current.hand[indexHand] = current.hand[indexHand]&(~remove);
			current.grave[indexHand] = current.grave[indexHand]|(remove);
			discard[i] = retCard(indexHand, binlog(remove));
		}
		return discard;
	}

	@Override
	public Card playCard() {

		return null;
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
						else
						{
							current.otherSuits[1][current.suit] = true;
						}
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
	private int removeWorstHand(int hand[])
	{

		int pc [] = new int[3];

		pc[0]= Integer.lowestOneBit(hand[1]);
		pc[1]= Integer.lowestOneBit(hand[2]);
		pc[2]= Integer.lowestOneBit(hand[3]);

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

	private boolean greaterThan(Card A, Card B, int suit)
	{
		int suitA = suitVal(A);
		int suitB = suitVal(B);

		if(suitA == suit && suitA !=0)
		{
			if(suitB == suit)
			{
				return B.rank > A.rank ? false : true;
			}
			else if(suitB == 0)
			{
				return false;
			}
			else
			{
				return true;
			}
		}
		else if(suitA == 0)
		{
			if(suitB == 0)
			{
				return B.rank > A.rank ? false : true;
			}
			else
			{
				return true;
			}

		}
		else
			return true;
	}

	
	private int[] beatable(View X)
	{
		
		if(current.tableSize() ==1)
		{
			//play unbeatable if not found, play strategic
			
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
					return new int[] {0, found};
				}

				else
				{
					//Remove random worst card that will reap rewards
					//TODO
					int removeSuit = removeWorstHand(X.hand);
					int removal = binlog(Integer.lowestOneBit(X.hand[removeSuit]));
					return new int[] {removeSuit, removal};	
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
						return new int[] {X.suit, found};
					}
					//play trash card of the same deck
					else
					{
						return new int[] {X.suit, binlog(Integer.lowestOneBit(X.getHandSet(X.suit)))};
					}
				}
				else
				{
					return new int[] {X.suit, binlog(Integer.lowestOneBit(X.hand[X.suit]))};
				}
			}
		}

		else //Nothing on table
		{
			//play unbeatable if not found, play strategic
			
		}
		
		//Tree as an inner class
		
		class Tree
		{
			View node;
			Tree children[];

			Tree(View view)
			{
				node = new View(view);
				//children = new Tree[node.handSize()];
			}
			
			
				
			

		}
		
		
		return null;
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
			if((grave[i]|hand[i]) == 0b1111111111111)
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
}