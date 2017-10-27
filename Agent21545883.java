package mosssidewhist;

import java.util.*;

/**
 * This is an agent that plays greedily once given the current situation of the game
 * It always plays an unbeatable card if it exists else plays an undesirable card
 * This agent wins 96% of the time against 2 random agents if the number of rounds is 9(3 games)
 * 
 * Suits have been assigned predefined integer values:
 * Spades: 0
 * Clubs: 1
 * Diamonds: 2
 * Hearts: 3
 * Ranks are similar: [2...A] -> [0...12]
 */
public class Agent21545883 implements MSWAgent  {

	public static final int LEADER = 0;
	public static final int LEFT = 1;
	public static final int RIGHT = 2;

	protected static final int THIRTEEN= 0b1111111111111; //Thirteen 1s in a set, Represents 13 cards

	public static final String NAME = "GreedyBits";

	private String leftAgent; // Name of the agent to the left
	private String rightAgent; //Name of the agent to the right
	
	private static Card cardSet[];

	//Game information stored here
	//See bottom for detail
	private View current;

	public Agent21545883() {
		
		current = new View(); //The table info
		cardSet = storeCardsInArray(); //Array of cards
	}

	@Override
	public void setup(String agentLeft, String agentRight) {
		leftAgent = agentLeft;
		rightAgent = agentRight;
	}


	@Override
	public void seeHand(List<Card> hand, int order) {

		//Resets the values of the match
		current.resetGame();

		//Adds all the cards to the View
		for(int i = 0; i < hand.size(); i++)
			current.addCard(hand.get(i));

		//Set player's position
		current.setPosition(order);

	}


	@Override
	public Card[] discard() {

		Card[] discard = new Card[4];
		//Removes 4 of the worst cards
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

		//Gets the card to play using the "beatable" function
		Card toRet = beatable(current);
		//Remove the card from the hand
		current.removeCard(suitVal(toRet), toRet.rank-2);
		return toRet;
	}



	@Override
	public void seeCard(Card card, String agent) {

		//Set the default suit
		if(current.isTableEmpty())
		{
			current.setSuit(suitVal(card));
		}

		//Check if players are out of the default suit
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
		//Add to table
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
		//THIS AGENT DOESN'T NEED THIS INFO
	}

	@Override
	public String sayName() {

		return NAME;
	}

	/**
	 * Function that returns which the worst Card according to it's rank and number of cards in the same suit
	 * @param hand Hand to be passed
	 * @return Suit where the bad card lies
	 */
	private Card removeWorstHand(int hand[])
	{

		int pc [] = new int[3];

		pc[0]= binlog(Integer.lowestOneBit(hand[1])); //Clubs
		pc[1]= binlog(Integer.lowestOneBit(hand[2])); //Diamonds
		pc[2]= binlog(Integer.lowestOneBit(hand[3])); //Hearts

		int min = 50; //value of rank
		int sec = 0; //Suit
		int countAssociated = 100; //number of cards

		for(int i = 0; i < 3; i++)
		{

			//choose card if it's the smallest
			if(pc[i]< min && pc[i] != -1)
			{
				sec = i+1;
				min = pc[i];
				countAssociated = Integer.bitCount(hand[i]);
			}
			//if same rank, choose with a lower hand count
			else if(pc[i] == min && pc[i] != -1)
			{
				if(Integer.bitCount(hand[i]) < countAssociated)
				{
					sec = i+1;
					min = pc[i];
					countAssociated = Integer.bitCount(hand[i]);
				}
			}
		}

		//Smallest card of the same suit
		return retCard(sec, binlog(Integer.lowestOneBit(hand[sec])));
	} 

	/**
	 * Returns user-defined suit vaules of a card
	 * @param c Card
	 * @return suit of the card
	 */
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


