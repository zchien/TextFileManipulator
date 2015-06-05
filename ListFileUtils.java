import java.io.*;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

public class ListFileUtils {
	
	
	public static ArrayList<File> listFiles(File target, boolean intoSub){
		ArrayList<File> ret = new ArrayList<File>();
		
		if (!target.exists()) {
			return ret;
		} else if (target.isFile()) {
			ret.add(target);
			return ret;
		} else if (target.isDirectory()) {
			File[] subFiles = target.listFiles();
			for (File subFile : subFiles) {
				if (subFile.isFile()) {
					ret.add(subFile);
				} else if (subFile.isDirectory() && intoSub) {
					ret.addAll(listFiles(subFile, intoSub));
				}
			}
		}
		return ret;
	}
	
	public static boolean move(File target, File newFile) {
		if (!newFile.exists()) {
			return target.renameTo(newFile);
		} else if (target.isFile()) {
			return true;
		} else if (target.isDirectory()) {
			System.out.println("duplicated file: " + newFile.getName());
			String subNewPath = newFile.getPath();
			File[] subFiles = target.listFiles();
			for (File subFile : subFiles) {
				File newSubFile = new File(subNewPath + File.separatorChar + subFile.getName());
				if (subFile.isFile()) {
					move(subFile, newSubFile);
				} else if (subFile.isDirectory()) {
					File[] secondSubFiles = subFile.listFiles();
					for (File secondSubFile : secondSubFiles) {
						move(subFile, newSubFile);
					}
				}
			}

			ListFileUtils.delete(target);
			return true;
		} else {
			return false;
		}
	}

	public static void copy(File fromFile, File toFile) throws IOException {
		if (fromFile.isFile()) {
			copyFile(fromFile, toFile);
		} else if (fromFile.isDirectory()) {
			copyFolder(fromFile, toFile);
		}
	}

	private static void copyFile(File fromFile, File toFile) throws IOException {
		String fromFileName = fromFile.getName();
		String toFileName = toFile.getName();

		if (toFile.exists())
			return;
		if (!fromFile.exists())
			throw new IOException("FileCopy: " + "no such source file: " + fromFileName);
		if (!fromFile.isFile())
			throw new IOException("FileCopy: " + "can't copy directory: " + fromFileName);
		if (!fromFile.canRead())
			throw new IOException("FileCopy: " + "source file is unreadable: " + fromFileName);

		if (toFile.isDirectory())
			toFile = new File(toFile, fromFile.getName());

		if (toFile.exists()) {
			if (!toFile.canWrite())
				throw new IOException("FileCopy: " + "destination file is unwriteable: " + toFileName);
			System.out.print("Overwrite existing file " + toFile.getName() + "? (Y/N): ");
			System.out.flush();
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String response = in.readLine();
			if (!response.equals("Y") && !response.equals("y"))
				throw new IOException("FileCopy: " + "existing file was not overwritten.");
		} else {
			String parent = toFile.getParent();
			if (parent == null)
				parent = System.getProperty("user.dir");
			File dir = new File(parent);
			if (!dir.exists())
				throw new IOException("FileCopy: " + "destination directory doesn't exist: " + parent);
			if (dir.isFile())
				throw new IOException("FileCopy: " + "destination is not a directory: " + parent);
			if (!dir.canWrite())
				throw new IOException("FileCopy: " + "destination directory is unwriteable: " + parent);
		}

		FileInputStream from = null;
		FileOutputStream to = null;
		try {
			from = new FileInputStream(fromFile);
			to = new FileOutputStream(toFile);
			byte[] buffer = new byte[4096];
			int bytesRead;

			while ((bytesRead = from.read(buffer)) != -1)
				to.write(buffer, 0, bytesRead); // write
		} finally {
			if (from != null)
				try {
					from.close();
				} catch (IOException e) {
					;
				}
			if (to != null)
				try {
					to.close();
				} catch (IOException e) {
					;
				}
		}
	}

	private static void copyFolder(File src, File dest) throws IOException {

		if (src.isDirectory()) {

			// if directory not exists, create it
			if (!dest.exists()) {
				dest.mkdir();
				System.out.println("Directory copied from " + src + "  to " + dest);
			}

			// list all the directory contents
			String files[] = src.list();

			for (String file : files) {
				// construct the src and dest file structure
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				// recursive copy
				copyFolder(srcFile, destFile);
			}

		} else {
			// if file, then copy it
			// Use bytes stream to support all file types
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest);

			byte[] buffer = new byte[1024];

			int length;
			// copy the file content in bytes
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}

			in.close();
			out.close();
			System.out.println("File copied from " + src + " to " + dest);
		}
	}

	// ==================================================================================
	// 清空資料夾下的檔案,並以遞迴方式以避免該資料夾下還有其他子資料夾
	public static void delete(File file) {
		if (file.isFile()) {
			file.delete();
		} else { // file.isDirectory() == true
			deleteSubFile(file);
		}
	}

	private static void deleteSubFile(File file) {
		String[] files = file.list();
		for (int i = 0; i < files.length; i++) {
			File subfile = new File(file, files[i]);
			if (subfile.isDirectory()) {
				deleteSubFile(subfile);
			}
			System.out.println("File : " + subfile.getName() + " delete...");
			subfile.delete();
		}
		System.out.println("Directory : " + file.getName() + " delete...");
		file.delete();
	}
	// ==================================================================================
}