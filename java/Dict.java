package chat.bot;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeSet;

public class Dict {

	private HashMap<String, String> Synonym;
	private HashMap<String, String> Answer;
	private ArrayList<String> Stop_Words;
	private final String answerfile = "Answer.txt";
	private final String synonymfile = "Synonym.txt";
	private final String stopwordsfile = "Stop.txt";
	
	public Dict()
	{
		LoadSynonym();
		LoadAnswer();
		LoadStopWords();
	}
	
	public String CheckAnswer(String s, int flag)
	{
		String kumpulan[] = s.split(" ");
		for (int i=0;i<kumpulan.length;++i)
		{
			boolean check = true;
			int j = 0;
			while ((check)&&(j<Stop_Words.size()))
			{
				if (kumpulan[i]==Stop_Words.get(j))
					check = false;
				j++;
			}
			if (!check)
				kumpulan[i] = "";
			else if (Synonym.containsKey(kumpulan[i]))
				kumpulan[i] = Synonym.get(kumpulan[i]);
		}
		
		HashMap<String, Double> result = new HashMap<>();
		
		if (flag==0)
		{
			// KMP
			// creating KMP table
			int total_banding = 0;
			int tempmap[][] = new int[kumpulan.length][];
			for (int i=0;i<kumpulan.length;++i)
			{
				total_banding += kumpulan[i].length();
				tempmap[i] = new int[kumpulan[i].length()];
				if (kumpulan[i].length() > 0)
				{
					tempmap[i][0] = 0;
					int cnd = 0;
					int j = 1;
					while (j<kumpulan[i].length())
					{
						if (kumpulan[i].charAt(j) == kumpulan[i].charAt(cnd))
						{
							tempmap[i][j] = cnd + 1;
							cnd = cnd + 1;
							++j;
						}
						else if (cnd > 0)
							cnd = tempmap[i][cnd-1];
						else if (cnd == 0)
						{
							tempmap[i][j] = 0;
							++j;
						}
					}
				}
			}
			for(String dict : Answer.keySet())
			{
				int total = 0;
				for (int i=0;i<kumpulan.length;++i)
				{
					int m = 0;
					int j = 0;
					int max = 0;
					boolean check = true;
					while ((check) && (m+j < dict.length()))
					{
						if (kumpulan[i].charAt(j)==dict.charAt(m+j))
						{
							if (j==dict.length()-1)
							{
								max = dict.length();
								check = false;
							}
							j++;
						}
						else
						{
							if (j > max)
								max = j;
							m = m + j - tempmap[i][j];
							j = tempmap[i][j];
						}
					}
					total += max;
				}
				if (((double)total/(double)total_banding) > (double)0.5)
				{
					result.put(dict, new Double(((double)total/(double)total_banding)));
				}
			}
		}
		else if (flag==1)
		{
			// Boyer Moore
			int total_banding = 0;
			int tempmap[][] = new int[kumpulan.length][];
			int tempmap2[][] = new int[kumpulan.length][];
			
			// creating Boyer Moore char table
			for (int i=0;i<kumpulan.length;++i)
			{
				total_banding += kumpulan[i].length();
				tempmap[i] = new int[256];
				for (int j=0;j<tempmap[i].length;++j)
					tempmap[i][j] = kumpulan[i].length();
				for (int j=0;j<kumpulan[i].length() - 1;++j)
					tempmap[i][kumpulan[i].charAt(j)] = kumpulan[i].length() - 1 - i;
			}
			
			// creating Boyer Moore scan offset table
			for (int i=0;i<kumpulan.length;++i)
			{
				if (kumpulan[i].length() > 0)
				{
					tempmap2[i] = new int[kumpulan[i].length()];
					int last_prefix = kumpulan[i].length();
					for (int j=(kumpulan[i].length() -1);j>=0;--j)
					{
						boolean check = true;
						int k = i + 1, l = 0;
						while ((check)&&(k<kumpulan[i].length()))
						{
							if (kumpulan[i].charAt(k)!=kumpulan[i].charAt(l)) 
								check = false;
							++l;
							++k;
						}
						if (check)
							last_prefix = j + 1;
						tempmap2[i][kumpulan[i].length() - 1 - j] = last_prefix - j + kumpulan[i].length() - 1;
					}
					for (int j=0;j<kumpulan[i].length() - 1;++j)
					{
						int slen = 0;
					    for (int k = j, l = kumpulan[i].length() - 1;k >= 0 && kumpulan[i].charAt(k) == kumpulan[i].charAt(l); --k, --l) 
					    	slen += 1;
					    tempmap2[i][slen] = kumpulan[i].length() - 1 - i + slen;
					}
				}
			}
			
			for(String dict : Answer.keySet())
			{
				int total = 0;
				for (int i=0;i<kumpulan.length;++i)
				{
					int max = 0;
					for (int j = kumpulan[i].length() - 1, k; j < dict.length();) 
				    {
				    	boolean check = true;
				    	k = kumpulan[i].length() - 1;
						while ((check)&&(kumpulan[i].charAt(k) == dict.charAt(j)))
				    	{
				    		if (k == 0)
				    			check = false;
				    		// i += needle.length - j; // For naive method
				    		j += Math.max(tempmap2[i][kumpulan[i].length() - 1 - k], tempmap[i][dict.charAt(j)]);
				    		--j;
				    		--k;
				    	}
						if (kumpulan[i].length() - 1 - k > max)
							max = kumpulan[i].length() - 1 - k;
				    }
					total += max;
				}
				if (((double)total/(double)total_banding) > (double)0.5)
				{
					result.put(dict, new Double(((double)total/(double)total_banding)));
				}
			}
		}
		
		// Returning result
		if (result.size() > 3)
			result = sortHashMap(result);
		
		StringBuilder output = new StringBuilder();
		if (result.size() == 1)
		{
			for (String temps: result.keySet())
				output.append(Answer.get(temps));
		}
		else
		{
			for (String temps: result.keySet())
			{
				output.append(temps);
				output.append("<br />");
			}
		}
		return output.toString();
	}
	
