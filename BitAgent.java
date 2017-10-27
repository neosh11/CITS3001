package mosssidewhist;

import java.util.*;

public class BitAgent implements MSWAgent  {

	public static final int LEADER = 0;
	public static final int LEFT = 1;
	public static final int RIGHT = 2;

	public static final int THIRTEEN= 0b1111111111111;

	private int hand[];
	private int grave[];
	private int tableB[];

	private int tricks;

	//>0 IF THEY HAVE THE CARD
	//-1 for no card
	//Sets upperbounds on players
	public int otherSuits[][];

	public ArrayList<Card> table;


	private String leftAgent;
	private String rightAgent;


	public BitAgent() {

		table = new ArrayList<Card>();
		tricks = 0;
	}

	@Override
	public void setup(String agentLeft, String agentRight) {
		leftAgent = agentLeft;
		rightAgent = agentRight;
	}

	@Override
	public void seeHand(List<Card> hand, int order) {

		this.hand = new int[4];
		this.grave = new int[4];
		this.otherSuits = new int[2][4];
		tableB = new int [4];


		for(int i = 0; i < hand.size(); i++)
		{
			Card c = hand.get(i);
			bitHand(this.hand, c);
		}

		for(int i = 0; i< 4; i++)
		{
			otherSuits[0][i] = binlog(Integer.highestOneBit((~grave[i]& ~this.hand[i] & 0b1111111111111)));
			otherSuits[1][i] = otherSuits[0][i];
		}
	}


	@Override
	public Card[] discard() {
		
		Card[] discard = new Card[4];
		for(int i = 0; i<4;i++)
		{
			int indexHand[] = removeWorstHand(hand);
			int remove = 1 << indexHand[1];
			hand[indexHand[0]] = hand[indexHand[0]]&(~remove&0b1111111111111);
			grave[indexHand[0]] = grave[indexHand[0]]|(remove);
			discard[i] = retCard(indexHand[0], binlog(remove));
		}
		return discard;
	}

	@Override
	public Card playCard() {



		int currentSuit = 0;
		if(!table.isEmpty())
			currentSuit = suitVal(table.get(0));


		int ar [] = beatable(hand, grave, currentSuit, table, otherSuits);

		hand[ar[0]] = hand[ar[0]]&(~(1<< ar[1])&0b1111111111111);
		grave[ar[0]] = grave[ar[0]]|(1<< ar[1]);

		//table.add(retCard(ar[0], ar[1]));
		return retCard(ar[0], ar[1]);

	}



	@Override
	public void seeCard(Card card, String agent) {

		if(table.size() >= 1)
		{
			if(!card.suit.equals(table.get(0).suit))
			{
				if(agent.equals(leftAgent))
				{
					otherSuits[0][(suitVal(table.get(0)))] = -1;
				}
				else if(agent.equals(rightAgent))
				{
					otherSuits[1][(suitVal(table.get(0)))] = -1;
				}
			}
		}
		
		grave[suitVal(card)] = grave[suitVal(card)] | (1 << card.rank-2);

		for(int i =0; i < 4; i++)
		{
			if((this.grave[i]|this.hand[i]) == (int) (Math.pow(2, 13)-1))
			{
				otherSuits[1][i] = -1;
				otherSuits[0][i] = -1;
			}
		}

		table.add(card);
		tableB[suitVal(card)] = tableB[suitVal(card)] | (1 << (card.rank-2));
	}

	//Ran once the trick has been won
	//Cards on table removed
	@Override
	public void seeResult(String winner) {

		tricks = (tricks+1)%16;

		if(tricks == 0)
		{
			grave = new int[4];
		}

		table.clear();
	}

	@Override
	public void seeScore(Map<String, Integer> scoreboard) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String sayName() {

		return "47Bit";
	}

	/**
	 * Function that returns which Suit the worst Card is in 
	 * @param hand Hand to be passed
	 * @return Suit where the bad card lies
	 */
	private int [] removeWorstHand(int hand[])
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

		return new int [] {sec, binlog(Integer.lowestOneBit(hand[sec]))};
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

	private int[] beatable(int[] hand, int [] grave, int curSuit, ArrayList<Card> table, int otherSuits[][])
	{
		int tableB[] = new int[4];
		for(Card c : table)
		{
			bitHand(tableB, c);
		}
		//Both cards on table
		if(table.size() == 2)
		{
			//play a card bigger than both

			if(hand[curSuit] == 0)
			{

				int start = binlog( Integer.highestOneBit(tableB[0]) );

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
					int start = binlog(Integer.highestOneBit(tableB[curSuit]));
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
						return new int[] {curSuit, binlog(Integer.lowestOneBit(hand[curSuit]))};
					}
				}
				else
				{
					return new int[] {curSuit, binlog(Integer.lowestOneBit(hand[curSuit]))};
				}
			}
		}

		if(table.isEmpty())
		{

			//System.out.println("HELLO");
			for(int k = 1; k < 4; k++)
			{
				if(!((otherSuits[0][k] == -1 && otherSuits[0][0] != -1) || (otherSuits[1][k] == -1 && otherSuits[1][0] != -1)))
				{
					//look for unbeatable card if found play
					//else play trash

					//highest card
					int start = binlog(Integer.highestOneBit(((~grave[k] &0b1111111111111)&(~hand[k] &0b1111111111111))));
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
			int start = binlog(Integer.highestOneBit(((~grave[0] &0b1111111111111)&(~hand[0] &0b1111111111111))));
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
		if(curSuit != 0 && otherSuits[1][curSuit] == -1 && otherSuits[1][0] != -1)
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
				int start = binlog(Integer.highestOneBit( tableB[0] | ((~grave[0] &0b1111111111111)&(~hand[0] &0b1111111111111))));

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
				return new int[] {curSuit, binlog(Integer.lowestOneBit(hand[curSuit]))};
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
					return new int[] {0, binlog(Integer.lowestOneBit(hand[0]))};
				}
			}
			//checks if beatable
			else
			{

				int start = binlog(Integer.highestOneBit(tableB[curSuit] | ((~grave[curSuit] &0b1111111111111)&(~hand[curSuit] &0b1111111111111))));
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
					return new int[] {curSuit, binlog(Integer.lowestOneBit(hand[curSuit]))};
				}
			}
		}

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
		if(bits == 0)
		{
			return -1;
		}
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
				if((hand[i] & (1 << j)) != 0)
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
				if((grave[i] & (1 << j)) != 0)
				{
					System.out.print(retCard(i, j).toString() +" ");
				}
			}
		}
		System.out.println();
	}

	public void bitHand(int hand[], Card c)
	{
		switch(c.suit)
		{
		case SPADES: hand[0] = (hand[0]|(1 << c.rank-2)); break;
		case CLUBS: hand[1] = (hand[1]|(1 << c.rank-2)); break;
		case DIAMONDS: hand[2] = (hand[2]|(1 << c.rank-2)); break;
		case HEARTS: hand[3] = (hand[3]|(1 << c.rank-2)); break;
		}
	}
}
