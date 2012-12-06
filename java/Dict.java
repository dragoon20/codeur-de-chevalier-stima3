package chat.bot;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

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
			int total_kata = 0;
			int tempmap[][] = new int[kumpulan.length][];
			for (int i=0;i<kumpulan.length;++i)
			{
				total_kata += kumpulan[i].length();
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
			boolean supercheck = true;
			for(String dict : Answer.keySet())
			{
				int total = 0;
				int total_banding = 0;
				String [] temp2 = dict.split(" ");
				for (int i=0;i<temp2.length;++i)
					total_banding += temp2[i].length();
				for (int i=0;i<kumpulan.length;++i)
				{
					int m = 0;
					int j = 0;
					int max = 0;
					boolean check = true;
					
					if (kumpulan[i].length() > 0)
					{
						while ((check) && (j < dict.length()))
						{
							if(kumpulan[i].charAt(m)==dict.charAt(j))
							{
								j++;
						        m++;
							}
							if (m == kumpulan[i].length())
						    {
						        max = kumpulan[i].length();
						        check = false;
						    }
							else if ((j < dict.length()) && (kumpulan[i].charAt(m)!=dict.charAt(j)))
							{
								if (m != 0)
									m = tempmap[i][m-1];
								else
									j = j+1;
							}
						}
						total += max;
					}
				}
				if (((double)total/(double)total_banding) > (double)0.9)
				{
					if (total_kata <= total_banding)
					{
						result.put(dict, new Double(((double)total/(double)total_banding)));
						supercheck = false;
					}
				}
				else if ((supercheck) && (((double)total/(double)total_banding) > (double)0.5))
				{
					if (total_kata <= total_banding)
					{
						result.put(dict, new Double(((double)total/(double)total_banding)));
					}
				}
			}
		}
		else if (flag==1)
		{
			// Boyer Moore
			int total_kata = 0;
			int tempmap[][] = new int[kumpulan.length][];
			int tempmap2[][] = new int[kumpulan.length][];
			
			// creating Boyer Moore char table
			for (int i=0;i<kumpulan.length;++i)
			{
				total_kata += kumpulan[i].length();
				tempmap[i] = new int[256];
				for (int j=0;j<tempmap[i].length;++j)
					tempmap[i][j] = kumpulan[i].length();
				for (int j=0;j<kumpulan[i].length() - 1;++j)
					tempmap[i][kumpulan[i].charAt(j)] = kumpulan[i].length() - 1 - j;
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
			
			boolean supercheck = true;
			for(String dict : Answer.keySet())
			{
				int total_banding = 0;
				int total = 0;
				String [] temp2 = dict.split(" ");
				for (int i=0;i<temp2.length;++i)
					total_banding += temp2[i].length();
				for (int i=0;i<kumpulan.length;++i)
				{
					int max = 0;
			    	boolean check = true;
					int j = kumpulan[i].length() - 1, k; 
					while ((check)&&(j < dict.length()))
				    {
				    	k = kumpulan[i].length() - 1;
						while ((check) && (kumpulan[i].charAt(k) == dict.charAt(j)))
				    	{
				    		if (k == 0)
				    			check = false;
				    		--j;
				    		--k;
				    	}
						// i += needle.length - j; // For naive method
						if (check)
						{
							j += Math.max(tempmap2[i][kumpulan[i].length() - 1 - k], tempmap[i][dict.charAt(j)]);
						}
						else
							max = kumpulan[i].length();
				    }
					total += max;
				}

				if (((double)total/(double)total_banding) > (double)0.9)
				{
					if (total_kata <= total_banding)
					{
						supercheck = false;
						result.put(dict, new Double(((double)total/(double)total_banding)));
					}
				}
				else if ((supercheck) && (((double)total/(double)total_banding) > (double)0.5))
				{
					if (total_kata <= total_banding)
					{
						result.put(dict, new Double(((double)total/(double)total_banding)));
					}
				}
			}
		}
		
		List<Map.Entry<String, Double>> finalresult = new ArrayList<Map.Entry<String,Double>>();
		// Returning result
		finalresult = sortHashMap(result);
		
		StringBuilder output = new StringBuilder();
		if (finalresult.size() == 1)
		{
			for (Map.Entry<String, Double> temps: finalresult)
				output.append(Answer.get(temps.getKey()));
		}
		else if (finalresult.size() > 1)
		{
			int count = 0;
			while ((count < finalresult.size()) && (count<3))
			{
				Map.Entry<String, Double> pairs = finalresult.get(count);
				if (pairs.getValue().equals((double)1))
				{
					output.append(Answer.get((String)pairs.getKey()));
					count = 3;
				}
				else
				{
					output.append((String)pairs.getKey());
					output.append("<br />");
					count++;
				}
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
				Answer.put(scan.nextLine().toLowerCase(), scan.nextLine().toLowerCase());
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
				Synonym.put(scan.nextLine().toLowerCase(), scan.nextLine().toLowerCase());
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
				Stop_Words.add(scan.nextLine().toLowerCase());
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
	
	private List<Map.Entry<String, Double>> sortHashMap(HashMap<String, Double> input)
	{
		List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(input.entrySet());
	    
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {

			@Override
			public int compare(Entry<String, Double> arg0,
					Entry<String, Double> arg1) {
				// TODO Auto-generated method stub
				return arg1.getValue().compareTo(arg0.getValue());
			}
		});

	    return list;
	}
}