	public void LoadAnswer()
	{
		Answer = new HashMap<String, String>();
		FileInputStream fis;
		try {
			fis = new FileInputStream(answerfile);
			Scanner scan = new Scanner(fis, "UTF-8");
			
			while (scan.hasNextLine())
			{
				Synonym.put(scan.nextLine(), scan.nextLine());
			}
			
			scan.close();
			fis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void LoadSynonym()
	{
		Synonym = new HashMap<String, String>();
		FileInputStream fis;
		try {
			fis = new FileInputStream(synonymfile);
			Scanner scan = new Scanner(fis, "UTF-8");

			while (scan.hasNextLine())
			{
				Answer.put(scan.nextLine(), scan.nextLine());
			}
			
			scan.close();
			fis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void LoadStopWords()
	{
		Stop_Words = new ArrayList<String>();
		FileInputStream fis;
		try {
			fis = new FileInputStream(stopwordsfile);
			Scanner scan = new Scanner(fis, "UTF-8");

			while (scan.hasNextLine())
			{
				Stop_Words.add(scan.nextLine());
			}
			
			scan.close();
			fis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private HashMap<String, Double> sortHashMap(HashMap<String, Double> input)
	{
	    Map<String, Double> tempMap = new HashMap<String, Double>();
	    for (String wsState : input.keySet())
	    {
	        tempMap.put(wsState,input.get(wsState));
	    }

	    List<String> mapKeys = new ArrayList<String>(tempMap.keySet());
	    List<Double> mapValues = new ArrayList<Double>(tempMap.values());
	    HashMap<String, Double> sortedMap = new LinkedHashMap<String, Double>();
	    TreeSet<Double> sortedSet = new TreeSet<Double>(mapValues);
	    Object[] sortedArray = sortedSet.descendingSet().toArray();
	    int size = sortedArray.length;
	    for (int i=0; i<size; i++)
	    {
	        sortedMap.put(mapKeys.get(mapValues.indexOf(sortedArray[i])), (Double)sortedArray[i]);
	    }
	    return sortedMap;
	}
}
