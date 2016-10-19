import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
public class Server extends Thread{

	public void run()
	{
		ServerSocket socketServer = null;
		try{
			socketServer = new ServerSocket(1337);
		}catch(Exception e) {}
		try{
			while(true)
			{
				Scanner s = new Scanner(System.in);
				Scanner s2 = new Scanner(System.in);
				Socket socketClient = socketServer.accept();
				try{
					while(true)
					{
						if (s.next().equalsIgnoreCase("send"))
						{
							PrintWriter out = new PrintWriter(socketClient.getOutputStream(), true);
							String output = s2.next();
							out.println(output);
							//out.println("file:///F:/downloadedstuff/dwnloads/AIRPORN.mp3|50.0");
						}
					}
				}catch(Exception e) {}finally{
					socketClient.close();
				}
			}
		}catch(Exception e) {}finally{
			try {
				socketServer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/*try(
							ServerSocket socketServer = new ServerSocket(1337);
							Socket socketClient = socketServer.accept();

							PrintWriter out = new PrintWriter(socketClient.getOutputStream());
							BufferedReader in = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
								Scanner s = new Scanner(System.in);
									){
								while(true){
									String x = s.nextLine();
									if (x.equalsIgnoreCase("send"))
									{
										out.println("file:///F:/downloadedstuff/dwnloads/AIRPORN.mp3|50");
									}
									out.println("file:///F:/downloadedstuff/dwnloads/AIRPORN.mp3|50");
						}catch(Exception e){}*/





	}
}
