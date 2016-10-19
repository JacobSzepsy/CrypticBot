
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;

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
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

//TODO do groups of batch updates? groups of 1000? 
//TODO auto add user to mod rank if they are mod
//TODO automatically add the bot to the first entry
//TODO create spreadsheet template if it doesnt already exist
//TODO Change point counter so that it adds 5 minutes to hours every 5 minutes and then if hours is divisible by point rate add that many points to the users file
public class PointsGoogle extends TimerTask{
    private static final String APPLICATION_NAME = "TwitchBot";
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
        System.getProperty("user.home"), ".credentials/pointcounter");
    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;
    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
        JacksonFactory.getDefaultInstance();
    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/sheets.googleapis.com-java-quickstart
     */
    private static final List<String> SCOPES =
        Arrays.asList(SheetsScopes.SPREADSHEETS);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
    	//TODO work on getting this and the credentials stored inside the jar or created in an encapsulating folder
    	InputStream in = new FileInputStream("F:\\Code\\Java\\CircuitBot\\client_secret.json");
        GoogleClientSecrets clientSecrets =
            GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
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

    /**
     * Build and return an authorized Sheets API client service.
     * @return an authorized Sheets API client service
     * @throws IOException
     */
    public static Sheets getSheetsService() throws IOException {
        Credential credential = authorize();
        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
    static ArrayList<String> online = new ArrayList<String>();
    static List<List<Object>> data4 = new ArrayList<List<Object>>();
    static List<ValueRange> data5 = new ArrayList<ValueRange>();
    static double ratetime;
    static int rateammount;
    
    
    
//TODO find a possible error causing the program to run through an infinite loop (probably a glitch that occurs when there is no viewers)
//fixed (probably)
    public void run() {

    	double start = System.currentTimeMillis();
		System.out.println("Starting counter...");
    	online.clear();
        Sheets service = null;
		String spreadsheetId = null;
		String range = null;
		ValueRange response = null;
		ValueRange response2 = null;
		ValueRange response3 = null;
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
			response3 = service.spreadsheets().values()
				    .get(spreadsheetId, "Settings!D2:G2")
				    .execute();
				
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        List<List<Object>> values = response.getValues();
        List<List<Object>> values2 = response2.getValues();
        ratetime = Double.parseDouble(response3.getValues().get(0).get(3).toString());

        rateammount = Integer.parseInt(response3.getValues().get(0).get(2).toString());
        System.out.println(rateammount + " per " + ratetime + " minutes");
        String channel = response3.getValues().get(0).get(0).toString().toLowerCase();
        URL url;
		try {
			url = new URL("https://tmi.twitch.tv/group/user/" + channel + "/chatters");
			//url = new URL("https://tmi.twitch.tv/group/user/" + channel.substring(1) + "/chatters");
			HttpURLConnection request = (HttpURLConnection) url.openConnection();
			request.connect();
			JsonParser parser = new JsonParser();
			JsonElement top = parser.parse(new InputStreamReader((InputStream) request.getContent()));
			//String viewers = top.getAsJsonObject().get("viewers").toString();
			String chatters = top.getAsJsonObject().get("chatters").toString();
			String[] tags = {"\"moderators\":", "\"staff\":", "\"admins\":", "\"global_mods\":", "\"viewers\":"};
			int x,y,z;
			//for(int i=5; i-->0;)
			for(int i=0; i<5; i++)
			{
				x = chatters.indexOf(tags[i].toString());
				y = chatters.indexOf("[", x);
				z = chatters.indexOf("]", y);
				if(z-y!=1)
				{
					boolean cont = true;
					String group = chatters.substring(y+1, z);
					//System.out.println(group);
					while(cont == true)
					{
						if(group.contains(","))
						{
							String user = group.substring(1, group.indexOf(",")-1);
							group = group.substring(group.indexOf(",")+1);
							online.add(user);
							//System.out.println(a);
						}else
						{
							String user = group.substring(1, group.lastIndexOf("\""));
							//System.out.println(a);
							cont = false;
							online.add(user);
						}
					}
				}
			}
			//System.out.println(online);
			//System.out.println(online.get(10));
		} catch (Exception e) {
			System.out.println("Something went wrong.");
			e.printStackTrace();
		}
		int size = online.size();
		//int size = 5;
		
		//TODO add check to see if spreadsheet is empty and if so add bot as user
		//System.out.println(size + " is");
		//System.out.println(values);
		//for(int i = size; i-->0;)
		
		
		//System.out.println(data4);
		for(int i = 0; i<size; i++)
		{
			
			//boolean test = true;
			//int c = 0;
			
			String name = online.get(i);
			if(values.toString().contains("[" + name))
			{
				
				List<Object> check = new ArrayList<Object>();
				List<Object> data1 = new ArrayList<Object>();
				List<List<Object>> data2 = new ArrayList<List<Object>>();
				ValueRange data = new ValueRange();
				
		        check.add(name);
				int c = values2.indexOf(check);
				String p = values.get(c).toString();
		        int a = p.indexOf(",");
		        int b = p.indexOf(",", a+1);
		        int r = p.indexOf(",", b+1);
		        String q = p.substring(b+2,r);
		        p = p.substring(a+2, b);
		        int points = Integer.parseInt(p);
		        double hours = Double.parseDouble(q);
		        //TODO implement system to check for hours separately at the set 5 minute intervals?
		        hours+=(ratetime/60);
		        points+=rateammount;
		        
		        data1.add(points);
		        data1.add(hours);
		        //TODO check to see if viewer has watched for x hours and if they have then give them y rank
		        data2.add(data1);
		        
		        //data.setRange(range);
		        //System.out.println(data);
		        //loc = loc + "B" + (c+2) + ",C" + (c+2) + ",";
		        String loc = "Users!B" + (c+2) + ":C" + (c+2);
		        //System.out.println(data1);
		        
		        data.setValues(data2);
		        data.setRange(loc);
		        //System.out.println(data);
		        data5.add(data);

		       
		       //System.out.println(data5);
/*				while(test == true)
				{


					if(values.get(c).toString().startsWith(name, 1))
					{
						
						
						System.out.println(name + " is on row " + c);
						List<Object> data1 = new ArrayList<Object>();
				        data1.add(name);
				        String p = values.get(c).toString();
				        int a = p.indexOf(",");

				        int b = p.indexOf(",", a+1);
				        int r = p.indexOf(",", b+1);
				        String q = p.substring(b+2,r);
				        p = p.substring(a+2, b);
				        System.out.println(p + " and " + a + " and " + b);
				        
				        int points = Integer.parseInt(p);
				        double hours = Double.parseDouble(q);
				        hours+=(5.0/60);
				        points+=5;
				        data1.add(points);
				        data1.add(hours);
				        //data1.add("0.0");
				        //data1.add("user");
				        //data1.add("base");
				        System.out.println(data1);
				        List<List<Object>> data2 = new ArrayList<List<Object>>();
				        data2.add(data1);
				        ValueRange data = new ValueRange();
				        data.setValues(data2);
				        //data.setRange(range);
				        System.out.println(data);
				        String loc = "A" + (c+2);
				        System.out.println(loc);
				       try {
						service.spreadsheets().values().update(spreadsheetId, loc, data).setValueInputOption("RAW").execute();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
						System.out.println(values.get(c));
						test = false;
					}else
					{
						c++;
					}

				}*/
			}else
			{
				List<Object> data3 = new ArrayList<Object>();
		        data3.add(name);
		        data3.add(rateammount);
		        data3.add((ratetime/60.0));
		        data3.add("user");
		        data3.add("base");
		        data4.add(data3);
		        
			}

		}
		
		if(!data4.isEmpty())
		{
			ValueRange data = new ValueRange();
			data.setValues(data4);
	        data.setRange(range);
			try {
				service.spreadsheets().values().append(spreadsheetId, range, data).setValueInputOption("RAW").execute();
			} catch (IOException e) {

				e.printStackTrace();
			}finally
			{
				data4.clear();
			}
		}
		if(!data5.isEmpty())
		{		
			BatchUpdateValuesRequest datax = new BatchUpdateValuesRequest();
			datax.setData(data5);
			datax.setValueInputOption("RAW");
	        try {
	        	service.spreadsheets().values().batchUpdate(spreadsheetId, datax).execute();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally
	        {
				data5.clear();
	        }
		}
		

        
		double stop = System.currentTimeMillis();
		double time = stop-start;
		System.out.println("Write finished in " + time + " milliseconds for " + online.size() + " users.");
      /*  ArrayList<String> users = new ArrayList<String>();
        int x = values.size();
        System.out.println( );
        if (values.toString().contains("User2") || values.size() == 0) {
            //System.out.println(values.indexOf("User2"));
            System.out.println(values.get(0));
            
            System.out.println(values.get(1));
        } else {
          System.out.println("USER, POINTS");
          for (List row : values) {
            // Print columns A and E, which correspond to indices 0 and 4.
            System.out.printf("%s, %s\n", row.get(0), row.get(1));
          }
        }*/
    }

}
