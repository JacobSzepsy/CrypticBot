import java.awt.Checkbox;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.cert.CertPathChecker;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.jibble.pircbot.PircBot;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;


//TODO increase speed
//TODO audio files can be mp3
//TODO fix volumes for audio clips (individual volume sliders?)
//TODO implement system to read all data from google sheet
//TODO allow multi lines for custom commands
//TODO add customizable help command
//TODO consider method of checking if this is the first message a user has sent based on if stream is up instead of when bot starts?
public class Bot extends PircBot { 

	private static final String APPLICATION_NAME = "TwitchBot";
	private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".credentials/pointcounter");
	private static FileDataStoreFactory DATA_STORE_FACTORY;
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static HttpTransport HTTP_TRANSPORT;
	private static final List<String> SCOPES =Arrays.asList(SheetsScopes.SPREADSHEETS);
	static ArrayList<String> queue = new ArrayList<String>();
	static ArrayList<Double> queue2 = new ArrayList<Double>();
	static ArrayList<String> sfxcooldown = new ArrayList<String>();
	static boolean cont, oyacooldown;
	static ArrayList<ArrayList<String>> cooldown = new ArrayList<ArrayList<String>>();
	static Map<String, ArrayList<String>> coolList = new HashMap<String, ArrayList<String>>();
	static ArrayList<String> frstmsg = new ArrayList<String>();
	static Map<String, ArrayList<String>> sfxcommands = new HashMap<String, ArrayList<String>>();
	static Map<String, ArrayList<String>> commands = new HashMap<String, ArrayList<String>>();
	static ArrayList<String> settings = new ArrayList<String>();
	static String prefix, currency;
	static int[] poll = null;
	static boolean pollactive = false;
	static ArrayList<String> options = null;
	static ArrayList<String> pollCheck = new ArrayList<String>();
	static String chnl;
	static Map<String, String> userRank = new HashMap<String, String>();
	final long start = System.currentTimeMillis();
	static Timer timer;
	static boolean first;
	
	
	public Bot(String botname)
	{
		this.setName(botname);

	}

	public void sendMsg(String output)
	{
		sendMessage(chnl, output);
	}

	static {
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
	}
	
	public static Credential authorize() throws IOException {
		InputStream in = new FileInputStream("F:\\Code\\Java\\CrypticBot\\client_secret.json");
		GoogleClientSecrets clientSecrets =
				GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		GoogleAuthorizationCodeFlow flow =
				new GoogleAuthorizationCodeFlow.Builder(
						HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
				.setDataStoreFactory(DATA_STORE_FACTORY)
				.setAccessType("offline")
				.build();
		Credential credential = new AuthorizationCodeInstalledApp(
				flow, new LocalServerReceiver()).authorize("user");
		System.out.println(
				"Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
		return credential;
	}
	public static Sheets getSheetsService() throws IOException {
		Credential credential = authorize();
		return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
				.setApplicationName(APPLICATION_NAME)
				.build();
	}
	public class Cooldown extends TimerTask
	{
		String command, user;
		Cooldown(String input1, String input2)
		{
			user = input1;
			command = input2;
		}

		public void run()
		{
			ArrayList<String> cool = coolList.get(user);
			System.out.println(cool);
			cool.remove(command);
			System.out.println(cool);
			coolList.put(user, cool);
		}
	}
	
	public class SfxCooldown extends TimerTask
	{
		public void run()
		{
			//sendMessage("#crypticcircuit", "" + sfxcooldown);
			sfxcooldown.remove(0);
			//sendMessage("#crypticcircuit", "" + sfxcooldown);
		}
	}
	public static class autoCommand extends TimerTask
	{
		String out;
		autoCommand(String output)
		{
			out = output;
		}
		public void run()
		{
			System.out.println(out);

		}
	}
	public class stopPoll extends TimerTask
	{
		
		public void run()
		{
			if(pollactive == false)
			{
				sendMessage(chnl, "No active polls");
			}else
			{
				int highest = poll[0];
				for(int i = 1; i <poll.length;i++)
				{
					if(poll[i]>highest)
					{
						highest = poll[i];
					}
				}

				int count = 0;
				ArrayList<Integer> locs = new ArrayList<Integer>();
				String msg = null;
				for(int i = 0; i <poll.length;i++)
				{
					if(poll[i] == highest)
					{
						locs.add(i);
						count++;
					}
				}

				if(count == 2)
				{
					msg = "There was a tie between " + options.get(locs.get(0)) + " and " + options.get(locs.get(1));

				}else if (count > 2)
				{
					msg = "There was a tie between ";
					for(int i = 0; i < count; i++)
					{

						if(i==count-1)
						{
							msg = msg + "and " + options.get(locs.get(i));
						}else{

							msg = msg + options.get(locs.get(i)) + ", ";
						}

					}
					msg = msg + " for " + highest + " votes!";
				}else{
					msg = "Option " + (locs.get(0)+1) + " (" + options.get(locs.get(0)) + ") won with " + highest + " votes!";  

				}
				sendMessage(chnl, msg);
				poll = null;
				pollactive = false;
				pollCheck.clear();
			}
		}
	}
	//TODO check to see if commands are null

	public class getCommands
	{
		public getCommands()
		{
			
			//TODO get users with sfx
			Sheets service = null;
			String spreadsheetId = null;
			String range = null;
			ValueRange response = null;
			ValueRange response2 = null;
			ValueRange response3 = null;
			try {
				service = getSheetsService();
				spreadsheetId = "1grtrxoU19NGYNUle31I0CE8H6tst6pBe1Sf7Iw-Ev8E";
				range = "sfx!A2:H";
				response = service.spreadsheets().values()
						.get(spreadsheetId, range)
						.execute();
				range = "commands!A2:F";
				response2 = service.spreadsheets().values()
						.get(spreadsheetId, range)
						.execute();
				range = "Settings!A2:G2";
				response3 = service.spreadsheets().values()
						.get(spreadsheetId, range)
						.execute();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			List<List<Object>> values = response.getValues();
			List<List<Object>> values2 = response2.getValues();
			List<List<Object>> values3 = response3.getValues();
			prefix = values3.get(0).get(2).toString().toLowerCase();
			currency = values3.get(0).get(4).toString();
			chnl = "#" + values3.get(0).get(3).toString();
			for(int i = 0; i<values.size(); i++)
			{
				String command = values.get(i).get(0).toString().toLowerCase();
				
				String filepath = values.get(i).get(6).toString();
				String output = values.get(i).get(7).toString();
				String uses = values.get(i).get(1).toString();
				String price = values.get(i).get(5).toString();
				String vol = values.get(i).get(4).toString();
				String usercooldown = values.get(i).get(3).toString();
				String globalcooldown = values.get(i).get(2).toString();
				ArrayList<String> paramaters = new ArrayList<String>();
				//filepath = filepath.replace("\"", "\\\"");
				//sendMessage("#crypticcircuit", filepath);
				paramaters.add("file:///" + filepath);
				paramaters.add("0");
				paramaters.add(uses);
				paramaters.add(usercooldown);
				paramaters.add(output);
				paramaters.add(vol);
				paramaters.add(globalcooldown);
				paramaters.add(price);
				sfxcommands.put(prefix + command, paramaters);
				System.out.println(sfxcommands);
			}
			for(int i = 0; i<values2.size(); i++)
			{
				String command = values2.get(i).get(0).toString().toLowerCase();
				if(values2.get(i).size() == 6)
				{
					timer = new Timer();
					first = false;
					
					if(command.equals("<timer>"))
					{
						String output = values2.get(i).get(4).toString();
						int wait = Integer.parseInt(values2.get(i).get(5).toString());
						timer = new Timer();

						timer.scheduleAtFixedRate(new autoCommand(output), wait, wait);
					}else
					{
						String output = values2.get(i).get(4).toString();
							int wait = Integer.parseInt(values2.get(i).get(5).toString());
							
								timer.scheduleAtFixedRate(new autoCommand(output), wait, wait);
						String price = values2.get(i).get(3).toString();
						String usercooldown = values2.get(i).get(2).toString();
						ArrayList<String> paramaters = new ArrayList<String>();
						paramaters.add(usercooldown);
						paramaters.add(output);
						paramaters.add(price);
						commands.put(prefix + command, paramaters);
						System.out.println(commands);
					}
				}else{
					String output = values2.get(i).get(4).toString();
					String price = values2.get(i).get(3).toString();
					String usercooldown = values2.get(i).get(2).toString();
					ArrayList<String> paramaters = new ArrayList<String>();
					paramaters.add(usercooldown);
					paramaters.add(output);
					paramaters.add(price);
					commands.put(prefix + command, paramaters);
					System.out.println(commands);
				}
				System.out.println(values2);
				

				
			}


			//get command list from google sheets
			//ArrayList<String> paramaters = new ArrayList<String>();
			//paramaters.add("file:///" + filename);
			//paramaters.add("0");
			//sfxcommands.put(commandname, paramaters);
			//System.out.println(sfxcommands);
		}
	}
	public void onMessage(String channel, String sender, String login, String hostname, String message)
	{
		//TODO on message gather neccesary data from spreadsheet instead of doing multiple times when needed? have a function for it?
		//TODO add default rank custimization
		//TODO performance lag over old system? move check to when its actually used but leave variables?
		//TODO do in sepe
		message = message.toLowerCase();
		String rank = null; //TODO convert rank to a numerical value for hierarchy  (use only numerical ranks?)
		String sfx = null;
		
		if (!frstmsg.contains(sender))
		{
			Sheets service = null;
			String spreadsheetId = "1grtrxoU19NGYNUle31I0CE8H6tst6pBe1Sf7Iw-Ev8E";
			ValueRange response1 = null;
			ValueRange response2 = null;
			try {
				//TODO change to auto set spreadsheet Id
				service = getSheetsService();
				response1 = service.spreadsheets().values()
						.get(spreadsheetId, "Users!D2:F")
						.execute();
				response2 = service.spreadsheets().values()
						.get(spreadsheetId, "Users!A2:A")
						.execute();
			} catch (IOException e1) {

				e1.printStackTrace();
			}
			List<List<Object>> userData = response1.getValues();
			List<List<Object>> nameList = response2.getValues();
			List<Object> nameCheck = new ArrayList<Object>();
			nameCheck.add(sender);
			if(nameList.contains(nameCheck))
			{
				int location = nameList.indexOf(nameCheck);
				rank = userData.get(location).get(0).toString();
				if(userData.get(location).size() == 3)
				{
					sfx = "file:///" + userData.get(location).get(2).toString();
					
				}
				
			}else
			{
				rank = "viewer";
				
			}
			userRank.put(sender, rank);
			if (sfx != null)
			{
				new addQueue(sfx, 30.0);
			}
			frstmsg.add(sender);
		}else
		{
			rank = userRank.get(sender);
		}
		//TODO add per user cooldown to sfx commands
		//TODO Check to see if filepath is valid before running command and if not throw error (.isFile())
		if(sfxcommands.containsKey(message)) //TODO uniquw votea required??
		{
			String filename = sfxcommands.get(message).get(0);
			String output = sfxcommands.get(message).get(4);
			output = output.replace("<sender>", sender);
			output = output.replace("<whisper>", "/w " + sender);
			int uses = Integer.parseInt(sfxcommands.get(message).get(2));
			int ucool = Integer.parseInt(sfxcommands.get(message).get(3));
			int gcool = Integer.parseInt(sfxcommands.get(message).get(6));
			int x = Integer.parseInt(sfxcommands.get(message).get(1));
			int cost = Integer.parseInt(sfxcommands.get(message).get(7));
			double volume = Double.parseDouble(sfxcommands.get(message).get(5));
			
			if((coolList.containsKey(sender) && coolList.get(sender).contains(message)))
			{
				sendMessage(channel, "currently on user cooldown");
			}else
			{
				if(coolList.containsKey(sender))
				{
					ArrayList<String> cool = coolList.get(sender);
					cool.add(message);
					coolList.put(sender, cool);

				}else
				{
					ArrayList<String> cool = new ArrayList<String>();
					cool.add(message);
					coolList.put(sender, cool);
				}
				if(sfxcooldown.contains(message))
				{
					if(!output.equals("<null>"))
					{
						sendMessage(channel, output);
					}
					sendMessage(channel, "currently on cooldown global cooldown");
				}else
				{
					if(x<uses-1)
					{
						x++;
						if(!output.equals("<null>"))
						{
							sendMessage(channel, output);
						}
						sfxcommands.get(message).set(1, String.valueOf(x));
					}else
					{

						if(!output.equals("<null>"))
						{
							sendMessage(channel, output);
						}
						new addQueue(filename, volume);
						sfxcommands.get(message).set(1, String.valueOf(0));
						sfxcooldown.add(message);
						new Timer().schedule(new SfxCooldown(), gcool);
					}
				}
				if(cost>0)
				{
					Sheets service = null;
					String spreadsheetId = null;
					String range = null;
					ValueRange response = null;
					ValueRange response2 = null;
					try {
						service = getSheetsService();
						spreadsheetId = "1grtrxoU19NGYNUle31I0CE8H6tst6pBe1Sf7Iw-Ev8E";
						range = "Users!A2:E";
						response = service.spreadsheets().values()
								.get(spreadsheetId, range)
								.execute();
						response2 = service.spreadsheets().values()
								.get(spreadsheetId, "Users!A2:A")
								.execute();
					} catch (IOException e1) {

						e1.printStackTrace();
					}
					List<List<Object>> values = response.getValues();
					List<List<Object>> names = response2.getValues();
					List<Object> check = new ArrayList<Object>();
					check.add(sender);
					if(!names.contains(check))
					{
						sendMessage(channel, "You do not have enough " + currency + " to use this command");
					}else
					{
						int find = names.indexOf(check);
						int points = Integer.parseInt(values.get(find).get(1).toString());
						points-=cost;
						List<Object> data1 = new ArrayList<Object>();
						List<List<Object>> data2 = new ArrayList<List<Object>>();
						ValueRange data = new ValueRange();
						data1.add(points);
						data2.add(data1);
						data.setValues(data2);
						range = "Users!B" + (names.indexOf(check)+2);
						try {
							service.spreadsheets().values().update(spreadsheetId, range, data).setValueInputOption("RAW").execute();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				new Timer().schedule(new Cooldown(sender, message), ucool);
			}
			//TODO add actual function custom command options such as calculations <calc> calculations </calc>, escape characters, and adding/subtracting points
			//TODO allow custom user input using <input#>?
		}else if(commands.containsKey(message))
		{
			String output = commands.get(message).get(1);

			output = output.replace("<sender>", sender);
			//TODO check to see if the message STARTS with <whisper> instead of just seeing if it contains?
			output = output.replace("<whisper>", "/w " + sender);
			output = output.replace("<currency>", currency);
			if (output.contains("<calc>"))
			{
				int beg, end;
				beg = output.indexOf("<calc>")+6;
				end = output.indexOf("</calc>");

				String calculation = output.substring(beg, end);
				//if calculation contains an input make sure that it is a number
			}else
			{
				//regular input check
			}
			int ucool = Integer.parseInt(commands.get(message).get(0));
			int cost = Integer.parseInt(commands.get(message).get(2));

			if((coolList.containsKey(sender) && coolList.get(sender).contains(message)))
			{
				sendMessage(channel, "currently on cooldown");
			}else
			{
				if (output.contains("<points>") || output.contains("<hours>") || output.contains("<ranks>"))
				{
					Sheets service = null;
					String spreadsheetId = null;
					String range = null;
					ValueRange response = null;
					ValueRange response2 = null;
					try {
						service = getSheetsService();
						spreadsheetId = "1grtrxoU19NGYNUle31I0CE8H6tst6pBe1Sf7Iw-Ev8E";
						range = "Users!A2:E";
						response = service.spreadsheets().values()
								.get(spreadsheetId, range)
								.execute();
						response2 = service.spreadsheets().values()
								.get(spreadsheetId, "Users!A2:A")
								.execute();
					} catch (IOException e1) {

						e1.printStackTrace();
					}
					List<List<Object>> values = response.getValues();
					List<List<Object>> names = response2.getValues();
					List<Object> check = new ArrayList<Object>();
					check.add(sender);
					if(!names.contains(check))
					{
						output = output.replace("<points>", "0");
						output = output.replace("<hours>", "0");
						output = output.replace("<ranks>", "base"); //TODO create check for base rank

					}else
					{

						output = output.replace("<points>", values.get(names.indexOf(check)).get(1).toString());
						output = output.replace("<hours>", values.get(names.indexOf(check)).get(2).toString()); //TODO round hours to 1 decimal
						output = output.replace("<ranks>", values.get(names.indexOf(check)).get(4).toString());
					}
				}
				if(output.contains("<calc>"))
				{
					int beg, end;
					beg = output.indexOf("<calc>")+6;
					end = output.indexOf("</calc>");

					String calculation = output.substring(beg, end);

					//calculation = calculation.replace(";", "");

					ScriptEngineManager mngr = new ScriptEngineManager();
					ScriptEngine eng = mngr.getEngineByName("JavaScript");
					String answer = null;
					try {
						answer = eng.eval("math.eval(" + calculation + ")").toString();
					} catch (Exception e) {
						sendMessage(channel, "error");
						e.printStackTrace();
					}

					output = output.replace(output.substring(beg-6, end+7), answer);
				}
				if (output.contains("<break>"))
				{
					String[] outputs = output.split("<break>");
					for(int i = 0; i<outputs.length; i++)
					{
						sendMessage(channel, outputs[i]);
					}
				}else
				{
					sendMessage(channel, output);
				}

				if(coolList.containsKey(sender))
				{
					ArrayList<String> cool = coolList.get(sender);
					cool.add(message);
					coolList.put(sender, cool);

				}else
				{
					ArrayList<String> cool = new ArrayList<String>();
					cool.add(message);
					coolList.put(sender, cool);
				}
				if(cost>0)
				{
					Sheets service = null;
					String spreadsheetId = null;
					String range = null;
					ValueRange response = null;
					ValueRange response2 = null;
					try {
						service = getSheetsService();
						spreadsheetId = "1grtrxoU19NGYNUle31I0CE8H6tst6pBe1Sf7Iw-Ev8E";
						range = "Users!A2:E";
						response = service.spreadsheets().values()
								.get(spreadsheetId, range)
								.execute();
						response2 = service.spreadsheets().values()
								.get(spreadsheetId, "Users!A2:A")
								.execute();
					} catch (IOException e1) {

						e1.printStackTrace();
					}
					List<List<Object>> values = response.getValues();
					List<List<Object>> names = response2.getValues();
					List<Object> check = new ArrayList<Object>();
					check.add(sender);
					if(!names.contains(check))
					{
						sendMessage(channel, "You do not have enough " + currency + " to use this command");
					}else
					{
						int find = names.indexOf(check);
						int points = Integer.parseInt(values.get(find).get(1).toString());
						points-=cost;
						List<Object> data1 = new ArrayList<Object>();
						List<List<Object>> data2 = new ArrayList<List<Object>>();
						ValueRange data = new ValueRange();
						data1.add(points);
						data2.add(data1);
						data.setValues(data2);
						range = "Users!B" + (names.indexOf(check)+2);
						try {
							service.spreadsheets().values().update(spreadsheetId, range, data).setValueInputOption("RAW").execute();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				new Timer().schedule(new Cooldown(sender, message), ucool);
			}

		}
		if(message.equalsIgnoreCase(prefix + "run"))
		{		

			sendMessage(channel, "starting run");
			new Timer().scheduleAtFixedRate(new PointsGoogle(), 0, 300000); //TODO after testing change to 300000, 300000
		}if(message.equalsIgnoreCase(prefix + "test"))
		{

			double d = 14.4535218523473897;
			d = Math.round(d*10.0)/10.0;
			sendMessage(channel, "this is answer: " + d);

		}else if(message.startsWith(prefix + "poll"))
		{

			if(pollactive == true)
			{
				sendMessage(channel, "a poll is already in effect");
			}else
			{
				if(message.equalsIgnoreCase(prefix + "gift") || !message.substring(prefix.length()+4).contains(" "))
				{
					sendMessage(channel, "Invalid Syntax. " + prefix + "poll <duration> option1|option2|option3 etc.");
				}else
				{
					Scanner s = new Scanner(message.substring(prefix.length()+5));
					int duration = s.nextInt();
					//sendMessage(channel, "" + duration);
					String[] input = message.replace(String.valueOf(duration) + " ", "").substring(prefix.length()+5).split("\\|");
					options = new ArrayList<String>(Arrays.asList(input));
					poll = new int[input.length];
					pollactive = true;
					String pollmsg = "Poll Started... ";
					if(options.size()==2)
					{
						pollmsg = pollmsg + prefix + "vote 1 for \"" + options.get(0) + " or " + prefix + "vote 2 for \"" + options.get(1) + "\"";
					}else
					{
						for(int i = 0; i<options.size(); i++)
						{
							if(i==options.size()-1)
							{
								pollmsg = pollmsg + "or " + prefix + "vote " + (i+1) + " for \"" + options.get(i) + "\"";
							}else{

								pollmsg = pollmsg + prefix + "vote " + (i+1) + " for \"" + options.get(i) + "\", ";
							}
						}
					}

					sendMessage(channel, pollmsg);
					new Timer().schedule(this.new stopPoll(), duration);

				}
			}
		}else if(message.startsWith(prefix + "vote"))
		{
			if(pollCheck.contains(sender))
			{
				sendMessage(channel, "You already voted " + sender + "!");
			}else
			{
				if(message.equalsIgnoreCase(prefix + "vote") || !message.substring(prefix.length()+4).contains(" "))
				{
					sendMessage(channel, "Invalid Syntax. " + prefix + "vote <id>");
				}else{
					
					try {
						int id = Integer.parseInt(message.substring(prefix.length()+5));
						if(id>poll.length)
						{
							sendMessage(channel, "" + id + " is not a valid option in the poll please try again.");
						}else
						{
							//sendMessage(channel, "" + poll[id-1]);
							poll[id-1]++;
							//sendMessage(channel, "" + poll[id-1]);
							pollCheck.add(sender);
						}
					} catch (NumberFormatException e) {
						sendMessage(channel, "That is not a valid please try again.");
					}
					
				}
			}

		}else if(message.equals(prefix + "stoppoll"))
		{
		}else if(message.toLowerCase().startsWith(prefix + "gift"))
		{
			int x;
			String user, q, p;
			if(message.equalsIgnoreCase(prefix + "gift") || !message.substring(prefix.length()+5).contains(" "))
			{
				sendMessage(channel, "Invalid syntax. " + prefix + "gift <user> <" + currency + ">");
			}
			p=message.substring(prefix.length()+5);
			x = p.indexOf(" ");
			user = p.substring(0, x);
			q = p.substring(x+1);
			if(user.equalsIgnoreCase(sender))
			{
				sendMessage(channel, "There's no point in giving " + currency + " to yourself.");

			}else if(q.startsWith("-"))
			{
				sendMessage(channel, "Nice try, you can only send a positive number of " + currency + " to someone.");
			}else if(q.equals("0"))
			{
				sendMessage(channel, "You can't send zero " + currency + " to someone.");
			}else
			{
				
				Sheets service = null;
				String spreadsheetId = null;
				String range = null;
				ValueRange response = null;
				ValueRange response2 = null;
				try {
					service = getSheetsService();
					spreadsheetId = "1grtrxoU19NGYNUle31I0CE8H6tst6pBe1Sf7Iw-Ev8E";
					range = "Users!A2:E";
					response = service.spreadsheets().values()
							.get(spreadsheetId, range)
							.execute();
					response2 = service.spreadsheets().values()
							.get(spreadsheetId, "Users!A2:A")
							.execute();
				} catch (IOException e1) {

					e1.printStackTrace();
				}
				List<List<Object>> values = response.getValues();
				List<List<Object>> names = response2.getValues();
				List<Object> check = new ArrayList<Object>();
				check.add(sender);
				List<Object> check2 = new ArrayList<Object>();
				check2.add(user);
				if(!names.contains(check))
				{
					sendMessage(channel, "You do not have enough " + currency + " to give right now");
				}else if (!names.contains(check2))
				{
					sendMessage(channel, "That user does not exist.");
				}else
				{
					
					int points = Integer.parseInt(values.get(names.indexOf(check)).get(1).toString());
					int points2 = Integer.parseInt(values.get(names.indexOf(check2)).get(1).toString());
					int change;
					
					try
					{
						change = Integer.parseInt(q);
						if (points < change)
						{
							sendMessage(channel, "You do not have enough " + currency + " to give right now");
						}else
						{
							points-=change;
							points2+=change;
							List<Object> data1 = new ArrayList<Object>();
							List<List<Object>> data2 = new ArrayList<List<Object>>();
							ValueRange data = new ValueRange();
							data1.add(points);
							data2.add(data1);
							data.setValues(data2);
							range = "Users!B" + (names.indexOf(check)+2);
							service.spreadsheets().values().update(spreadsheetId, range, data).setValueInputOption("RAW").execute();
							
							List<Object> data3 = new ArrayList<Object>();
							List<List<Object>> data4 = new ArrayList<List<Object>>();
							ValueRange datax = new ValueRange();
							data3.add(points2);
							data4.add(data3);
							datax.setValues(data4);
							range = "Users!B" + (names.indexOf(check2)+2);
							service.spreadsheets().values().update(spreadsheetId, range, datax).setValueInputOption("RAW").execute();
							
							sendMessage(channel, sender + " has sent " + change + " " + currency + " to " + user);
						}
						
					}catch (NumberFormatException e) {
						sendMessage(channel, "The number you entered was invalid. Please make sure that you are using a positive whole number. " + prefix + "gift <user> <" + currency + ">");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}else if(message.equalsIgnoreCase(prefix + "update") && rank.equals("mod"))
		{
			if(timer != null)
			{
				timer.cancel();
				
			}
			new getCommands();
		}else if(sender.equals("crypticcircuit") && message.equalsIgnoreCase("circuitbot would you kindly activate code yellow"))
		{
			new addQueue("https://dl.dropboxusercontent.com/s/7kwofhleetg5ocw/code%20yellow.mp3?dl=0", 30.0);
		}else if(message.equalsIgnoreCase(prefix + "uptime"))
		{
			int time = (int) (System.currentTimeMillis() - start);
			sendMessage(channel, "" + time);
			int hrs, minutes = 0, seconds = 0;
			hrs = time;

			if(hrs%3600000 != 0)
			{
				minutes = hrs%3600000;
				hrs /= 3600000;
				if(minutes%60000 != 0)
				{
					seconds = (minutes%60000)/1000;
					minutes/=60000;
				}else
				{
					minutes/=60000;
				}
			}else
			{
				hrs /= 3600000;
			}
			sendMessage(channel, "The stream has been live for " + hrs + " hours " + minutes + " minutes " + seconds + " seconds.");
		}
	}
}