	/**
	 * Returns an unbeatable card if possible, else returns a bad card
	 * @param X View to be tested
	 * @return Card which should be played
	 */
	private Card beatable(View X)
	{

		if(current.tableSize() ==1)
		{
			//play unbeatable if not found(greedy),
			//play strategic

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
						// REMOVE WORST CARD strategically
						return removeWorstHand(X.hand);

					}

					//check if possible to beat with spades
					int start = binlog(Integer.highestOneBit(X.getTableSet(0) | ((~X.getgraveSet(0) & ~X.getHandSet(0) & THIRTEEN))));
					int found = X.cardGreaterThanInHand(0, start);

					//If beatable play the spade
					if(found != -1)
					{
						return retCard(0, found);
					}
					//else throw a trashy card of any deck but spades
					else
					{

						return removeWorstHand(X.hand);
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
					//Check if have any spades if not throw a trashy card of any deck
					if(X.getHandSet(0) == 0)
					{

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


					return removeWorstHand(X.hand);
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
					//look for unbeatable card if found play
					//else play trash

					//highest card
					int start = binlog(Integer.highestOneBit(~X.grave[k] & ~X.hand[k] & THIRTEEN));
					int found = X.cardGreaterThanInHand(k, start);

					//If beatable play
					if(found != -1)
					{
						return retCard(k, found);
					}
				}
			}

			//try spade
			int start = binlog(Integer.highestOneBit(~X.grave[0] & ~X.hand[0] & THIRTEEN));
			int found = X.cardGreaterThanInHand(0, start);
			//If beatable play
			if(found != -1)
			{
				return retCard(0, found);
			}
			//play strategic card
			else
			{

				return removeWorstHand(X.hand);

			}

		}
	}

	/**
	 * Returns a card from the given suit value and a rank value
	 * This was done to not search for matches repeatedly.
	 * @param s suit Value
	 * @param val rank value
	 * @return Card from these user defined values
	 */
	protected static Card retCard(int s, int val)
	{
		return cardSet[s*13+val];
	}

	/**
	 * Stores Cards in an array for easy access
	 * @return Array of cards
	 */
	private Card [] storeCardsInArray()
	{
		Card card [] = new Card[52];

		card[12]= Card.ACE_S; 
		card[11]= Card.KING_S; 
		card[10]= Card.QUEEN_S; 
		card[9] = Card.JACK_S; 
		card[8] = Card.TEN_S; 
		card[7] = Card.NINE_S; 
		card[6] = Card.EIGHT_S; 
		card[5] = Card.SEVEN_S; 
		card[4] = Card.SIX_S; 
		card[3] = Card.FIVE_S; 
		card[2] = Card.FOUR_S; 
		card[1] = Card.THREE_S; 
		card[0] = Card.TWO_S; 

		card[25]  = Card.ACE_C; 
		card[24] = Card.KING_C; 
		card[23] = Card.QUEEN_C; 
		card[22] = Card.JACK_C; 
		card[21] = Card.TEN_C; 
		card[20] = Card.NINE_C; 
		card[19] = Card.EIGHT_C; 
		card[18] = Card.SEVEN_C; 
		card[17] = Card.SIX_C; 
		card[16] = Card.FIVE_C; 
		card[15] = Card.FOUR_C; 
		card[14] = Card.THREE_C; 
		card[13] = Card.TWO_C; 

		card[38] = Card.ACE_D; 
		card[37] = Card.KING_D; 
		card[36] = Card.QUEEN_D; 
		card[35] = Card.JACK_D; 
		card[34] = Card.TEN_D; 
		card[33] = Card.NINE_D; 
		card[32] = Card.EIGHT_D; 
		card[31] = Card.SEVEN_D; 
		card[30] = Card.SIX_D; 
		card[29] = Card.FIVE_D; 
		card[28] = Card.FOUR_D; 
		card[27] = Card.THREE_D; 
		card[26] = Card.TWO_D; 

		card[51]  = Card.ACE_H; 
		card[50] = Card.KING_H; 
		card[49] = Card.QUEEN_H; 
		card[48] = Card.JACK_H; 
		card[47] = Card.TEN_H; 
		card[46] = Card.NINE_H; 
		card[45] = Card.EIGHT_H; 
		card[44] = Card.SEVEN_H; 
		card[43] = Card.SIX_H; 
		card[42] = Card.FIVE_H; 
		card[41] = Card.FOUR_H; 
		card[40] = Card.THREE_H; 
		card[39] = Card.TWO_H; 

		return card;

	}
	//https://stackoverflow.com/questions/3305059/how-do-you-calculate-log-base-2-in-java-for-integers
	//It is slightly faster than Integer.numberOfLeadingZeros() (20-30%) 
	//and almost 10 times faster (jdk 1.6 x64) than a Math.log() based implementation

	/**
	 * Rank of the card
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


}

/**
 * This class holds information about everything required for out agents in a single round
 * It store hands, grave, table data as bits. Stores info about the card status, position of the agent,
 * current suit
 *
 */
