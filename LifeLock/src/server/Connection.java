package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Connection implements Runnable{
	private LockServer server;
	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;
	private boolean running;
	public Connection(LockServer server, Socket socket) {
		this.server = server;
		this.socket = socket;
		
		try {
			this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		running = true;
		while(running) {
			
			try {
				String request = reader.readLine();
				handle(request);
			} catch (IOException e) {
				
				System.out.println("Connection closed");
				this.close();
			}
		}
	}
	private void handle(String request) {
		System.out.println(request);
		String[] params = request.split("<");
		
		switch(params[0]) {
		
			case "CHECK":
				if(server.getConfig().keySet().contains(params[1])) {
					int timeLeft = server.getConfig().get(params[1]) - server.currentTimeMinutes();
					if(timeLeft <0)
						timeLeft = 0;
					send("RESPONSE", checkString(timeLeft));
				}
				
				else
					send("RESPONSE", "Invalid file name");
				break;
			case "LIST":
				String list = "";
				for (File fileDirectory : new File(LockServer.FILES_DIR).listFiles())
				{
					String file = fileDirectory.getName();
					list += file + ">";
				}
				this.send("LIST", list);
				break;
			case "SET":
				String file = params[1];
				int time = Integer.parseInt(params[2]);
				String response = server.setTime(file, time + server.currentTimeMinutes());
				send("RESPONSE", response);
				break;
			case "PUSH":
				File inputFile = new File(params[1]);
			try {
				Files.copy(inputFile.toPath(),new File(LockServer.FILES_DIR +inputFile.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				send("RESPONSE", "error copying file");
				e.printStackTrace();
			}
				break;
			case "CLONE":
				File toCopy = new File(LockServer.FILES_DIR + params[1]);
				
				if((!server.getConfig().keySet().contains(params[1])) ||
					(server.currentTimeMinutes() - server.getConfig().get(params[1]) ) >= 0) {
			try {
				Files.copy(toCopy.toPath(),new File(params[2] + "/" + params[1]).toPath(), StandardCopyOption.REPLACE_EXISTING);
				send("RESPONSE", "ok");
			} catch (IOException e) {
				send("RESPONSE", "error copying file");
				e.printStackTrace();
			}
				}else {
					send("RESPONSE", "I can't let you do that, " + checkString(server.getConfig().get(params[1]) - server.currentTimeMinutes()) + " left.");
				}
				break;
		}
	}
	
	private String checkString(int remainder) {
		int hoursLeft = remainder/60;
		int daysLeft = hoursLeft/24;
		
		int remainder_hours = hoursLeft - daysLeft*24;
		int remainder_minutes = remainder - (daysLeft*24*60 + remainder_hours*60);
		return (daysLeft + " days " + remainder_hours + " hours " + remainder_minutes + " minutes");
	}
	private void send(String... params) {
		String response = "";
		for(String param: params) {
			response += param + "<";
		}
		writer.println(response);
		writer.flush();
	}
	public void close() {
		try {
			this.socket.close();
			this.reader.close();
			this.writer.close();
			running = false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
