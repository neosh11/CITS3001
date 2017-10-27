package mosssidewhist;

import java.util.*;
import java.util.Map.Entry;


/**
 * This is an agent that plays greedily if it can find an unbeatable card
 * Else plays using a monte-carlo recursive method
 * 
 * Suits have been assigned predefined integer values:
 * Spades: 0
 * Clubs: 1
 * Diamonds: 2
 * Hearts: 3
 * Ranks are similar: [2...A] -> [0...12]
 *
 */
public class Agent21959462 implements MSWAgent  {

	public static final int LEADER = 0;
	public static final int LEFT = 1;
	public static final int RIGHT = 2;
	
	public static final int THIRTEEN= 0b1111111111111; //Thirteen 1s in a set, Represents 13 cards

	public static final String NAME = "DirtyBit";

	private String leftAgent; //left agent name
	private String rightAgent; //right agent name
	
	private static Card cardSet[]; //Holds cards
	

	private int scores[];
	private int removal[];
	private int currentWinner;

	View current;

	public Agent21959462() {

		current = new View(); //empty game state
		scores = new int[3]; //new scores
		removal = new int[3]; //points to be removed
		cardSet = storeCardsInArray(); //stores cards in cardSet array
	}

	@Override
	public void setup(String agentLeft, String agentRight) {
		leftAgent = agentLeft; //stores the name of the agent on the left 
		rightAgent = agentRight; //stores the name of the agent on the right
	}


	@Override
	public void seeHand(List<Card> hand, int order) {

		current.resetGame(); //reset game

		for(int i = 0; i < hand.size(); i++)
			current.addCard(hand.get(i)); //Add all cards

		current.setPosition(order); //set position


		//Stores the removal values
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

		Card[] discard = new Card[4];
		if(current.position != LEADER && current.handSize() !=16)
		{
			return new Card[4];
		}
		//remove the worst 4 hands
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
		} //Set current suit

		else //check if players out of cards
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


