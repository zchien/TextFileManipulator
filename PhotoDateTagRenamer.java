import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 *	Change Date Formate from "YYYYMMDD " to "YYYYMMDD-"
 *	@author zchien
 */
public class PhotoDateTagRenamer extends Thread {
	boolean isTerminated = false;
	public void run() {
		if(isTerminated) return;
		File target = readTarget();
		tracePath(target);
		tracePath2(target);
		System.out.println("done.............................");
		isTerminated = true;
	}
	
	//===============================================================================
	//find "####-#"
	void tracePath(File target) {
		if (null==target || !target.exists()) return;
		if(isMatchDatePattern(target)) doRename(target);
		if (target.isDirectory()) {
			File[] subTargets = target.listFiles();
			for(File subTarget:subTargets){
				tracePath(subTarget);
			}
		}
	}
	
	boolean isMatchDatePattern(File target) {
		char[] fileName = target.getName().trim().toCharArray();
		int length=fileName.length;
		if(length<6) return false;
		for(int index=0; index<length; index++){
			char c = fileName[index];
			if(index<4){
				if (!Character.isDigit(c)){
					return false;
				}
			}else if(index==4){
				if (c=='-' && Character.isDigit(fileName[index+1])){
					return true;
				}else{
					return false;
				}
			}
		}
		return false;
	}
	//####-# to #####
	private boolean doRename(File target) {
		String name = target.getName();
		String fullName = target.getPath();
		String newName = StringUtils.replaceOnce(name, "-", ""); 
		String newFullName = StringUtils.replace(fullName, name, newName); 
		File newFile = new File(newFullName);
		return ListFileUtils.move(target,newFile);
	}
//===============================================================================

//===============================================================================
	//"######## " to "#########-"
	void tracePath2(File target) {
		if (null==target || !target.exists()) return;
		if(isMatchDatePattern2(target)) doRename2(target);
		if (target.isDirectory()) {
			File[] subTargets = target.listFiles();
			for(File subTarget:subTargets){
				tracePath2(subTarget);
			}
		}
	}
	boolean isMatchDatePattern2(File target) {
		char[] fileName = target.getName().trim().toCharArray();
		int length=fileName.length;
		if(length<10) return false;
		for(int index=0; index<length; index++){
			char c = fileName[index];
			if(index<8){
				if (!Character.isDigit(c)){
					return false;
				}
			}else if(index==8){
				if (c==' '){
					return true;
				}else{
					return false;
				}
			}
		}
		return false;
	}


	//"####### " to "#######-"
	private boolean doRename2(File target) {
		String name = target.getName();
		String fullName = target.getPath();
		String newName = StringUtils.replaceOnce(name, " ", "-"); 
		String newFullName = StringUtils.replace(fullName, name, newName); 
		File newFile = new File(newFullName);
		return ListFileUtils.move(target,newFile);
	}


//===============================================================================

	File readTarget(){
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String imputData="";
		while(true){
			String path = "D:/MyDoc/My Albums/";
			System.out.print("Input target path ["+path+"] ([Ctrl+c] to exit): ");
			try {
				imputData = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			path = (StringUtils.isBlank(imputData))? path : imputData;
			File targetFile = new File(path);
			if(null==targetFile || !targetFile.exists()){
				System.out.println(path + " doesn't exist");
			}else{
				System.out.print("sure to execute [n]/y ?(case insensitivie): ");
				try {
					imputData = br.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(StringUtils.isNotBlank(imputData) && imputData.equalsIgnoreCase("y")){
					return targetFile;
				}
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PhotoDateTagRenamer photoDateTagRenamer = new PhotoDateTagRenamer();
					photoDateTagRenamer.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