class View
{

	int hand[]; //Agent's hand
	int grave[];
	int position;

	//FALSE IF THEY HAVE THE CARD
	boolean otherSuits[][];
	int table[];
	int suit;

	/**
	 * Default constructor
	 * Initializes everything to a blank
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
	 * A clone (copies actual data rather than references)
	 * 
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

	/**
	 * Removes everything from the table
	 */
	void resetTable()
	{
		table = new int[4];
	}

	/**
	 * Removes the card from the hand and also adds it to the graveyard
	 * @param suit Suit to be removed
	 * @param value Rank to be removed
	 */
	void removeCard(int suit, int value)
	{
		hand[suit] = ((hand[suit]&(~(1<< value))) & Agent21545883.THIRTEEN);
		addToGrave(suit, value);
	}

	/**
	 * The Card supplied is added to the hand
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
	 * Adds the card of the suit and rank to the graveyard
	 * @param suit Suit to be added [0,3]
	 * @param card Value to be added [0,12]
	 */
	void addToGrave(int suit, int card)
	{
		grave[suit] = grave[suit] | (1 << card);

		for(int i =0; i < 4; i++)
		{
			if((grave[i]|hand[i]) == Agent21545883.THIRTEEN)
			{
				otherSuits[1][i] = true;
				otherSuits[0][i] = true;
			}
		}
	}

	/**
	 * Adds the card of the rank and suit to the graveYard
	 * @param suit Suit to be added [0,3]
	 * @param card Value to be added [0,12]
	 */
	void addToTable(int suit, int card)
	{
		if(tableSize() == 0)
		{
			this.suit = suit;
		}
		table[suit] = table[suit] | (1 << card);
		addToGrave(suit, card);
	}

	/**
	 * Sets position of agent on the table
	 * @param p the position of the Agent
	 */
	void setPosition(int p)
	{
		position = p;
	}

	/**
	 * The default rank of a single trick round
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

	/**
	 * Returns the set of cards of a suit in bit-set form
	 * @param suit suit to get
	 * @return set of cards of a suit in bit-set form
	 */
	int getHandSet(int suit)
	{
		return hand[suit];
	}

	/**
	 * Gets the bit-set of the suit in the grave
	 * @param suit suit to get
	 * @return the bit-set of the suit in the grave
	 */
	int getgraveSet(int suit)
	{
		return grave[suit];
	}

	/**
	 * Gets the bit-set of the suit on the table
	 * @param suit suit to get
	 * @return the bit-set of the suit on the table
	 */
	int getTableSet(int suit)
	{
		return table[suit];
	}

	/**
	 * Gets the rank of the highest card of a suit
	 * @param i Bit-set (Suit)
	 * @return the rank of the highest card of a suit
	 */
	int getSetHighestCard(int i)
	{
		return Agent21545883.binlog(Integer.highestOneBit(i));
	}

	/**
	 * Returns the value of the smallest card bigger than a certain card, -1 if not found
	 * @param suit Suit of a card
	 * @param value rank of the card
	 * @return rank of a card greater than the other card in the same suit
	 */
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
