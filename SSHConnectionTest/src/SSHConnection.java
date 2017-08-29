import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SSHConnection {

	public static void main(String[] args) throws InterruptedException {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter your username@hostname for SSH");
		String host = "xinu01.cs.purdue.edu";
		String user = "aveerago";
		if (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] vals = line.split("@");
			if (vals.length != 2) {
				System.out.println("Entered wrong format..Exiting!");
				System.exit(0);
			}
			user = vals[0];
			host = vals[1];
		}
	    String command1= "ls";
	    System.out.println("Enter command to be executed: ");
	    if (scanner.hasNextLine()) {
	    	command1 = scanner.nextLine();
	    }
	    Console console = System.console();
	    if (console == null) {
            System.out.println("Couldn't get Console instance");
            System.exit(0);
        }
	    char passwordArray[] = console.readPassword("Enter your password: ");
	    String password = new String(passwordArray);
	    try{
	    	java.util.Properties config = new java.util.Properties(); 
	    	config.put("StrictHostKeyChecking", "no");
	    	JSch jsch = new JSch();
	    	Session session=jsch.getSession(user, host, 22);
	    	session.setPassword(password);
	    	session.setConfig(config);
	    	session.connect();
	    	System.out.println("Connected");
	    	
	    	Channel channel=session.openChannel("exec");
	        ((ChannelExec)channel).setCommand(command1);
	        channel.setInputStream(null);
	        ((ChannelExec)channel).setErrStream(System.err);
	        
	        InputStream in=channel.getInputStream(); // Output stream from channel
	         channel.connect();
	        /*byte[] tmp=new byte[1024]; // Reading 1024 bytes everytime from the channel
	        while(true){
	          while(in.available()>0){
	            int i=in.read(tmp, 0, 1024);
	            if(i<0)break;
	            System.out.print(new String(tmp, 0, i));
	          }
	          if(channel.isClosed()){
	            System.out.println("exit-status: "+channel.getExitStatus());
	            break;
	          }
	          try{Thread.sleep(1000);}catch(Exception ee){}
	        }*/
	        BufferedReader br = new BufferedReader(new InputStreamReader(in));
	        String line;
	        while ((line = br.readLine()) != null)
	            System.out.println(line);
	        channel.disconnect();
	        session.disconnect();
	        System.out.println("DONE");
	    }catch(Exception e){
	    	e.printStackTrace();
	    } finally {
	    	scanner.close();
	    }
	}
}