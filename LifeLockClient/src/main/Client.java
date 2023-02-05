package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client implements Runnable{
	
	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;
	private boolean running;
	
	
	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		boolean running = true;
		Client client = new Client();
		new Thread(client).start();
		System.out.println("Welcome to NopoLock version 1.0");
		System.out.println("Type \"help\" for a list of commands");
		while(running) {
			
			String[] params = input.nextLine().split(" ");
			
			switch(params[0]) {
			
			case "list":
				client.sendRequest("LIST");
				break;
			case "set":
				client.sendRequest("SET", params[1], params[2]);
				break;
			case "check":
				client.sendRequest("CHECK", params[1]);
				break;
			case "push":
				client.sendRequest("PUSH", params[1]);
				break;
			case "clone":
				client.sendRequest("CLONE", params[1], params[2]);
				break;
			case "help":
				System.out.println("list - get the list of files.");
				System.out.println("set - usage: set filename.txt <time in seconds>");
				System.out.println("check - usage: check filename.txt");
				System.out.println("push - usage: push files/dir/filename.txt");
				System.out.println("clone - usage: clone filename.txt files/dir/");
				break;
			
			default:
				System.out.println("Unknown command, for a list of commands use \"help\"");
				break;
			}
		}
		input.close();
	}
	
	@Override
	public void run() {
		running = true;
		try {
			this.socket = new Socket("localhost", 6778);
			this.writer = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()));
			this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while(running) {
			try {
				handle(reader.readLine());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	private void handle(String req) {
		String[] params = req.split("<");
		switch(params[0]) {
		case "LIST":
			if(params.length<2) {
				System.out.println("There are no files in the directory.");
				return;
			}
			String[] files = params[1].split(">");
			for(String filename: files) {
				System.out.println(filename);
			}
			break;
		case "RESPONSE":
			System.out.println(params[1]);
			break;
			
		}
	}
	
	private void sendRequest(String... params) {
		String request = "";
		for(String param: params)
			request += param + "<";
		writer.println(request);
		writer.flush();
	}
	
	public void close() throws IOException {
		this.socket.close();
		this.writer.close();
		this.reader.close();
		running = false;
	}
}