		if(State.greaterThanTable(suitVal(card), card.rank-2, current.table, current.suit))
		{
			//Store current winner
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
	 * Return value of the suit of a hand
	 * @param c Card
	 * @return Value of the suit
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
	 * Returns the card a player should play, either greedily or using Monte-Carlo methods
	 * @param X game state
	 * @param currentWin current winner
	 * @return Card that should be played
	 */
	private Card beatable(View X, int currentWin)
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

						Card toRet = Simulator2.simulateBest(X, currentWin, scores, removal);
				
						if(toRet == null)
						{
							//If simulation leads to no results, remove lowest numbered card
							return removeWorstHand(X.hand);
						}
						else
						{
							//return card from simulation
							return toRet;
						}

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
						//Monte-Carlo
						Card toRet = Simulator2.simulateBest(X, currentWin, scores, removal);
				
						if(toRet == null)
						{
							return removeWorstHand(X.hand);
						}
						else
						{
							//System.out.println(suitVal(toRet)+" "+ (toRet.rank+2));
							return toRet;
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
					// Play strategic instead
					//Monte-Carlo
					Card toRet = Simulator2.simulateBest(X, currentWin, scores, removal);
				
					if(toRet == null)
					{

						return removeWorstHand(X.hand);
					}
					else
					{
						//System.out.println(suitVal(toRet)+" "+ (toRet.rank+2));
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
				//Monte Carlo
				Card toRet = Simulator2.simulateBest(X, currentWin, scores, removal);
				
				if(toRet == null)
				{
					return removeWorstHand(X.hand);
				}
				else
				{
					//System.out.println(suitVal(toRet)+" "+ (toRet.rank+2));
					return toRet;
				}
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
		if(s >=0 && s <4 && val >=0 && val < 13)
			return cardSet[s*13+val];
		else
			return null;
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
	 * Returns rank of the card (position)
	 * @param bits Set which to check on
	 * @return location of the bit, -1 if empty
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
 * Monte-Carlo Simulator
 *
 */
class Simulator2
{
	private static Random random = new Random();
	public static final int THIRTEEN= 0b1111111111111;

	/**
	 * Constructs 2 other players hands using the info
	 * @param leftOvers cards left over(will be modified)
	 * @param left leftPlayer's card (will be modified)
	 * @param leftVal number of cards to be added (will be modified)
	 * @param right right player's cards (will be modified)
	 * @param rightVal number of cards for the right player
	 * @param otherSuits info about other players cards
	 * @param leader if left is leader
	 */
	static void constructBoth(int leftOvers[], int left[], int leftVal, int right[], int rightVal, boolean otherSuits[][], boolean leader)
	{


		int count = 0;
		int oldLCount = 0;
		int lcount, rcount;
		lcount = rcount = 0;

		boolean distributed = false;

		while(!distributed)
		{
			oldLCount = lcount;
			for(int i = 0; i < 4; i++)
			{
				//Distribute
				for(int k = 0; k < 13; k++)
				{
					if((leftOvers[i] & (1 << k)) != 0)
					{
						if(random.nextInt(10) < 5)
						{
							if(random.nextBoolean() && otherSuits[0][i] == false && (lcount < leftVal))
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
			if(rcount== rightVal && lcount == leftVal)
			{
				if(leader)
				{
					//add remains to leader
					for(int l = 0; l < 4; l++ )
					{
						left[l] = left[l] | leftOvers[l];
					}
				}

				distributed =true;
			}

			//if possible to add to either, if not swap some cards
			int canBeAddedLeft = 0;
			int canBeAddedRight= 0;
			for(int l = 0; l < 4; l++)
			{
				if(otherSuits[0][l] == false)
				{
					canBeAddedLeft += Integer.bitCount(leftOvers[l]);
				}
				if(otherSuits[1][l] == false)
				{
					canBeAddedRight += Integer.bitCount(leftOvers[l]);
				}
			}

			if(lcount == 0 && rcount ==0)
			{
				continue;
			}

			if((canBeAddedLeft == 0 && lcount <leftVal))
			{
				//swap rights's cards with leftovers


				for(int l = 0; l < 4; l++)
				{
					if(leftOvers[l] != 0)
					{
						if(!otherSuits[1][l])
						{
							//pick a random card
							int card = (1<<random.nextInt(13));

							//Definitely exits, not infinite loop
							while((leftOvers[l] & card) == 0)
							{
								card = (1<<random.nextInt(13));
							}

							//swap this card with a card that left can take
							int suitX = random.nextInt(4);
							while(otherSuits[0][suitX] || right[suitX] == 0)
							{
								suitX = random.nextInt(4);
							}

							int Othercard = (1<<random.nextInt(13));
							while((right[suitX] & Othercard) == 0)
							{
								Othercard = (1<<random.nextInt(13));
							}

							//swap (l, card) and (suitX, otherCard)

							leftOvers[l] = (leftOvers[l] & ~(card));
							leftOvers[suitX] = leftOvers[suitX] | Othercard;

							right[suitX] = (right[suitX] & ~(Othercard));
							right[l] = right[l] | card;	
							break;

						}
					}
				}

				if((canBeAddedRight==0 && rcount <rightVal))
				{

					//swap left's cards with leftovers

					for(int l = 0; l < 4; l++)
					{
						if(leftOvers[l] != 0)
						{
							if(!otherSuits[0][l])
							{
								//pick a random card
								int card = (1<<random.nextInt(13));
								while((leftOvers[l] & card) == 0)
								{
									card = (1<<random.nextInt(13));
								}
								//swap this card with a card that right can take
								int suitX = random.nextInt(4);
								while(otherSuits[1][suitX] || left[suitX] == 0)
								{
									suitX = random.nextInt(4);
								}

								int Othercard = (1<<random.nextInt(13));
								while((left[suitX] & Othercard) == 0)
								{
									Othercard = (1<<random.nextInt(13));
								}

								//swap (l, card) and (suitX, otherCard)

								leftOvers[l] = (leftOvers[l] & ~(card));
								leftOvers[suitX] = leftOvers[suitX] | Othercard;

								left[suitX] = (left[suitX] & ~(Othercard));
								left[l] = left[l] | card;	

								break;

							}
						}
					}
				}

			}

			if(lcount == oldLCount)
			{
				count++;
			}
			if(count > 20)
			{
				//Force distribute
				while(lcount < leftVal)
				{
					for(int l = 0; l < 4; l++)
					{
						if(leftOvers[l] != 0)
						{
							//pick a random card
							int card = (1<<random.nextInt(13));
							while((leftOvers[l] & card) == 0)
							{
								card = (1<<random.nextInt(13));
							}
							
							left[l] = left[l] | (1 << card);
							leftOvers[l] = leftOvers[l] & ~(1 << card) & THIRTEEN;
							lcount ++;
						}
					}
				}
				while(rcount < rightVal)
				{
					for(int l = 0; l < 4; l++)
					{
						if(leftOvers[l] != 0)
						{
							//pick a random card
							int card = (1<<random.nextInt(13));
							while((leftOvers[l] & card) == 0)
							{
								card = (1<<random.nextInt(13));
							}
							
							right[l] = right[l] | (1 << card);
							leftOvers[l] = leftOvers[l] & ~(1 << card) & THIRTEEN;
							rcount ++;
						}
					}
				}
				break;
			}


		}

	}

	/**
	 * Returns the card which the agent should play according to a simulation
	 * @param X Game state
	 * @param currWinner player winning
	 * @param scores scores of players
	 * @param removes score to be subtracted
	 * @return Best card to play
	 */
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
		
		if(scores == null)
		{
			return null;
		}

		HashMap<Card,MutableInt> map = new HashMap<Card, MutableInt>();

		//Time Limited
		
		long endTime = System.currentTimeMillis() + 180;
		while(System.currentTimeMillis() < endTime)
		{
			int leftOvers[] = new int[4];
			int leftP[] = new int[4];
			int rightP[] = new int[4];

			for(int l = 0; l < 4; l++)
			{
				leftOvers[l] = (~X.grave[l] & ~X.hand[l] & THIRTEEN);
			}

			//0 for agent 1 for left, 2 for right

			int leftCount = 0;
			int rightCount = 0;
			int tSize = X.tableSize();

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

			int templeftovers[] = leftOvers.clone();

			if(removes[1] == 8)
			{
				constructBoth(templeftovers, leftP, leftCount, rightP, rightCount, X.otherSuits, true);

				int numberOfC = Integer.bitCount(leftP[0]) + Integer.bitCount(leftP[1])+ Integer.bitCount(leftP[2])+ Integer.bitCount(leftP[3]);
				for(int l = 0; l  < numberOfC - leftCount; l ++)
				{
					int ar[] = removeWorstHand(leftP);
					leftP[ar[0]] = leftP[ar[0]] & ~ (1<< ar[1]);
				}
			}
			//right is leader
			else if(removes[2] == 8)
			{
				constructBoth(templeftovers, rightP, rightCount, leftP, leftCount, X.otherSuits, true);

				int numberOfC = Integer.bitCount(rightP[0]) + Integer.bitCount(rightP[1])+ Integer.bitCount(rightP[2])+ Integer.bitCount(rightP[3]);
				for(int l = 0; l  <  numberOfC - rightCount; l ++)
				{
					int ar[] = removeWorstHand(rightP);
					rightP[ar[0]] = rightP[ar[0]] & ~ (1<< ar[1]);
				}
			}
			else
			{
				constructBoth(templeftovers, leftP, leftCount, rightP, rightCount, X.otherSuits, false);
			}


			//Simulate the constructed hand


			State tempS = new State(X, 0, 0, leftP, rightP, scores, currWinner);


			int [] choice = new int[2];
			Arrays.fill(choice, -1);

			tempS.max(tempS, tempS.score, choice, removes);


			Card tempC = Agent21959462.retCard(choice[0], choice[1]);
			MutableInt count = map.get(tempC);
			if (count == null) {
				map.put(tempC, new MutableInt());
			}
			else {
				count.increment();
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
			return Agent21959462.retCard(ar[0], ar[1]);
		}
		
		return toReturn;

	}


	public static void printCards(int hand[])
	{
		for(int i = 0; i < 4; i++)
		{
			for(int j = 0; j < 13; j++)
			{
				if((hand[i] & (1 << j)) != 0)
				{
					System.out.print(Agent21959462.retCard(i, j).toString() +" ");
				}
			}
		}
		System.out.println();
	}


	private static int [] removeWorstHand(int hand[])
	{

		int pc [] = new int[3];

		pc[0]= Agent21959462.binlog(Integer.lowestOneBit(hand[1]));
		pc[1]= Agent21959462.binlog(Integer.lowestOneBit(hand[2]));
		pc[2]= Agent21959462.binlog(Integer.lowestOneBit(hand[3]));

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

		return new int [] {sec, Agent21959462.binlog(Integer.lowestOneBit(hand[sec]))};
	} 

}

class State
{
	View current;
	int depth;
	int turn;
	int currentWinner;
	int score[];
	int left[];
	int right[];

	boolean agentSuits[];

	State children[] = null;
	int numberOfChildren = 0;

	State(View x, int depth ,int turn, int left[], int right[], int score[], int currentWinner)
	{
		this.left = left.clone();
		this.right = right.clone();
		this.currentWinner = currentWinner;
		this.current = new View(x);
		this.depth = depth;
		this.turn = turn;
		this.score = score.clone();
		this.agentSuits = new boolean[4];
		fixKnowledge(x.hand, this.agentSuits);
	}	

	//MINIMAX
	//Using Stored State Tree

	int [] max(State a, int scores[], int choice[], int removes[])
	{

		if(a.depth > 12 || a.current.handSize() < 2)
		{
			return scores;
		}

		if(a.current.tableSize() ==3)
		{
			scores[a.currentWinner]++;
			a.turn = a.currentWinner;
			a.current.resetTable();
		}
		int [] cards;
		boolean relativeOthers[][]= new boolean[2][4];;
		int maximum = Integer.MIN_VALUE;
		int maxC[] = new int[2];
		Arrays.fill(maxC, -1);

		int scoreSet[] = null;
		int returnScores[] = null;
		int tempScore[] = null;
		switch(a.turn)
		{

		case 0:
			cards = beatable(a.current.hand, a.current.grave, a.current.suit, a.current.table, a.current.otherSuits);

			for(int k = 0; k < 4; k++)
			{
				if(cards[k]!= -1)
				{
					View tempV = new View(a.current);
					tempV.addToTable(k, cards[k]);
					tempV.removeCard(k, cards[k]);
					int tempWinner= a.currentWinner;
					if(greaterThanTable(k, cards[k], a.current.table, a.current.suit))
						tempWinner = 0;
					tempScore= scores.clone();

					State tempS = new State(tempV, a.depth+1, (a.turn+4)%3, a.left, a.right, tempScore, tempWinner);
					
					returnScores = max(tempS, tempScore, null, removes);
					int maxOfOthers = returnScores [1]> returnScores[2]? returnScores [1] : returnScores [2];
					if(returnScores[0] - maxOfOthers - removes[0]> maximum)
					{
						maximum = returnScores[0] - maxOfOthers - removes[0];
						maxC = new int [] {k, cards[k]};
						scoreSet = returnScores.clone();
					}
				}
			}

			break;

		case 1:


			relativeOthers[0] = a.current.otherSuits[1];
			relativeOthers[1] = a.agentSuits;

			cards = beatable(a.left, a.current.grave, a.current.suit, a.current.table, relativeOthers);

			for(int k = 0; k < 4; k++)
			{
				if(cards[k]!= -1)
				{
					View tempV = new View(a.current);
					tempV.addToTable(k, cards[k]);
					tempV.addToGrave(k, cards[k]);
					int tempWinner= a.currentWinner;
					if(greaterThanTable(k, cards[k], a.current.table, a.current.suit))
						tempWinner = 1;
					//remove from hand 
					//DONE AFTER STATE CREATION

					//RemoveCard and add to graveYard
					tempScore= scores.clone();
					
					State tempS = new State(tempV, a.depth+1, (a.turn+4)%3, a.left, a.right, tempScore, tempWinner);

					tempS.left[k] = tempS.left[k] & ~ (1<<cards[k]);

					returnScores = max(tempS, tempScore, null, removes);
					int maxOfOthers = returnScores [0]> returnScores[2]? returnScores [0] : returnScores [2];
					if(returnScores[1] - maxOfOthers -removes[0]> maximum)
					{
						maximum = returnScores[1] -maxOfOthers -removes[1];
						maxC = new int [] {k, cards[k]};
						scoreSet = returnScores.clone();
					}
				}
			}


			break;

		case 2:


			relativeOthers[0] = a.agentSuits;
			relativeOthers[1] = a.current.otherSuits[1];

			cards = beatable(a.right, a.current.grave, a.current.suit, a.current.table, relativeOthers);

			for(int k = 0; k < 4; k++)
			{
				if(cards[k]!= -1)
				{
					View tempV = new View(a.current);
					tempV.addToTable(k, cards[k]);
					tempV.addToGrave(k, cards[k]);
					//remove from hand 
					//DONE AFTER STATE CREATION

					int tempWinner= a.currentWinner;
					if(greaterThanTable(k, cards[k], a.current.table, a.current.suit))
						tempWinner = 2;

					//RemoveCard and add to graveYard
					tempScore= scores.clone();
					State tempS = new State(tempV, a.depth+1, (a.turn+4)%3, a.left, a.right, tempScore, tempWinner);

					tempS.right[k] = tempS.right[k] & ~ (1<<cards[k]);

					returnScores = max(tempS, tempScore, null, removes);
					int maxOfOthers = returnScores [0]> returnScores[1]? returnScores [0] : returnScores [1];
					
					if(returnScores[2] - maxOfOthers - -removes[2]> maximum)
					{
						maximum = returnScores[2]- maxOfOthers - -removes[2];
						maxC = new int [] {k, cards[k]};
						scoreSet = returnScores.clone();
					}
				}
			}

			break;
		}
		if(choice != null)
		{
			if(choice[0] != -1)
				choice = maxC.clone();
		}
		if(scoreSet == null)
		{
			return scores;
		}
		return scoreSet;

	}
	/**
	 * Updates information about the player, such as if the player has the card or not
	 * @param handP hand to be read
	 * @param arr updates are fed to this array
	 */
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

	/**
	 * Checks if the card is bigger than all other cards on the table
	 * @param suit1 suit value
	 * @param card1 rank value
	 * @param table tableSet
	 * @param suit current suit
	 * @return true if bigger, false otherwise
	 */
	protected static boolean greaterThanTable(int suit1, int card1, int table[], int suit)
	{
		if(table[0]+table[1]+table[2]+table[3]==0)
			return true;

		if(suit1 == suit && suit1 !=0)
		{
			if(table[0] == 0)
			{
				return card1 > Agent21959462.binlog(Integer.highestOneBit(table[suit])) ? true : false;
			}
			else
			{
				return false;
			}

		}

		else if(suit1 == 0)
		{
			return card1 > Agent21959462.binlog(Integer.highestOneBit(table[suit])) ? true : false;

		}
		else
			return false;
	}

	/**
	 * Checks if cards in the hand can beat others
	 * @param hand hand of a player
	 * @param grave cards in the graveyard
	 * @param curSuit current Suit
	 * @param tableB cards on the table
	 * @param otherSuits state of other player's suits, i.e. if they're out or not
	 * @return the cards of each suit that the agent should play
	 */
	static int [] beatable(int[] hand, int [] grave, int curSuit, int[] tableB, boolean otherSuits[][])
	{
		int sizeT = Integer.bitCount(tableB[0])+Integer.bitCount(tableB[1])+Integer.bitCount(tableB[2])+Integer.bitCount(tableB[3]);
		//Both cards on table

		int [] returnStuff = new int[4];

		Arrays.fill(returnStuff, -1);

		if(sizeT == 2)
		{
			//play a card bigger than both

			if(hand[curSuit] == 0)
			{

				int start = Agent21959462.binlog( Integer.highestOneBit(tableB[0]) );

				int found = -1;
				for(int i = start; i < 13; i++)
				{
					if( ((1 << i) & hand[0]) != 0)
					{
						found = i;
						returnStuff[0] = i;
						break;
					}
				}

				//If beatable play the spade
				if(found != -1)
				{
					return returnStuff;
				}

				else
				{
					for(int k = 0; k < 4; k++)
						returnStuff[k] = Agent21959462.binlog(Integer.lowestOneBit(hand[k]));
					return returnStuff;
				}
			}

			else
			{

				if(tableB[0] == 0 || curSuit == 0)
				{
					int start = Agent21959462.binlog(Integer.highestOneBit(tableB[curSuit]));
					int found = -1;
					for(int i = start; i < 13; i++)
					{
						if( ((1 << i) & hand[curSuit]) != 0 )
						{
							found = i;
							returnStuff[curSuit] = i;
							break;
						}
					}

					//if beatable throw card
					if(found != -1)
					{
						return returnStuff;
					}
					//play trash card of the same deck
					else
					{
						returnStuff[curSuit] = Agent21959462.binlog(Integer.lowestOneBit(hand[curSuit]));
						return returnStuff;
					}
				}
				else
				{
					returnStuff[curSuit] = Agent21959462.binlog(Integer.lowestOneBit(hand[curSuit]));
					return returnStuff;
				}
			}
		}

		if(sizeT==0)
		{

			int found = -1;
			for(int k = 0; k < 4; k++)
			{
				int tempF = -1;
				if(!((otherSuits[0][k] == true && otherSuits[0][0] == false) || (otherSuits[1][k] == true && otherSuits[1][0] == false)))
				{
					//look for unbeatable card if found play
					//else play trash

					//highest card
					int start = Agent21959462.binlog(Integer.highestOneBit((~grave[k]&~hand[k])&Agent21959462.THIRTEEN));
					for(int i = start; i < 13; i++)
					{
						if( ((1 << i) & hand[k]) != 0)
						{
							tempF = i;
							found = -1;
							break;
						}
					}
					//If beatable play
					if(tempF != -1)
					{
						returnStuff[k] = tempF;
					}
				}
			}

			if(found != -1)
			{
				return returnStuff;
			}
			else
			{
				for(int k = 0; k < 4; k++)
					returnStuff[k] = Agent21959462.binlog(Integer.lowestOneBit(hand[k]));
				return returnStuff;
			}
		}

		//next player
		//out of current hand but has spades

		if(curSuit != 0 && otherSuits[1][curSuit] == true && otherSuits[1][0] == false)
		{
			//Check if out of cards of the required deck
			if(hand[curSuit] == 0 || curSuit == 0)
			{

				//Check if have any spades if not throw a trashy card of any deck but spades
				if(hand[0] == 0)
				{

					for(int k = 0; k < 4; k++)
						returnStuff[k] = Agent21959462.binlog(Integer.lowestOneBit(hand[k]));
					return returnStuff;
				}

				//check if possible to beat with spades
				int start = Agent21959462.binlog(Integer.highestOneBit( tableB[0] | ((~grave[0] &~hand[0] & Agent21959462.THIRTEEN))));

				int found = -1;
				for(int i = start; i < 13; i++)
				{
					if( ((1 << i) & hand[0]) != 0)
					{
						found = i;
						returnStuff[0] = i;
						break;
					}
				}

				//If beatable play the spade
				if(found != -1)
				{
					return returnStuff;
				}
				//else throw a trashy card of any deck but spades
				else
				{
					//TOD
					for(int k = 0; k < 4; k++)
						returnStuff[k] = Agent21959462.binlog(Integer.lowestOneBit(hand[k]));
					return returnStuff;

				}
			}
			//Throw a bad card of the deck since it can't win
			else
			{

				returnStuff[curSuit]= Agent21959462.binlog(Integer.lowestOneBit(hand[curSuit]));
				return returnStuff;
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
					for(int k = 0; k < 4; k++)
						returnStuff[k] = Agent21959462.binlog(Integer.lowestOneBit(hand[k]));
					return returnStuff;
				}
				else
				{
					returnStuff[0] =Agent21959462.binlog(Integer.lowestOneBit(hand[0]));
					return returnStuff;
				}
			}
			//checks if beatable
			else
			{

				int start = Agent21959462.binlog(Integer.highestOneBit(tableB[curSuit] | ((~grave[curSuit] & ~hand[curSuit]) & Agent21959462.THIRTEEN)));
				int found = -1;
				for(int i = start; i < 13; i++)
				{
					if( ((1 << i) & hand[curSuit]) != 0 )
					{
						found = i;
						returnStuff[curSuit] = i;
						break;
					}
				}

				//if beatable throw card
				if(found != -1)
				{
					return returnStuff;
				}
				//play trash card of the same deck
				else
				{
					returnStuff[curSuit] = Agent21959462.binlog(Integer.lowestOneBit(hand[curSuit]));
					return returnStuff;
				}
			}
		}

	}
}

/**
 * This class holds information about everything required for out agents in a single round
 * It store hands, grave, table data as bits. Stores info about the card status, position of the agent,
 * current suit
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
		hand[suit] = ((hand[suit]&(~(1<< value))) & Agent21959462.THIRTEEN);
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
			if((grave[i]|hand[i]) == Agent21959462.THIRTEEN)
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
		return Agent21959462.binlog(Integer.highestOneBit(i));
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
	
	/**
	 * checks if card 1 is bigger than card 2
	 * @param suit1 Card1's suit
	 * @param card1 Card1's rank
	 * @param suit2 Card2's suit
	 * @param card2 Card2's rank
	 * @param suit Default suit of the table
	 * @return true if card 1 is greater, false otherwise
	 */
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
	/**
	 * Checks if the card to play is legal
	 * @param hand Agent's hand
	 * @param table table of the game
	 * @param suit1 Suit to play
	 * @param card1 Rank to play
	 * @param suit default suit
	 * @return true if legal, false otherwise
	 */
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
