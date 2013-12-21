/*USB Tester OLED Data Logger
 * Created: 01/12/2013
 * By: William Garrido (MobileWill)
 * Modified: 12/15/2013
 * This app is used in conjuction with the USB Tester OLED backpack.
 * The backpack sends the voltage and current used by a USB device
 * via a serial port. Once captured you have the opention to save the
 * data to a text file. 
 * You can find more information on the USB Tester at
 * http://www.friedcircuits.us/docs
 * 
 * License:
 * This program is open source and released by FriedCircuits. It can be freely used and modified
 * as long as the orginal author and website are given credit and kept within the source code.
 * This code uses libraries from 
 * JFreeChart -  http://www.jfree.org/jfreechart/
 * RXTXComm - http://rxtx.qbang.org/
 * I also give credit to various code examples around the web, thanks!
 */


package Main;

import com.fc.usbtester.gson.USBTester;
import javax.swing.JOptionPane;
import gnu.io.*;
import gnu.io.SerialPort;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
//import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.Timer;
import javax.swing.UIDefaults;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
//import org.jfree.data.category.CategoryDataset;
//import org.jfree.data.category.DefaultCategoryDataset;
//import org.jfree.data.time.DynamicTimeSeriesCollection;
//import org.jfree.data.time.Second;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
//import sun.java2d.loops.ProcessPath.ProcessHandler;
//import org.jfree.ui.ApplicationFrame;
//import org.jfree.ui.RefineryUtilities;

//Json Parsing
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.RangeType;

/**
 *
 * @author William Garrido - www.mobilewill.us
 * @version 2.0
 */
public class Interface_Main extends javax.swing.JFrame {
               
        static SerialPort serialPort = null;
	static OutputStream outStream = null;
	static InputStream inStream = null;
        static ArrayList serialData = new java.util.ArrayList(); //Raw data from the serial port
        static ArrayList csvData = new java.util.ArrayList(); //CSV formated data
        
         /** The time series data. */
        private TimeSeries seriesCurrent;
        private TimeSeries seriesVolt;
        private TimeSeries seriesWatt;
        private TimeSeries seriesDm;
        private TimeSeries seriesDp;
      
        /** The last element of serialData array processed */
        private int lastSize= 0;

        
 /*Runs once a second triggered by a timer
  *Then checks if since the run if there new data to process
  * if so then splits the data and updates the graph
  */
 ActionListener graphShow = new ActionListener()
 {
       @Override
       public void actionPerformed(ActionEvent evt) {
           
                    
            int serialDataSize = serialData.size();
            Millisecond timeMillis = new Millisecond();
            final long timestamp = new Date().getTime();
            final Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(timestamp);
            final String timeString = new SimpleDateFormat("HH:mm:ss:SSS").format(cal.getTime());

            if (lastSize < serialDataSize)  {              
                for (int i = lastSize; i <= serialDataSize-1; i++){
                    //String elementData = serialData.get(i).toString();
                    //String splits[] = elementData.split(":");
                    Gson gson = new GsonBuilder().create();
                    USBTester usbtester = gson.fromJson(serialData.get(i).toString(), USBTester.class);
                    
                    /*
                    System.out.println(usbtester);
                    System.out.println(usbtester.getRAM());
                    System.out.println(usbtester.getV());
                    System.out.println(usbtester.getVMin());
                    System.out.println(usbtester.getVMax());
                    System.out.println(usbtester.getA());
                    System.out.println(usbtester.getAMin());
                    System.out.println(usbtester.getAMax());
                    
                    System.out.println(usbtester.getmwh());
                    System.out.println(usbtester.getmah());
                    System.out.println(usbtester.getDp());
                    System.out.println(usbtester.getDm());
                    
                    System.out.println(usbtester.getShunt());*/
                    
                    Double current = usbtester.getA();
                    Double voltage = usbtester.getV();
                    
                    
                    //Double current = Double.parseDouble(splits[4]);
                    //Double voltage = Double.parseDouble(splits[3]);
                    
                    DecimalFormat twoDForm = new DecimalFormat("#.##");
                    Double wattage = Double.valueOf(twoDForm.format((current/1000)*voltage));
                    
                    //System.out.println("Current:" + current);
                    //System.out.println("Voltage:" + voltage);
                    //System.out.println("Wattage:" + wattage);
                               
                    if (cboxGraph.isSelected()){
                        seriesCurrent.addOrUpdate(timeMillis, current); //Is this causing data to be overwritten?
                    }
                    lblCurrentValue.setText(current.toString() + "mA");
                    lblAMaxValue.setText(usbtester.getAMax().toString());
                    lblAMinValue.setText(usbtester.getAMin().toString());
                    
                    if (cboxGraph.isSelected()){
                        seriesVolt.addOrUpdate(timeMillis, voltage);
                    }
                    lblVoltsValue.setText(voltage.toString());
                    lblVMaxValue.setText(usbtester.getVMax().toString());
                    lblVMinValue.setText(usbtester.getVMin().toString());
                    
                    if (cboxGraph.isSelected()){
                        seriesWatt.addOrUpdate(timeMillis, wattage); 
                    }
                    lblWattsValue.setText(wattage.toString());
                    
                     
                    lblmWhValue.setText(usbtester.getmwh().toString());
                    
                    lblmAhValue.setText(usbtester.getmah().toString());
                    
                    if (cboxGraph.isSelected()){
                        seriesDp.addOrUpdate(timeMillis, usbtester.getDp());
                        seriesDm.addOrUpdate(timeMillis, usbtester.getDm());
                    }
                    lblDmValue.setText(usbtester.getDm().toString());
                    lblDpValue.setText(usbtester.getDp().toString());
                    
                    csvData.add(timeString + "," + usbtester.getA() + "," + usbtester.getAMax() + "," + usbtester.getAMin()
                                + "," + usbtester.getV() + "," + usbtester.getVMax() + "," + usbtester.getVMin()
                                + "," + wattage + "," + usbtester.getmah() + "," + usbtester.getmwh()
                                + "," + usbtester.getDp() + "," + usbtester.getDm()
                               );
                    

                }
                
                lastSize = serialDataSize;
            }         
            
            //System.out.println("Tick");
                
            //final Millisecond now = new Millisecond();
            System.out.println("Now = " + timeMillis.toString());
            //System.out.println(csvData.toString());
            
       }
     
     
     
 };

