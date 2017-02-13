package tr.com.promis.jrviewer;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PrinterName;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRCsvDataSource;
import net.sf.jasperreports.engine.export.JRPrintServiceExporter;
import net.sf.jasperreports.engine.type.OrientationEnum;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimplePrintServiceExporterConfiguration;
import net.sf.jasperreports.view.JasperViewer;

public class App {

	private JFrame frame;
	private JTextField txtReportTemplatePath;
	private JButton btnChooseTemplate;
	private JTextField txtCsvDataFilePath;
	private JButton btnChooseDataFile;
	private JButton btnShowReport;
	private JButton btnPrintReport;
	private JTextField txtColumnNames;
	private JCheckBox chckbxFirstRowAsHeader;
	private JLabel lblPrinterName;
	private JTextField txtPrinterName;
	private JButton btnExportToPdf;
	private JLabel lblExportPath;
	private JTextField txtExportPath;
	private JButton btnChooseExportPath;
	private JMenuItem mntmAbout;
	
	private CmdLineParser cmdLineParser;
	
	@Option(name = "-template", usage = "report template path")
	private String reportTemplatePath;
	
	@Option(name = "-data-file", usage = "data file path")
	private String dataFilePath;
	
	@Option(name = "-first-row-as-header", usage = "specifies that data file's first row contains headers", aliases = "-frah", forbids = "-column-names")
	private boolean firstRowAsHeader;
	
	@Option(name = "-column-names", usage = "column names of data specify using commas", forbids = "-first-row-as-header")
	private String columnNames;
	
	@Option(name = "-export-path", usage = "destination file path used when exporting")
	private String exportPath;
	
	@Option(name = "-printer", usage = "printer's name or part of it")
	private String printerName;
	
	private enum Action {
		SHOW,
		EXPORT,
		PRINT
	}
	
	@Option(name = "-action", usage = "action to be taken (show|export|print)")
	private Action action;
	
	private enum Mode {
		GUI,
		CONSOLE
	}
	
	@Option(name = "-mode", required = true, usage = "runs program as a console or ui app (gui|console)")
	private Mode mode;

	public static void main(String[] args) {
		new App().doMain(args);
	}

	private void doMain(String[] args) {
		cmdLineParser = new CmdLineParser(this);
		try {
            cmdLineParser.parseArgument(args);
            if (mode == Mode.GUI) {
            	App app = this;
            	app.initialize();
            	app.createEvents();
            	EventQueue.invokeLater(new Runnable() {
        			public void run() {
        				app.frame.setVisible(true);
        			}
        		});
            } else if (mode == Mode.CONSOLE) {
            	invokeAction(action);
            }
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("Usage: ");
            cmdLineParser.printUsage(System.err);
            return;
        } catch (Exception e) {
        	System.err.println(e.getMessage());
        	return;
        }
	}

	public App() {
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 841, 280);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu mnMenu = new JMenu("JRViewer");
		mnMenu.setActionCommand("Menu");
		menuBar.add(mnMenu);
		
		mntmAbout = new JMenuItem("About");
		mnMenu.add(mntmAbout);
		
		JLabel lblReportTemplate = new JLabel("Report Template:");
		
		txtReportTemplatePath = new JTextField();
		txtReportTemplatePath.setEditable(false);
		txtReportTemplatePath.setColumns(10);
		txtReportTemplatePath.setText(reportTemplatePath);
		
		btnChooseTemplate = new JButton("Choose Template");
		
		JLabel lblCsvDataFile = new JLabel("Csv Data File:");
		
		txtCsvDataFilePath = new JTextField();
		txtCsvDataFilePath.setEditable(false);
		txtCsvDataFilePath.setColumns(10);
		txtCsvDataFilePath.setText(dataFilePath);
		
		btnChooseDataFile = new JButton("Choose Csv File");
		
		chckbxFirstRowAsHeader = new JCheckBox("First Row as Header");
		chckbxFirstRowAsHeader.setHorizontalTextPosition(SwingConstants.LEFT);
		chckbxFirstRowAsHeader.setSelected(firstRowAsHeader);
		
		JLabel lblColumnNames = new JLabel("Column Names:");
		
		txtColumnNames = new JTextField();
		txtColumnNames.setColumns(10);
		txtColumnNames.setText(columnNames);
		
		lblPrinterName = new JLabel("Printer Name: ");
		
		txtPrinterName = new JTextField();
		txtPrinterName.setColumns(10);
		txtPrinterName.setText(printerName);
		
		lblExportPath = new JLabel("Export Path:");
		
		txtExportPath = new JTextField();
		txtExportPath.setEditable(false);
		txtExportPath.setColumns(10);
		txtExportPath.setText(exportPath);	
		
		btnChooseExportPath = new JButton("Choose Path");
		
		JToolBar toolBar = new JToolBar();
		
		GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(lblPrinterName)
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
									.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
										.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
											.addComponent(lblCsvDataFile, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
											.addComponent(lblReportTemplate, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
										.addComponent(lblColumnNames))
									.addPreferredGap(ComponentPlacement.UNRELATED)
									.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
										.addGroup(groupLayout.createSequentialGroup()
											.addComponent(txtPrinterName, GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
											.addGap(237))
										.addComponent(txtColumnNames, GroupLayout.DEFAULT_SIZE, 552, Short.MAX_VALUE)
										.addComponent(txtCsvDataFilePath, GroupLayout.DEFAULT_SIZE, 552, Short.MAX_VALUE)
										.addComponent(txtExportPath, GroupLayout.DEFAULT_SIZE, 552, Short.MAX_VALUE)
										.addComponent(txtReportTemplatePath, GroupLayout.DEFAULT_SIZE, 552, Short.MAX_VALUE))
									.addGap(18))
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(lblExportPath)
									.addGap(573)))
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
								.addComponent(btnChooseExportPath, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(chckbxFirstRowAsHeader, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(btnChooseTemplate, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(btnChooseDataFile, GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE))
							.addGap(13)))
					.addContainerGap())
				.addComponent(toolBar, GroupLayout.DEFAULT_SIZE, 825, Short.MAX_VALUE)
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblReportTemplate)
						.addComponent(txtReportTemplatePath, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnChooseTemplate))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblCsvDataFile)
						.addComponent(txtCsvDataFilePath, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnChooseDataFile))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblColumnNames)
						.addComponent(txtColumnNames, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(chckbxFirstRowAsHeader))
					.addGap(9)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblExportPath)
						.addComponent(txtExportPath, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnChooseExportPath))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblPrinterName)
						.addComponent(txtPrinterName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED, 27, Short.MAX_VALUE)
					.addComponent(toolBar, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE))
		);
		
		btnShowReport = new JButton("Show Report");
		toolBar.add(btnShowReport);
		
		btnExportToPdf = new JButton("Export To Pdf");
		toolBar.add(btnExportToPdf);
		
		btnPrintReport = new JButton("Print Report");
		toolBar.add(btnPrintReport);
		btnPrintReport.setPreferredSize(new Dimension(99, 23));
		frame.getContentPane().setLayout(groupLayout);
	}
	
	private void createEvents() {
		btnChooseTemplate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileDialog fileDialog = new FileDialog(frame, "", FileDialog.LOAD);
				fileDialog.setFile("*.jrxml");
				fileDialog.setFilenameFilter((dir, name) -> name.endsWith(".jrxml"));
				fileDialog.setVisible(true);
				String fileDir = fileDialog.getDirectory();
				String filename = fileDialog.getFile();
				if (filename != null && fileDir != null) {
					txtReportTemplatePath.setText(Paths.get(fileDir, filename).toString());
				}
			}
		});
		
		btnChooseDataFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileDialog fileDialog = new FileDialog(frame, "", FileDialog.LOAD);
				fileDialog.setFile("*.csv");
				fileDialog.setFilenameFilter((dir, name) -> name.endsWith(".csv"));
				fileDialog.setVisible(true);
				String fileDir = fileDialog.getDirectory();
				String filename = fileDialog.getFile();
				if (filename != null && fileDir != null) {
					txtCsvDataFilePath.setText(Paths.get(fileDir, filename).toString());
				}
			}
		});
		
		btnChooseExportPath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileDialog fileDialog = new FileDialog(frame, "", FileDialog.SAVE);
				fileDialog.setFile("*.pdf");
				fileDialog.setFilenameFilter((dir, name) -> name.endsWith(".pdf"));
				fileDialog.setVisible(true);
				String fileDir = fileDialog.getDirectory();
				String filename = fileDialog.getFile();
				if (filename != null && fileDir != null) {
					txtExportPath.setText(Paths.get(fileDir, filename).toString());
				}
			}
		});
		
		chckbxFirstRowAsHeader.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtColumnNames.setEditable(!chckbxFirstRowAsHeader.isSelected());
			}
		});
		
		btnShowReport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				invokeActionUsingFormValues(Action.SHOW);
			}
		});
		
		btnExportToPdf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				invokeActionUsingFormValues(Action.EXPORT);
			}
		});
		
		btnPrintReport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				invokeActionUsingFormValues(Action.PRINT);
			}
		});
		
		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
	}
	
	private void invokeActionUsingFormValues(Action action) {
		this.reportTemplatePath = txtReportTemplatePath.getText();
		this.dataFilePath = txtCsvDataFilePath.getText();
		this.firstRowAsHeader = chckbxFirstRowAsHeader.isSelected();
		this.columnNames = txtColumnNames.getText();
		this.exportPath = txtExportPath.getText();
		this.printerName = txtPrinterName.getText();
		invokeAction(action);
	}
	
	private boolean validateValues(Action action) {
		StringBuilder strBuilder = new StringBuilder();
		
		if (reportTemplatePath == null || reportTemplatePath.isEmpty()) {
			strBuilder.append("Report template not chosen.\n");
		}
		
		if (dataFilePath == null || dataFilePath.isEmpty()) {
			strBuilder.append("Data file not chosen.\n");
		}
		
		if (!firstRowAsHeader && (columnNames == null || columnNames.isEmpty())) {
			strBuilder.append("Column names not specified. Specify using commas.\n");
		}
		
		if (action == null) {
			strBuilder.append("Action is not specified.\n");
		}
		
		if (action == Action.EXPORT && (exportPath == null || exportPath.isEmpty())) {
			strBuilder.append("Export path not specified.\n");
		}
		
		if (action == Action.PRINT && (printerName == null || printerName.isEmpty())) {
			strBuilder.append("Printer name not specified.\n");
		}
		
		if (strBuilder.length() > 0) {
			if (mode == Mode.GUI) {
				JOptionPane.showMessageDialog(frame, strBuilder.toString(), "Warning", JOptionPane.WARNING_MESSAGE);
			} else if (mode == Mode.CONSOLE) {
				System.err.println(strBuilder.toString());
				System.err.println("Usage: ");
	            cmdLineParser.printUsage(System.err);
			}
			return false;
		}
		return true;
	}
	
	private void invokeAction(Action action) {
		boolean isValid = validateValues(action);
		if (!isValid)
			return;
		
		try {
			switch (action) {
			case EXPORT:
				exportReportToPdf();
				break;
			case PRINT:
				printReport();
				break;
			case SHOW:
				showReport();
				break;
			default:
				break;
			}
		} catch (Exception e) {
			String errorMsg = "Operation failed. Message is: " + e.getMessage();
			if (mode == Mode.GUI) {
				JOptionPane.showMessageDialog(frame, errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
			} else if (mode == Mode.CONSOLE) {
				System.err.println(errorMsg);
			}
		}
	}
	
	private void showReport() throws JRException {
		JasperPrint jp = prepareJasperPrint();
		JasperViewer.viewReport(jp, false);
	}
	
	private void exportReportToPdf() throws JRException {
		JasperPrint jp = prepareJasperPrint();
		JasperExportManager.exportReportToPdfFile(jp, exportPath);
	}
	
	/*
	 * https://gist.github.com/quchie/453ad4c78c63913d32a1953ce46877d1
	 */
	private void printReport() throws JRException {
		PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
		if (printServices == null || printServices.length == 0) {
			return;
		}
		
		PrintService printService = Arrays.asList(printServices).stream()
			.filter(p -> p.getName().contains(printerName))
			.findFirst()
			.orElse(printServices[0]);
		
		JasperPrint jp = prepareJasperPrint();
		
		PrintRequestAttributeSet printRequestAttributeSet = new HashPrintRequestAttributeSet();
		printRequestAttributeSet.add(MediaSizeName.ISO_A4);
		if (jp.getOrientationValue() == OrientationEnum.LANDSCAPE) { 
		  printRequestAttributeSet.add(OrientationRequested.LANDSCAPE);
		} else { 
		  printRequestAttributeSet.add(OrientationRequested.PORTRAIT); 
		} 
		
		PrintServiceAttributeSet printServiceAttributeSet = new HashPrintServiceAttributeSet();
		printServiceAttributeSet.add(new PrinterName(printService.getName(), null));
		
		JRPrintServiceExporter exporter = new JRPrintServiceExporter();
		SimplePrintServiceExporterConfiguration configuration = new SimplePrintServiceExporterConfiguration();
		configuration.setPrintRequestAttributeSet(printRequestAttributeSet);
		configuration.setPrintServiceAttributeSet(printServiceAttributeSet);
		configuration.setDisplayPageDialog(false);
		configuration.setDisplayPrintDialog(false);

		exporter.setExporterInput(new SimpleExporterInput(jp));
		exporter.setConfiguration(configuration);
		exporter.exportReport();
	}
	
	private JasperPrint prepareJasperPrint() throws JRException {
		JRDataSource ds = prepareJRCsvDataSource(); 
		JasperReport jr = JasperCompileManager.compileReport(reportTemplatePath);
		JasperPrint  jp = JasperFillManager.fillReport(jr, null, ds);
		return jp;
	}
	
	private JRDataSource prepareJRCsvDataSource() throws JRException {
		JRCsvDataSource ds = new JRCsvDataSource(dataFilePath);
		if (firstRowAsHeader) {
			ds.setUseFirstRowAsHeader(true);
		} else {
			ds.setColumnNames(columnNames.split(","));
		}
		return ds;
	}
}
