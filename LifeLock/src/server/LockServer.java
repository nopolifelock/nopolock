package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;
import java.util.HashMap;

public class LockServer implements Runnable{
	private ServerSocket serverSocket;
	private boolean running;
	public static final String FILES_DIR = "C:\\Program Files (x86)\\nopolock\\files\\";
	public static final String DIR = "C:\\Program Files (x86)\\nopolock";
	public static String TIME_SERVER = "https://currentmillis.com/time/minutes-since-unix-epoch.php";
	private HashMap<String,Integer> config = new HashMap<String, Integer>();
	public static void main(String[] args) {
		LockServer server = new LockServer();
		new Thread(server).start();
	}
	
	public LockServer() {
		this.loadConfig();
	}
	@Override
	public void run() {
		
		running = true;
		try {
			serverSocket = new ServerSocket(6778);
			while(running) {
				new Thread(new Connection(this, serverSocket.accept())).start();
				System.out.println("new connection");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void loadConfig() {
		File configFile = new File(DIR + "\\config");
		if(configFile.exists()) {
			
			try {
				BufferedReader reader = new BufferedReader(new FileReader(configFile));
				String line;
				String[] params;
				while((line = reader.readLine())!=null) {
					params = line.split(">");
					config.put(params[0], Integer.parseInt(params[1]));
				}
				reader.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else
			try {
				configFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	private void writeConfig(String file, int time) {
		File configFile = new File(DIR + "\\config");
		try {
			configFile.createNewFile();
			PrintWriter writer = new PrintWriter(configFile);
			config.put(file, time);
			for(String f: config.keySet()) {
				writer.println(f + ">" + Integer.toString(time));
			}
			writer.close();
			config.clear();
			loadConfig();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public synchronized String setTime(String fileName, int time) {
		if(config.containsKey(fileName)) {
		if(currentTimeMinutes() >= config.get(fileName)) {
			writeConfig(fileName, time);
			return "ok";
		}else {
			return "I can't let you do that, time difference is " + Integer.toString(config.get(fileName) - currentTimeMinutes()) + " minutes.";
			}
		}else {
			writeConfig(fileName, time);
			return "ok";
		}

	}
	public int currentTimeMinutes() {
		
		try {
			URL time_url = new URL(TIME_SERVER);
			HttpURLConnection con = (HttpURLConnection) time_url.openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			
			
			int time = Integer.parseInt(reader.readLine());
			reader.close();
			return time;
			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	public HashMap<String, Integer>getConfig(){
		return config;
	}
	
}
