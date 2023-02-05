import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import com.kstruct.gethostname4j.Hostname;

public class Installer {
	public static void main(String[] args) {
		ArrayList<String> users = getUsers();
		Scanner input = new Scanner(System.in);
		
		System.out.println("Select the account to install to:");
		for(String user: users) {
			System.out.println(user);
		}
		
		
		String user;
		do {
			user = input.nextLine();
			if(!users.contains(user))
				System.out.println("user invalid, try again");
			
		}while(!users.contains(user));
		
		input.close();
		
		try {
			installStartup(user);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			System.out.println("Keep the change you filthy animal");
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	public static String currentPath() {
		try {
			String pathRaw =  new File(Installer.class.getProtectionDomain().getCodeSource().getLocation()
				    .toURI()).getPath();
			
			return new File(pathRaw).getParentFile().getPath();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static void installStartup(String user) throws IOException {
		String hostname = Hostname.getHostname();
		String taskScheduler = "schtasks /create /xml \"C:\\Windows\\System32\\Tasks\\NoPoLock\" /tn \"NoPoLock\"";
		String xmlContents = "<?xml version=\"1.0\" encoding=\"UTF-16\"?>\r\n"
				+ "<Task version=\"1.2\" xmlns=\"http://schemas.microsoft.com/windows/2004/02/mit/task\">\r\n"
				+ "  <RegistrationInfo>\r\n"
				+ "    <Date>2023-01-28T15:18:23</Date>\r\n"
				+ "    <Author>"+ hostname + "\\" + System.getProperty("user.name") + "</Author>\r\n"
				+ "    <Description>runs nopo</Description>\r\n"
				+ "    <URI>\\NoPoLock</URI>\r\n"
				+ "  </RegistrationInfo>\r\n"
				+ "  <Triggers>\r\n"
				+ "    <LogonTrigger>\r\n"
				+ "      <StartBoundary>2023-01-28T15:18:00</StartBoundary>\r\n"
				+ "      <Enabled>true</Enabled>\r\n"
				+ "      <UserId>" + hostname + "\\"+user+"</UserId>\r\n"
				+ "    </LogonTrigger>\r\n"
				+ "  </Triggers>\r\n"
				+ "  <Principals>\r\n"
				+ "    <Principal id=\"Author\">\r\n"
				+ "      <RunLevel>HighestAvailable</RunLevel>\r\n"
				+ "      <UserId>S-1-5-18</UserId>\r\n"
				+ "      <LogonType>InteractiveToken</LogonType>\r\n"
				+ "    </Principal>\r\n"
				+ "  </Principals>\r\n"
				+ "  <Settings>\r\n"
				+ "    <MultipleInstancesPolicy>IgnoreNew</MultipleInstancesPolicy>\r\n"
				+ "    <DisallowStartIfOnBatteries>false</DisallowStartIfOnBatteries>\r\n"
				+ "    <StopIfGoingOnBatteries>false</StopIfGoingOnBatteries>\r\n"
				+ "    <AllowHardTerminate>true</AllowHardTerminate>\r\n"
				+ "    <StartWhenAvailable>false</StartWhenAvailable>\r\n"
				+ "    <RunOnlyIfNetworkAvailable>false</RunOnlyIfNetworkAvailable>\r\n"
				+ "    <IdleSettings>\r\n"
				+ "      <StopOnIdleEnd>true</StopOnIdleEnd>\r\n"
				+ "      <RestartOnIdle>false</RestartOnIdle>\r\n"
				+ "    </IdleSettings>\r\n"
				+ "    <AllowStartOnDemand>true</AllowStartOnDemand>\r\n"
				+ "    <Enabled>true</Enabled>\r\n"
				+ "    <Hidden>false</Hidden>\r\n"
				+ "    <RunOnlyIfIdle>false</RunOnlyIfIdle>\r\n"
				+ "    <WakeToRun>false</WakeToRun>\r\n"
				+ "    <ExecutionTimeLimit>P3D</ExecutionTimeLimit>\r\n"
				+ "    <Priority>7</Priority>\r\n"
				+ "  </Settings>\r\n"
				+ "  <Actions Context=\"Author\">\r\n"
				+ "    <Exec>\r\n"
				+ "      <Command>java</Command>\r\n"
				+ "      <Arguments>-jar C:\\PROGRA~2\\nopolock\\nopolock.jar</Arguments>\r\n"
				+ "    </Exec>\r\n"
				+ "  </Actions>\r\n"
				+ "</Task>";
		
		File taskFile = new File("C:\\Windows\\System32\\Tasks\\NoPoLock");
		taskFile.createNewFile();
		BufferedWriter xmlwriter = new BufferedWriter(new FileWriter(taskFile));
		xmlwriter.write(xmlContents);
		xmlwriter.close();
		
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(taskScheduler);
		String response = getResponse(pr);
		System.out.println(response);
		pr.destroy();
		
		File nopoNetFolder = new File("C://Program Files (x86)//nopolock");
		nopoNetFolder.mkdirs();
		File fileFolder = new File("C://Program Files (x86)//nopolock//files");
		fileFolder.mkdir();
		rt = Runtime.getRuntime();
		pr = rt.exec("icacls \"C:\\Program Files (x86)\\nopolock\\files\" /deny " + user +":(OI)(CI)F /grant Administrators:(OI)(CI)F /grant SYSTEM:(OI)(CI)F/T");
		response = getResponse(pr);
		
		
		download("https://github.com/nopolifelock/nopolock/releases/download/installer/nopolock.jar", "C:\\Program Files (x86)\\nopolock\\nopolock.jar" );
		
		pr.getInputStream().read();
		pr.destroy();
		
		
		String repoLink = "https://github.com/nopolifelock/lists.git";
		File config = new File("C://Program Files (x86)//noponet//config");
		config.createNewFile();
		FileWriter writer = new FileWriter(config);
		writer.write(repoLink);
		writer.close();
	}
	public static void download(String url, String path) {
		try (BufferedInputStream inputStream = new BufferedInputStream(new URL(url).openStream());
				  FileOutputStream fileOS = new FileOutputStream(path)) {
				    byte data[] = new byte[1024];
				    int byteContent;
				    while ((byteContent = inputStream.read(data, 0, 1024)) != -1) {
				        fileOS.write(data, 0, byteContent);
				    }
				} catch (IOException e) {
				    // handles IO exceptions
				}
	}

	public static String getResponse(Process pr) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		
		String line;
		
		String response = "";
		try {
			while((line = reader.readLine()) != null) {
				response += line + "\n";
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}
	public static ArrayList<String> getUsers() {
		ArrayList<String> users = new ArrayList<String>();
			
			for (File userDirectory : new File("C:/Users").listFiles())
			{
				String userName = userDirectory.getName();
				users.add(userName);
				
			}
		return users;
	}
	

}