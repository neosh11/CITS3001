package mosssidewhist;

import java.util.*;
import java.util.Map.Entry;


public class BitAgent3 implements MSWAgent  {

	public static final int LEADER = 0;
	public static final int LEFT = 1;
	public static final int RIGHT = 2;
	public static final int THIRTEEN= 0b1111111111111;
	
	public static long maxTime = 0;

	public static final String NAME = "DirtyBit";

	private String leftAgent;
	private String rightAgent;

	private int scores[];
	private int removal[];
	private int currentWinner;

	View current;

	public BitAgent3() {

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
			current.addCard(hand.get(i));

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

		long startTime = System.currentTimeMillis();
		
		Card toRet = beatable(current, currentWinner);
		current.removeCard(suitVal(toRet), toRet.rank-2);

		System.out.println("TIME FOR PLAY CARD: "+ (System.currentTimeMillis()-startTime));
		if(maxTime < System.currentTimeMillis()-startTime)
		{
			maxTime = System.currentTimeMillis()-startTime;
		}
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


		if(State.greaterThanTable(suitVal(card), card.rank-2, current.table, current.suit))
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
		int countAssociated = 100;
		for(int i = 0; i < 3; i++)
		{

			if(pc[i]< min && pc[i] != -1)
			{
				sec = i+1;
				min = pc[i];
				countAssociated = Integer.bitCount(hand[i]);
			}
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


	/**
	 * 
	 * @param X
	 * @param currentWin
	 * @return
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

class Simulator2
{
	private static Random random = new Random();
	public static final int THIRTEEN= 0b1111111111111;


	static boolean constructBoth(int leftOvers[], int left[], int leftVal, int right[], int rightVal, boolean otherSuits[][], boolean leader)
	{



		int count = 0;
		int oldLCount = 0;
		int lcount, rcount;
		lcount = rcount = 0;

		boolean retry = false;



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
		return !retry;

	}

	//TODO FIX THIS, LOTS OF NULL RETURNS FOR SOME REASON
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
			System.out.println("NULL SCORES");
			System.exit(1);
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

			tempS.max(tempS, tempS.score, choice);


			Card tempC = BitAgent3.retCard(choice[0], choice[1]);
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
			return BitAgent3.retCard(ar[0], ar[1]);
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
					System.out.print(BitAgent3.retCard(i, j).toString() +" ");
				}
			}
		}
		System.out.println();
	}


	private static int [] removeWorstHand(int hand[])
	{

		int pc [] = new int[3];

		pc[0]= BitAgent3.binlog(Integer.lowestOneBit(hand[1]));
		pc[1]= BitAgent3.binlog(Integer.lowestOneBit(hand[2]));
		pc[2]= BitAgent3.binlog(Integer.lowestOneBit(hand[3]));

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

		return new int [] {sec, BitAgent3.binlog(Integer.lowestOneBit(hand[sec]))};
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

	int [] max(State a, int scores[], int choice[])
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
					
					returnScores = max(tempS, tempScore, null);
					if(returnScores[0] > maximum)
					{
						maximum = returnScores[0];
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

					returnScores = max(tempS, tempScore, null);
					if(returnScores[1] > maximum)
					{
						maximum = returnScores[1];
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

					returnScores = max(tempS, tempScore, null);
					
					if(returnScores[2] > maximum)
					{
						maximum = returnScores[2];
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
	 * 
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

	protected static boolean greaterThanTable(int suit1, int card1, int table[], int suit)
	{
		if(table[0]+table[1]+table[2]+table[3]==0)
			return true;

		if(suit1 == suit && suit1 !=0)
		{
			if(table[0] == 0)
			{
				return card1 > BitAgent3.binlog(Integer.highestOneBit(table[suit])) ? true : false;
			}
			else
			{
				return false;
			}

		}

		else if(suit1 == 0)
		{
			return card1 > BitAgent3.binlog(Integer.highestOneBit(table[suit])) ? true : false;

		}
		else
			return false;
	}

	
	//TODO CHECK INNER WORKING 
	//NEED TO CONFIRM IF IT'S WORKING PROPERLY
	/**
	 * 
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

				int start = BitAgent3.binlog( Integer.highestOneBit(tableB[0]) );

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
					//TODO REMOVE WORST CARD
					for(int k = 0; k < 4; k++)
						returnStuff[k] = BitAgent3.binlog(Integer.lowestOneBit(hand[k]));
					return returnStuff;
				}
			}

			else
			{

				if(tableB[0] == 0 || curSuit == 0)
				{
					int start = BitAgent3.binlog(Integer.highestOneBit(tableB[curSuit]));
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
						returnStuff[curSuit] = BitAgent3.binlog(Integer.lowestOneBit(hand[curSuit]));
						return returnStuff;
					}
				}
				else
				{
					returnStuff[curSuit] = BitAgent3.binlog(Integer.lowestOneBit(hand[curSuit]));
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
					int start = BitAgent3.binlog(Integer.highestOneBit(((~grave[k] &0b1111111111111)&(~hand[k] &0b1111111111111))));
					for(int i = start; i < 13; i++)
					{
						if( ((1 << i) & hand[k]) != 0)
						{
							tempF = i;
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
				//TODO REMOVE WORST CARD
				for(int k = 0; k < 4; k++)
					returnStuff[k] = BitAgent3.binlog(Integer.lowestOneBit(hand[k]));
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
					//TODO REMOVE WORST CARD

					for(int k = 0; k < 4; k++)
						returnStuff[k] = BitAgent3.binlog(Integer.lowestOneBit(hand[k]));
					return returnStuff;
				}

				//check if possible to beat with spades
				int start = BitAgent3.binlog(Integer.highestOneBit( tableB[0] | ((~grave[0] &0b1111111111111)&(~hand[0] &0b1111111111111))));

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
						returnStuff[k] = BitAgent3.binlog(Integer.lowestOneBit(hand[k]));
					return returnStuff;

				}
			}
			//Throw a bad card of the deck since it can't win
			else
			{

				returnStuff[curSuit]= BitAgent3.binlog(Integer.lowestOneBit(hand[curSuit]));
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
					//TODO
					for(int k = 0; k < 4; k++)
						returnStuff[k] = BitAgent3.binlog(Integer.lowestOneBit(hand[k]));
					return returnStuff;
				}
				else
				{
					returnStuff[0] =BitAgent3.binlog(Integer.lowestOneBit(hand[0]));
					return returnStuff;
				}
			}
			//checks if beatable
			else
			{

				int start = BitAgent3.binlog(Integer.highestOneBit(tableB[curSuit] | ((~grave[curSuit] &0b1111111111111)&(~hand[curSuit] &0b1111111111111))));
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
					returnStuff[curSuit] = BitAgent3.binlog(Integer.lowestOneBit(hand[curSuit]));
					return returnStuff;
				}
			}
		}

	}
}

class View
{

	int hand[];
	int grave[];
	int position;

	//FALSE IF THEY HAVE THE CARD
	boolean otherSuits[][];
	int table[];
	int suit;
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
		hand[suit] = ((hand[suit]&(~(1<< value))) & BitAgent3.THIRTEEN);
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
			if((grave[i]|hand[i]) == BitAgent3.THIRTEEN)
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
		if(tableSize() == 0)
		{
			this.suit = suit;
		}
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
		return BitAgent3.binlog(Integer.highestOneBit(i));
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
