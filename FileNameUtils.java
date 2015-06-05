import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;
import org.mozilla.universalchardet.UniversalDetector;

import com.beaglebuddy.mp3.MP3;

public class FileNameUtils implements ActionListener {

	private JFrame frame;
	private JPanel northPane;
	private JTextField pathInputField;

	private JPanel centerPane; // funtion buttons

	private JProgressBar statusBar;
	private static final String STATUS_IDLE = "Idling...";

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FileNameUtils window = new FileNameUtils();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public FileNameUtils() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());

		northPane = new JPanel(new BorderLayout(5, 5));
		pathInputField = new JTextField();
		northPane.add(pathInputField, BorderLayout.CENTER);
		JButton pathChooser = new JButton("Dir/File");
		pathChooser.setFocusable(false);
		pathChooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc = new JFileChooser(pathInputField.getText());
				jfc.setMultiSelectionEnabled(false);
				jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				jfc.showDialog(frame, "OK");
				File target = jfc.getSelectedFile();
				if (null != target && target.exists()) {
					pathInputField.setText(target.getPath());
				}
			}
		});
		northPane.add(pathChooser, BorderLayout.EAST);
		frame.add(northPane, BorderLayout.NORTH);

		centerPane = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
		frame.add(centerPane, BorderLayout.CENTER);

		centerPane.add(createFunctionButton_NumberPrixRemover());
		centerPane.add(createFunctionButton_DashSwaper());
		centerPane.add(createFunctionButton_PrograssBarTest());
		centerPane.add(createFunctionButton_MP3DirByID3ArtistTag());
		centerPane.add(createFunctionButton_textToUTF8());
		centerPane.add(createFunctionButton_findDuplecatedFiles());
		centerPane.add(createFunctionButton_DashToUnderLine());

		statusBar = new JProgressBar(0, 100);
		statusBar.setStringPainted(true);
		statusBar.setString(STATUS_IDLE);
		frame.add(statusBar, BorderLayout.SOUTH);
	}


	private JButton createFunctionButton_DashToUnderLine() {
		JButton btn = new JButton("Dash to UnderLine");
		btn.setActionCommand("action_DashToUnderLine");
		btn.addActionListener(this);
		btn.setFocusable(false);
		return btn;
	}

	private Component createFunctionButton_findDuplecatedFiles() {
		JButton btn = new JButton("find duplicated files");
		btn.setActionCommand("action_findDuplecatedFiles");
		btn.addActionListener(this);
		btn.setFocusable(false);
		return btn;
	}

	private Component createFunctionButton_textToUTF8() {
		JButton btn = new JButton("text To UTF8");
		btn.setActionCommand("action_textToUTF8");
		btn.addActionListener(this);
		btn.setFocusable(false);
		return btn;
	}

	private JButton createFunctionButton_MP3DirByID3ArtistTag() {
		JButton btn = new JButton("Dir by Artist");
		btn.setActionCommand("action_MP3DirByID3ArtistTag");
		btn.addActionListener(this);
		btn.setFocusable(false);
		return btn;
	}

	private JButton createFunctionButton_PrograssBarTest() {
		JButton btn = new JButton("PrograssBar Test");
		btn.setActionCommand("action_PrograssBarTest");
		btn.addActionListener(this);
		btn.setFocusable(false);
		return btn;
	}

	private JButton createFunctionButton_NumberPrixRemover() {
		JButton btn = new JButton("NumberPrixRemover");
		btn.setActionCommand("action_NumberPrixRemover");
		btn.addActionListener(this);
		btn.setFocusable(false);
		return btn;
	}

	private JButton createFunctionButton_DashSwaper() {
		JButton btn = new JButton("Dash swaper");
		btn.setActionCommand("action_DashSwaper");
		btn.addActionListener(this);
		btn.setFocusable(false);
		return btn;
	}

	private void findDuplicatedFiles() {
		SwingWorker worker = new SwingWorker() {
			int totalNum = 0;
			int currentNum = 0;

			@Override
			protected Object doInBackground() throws Exception {
				File target = new File(pathInputField.getText());
				if (null == target || !target.exists()) {
					return null;
				}

				frame.getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				enableUI(false);
				doFind(target);
				enableUI(true);
				frame.getRootPane().setCursor(Cursor.getDefaultCursor());
				return null;
			}

			private void doFind(final File target) {
				ArrayList<File> totalFileList = ListFileUtils.listFiles(target, true);
				totalNum = totalFileList.size();
				HashMap<String, ArrayList<File>> nameMap = new HashMap<String, ArrayList<File>>();

				String fileName;
				ArrayList<File> sameFileNameList;
				for (File f : totalFileList) {
					try {
						fileName = f.getName();
						if (!fileName.toLowerCase().endsWith("mp3")) {
							continue;
						}
						MP3 mp3 = new MP3(f);
						fileName = mp3.getTitle();
						sameFileNameList = nameMap.get(fileName);
						if (null == sameFileNameList) {
							sameFileNameList = new ArrayList<File>();
						}
						sameFileNameList.add(f);
						nameMap.put(fileName, sameFileNameList);
						updateStatus(++currentNum, totalNum, "finding (step 1 of 2)...");
					} catch (IOException e) {
						System.out.println(f.getName() + ": " + e.getMessage());
					}
				}

				File outpuFile = new File(target.getParent() + File.separator + "duplicatedFiles.txt");
				try {
					FileOutputStream fos = new FileOutputStream(outpuFile, false);
					Writer out = new OutputStreamWriter(fos, "UTF8");
					currentNum = 0;
					totalNum = nameMap.size();
					String fullName;
					for (Map.Entry<String, ArrayList<File>> e : nameMap.entrySet()) {
						if (e.getValue().size() > 1) {
							System.out.println("================================================================================");
							out.write("================================================================================\r\n");
							for (File f : e.getValue()) {
								fullName = f.getPath() + File.separator + f.getName();
								System.out.println(fullName);
								out.write(fullName + "\r\n");
							}
							System.out.println("");
							out.write("\r\n");
						}
						out.flush();
						updateStatus(++currentNum, totalNum, "finding (step 2 of 2)...");
					}
					out.close();
					fos.close();
				} catch (Exception e1) {
					System.out.println(e1.getMessage());
				}
			}
		};
		worker.execute();
	}

	private void textToUTF8() {
		SwingWorker worker = new SwingWorker() {
			int totalNum = 0;
			int currentNum = 0;
			File outPutDir;

			@Override
			protected Object doInBackground() throws Exception {
				File target = new File(pathInputField.getText());
				if (null == target || !target.exists()) {
					return null;
				}
				outPutDir = new File(target.getPath() + File.separator + "output" + File.separator);
				if (!outPutDir.exists() || outPutDir.isFile()) {
					outPutDir.mkdir();
				}

				frame.getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				enableUI(false);
				rename(target);
				enableUI(true);
				frame.getRootPane().setCursor(Cursor.getDefaultCursor());
				return null;
			}

			private void rename(final File target) {
				if (null == target || !target.exists()) {
				} else if (target.isFile()) {
					try {
						byte[] buf = new byte[4096];
						FileInputStream fis = new FileInputStream(target);

						UniversalDetector detector = new UniversalDetector(null);

						int nread;
						while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
							detector.handleData(buf, 0, nread);
						}
						detector.dataEnd();

						String encoding = detector.getDetectedCharset();

						detector.reset();
						fis.close();

						if ("UTF-8" != encoding) {
							String newContent = (null == encoding) ? new String(Files.readAllBytes(target.toPath())) : new String(Files.readAllBytes(target.toPath()), encoding);
							FileOutputStream fos = new FileOutputStream(target);
							fos.write(newContent.getBytes("UTF-8"));
							fos.close();
						}
						ListFileUtils.move(target, new File(outPutDir.getPath() + File.separator + target.getName()));
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else if (target.isDirectory()) {
					File[] subFiles = target.listFiles();
					totalNum += subFiles.length;
					for (File subFile : subFiles) {
						rename(subFile);
					}
				}
				updateStatus(++currentNum, totalNum, "Renaming...");
			}
		};
		worker.execute();
	}

	private void mP3DirByID3ArtistTag() {
		SwingWorker worker = new SwingWorker() {
			String swapSeperator = " - ";
			int totalNum = 0;
			int currentNum = 0;
			File rootDir;

			@Override
			protected Object doInBackground() throws Exception {
				rootDir = new File(pathInputField.getText());
				if (null == rootDir || !rootDir.exists()) {
					return null;
				}
				if (!rootDir.isDirectory()) {
					rootDir = rootDir.getParentFile();
				}
				if (null == rootDir || !rootDir.exists()) {
					return null;
				}

				frame.getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				enableUI(false);

				doClassify(rootDir);

				enableUI(true);
				frame.getRootPane().setCursor(Cursor.getDefaultCursor());
				return null;
			}

			private void doClassify(final File target) {
				if (null == target || !target.exists()) {
				} else if (target.isFile()) {
					String destDirName = getClassifedName(target);
					if (StringUtils.isNotBlank(destDirName)) {
						File destDir = new File(rootDir.getPath() + File.separator + destDirName);
						if (!destDir.exists()) {
							destDir.mkdir();
						}
						String newFileName = destDir.getPath() + File.separator + target.getName();
						ListFileUtils.move(target, new File(newFileName));
					}
				} else if (target.isDirectory()) {
					File[] subFiles = target.listFiles();
					totalNum += subFiles.length;
					for (File subFile : subFiles) {
						doClassify(subFile);
					}
				}
				updateStatus(++currentNum, totalNum, "classifying...");
			}


			private String getClassifedName(File target) {
				String ret = "";
				try {
					if (target.getName().toLowerCase().endsWith("mp3")) {
						MP3 mp3 = new MP3(target);
						ret = mp3.getLeadPerformer();mp3.getID3v23Tag();
						return ret;
					}
				} catch (Exception e) {
					System.out.println("Unable to classify file: " + target.getName());
					e.printStackTrace();
				}

				String fullName = target.getName();
				String shortName = (fullName.lastIndexOf(".") > 0) ? fullName.substring(0, fullName.lastIndexOf(".")).trim() : fullName;
				String namePart[] = shortName.split(swapSeperator);
				ret = namePart[namePart.length - 1];
				return ret;
			}

		};
		worker.execute();
	}

	private void numberPrixRemover() {
		SwingWorker worker = new SwingWorker() {
			String patternStr = "^[0-9]+_.";		// 0943798_...
			Pattern pattern = Pattern.compile(patternStr);
			int totalNum = 0;
			int currentNum = 0;

			@Override
			protected Object doInBackground() throws Exception {
				File target = new File(pathInputField.getText());
				if (null == target || !target.exists()) {
					return null;
				}

				frame.getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				enableUI(false);
				rename(target);
				enableUI(true);
				frame.getRootPane().setCursor(Cursor.getDefaultCursor());
				return null;
			}

			private void rename(final File target) {
				if (null == target || !target.exists()) {
				} else if (target.isFile()) {
					String fullName = target.getName();
					Matcher matcher = pattern.matcher(fullName);
					boolean matchFound = matcher.find();
					// System.out.println(patternStr + ": "+fullName + ", " +
					// matchFound);
					if (matchFound) {
						String newName = fullName.substring(fullName.indexOf("_") + 1);
						ListFileUtils.move(target, new File(target.getParentFile().getPath() + File.separator + newName.toString()));
					}
					//					
//					String name = (fullName.lastIndexOf(".") > 0) ? fullName.substring(0, fullName.lastIndexOf(".")).trim() : fullName;
//					String ext = (fullName.lastIndexOf(".") > 0) ? fullName.substring(fullName.lastIndexOf(".")).trim() : "";
//					String namePart[] = name.split(swapSeperator);
//
//					StringBuffer newName = new StringBuffer(namePart[namePart.length - 1]);
//					for (int partIndex = 0, size = namePart.length - 1; partIndex < size; partIndex++) {
//						if (partIndex == 0) {
//							newName.append(swapSeperator);
//						}
//						newName.append(namePart[partIndex]);
//					}
//					newName.append(ext);
//
//					if (!fullName.equals(newName.toString())) {
//						ListFileUtils.move(target, new File(target.getParentFile().getPath() + File.separator + newName.toString()));
//					}
				} else if (target.isDirectory()) {
					File[] subFiles = target.listFiles();
					totalNum += subFiles.length;
					for (File subFile : subFiles) {
						rename(subFile);
					}
				}
				updateStatus(++currentNum, totalNum, "Renaming...");
			}
		};
		worker.execute();
	}

	private void dashToUnderLine() {
		SwingWorker worker = new SwingWorker() {
			String dashString = "_";
			String underLineString = " - ";
			int totalNum = 0;
			int currentNum = 0;

			@Override
			protected Object doInBackground() throws Exception {
				File target = new File(pathInputField.getText());
				if (null == target || !target.exists()) {
					return null;
				}

				frame.getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				enableUI(false);
				rename(target);
				enableUI(true);
				frame.getRootPane().setCursor(Cursor.getDefaultCursor());
				return null;
			}

			private void rename(final File target) {
				if (null == target || !target.exists()) {
				} else if (target.isFile()) {
					String fullName = target.getName();
					int dashIndex = fullName.lastIndexOf(dashString);
					StringBuffer newName = new StringBuffer();

					if (dashIndex > 0) {
						newName.append(fullName.subSequence(0, dashIndex));
						newName.append(underLineString);
						newName.append(fullName.substring(dashIndex + dashString.length()));
						if (!fullName.equals(newName.toString())) {
							System.out.println(fullName + " -> " + newName);
							ListFileUtils.move(target, new File(target.getParentFile().getPath() + File.separator + newName.toString()));
						}
					}

				} else if (target.isDirectory()) {
					File[] subFiles = target.listFiles();
					totalNum += subFiles.length;
					for (File subFile : subFiles) {
						rename(subFile);
					}
				}
				updateStatus(++currentNum, totalNum, "Renaming...");
			}
		};
		worker.execute();
	}

	private void dashSwaper() {
		SwingWorker worker = new SwingWorker() {
			String swapSeperator = " - ";
			int totalNum = 0;
			int currentNum = 0;

			@Override
			protected Object doInBackground() throws Exception {
				File target = new File(pathInputField.getText());
				if (null == target || !target.exists()) {
					return null;
				}

				frame.getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				enableUI(false);
				rename(target);
				enableUI(true);
				frame.getRootPane().setCursor(Cursor.getDefaultCursor());
				return null;
			}

			private void rename(final File target) {
				if (null == target || !target.exists()) {
				} else if (target.isFile()) {
					String fullName = target.getName();
					String name = (fullName.lastIndexOf(".") > 0) ? fullName.substring(0, fullName.lastIndexOf(".")).trim() : fullName;
					String ext = (fullName.lastIndexOf(".") > 0) ? fullName.substring(fullName.lastIndexOf(".")).trim() : "";
					String namePart[] = name.split(swapSeperator);

					StringBuffer newName = new StringBuffer(namePart[namePart.length - 1]);
					for (int partIndex = 0, size = namePart.length - 1; partIndex < size; partIndex++) {
						if (partIndex == 0) {
							newName.append(swapSeperator);
						}
						newName.append(namePart[partIndex]);
					}
					newName.append(ext);

					if (!fullName.equals(newName.toString())) {
						ListFileUtils.move(target, new File(target.getParentFile().getPath() + File.separator + newName.toString()));
					}
				} else if (target.isDirectory()) {
					File[] subFiles = target.listFiles();
					totalNum += subFiles.length;
					for (File subFile : subFiles) {
						rename(subFile);
					}
				}
				updateStatus(++currentNum, totalNum, "Renaming...");
			}
		};
		worker.execute();
	}

	private void prograssBarTest() {
		SwingWorker worker = new SwingWorker() {
			@Override
			protected Object doInBackground() throws Exception {
				frame.getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				enableUI(false);
				for (int i = 0, size = 10; i < size; i++) {
					try {
						updateStatus(i + 1, size, "PrograssBarTest");
						Thread.sleep(300);
					} catch (InterruptedException e1) {
					}
				}
				enableUI(true);
				frame.getRootPane().setCursor(Cursor.getDefaultCursor());
				return null;
			}
		};
		worker.execute();
	}

	private void updateStatus(int currentNum, int totalNum, String msg) {
		if (0 < currentNum && currentNum < totalNum) {
			int percentage = (int) (((double) ((double) currentNum / (double) totalNum)) * 100);
			statusBar.setValue(percentage);
			statusBar.setString(msg + " " + currentNum + " / " + totalNum + " (" + percentage + "%)");
		} else {
			statusBar.setValue(0);
			statusBar.setString(STATUS_IDLE);
		}
	}

	public void actionPerformed(final ActionEvent event) {
		String actionCommand = event.getActionCommand();
		if (actionCommand.equals("action_NumberPrixRemover")) {
			numberPrixRemover();
		} else if (actionCommand.equals("action_DashToUnderLine")) {
			dashToUnderLine();
		} else if (actionCommand.equals("action_DashSwaper")) {
			dashSwaper();
		} else if (actionCommand.equals("action_textToUTF8")) {
			textToUTF8();
		} else if (actionCommand.equals("action_PrograssBarTest")) {
			prograssBarTest();
		} else if (actionCommand.equals("action_MP3DirByID3ArtistTag")) {
			mP3DirByID3ArtistTag();
		} else if (actionCommand.equals("action_findDuplecatedFiles")) {
			findDuplicatedFiles();
		} else if (actionCommand.equals("")) {
		}
	}

	private void enableUI(final boolean isEnable) {
		for (Component c : northPane.getComponents()) {
			c.setEnabled(isEnable);
		}
		for (Component c : centerPane.getComponents()) {
			c.setEnabled(isEnable);
		}
	}
}
