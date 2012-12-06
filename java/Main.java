package chat.bot;

import java.util.Scanner;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Scanner input = new Scanner(System.in);
		Dict dict = new Dict();
		
		boolean check = true;
		while (check)
		{
			String masukan = input.nextLine();
			System.out.println(masukan);
			if (masukan.equals("quit"))
				check = false;
			else
				System.out.println(dict.CheckAnswer(masukan.toLowerCase(), 0));
		}
		System.out.println("Program exited.");
		input.close();
	}

}
