package track.er.ja.cker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class FileSystemHandler extends AbstractHandler{
	
	 FileInputStream fis;
	 OutputStream out;

	@Override
	 public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) throws IOException, ServletException{
        String path = request.getParameter("path");
        path = ((path==null) ? "/" : path);
        
        File dir = new File(path);
        if(dir.exists() && dir.canRead()){
        	if(dir.isDirectory()){
        		response.setContentType("text/html;charset=utf-8");
                response.setStatus(HttpServletResponse.SC_OK);
                baseRequest.setHandled(true);
                response.getWriter().println("<DOCTYPE html><head></head><body><h1>Current Directory: "+dir.getAbsolutePath()+"</h1><ul>");
                
        		File[] files = dir.listFiles();
        		if(dir.getParentFile() != null){
        			response.getWriter().println("<li><a href='?path="+dir.getParentFile().getAbsolutePath()+"'>..</a></li>");
        		}
        		for(int i=0;i<files.length;i++){
        			response.getWriter().println("<li><a href='?path="+files[i].getAbsolutePath()+"'>"+files[i].getName()+"</a></li>");
        		}
        		
        		response.getWriter().println("</ul></body>");
            }else{
            	response.setContentType("application/octet-stream");
                response.setStatus(HttpServletResponse.SC_OK);
                baseRequest.setHandled(true);
                
                try{
		            fis = new FileInputStream(dir);
		            out = response.getOutputStream();
		            IOUtils.copy(fis, out);
                }finally{
                	IOUtils.closeQuietly(out); 
                    IOUtils.closeQuietly(fis); 
                }
            }
        }
	}

}