    Timer graphTimer = new Timer(1000, graphShow);//Init timer for graphs updating
    
    //Retrieve list of avaiable ports on the system
    static ArrayList listPorts()
    {
        ArrayList portNames = new java.util.ArrayList();
        java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
        while ( portEnum.hasMoreElements() ) 
        {
            CommPortIdentifier portIdentifier = portEnum.nextElement();
            portNames.add(portIdentifier.getName()); //+  " - " +  getPortTypeName(portIdentifier.getPortType())
            System.out.println(portNames);
        }   
        
        return portNames;
    }
    
    //Get type of port, currently unused, was from testing
    static String getPortTypeName ( int portType )
    {
        switch ( portType )
        {
            case CommPortIdentifier.PORT_I2C:
                return "I2C";
            case CommPortIdentifier.PORT_PARALLEL:
                return "Parallel";
            case CommPortIdentifier.PORT_RAW:
                return "Raw";
            case CommPortIdentifier.PORT_RS485:
                return "RS485";
            case CommPortIdentifier.PORT_SERIAL:
                return "Serial";
            default:
                return "unknown type";
        }
    }
    //Atempt to open the serial port
    void connect ( String portName, int portBaud ) throws Exception
    {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if ( portIdentifier.isCurrentlyOwned() )
        {
            lblStatus.setText("Error: Port is currently in use");
        }
        else
        {
            CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
            
            if ( commPort instanceof SerialPort )
            {
                serialPort = (SerialPort) commPort;
                 
                serialPort.setSerialPortParams(portBaud,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
                
                inStream = serialPort.getInputStream();
                outStream = serialPort.getOutputStream();
                
                //(new Thread(new SerialReader(inStream))).start();
                //(new Thread(new SerialWriter(out))).start();
                
                serialPort.addEventListener(new SerialReader(inStream));
                serialPort.notifyOnDataAvailable(true);

            }
            else
            {
                lblStatus.setText("Error: Only serial ports are usable");
            }
        }     
    }
    
 /*Runs in a new thread that handles incoming serial data
  *Processes new data and appends it to and ArrayList  
  */   
 public static class SerialReader implements SerialPortEventListener 
    {
        private InputStream in;
        private byte[] buffer;
        
        public SerialReader ( InputStream in )
        {
            this.buffer = new byte[1024];
            this.in = in;
        }
        
        @Override
        public void serialEvent(SerialPortEvent arg0) {
            int data;
          
            try
            {
                int len = 0;
                while ( ( data = in.read()) > -1 )
                {
                    if ( data == '\n' ) {
                        break;
                    }
                    buffer[len++] = (byte) data;
                }
                if(len > 150){
                    System.out.print(new String(buffer,0,len));
                    System.out.print(len + ":");
                    System.out.print(2*len+38 + ":");
                    serialData.add(new String(buffer,0,len)); 
                    System.out.print(serialData.size()+":");
                    lblSamplesValue.setText(String.valueOf(serialData.size()));
                    System.out.println((2*len+38)*serialData.size());
                }
                //Command Response
                else {
                    String dataCheck = "{\"W\":" + txtThreshold.getText();
                    if (new String(buffer,0,len).contains(dataCheck)) {lblStatus.setText("Threshold Set!");}
                }
               
                
            }
            catch ( IOException e )
            {
                e.printStackTrace();
                System.exit(-1);
            }            
        }
    }
 
    /**
     * Sets up the graph for voltage.
     * 
     * @param dataset  the dataset.
     * 
     * @return Current mA chart.
     */
    private JFreeChart createChartCurrent(final XYDataset dataset) {
        final JFreeChart result = ChartFactory.createTimeSeriesChart(
            "Current", 
            "Time", 
            "mA",
            dataset, 
            false, 
            true, 
            false
        );
        final XYPlot plot = result.getXYPlot();
        XYItemRenderer xyir = plot.getRenderer();
        xyir.setSeriesPaint(0, Color.GREEN);
        
        plot.setBackgroundPaint(Color.BLACK);
        ValueAxis domainAxis = plot.getDomainAxis();
        domainAxis.setAutoRange(true);

        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setAutoRangeMinimumSize(500);
        yAxis.setRangeType(RangeType.POSITIVE);
        yAxis.setAutoRangeIncludesZero(true);      
        
        ValueAxis rangeAxis = plot.getRangeAxis();
        rangeAxis.setAutoRange(true);
        return result;
    }
    
    //Chart for Voltage
    private JFreeChart createChartVolt(final XYDataset dataset) {
        final JFreeChart result = ChartFactory.createTimeSeriesChart(
            "Voltage", 
            "Time", 
            "Volts",
            dataset, 
            false, 
            true, 
            false
        );
        final XYPlot plot = result.getXYPlot();
        XYItemRenderer xyir = plot.getRenderer();
        xyir.setSeriesPaint(0, Color.GREEN);
        
        plot.setBackgroundPaint(Color.BLACK);
        ValueAxis domainAxis = plot.getDomainAxis();
        domainAxis.setAutoRange(true);

        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setAutoRangeMinimumSize(5.2);
        yAxis.setRangeType(RangeType.POSITIVE);
        yAxis.setAutoRangeIncludesZero(true);      
        
        ValueAxis rangeAxis = plot.getRangeAxis();
        rangeAxis.setAutoRange(true);
        return result;
    }
    
    //Chart for Wattage
    private JFreeChart createChartWatt(final XYDataset dataset) {
        final JFreeChart result = ChartFactory.createTimeSeriesChart(
            "Wattage", 
            "Time", 
            "Watts",
            dataset, 
            false, 
            true, 
            false
        );
        final XYPlot plot = result.getXYPlot();
        XYItemRenderer xyir = plot.getRenderer();
        xyir.setSeriesPaint(0, Color.GREEN);
        
        plot.setBackgroundPaint(Color.BLACK);
        ValueAxis domainAxis = plot.getDomainAxis();
        domainAxis.setAutoRange(true);

        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setAutoRangeMinimumSize(5);
        yAxis.setRangeType(RangeType.POSITIVE);
        yAxis.setAutoRangeIncludesZero(true);      
        
        ValueAxis rangeAxis = plot.getRangeAxis();
        rangeAxis.setAutoRange(true);
        return result;
    }

    //Chart for Wattage Hour
    private JFreeChart createChartDp(final XYDataset dataset) {
        final JFreeChart result = ChartFactory.createTimeSeriesChart(
            "D+", 
            "Time", 
            "Volts",
            dataset, 
            false, 
            true, 
            false
        );
        final XYPlot plot = result.getXYPlot();
        XYItemRenderer xyir = plot.getRenderer();
        xyir.setSeriesPaint(0, Color.GREEN);
        
        plot.setBackgroundPaint(Color.BLACK);
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis = plot.getRangeAxis();
        axis.setRange(0.0, 5.2); 
        return result;
    }
    
    //Chart for Amp Hour
    private JFreeChart createChartDm(final XYDataset dataset) {
        final JFreeChart result = ChartFactory.createTimeSeriesChart(
            "D-", 
            "Time", 
            "Volts",
            dataset, 
            false, 
            true, 
            false
        );
        final XYPlot plot = result.getXYPlot();
        XYItemRenderer xyir = plot.getRenderer();
        xyir.setSeriesPaint(0, Color.GREEN);
        
        plot.setBackgroundPaint(Color.BLACK);
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis = plot.getRangeAxis();
        axis.setRange(0.0, 5.2); 
        return result;
    }
    

    /**
     * Creates new form Interface_Main
     * Populates the com port combo box
     * Init the graphs for Current, Voltage, and Wattage
     */
    public Interface_Main() {
        initComponents();
        btnStop.setEnabled(false);
        btnStart.setEnabled(false);
        btnThreshold.setEnabled(false);
        sldScreen.setEnabled(false);
        btnReset.setEnabled(false);
        spnRefSpd.setEnabled(false);
        
        Image im = null;
        
        try{
        im = ImageIO.read(getClass().getResource("/faviconbot2edit.png"));
                setIconImage(im);
        }
        catch (IOException ex){
           
        }
        

        
        cmbPort.removeAllItems();
        ArrayList portNames = listPorts();
        
        for(int i = 0; i < portNames.size(); i++){
        cmbPort.addItem(portNames.get(i));
        }

        cmbBaud.setSelectedIndex(7);
                
        this.seriesCurrent = new TimeSeries("Time", Millisecond.class);
        final TimeSeriesCollection dataset = new TimeSeriesCollection(this.seriesCurrent);
        JFreeChart chart = createChartCurrent(dataset);
        ChartPanel chartPanel = new ChartPanel(chart);
        //chartPanel.setPreferredSize(new Dimension(100, 260)); //size according to my window
        chartPanel.setMouseWheelEnabled(true);
        plCurrent.add(chartPanel, BorderLayout.CENTER);
        plCurrent.validate();
        
        this.seriesVolt = new TimeSeries("Time", Millisecond.class);
        final TimeSeriesCollection datasetVolt = new TimeSeriesCollection(this.seriesVolt);
        JFreeChart chartVolt = createChartVolt(datasetVolt);
        ChartPanel chartPanelVolt = new ChartPanel(chartVolt);
        //chartPanelVolt.setPreferredSize(new Dimension(400, 260)); //size according to my window
        chartPanelVolt.setMouseWheelEnabled(true);
        plVoltage.add(chartPanelVolt, BorderLayout.CENTER);
        plVoltage.validate();
        
        this.seriesWatt = new TimeSeries("Time", Millisecond.class);
        final TimeSeriesCollection datasetWatt = new TimeSeriesCollection(this.seriesWatt);
        JFreeChart chartWatt = createChartWatt(datasetWatt);
        ChartPanel chartPanelWatt = new ChartPanel(chartWatt);
        //chartPanelWatt.setPreferredSize(new Dimension(400, 260)); //size according to my window
        chartPanelWatt.setMouseWheelEnabled(true);
        plWattage.add(chartPanelWatt, BorderLayout.CENTER);
        plWattage.validate();
        
        this.seriesDm = new TimeSeries("Time", Millisecond.class);
        final TimeSeriesCollection datasetDm = new TimeSeriesCollection(this.seriesDm);
        JFreeChart chartDm = createChartDm(datasetDm);
        ChartPanel chartPanelDm = new ChartPanel(chartDm);
        //chartPanelWatt.setPreferredSize(new Dimension(400, 260)); //size according to my window
        chartPanelDm.setMouseWheelEnabled(true);
        plmWh.add(chartPanelDm, BorderLayout.CENTER);
        plmWh.validate();
        
        this.seriesDp = new TimeSeries("Time", Millisecond.class);
        final TimeSeriesCollection datasetDp = new TimeSeriesCollection(this.seriesDp);
        JFreeChart chartDp = createChartDp(datasetDp);
        ChartPanel chartPanelDp = new ChartPanel(chartDp);
        //chartPanelWatt.setPreferredSize(new Dimension(400, 260)); //size according to my window
        chartPanelDp.setMouseWheelEnabled(true);
        plmAh.add(chartPanelDp, BorderLayout.CENTER);
        plmAh.validate();
        
        
        
        if (cmbPort.getItemCount() > 0) {btnStart.setEnabled(true); lblStatus.setText("Select COM Port and Baud Rate."); } else lblStatus.setText("No serial port found.");
        
        System.out.println(cmbPort.getItemCount());
        

        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSeparator1 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        plControl = new javax.swing.JPanel();
        btnSave = new javax.swing.JButton();
        btnStart = new javax.swing.JButton();
        btnStop = new javax.swing.JButton();
        cmbBaud = new javax.swing.JComboBox();
        lblBaud = new javax.swing.JLabel();
        cmbPort = new javax.swing.JComboBox();
        lblPort = new javax.swing.JLabel();
        lblStatus = new javax.swing.JLabel();
        btnThreshold = new javax.swing.JButton();
        txtThreshold = new javax.swing.JTextField();
        btnAbout = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        lblControl = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        btnClear = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        lblVMax = new javax.swing.JLabel();
        lblVMin = new javax.swing.JLabel();
        lblVMinValue = new javax.swing.JLabel();
        lblVolts = new javax.swing.JLabel();
        lblVMaxValue = new javax.swing.JLabel();
        lblVoltsValue = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        lblCurrent = new javax.swing.JLabel();
        lblCurrentValue = new javax.swing.JLabel();
        lblAmax = new javax.swing.JLabel();
        lblAMaxValue = new javax.swing.JLabel();
        lblAMin = new javax.swing.JLabel();
        lblAMinValue = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        lblWatts = new javax.swing.JLabel();
        lblWattsValue = new javax.swing.JLabel();
        lblmWh = new javax.swing.JLabel();
        lblmWhValue = new javax.swing.JLabel();
        lblmAh = new javax.swing.JLabel();
        lblmAhValue = new javax.swing.JLabel();
        lblSamples = new javax.swing.JPanel();
        lblDp = new javax.swing.JLabel();
        lblDpValue = new javax.swing.JLabel();
        lblDm = new javax.swing.JLabel();
        lblDmValue = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        lblSamplesValue = new javax.swing.JLabel();
        cboxGraph = new javax.swing.JCheckBox();
        sldScreen = new javax.swing.JSlider();
        jLabel4 = new javax.swing.JLabel();
        btnReset = new javax.swing.JButton();
        spnRefSpd = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        spnGphHstry = new javax.swing.JSpinner();
        plCurrent = new javax.swing.JPanel();
        plVoltage = new javax.swing.JPanel();
        plWattage = new javax.swing.JPanel();
        plmAh = new javax.swing.JPanel();
        plmWh = new javax.swing.JPanel();

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel2.setText("Samples:");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("USB Tester Data Logger - FriedCircuits.us");
        setMinimumSize(new java.awt.Dimension(1000, 680));

        plControl.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true));
        plControl.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnSave.setText("Save Serial Data");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        plControl.add(btnSave, new org.netbeans.lib.awtextra.AbsoluteConstraints(228, 198, -1, -1));

