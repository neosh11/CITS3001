package mosssidewhist;

import java.util.*;
import java.util.Map;

public class ProjectAgent implements MSWAgent  {

	public static final int LEADER = 0;
	public static final int LEFT = 1;
	public static final int RIGHT = 2;

	private ArrayList<Card> hand;
	private ArrayList<Integer> priorityH;
	
	public ProjectAgent() {
		
		priorityH = new ArrayList<Integer>();
	}

	@Override
	public void setup(String agentLeft, String agentRight) {
		// TODO Auto-generated method stub

	}

	@Override
	public void seeHand(List<Card> hand, int order) {
		this.hand  = (ArrayList<Card>) hand;
	}

	@Override
	public Card[] discard() {
		
		if(priorityH.isEmpty())
		{
			ass(hand, priorityH);
		}
		Card[] discard = new Card[4];
		for(int i = 0; i<4;i++)
		{
			int min = 1000;
			int index = 0;
			
			for(int d = 0; d < hand.size(); d++)
			{
				if(priorityH.get(d)< min)
				{
					min = priorityH.get(d);
					index = d;
				}
			}
			
			discard[i] = hand.remove(index);
			priorityH.remove(index);
		}
		
		return discard;

	}

	@Override
	public Card playCard() {
		if(priorityH.isEmpty())
		{
			ass(hand, priorityH);
		}
		int max = -1000;
		int index = 0;
		
		for(int d = 0; d < priorityH.size(); d++)
		{
			if(priorityH.get(d) > max)
			{
				max = priorityH.get(d);
				index = d;
			}
		}
		
		priorityH.remove(index);
		
		return hand.remove(index);
	}


	@Override
	public void seeCard(Card card, String agent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void seeResult(String winner) {
		// TODO Auto-generated method stub

	}

	@Override
	public void seeScore(Map<String, Integer> scoreboard) {
		// TODO Auto-generated method stub

	}

	@Override
	public String sayName() {
		
		return "47";
	}
	
	private boolean ass(ArrayList<Card> hand, ArrayList<Integer> prio)
	{
		
		//HEARTS, CLUBS, DIAMONDS, SPADES
		
		int pc [][] = new int[3][2];

		
		for(int i = 0; i < hand.size(); i++)
		{
			switch(hand.get(i).suit)
			{
			case HEARTS: pc[0][0]++; break;
			case CLUBS: pc[1][0]++; break;
			case DIAMONDS: pc[2][0]++; break;
			default: break;
			}
		}
		
		int max = 0;
		int sec = 0;
		
		for(int y = 0; y < 3; y++)
		{
			if(pc[y][0]> max)
			{
				max = pc[y][0];
				pc[y][1] = 0;
				continue;
			}
			
			else if(pc[y][0] > sec)
			{
				sec = pc[y][0];
				pc[y][1] = 13;
				continue;
			}
			else
			{
				pc[y][1] = 13*2;
			}			
		}
		
		for(int i = 0; i < hand.size(); i++)
		{
			switch(hand.get(i).suit)
			{
			
			case HEARTS: prio.add(hand.get(i).rank + pc[0][1]); break;
			case CLUBS: prio.add(hand.get(i).rank + pc[1][1]); break;
			case DIAMONDS:  prio.add(hand.get(i).rank+pc[2][1]); break;
			case SPADES: prio.add(hand.get(i).rank + 13*3); break;
			default: break;
			
			}
		}
		return true;
	}

}
