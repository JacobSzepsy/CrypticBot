import java.util.ArrayList;
import javafx.scene.media.AudioClip;

public class addQueue extends Thread{
	static ArrayList<String> songList = new ArrayList<String>();
	static ArrayList<Double> volumeList = new ArrayList<Double>();
	public addQueue(String filename, double volume)
	{
		if(songList.isEmpty())
		{
			System.out.println(songList);
			
			songList.add(filename);
			volumeList.add(volume);
			this.start();
			System.out.println(songList);
		}else
		{
			System.out.println(songList);
			
			songList.add(filename);
			volumeList.add(volume);
			System.out.println(songList);
		}
	}
	
	public void run()
	{
		playSound();
	}
	
	public static void playSound()
	{
		while(true)
		{
			String entry = songList.get(0);
			AudioClip sfx = new AudioClip(entry);
			System.out.println(sfx.getVolume());
			sfx.play(volumeList.get(0)/100);
			System.out.println(songList.get(0) + "@" + sfx.getVolume());
			while(sfx.isPlaying())
			{
				
			}
			songList.remove(0);
			volumeList.remove(0);
				if(songList.isEmpty())
				{
					break;
				}
		}
	}
}