        btnStart.setText("Start");
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });
        plControl.add(btnStart, new org.netbeans.lib.awtextra.AbsoluteConstraints(349, 198, 65, -1));

        btnStop.setText("Stop");
        btnStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStopActionPerformed(evt);
            }
        });
        plControl.add(btnStop, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 198, 62, -1));

        cmbBaud.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "4800", "9600", "14400", "19200", "28800", "38400", "57600", "115200", "", "" }));
        plControl.add(cmbBaud, new org.netbeans.lib.awtextra.AbsoluteConstraints(151, 199, -1, -1));

        lblBaud.setLabelFor(cmbBaud);
        lblBaud.setText("Baud:");
        plControl.add(lblBaud, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 202, -1, -1));

        cmbPort.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        plControl.add(cmbPort, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 199, -1, -1));

        lblPort.setLabelFor(cmbPort);
        lblPort.setText("Port:");
        plControl.add(lblPort, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 202, -1, -1));

        lblStatus.setText("Status");
        plControl.add(lblStatus, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 173, 206, -1));

        btnThreshold.setText("Set Threshold");
        btnThreshold.setToolTipText("Sets LED warning threshold.");
        btnThreshold.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnThresholdActionPerformed(evt);
            }
        });
        plControl.add(btnThreshold, new org.netbeans.lib.awtextra.AbsoluteConstraints(315, 169, -1, -1));

        txtThreshold.setText("450");
        txtThreshold.setToolTipText("Sets LED warning threshold in mA.");
        plControl.add(txtThreshold, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 170, 62, -1));

        btnAbout.setText("About");
        btnAbout.setToolTipText("");
        btnAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAboutActionPerformed(evt);
            }
        });
        plControl.add(btnAbout, new org.netbeans.lib.awtextra.AbsoluteConstraints(244, 169, 65, -1));
        plControl.add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 90, 470, 10));

        lblControl.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblControl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblControl.setText("Current Values");
        lblControl.setToolTipText("");
        lblControl.setAlignmentY(0.0F);
        lblControl.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        plControl.add(lblControl, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 2, -1, -1));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel1.setText("Settings");
        plControl.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 100, -1, -1));

        btnClear.setText("Clear All");
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });
        plControl.add(btnClear, new org.netbeans.lib.awtextra.AbsoluteConstraints(411, 140, -1, -1));

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblVMax.setText(" Max:");
        jPanel1.add(lblVMax, new org.netbeans.lib.awtextra.AbsoluteConstraints(4, 20, 30, -1));

        lblVMin.setText("   Min:");
        jPanel1.add(lblVMin, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 40, 40, -1));

        lblVMinValue.setText("0.0");
        jPanel1.add(lblVMinValue, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 40, -1, -1));

        lblVolts.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblVolts.setText("Volts:");
        jPanel1.add(lblVolts, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        lblVMaxValue.setText("0.0");
        jPanel1.add(lblVMaxValue, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 20, -1, -1));

        lblVoltsValue.setText("0.0");
        jPanel1.add(lblVoltsValue, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 0, 29, -1));

        plControl.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 20, 80, 60));

        lblCurrent.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblCurrent.setText("Ampere:");

        lblCurrentValue.setText("0.0mA");

        lblAmax.setText("Max:");

        lblAMaxValue.setText("0.0");

        lblAMin.setText("Min:");

        lblAMinValue.setText("0.0");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblAMin)
                    .addComponent(lblAmax)
                    .addComponent(lblCurrent))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblAMaxValue)
                            .addComponent(lblAMinValue))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(lblCurrentValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCurrent)
                    .addComponent(lblCurrentValue))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAmax)
                    .addComponent(lblAMaxValue))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAMin)
                    .addComponent(lblAMinValue))
                .addGap(0, 6, Short.MAX_VALUE))
        );

        plControl.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, 120, 60));

        lblWatts.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblWatts.setText("Watts:");

        lblWattsValue.setText("0.0");

        lblmWh.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblmWh.setText("mWh:");

        lblmWhValue.setText("0.0");

        lblmAh.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblmAh.setText("mAh:");

        lblmAhValue.setText("0.0");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(lblWatts)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblWattsValue))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(lblmWh)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblmWhValue))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(lblmAh)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblmAhValue)))
                .addGap(0, 21, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblWatts)
                    .addComponent(lblWattsValue))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblmWh)
                    .addComponent(lblmWhValue))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblmAh)
                    .addComponent(lblmAhValue))
                .addContainerGap())
        );

        plControl.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 20, 80, 60));

        lblDp.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblDp.setText("D+:");

        lblDpValue.setText("0.0");

        lblDm.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblDm.setText("D-:");

        lblDmValue.setText("0.0");

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel3.setText("Samples:");

        lblSamplesValue.setText("0");

        javax.swing.GroupLayout lblSamplesLayout = new javax.swing.GroupLayout(lblSamples);
        lblSamples.setLayout(lblSamplesLayout);
        lblSamplesLayout.setHorizontalGroup(
            lblSamplesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lblSamplesLayout.createSequentialGroup()
                .addGroup(lblSamplesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(lblSamplesLayout.createSequentialGroup()
                        .addComponent(lblDp)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblDpValue))
                    .addGroup(lblSamplesLayout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(lblDm)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblDmValue))
                    .addGroup(lblSamplesLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblSamplesValue)))
                .addGap(0, 47, Short.MAX_VALUE))
        );
        lblSamplesLayout.setVerticalGroup(
            lblSamplesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lblSamplesLayout.createSequentialGroup()
                .addGroup(lblSamplesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDp)
                    .addComponent(lblDpValue))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(lblSamplesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDm)
                    .addComponent(lblDmValue))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(lblSamplesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(lblSamplesValue))
                .addContainerGap())
        );

        plControl.add(lblSamples, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 20, 110, 60));

        cboxGraph.setSelected(true);
        cboxGraph.setText("Update Graph?");
        cboxGraph.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboxGraphActionPerformed(evt);
            }
        });
        plControl.add(cboxGraph, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 140, 110, -1));

        sldScreen.setMaximum(6);
        sldScreen.setMinimum(1);
        sldScreen.setSnapToTicks(true);
        sldScreen.setValue(1);
        sldScreen.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sldScreenStateChanged(evt);
            }
        });
        plControl.add(sldScreen, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 100, 220, -1));

        jLabel4.setText("OLED Mode");
        plControl.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 100, 70, -1));

        btnReset.setText("Reset");
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });
        plControl.add(btnReset, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 140, -1, -1));

        spnRefSpd.setModel(new javax.swing.SpinnerNumberModel(1000, 150, 5000, 50));
        spnRefSpd.setToolTipText("Delay in miliseconds of serial sample rate");
        spnRefSpd.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnRefSpdStateChanged(evt);
            }
        });
        plControl.add(spnRefSpd, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 140, 70, -1));

        jLabel5.setText("Sample Spd (ms)");
        plControl.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 120, 110, -1));

        jLabel6.setText("Graph History (min)");
        plControl.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, -1, -1));

        spnGphHstry.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        spnGphHstry.setToolTipText("Graph history to keep, 0 is 24.855 days but probably run out of RAM before then");
        spnGphHstry.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnGphHstryStateChanged(evt);
            }
        });
        plControl.add(spnGphHstry, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 140, 70, -1));

        plCurrent.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true));
        plCurrent.setLayout(new java.awt.BorderLayout());

        plVoltage.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true));
        plVoltage.setLayout(new java.awt.BorderLayout());

        plWattage.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true));
        plWattage.setLayout(new java.awt.BorderLayout());

        plmAh.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true));
        plmAh.setLayout(new java.awt.BorderLayout());

        plmWh.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true));
        plmWh.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(plWattage, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE)
                    .addComponent(plCurrent, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(plmAh, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(plmWh, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(plControl, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 491, Short.MAX_VALUE)
                    .addComponent(plVoltage, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(plCurrent, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(plVoltage, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(plWattage, javax.swing.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE)
                    .addComponent(plControl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(plmAh, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
                    .addComponent(plmWh, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //Displays dialog box with about the program and a clickable link to the site
    private void btnAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAboutActionPerformed

        // for copying style
        JLabel label = new JLabel();
        Font font = label.getFont();

        // create some css from the label's font
        StringBuffer style = new StringBuffer("font-family:" + font.getFamily() + ";");
        style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
        style.append("font-size:" + font.getSize() + "pt;");

        // html content
        JEditorPane ep = new JEditorPane("text/html", "<html><body style=\"" + style + "\">" //
            + "This application was designed by <A HREF=http://www.friedcircuits.us>FriedCircuits</A> for the USB Tester." //
            + "</body></html>");

        // handle link events
        ep.addHyperlinkListener(new HyperlinkListener()
            {
                @Override
                public void hyperlinkUpdate(HyperlinkEvent e)
                {
                    if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

                        URI uri;
                        try {
                            uri = new java.net.URI("www.friedcircuits.us" );
                            if (desktop.isSupported(Desktop.Action.BROWSE)) {try {
                                desktop.browse(uri);
                            } catch (IOException ex) {
                                Logger.getLogger(Interface_Main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    } catch (URISyntaxException ex) {
                        Logger.getLogger(Interface_Main.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } // roll your own link launcher or use Desktop if J6+
            }
        });
        Color bgColor = label.getBackground();
        UIDefaults defaults = new UIDefaults();
        defaults.put("EditorPane[Enabled].backgroundPainter", bgColor);
        ep.putClientProperty("Nimbus.Overrides", defaults);
        ep.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
        ep.setEditable(false);
        ep.setBackground(bgColor);

        // show
        ImageIcon myCustomIcon = new ImageIcon(getClass().getResource("/faviconbot2edit.png"));
        JOptionPane.showMessageDialog(plCurrent, ep, "About", JOptionPane.INFORMATION_MESSAGE, myCustomIcon);
    }//GEN-LAST:event_btnAboutActionPerformed

    //Sets the threshold LED on the USB Tester, value is pulled from textbox
    private void btnThresholdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThresholdActionPerformed
        try {
            String outData = "W:" + txtThreshold.getText();
            Interface_Main.outStream.write(outData.getBytes());
            Interface_Main.outStream.write('\n');
            //this.outStream.flush();
        }
        catch (IOException e){
            lblStatus.setText("Error setting threshold LED - " + e.toString());
        }
    }//GEN-LAST:event_btnThresholdActionPerformed

    //Closes serial port and stops graph timer
    private void btnStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStopActionPerformed
        //JOptionPane.showMessageDialog(rootPane, "Closing Com Port", "Info", JOptionPane.INFORMATION_MESSAGE);
        System.out.println("Closing: ");
        graphTimer.stop();

        if (serialPort != null)
        {
            try
            {
                // close the i/o streams.
                //outStream.close();
                inStream.close();
                outStream.close();
            }
            catch (IOException ex)
            {
                // don't care
            }
            finally
            {
                // Close the port.
                System.out.println("\nDisconnecting from Serial Port");
                serialPort.removeEventListener();
                serialPort.close();
                lblStatus.setText("Disconnected.");
                btnStop.setEnabled(false);
                btnStart.setEnabled(true);
                btnThreshold.setEnabled(false);
                sldScreen.setEnabled(false);
                btnReset.setEnabled(false);
                spnRefSpd.setEnabled(false);
            }

        }
    }//GEN-LAST:event_btnStopActionPerformed

    //Opens serial port and start timer to update graph
    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed

        String baud = cmbBaud.getSelectedItem().toString();
        String port = cmbPort.getSelectedItem().toString();
        try
        {
            connect(port, Integer.parseInt(baud));
            if (serialPort != null)  {
                graphTimer.start();
                lblStatus.setText("Connected.");
                btnStop.setEnabled(true);
                btnStart.setEnabled(false);
                btnThreshold.setEnabled(true);
                sldScreen.setEnabled(true);
                btnReset.setEnabled(true);
                spnRefSpd.setEnabled(true);
            }
            //else lblStatus.setText("Error: Serial could not connect.");

            System.out.println(serialPort);
        }
        catch (Exception e)
        {
            lblStatus.setText("Error: Serial could not connect.");
        }

    }//GEN-LAST:event_btnStartActionPerformed

    /*Using data in the serialData Array list, saves it to a text file of
     * the users choosing
     */
    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        System.out.print(serialData);

        //Create a file chooser
        final JFileChooser fc = new JFileChooser();
        FileFilter ft = new FileNameExtensionFilter( "Comma Seperated Value (*.csv)", "csv" );
        fc.setFileFilter(ft);

        //In response to a button click:
        int returnVal = fc.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            System.out.println(file);

            try{
                BufferedWriter writer = null;
                writer = new BufferedWriter(new FileWriter(file + ".csv")); //add .txt?
                writer.write("time, amp, max, min, volt, max, min, wattage, mah, mwh, dp, dm");
                writer.newLine();
                for (int i = 0; i < csvData.size(); i++){
                    //String serialStringData = csvData.toString();
                    writer.write(csvData.get(i).toString());
                    writer.newLine();
                }
                writer.close( );
                JOptionPane.showMessageDialog(this, "Data exported successfully!",
                    "Success!", JOptionPane.INFORMATION_MESSAGE);
            }
            catch(java.io.IOException e) {

                JOptionPane.showMessageDialog(this, e);

            }

        } else {
            System.out.println("Save Canceled");
        }

    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        seriesCurrent.clear(); 
        seriesVolt.clear();
        seriesWatt.clear();
        seriesDp.clear();
        seriesDm.clear();
        lastSize = 0;
        serialData.clear();
        
    }//GEN-LAST:event_btnClearActionPerformed

    private void cboxGraphActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboxGraphActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cboxGraphActionPerformed

    private void sldScreenStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sldScreenStateChanged
        try {
            String outData = "S:" + sldScreen.getValue();
            Interface_Main.outStream.write(outData.getBytes());
            Interface_Main.outStream.write('\n');
        }
        catch (IOException e){
            lblStatus.setText("Error setting Screen - " + e.toString());
        }
    }//GEN-LAST:event_sldScreenStateChanged

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
        try {
            String outData = "Z:";
            Interface_Main.outStream.write(outData.getBytes());
            Interface_Main.outStream.write('\n');
        }
        catch (IOException e){
            lblStatus.setText("Error Reseting - " + e.toString());
        }
    }//GEN-LAST:event_btnResetActionPerformed

    private void spnRefSpdStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnRefSpdStateChanged
        try {
            String outData = "R:" + spnRefSpd.getValue();
            Interface_Main.outStream.write(outData.getBytes());
            Interface_Main.outStream.write('\n');
        }
        catch (IOException e){
            lblStatus.setText("Error setting Refesh Speed - " + e.toString());
        }
    }//GEN-LAST:event_spnRefSpdStateChanged

    private void spnGphHstryStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnGphHstryStateChanged
      long history = (int)spnGphHstry.getValue();
      history = (history * 60000);
      if (history == 0) history = 2147483647;
      this.seriesCurrent.setMaximumItemAge(history);
      this.seriesVolt.setMaximumItemAge(history);
      this.seriesWatt.setMaximumItemAge(history);
      this.seriesDp.setMaximumItemAge(history);
      this.seriesDm.setMaximumItemAge(history);
    }//GEN-LAST:event_spnGphHstryStateChanged

    
   
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (    ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Interface_Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Interface_Main().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAbout;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnStart;
    private javax.swing.JButton btnStop;
    private javax.swing.JButton btnThreshold;
    private javax.swing.JCheckBox cboxGraph;
    private javax.swing.JComboBox cmbBaud;
    private javax.swing.JComboBox cmbPort;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lblAMaxValue;
    private javax.swing.JLabel lblAMin;
    private javax.swing.JLabel lblAMinValue;
    private javax.swing.JLabel lblAmax;
    private javax.swing.JLabel lblBaud;
    private javax.swing.JLabel lblControl;
    private javax.swing.JLabel lblCurrent;
    private javax.swing.JLabel lblCurrentValue;
    private javax.swing.JLabel lblDm;
    private javax.swing.JLabel lblDmValue;
    private javax.swing.JLabel lblDp;
    private javax.swing.JLabel lblDpValue;
    private javax.swing.JLabel lblPort;
    private javax.swing.JPanel lblSamples;
    private static javax.swing.JLabel lblSamplesValue;
    private static javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblVMax;
    private javax.swing.JLabel lblVMaxValue;
    private javax.swing.JLabel lblVMin;
    private javax.swing.JLabel lblVMinValue;
    private javax.swing.JLabel lblVolts;
    private javax.swing.JLabel lblVoltsValue;
    private javax.swing.JLabel lblWatts;
    private javax.swing.JLabel lblWattsValue;
    private javax.swing.JLabel lblmAh;
    private javax.swing.JLabel lblmAhValue;
    private javax.swing.JLabel lblmWh;
    private javax.swing.JLabel lblmWhValue;
    private javax.swing.JPanel plControl;
    private javax.swing.JPanel plCurrent;
    private javax.swing.JPanel plVoltage;
    private javax.swing.JPanel plWattage;
    private javax.swing.JPanel plmAh;
    private javax.swing.JPanel plmWh;
    private javax.swing.JSlider sldScreen;
    private javax.swing.JSpinner spnGphHstry;
    private javax.swing.JSpinner spnRefSpd;
    private static javax.swing.JTextField txtThreshold;
    // End of variables declaration//GEN-END:variables
}

