/*USB Tester OLED Data Logger
 * Created: 01/12/2013
 * By: William Garrido (MobileWill)
 * Modified: 01/18/2013
 * This app is used in conjuction with the USB Tester OLED backpack.
 * The backpack sends the voltage and current used by a USB device
 * via a serial port. Once captured you have the opention to save the
 * data to a text file. 
 * You can find more information on the USB Tester at
 * http://www.mobilewill.us
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




/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author William Garrido - www.mobilewill.us
 */
public class Interface_Main extends javax.swing.JFrame {
    
        static SerialPort serialPort = null;
	static OutputStream outStream = null;
	static InputStream inStream = null;
        static ArrayList serialData = new java.util.ArrayList(); //Raw data from the serial port
        static ArrayList serialDataSplit = new java.util.ArrayList();//Serial port data after 1 element has been split
        
         /** The time series data. */
        private TimeSeries seriesCurrent;
        private TimeSeries seriesVolt;
        private TimeSeries seriesWatt;
      
        /** The last element of serialData array processed */
        private int lastSize= 0;

        
 /*Runs once a second triggered by a timer
  *Then checks if since the run if there new data to process
  * if so then splits the data and updates the graph
  */
 ActionListener graphShow = new ActionListener()
 {
       public void actionPerformed(ActionEvent evt) {
           
                    
            int serialDataSize = serialData.size();
            Millisecond timeMillis = new Millisecond();
                
            if (lastSize < serialDataSize)  {              
                for (int i = lastSize; i <= serialDataSize-1; i++){
                    String elementData = serialData.get(i).toString();
                    String splits[] = elementData.split(":");
                    Double current = Double.parseDouble(splits[4]);
                    Double voltage = Double.parseDouble(splits[3]);
                    
                    DecimalFormat twoDForm = new DecimalFormat("#.##");
                    Double wattage = Double.valueOf(twoDForm.format((current/1000)*voltage));
                    
                    System.out.println("Current:" + current);
                    System.out.println("Voltage:" + voltage);
                    System.out.println("Wattage:" + wattage);
                    
                    
                    seriesCurrent.addOrUpdate(timeMillis, current); //Is this causing data to be overwritten?
                    lblCurrentValue.setText(current.toString() + "mA");
                    
                    seriesVolt.addOrUpdate(timeMillis, voltage); //Is this causing data to be overwritten?
                    lblVoltsValue.setText(voltage.toString());
                    
                    seriesWatt.addOrUpdate(timeMillis, wattage); //Is this causing data to be overwritten?
                    lblWattsValue.setText(wattage.toString());
                }
                
                lastSize = serialDataSize;
            }         
            
            System.out.println("Tick");
                
            //final Millisecond now = new Millisecond();
            System.out.println("Now = " + timeMillis.toString());
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
        private byte[] buffer = new byte[1024];
        
        public SerialReader ( InputStream in )
        {
            this.in = in;
        }
        
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
                System.out.print(new String(buffer,0,len));
                serialData.add(new String(buffer,0,len)); 
                
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
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis = plot.getRangeAxis();
        axis.setRange(0.0, 500); 
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
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis = plot.getRangeAxis();
        axis.setRange(0.0, 5.0); 
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
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis = plot.getRangeAxis();
        axis.setRange(0.0, 5.0); 
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
        plControl = new javax.swing.JPanel();
        btnSave = new javax.swing.JButton();
        btnStart = new javax.swing.JButton();
        btnStop = new javax.swing.JButton();
        cmbBaud = new javax.swing.JComboBox();
        lblBaud = new javax.swing.JLabel();
        cmbPort = new javax.swing.JComboBox();
        lblPort = new javax.swing.JLabel();
        lblStatus = new javax.swing.JLabel();
        lblCurrent = new javax.swing.JLabel();
        lblCurrentValue = new javax.swing.JLabel();
        lblVolts = new javax.swing.JLabel();
        lblVoltsValue = new javax.swing.JLabel();
        lblWatts = new javax.swing.JLabel();
        lblWattsValue = new javax.swing.JLabel();
        btnThreshold = new javax.swing.JButton();
        txtThreshold = new javax.swing.JTextField();
        btnAbout = new javax.swing.JButton();
        plCurrent = new javax.swing.JPanel();
        plVoltage = new javax.swing.JPanel();
        plWattage = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("USB Tester Data Logger - FriedCircuits.us");

        plControl.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true));

        btnSave.setText("Save Serial Data");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnStart.setText("Start");
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });

        btnStop.setText("Stop");
        btnStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStopActionPerformed(evt);
            }
        });

        cmbBaud.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "4800", "9600", "14400", "19200", "28800", "38400", "57600", "115200", "", "" }));

        lblBaud.setLabelFor(cmbBaud);
        lblBaud.setText("Baud:");

        cmbPort.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        lblPort.setLabelFor(cmbPort);
        lblPort.setText("Port:");

        lblStatus.setText("Status");

        lblCurrent.setText("Current:");

        lblCurrentValue.setText("0.0mA");

        lblVolts.setText("Volts:");

        lblVoltsValue.setText("0.0");

        lblWatts.setText("Watts:");

        lblWattsValue.setText("0.0");

        btnThreshold.setText("Set Threshold");
        btnThreshold.setToolTipText("Sets LED warning threshold.");
        btnThreshold.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnThresholdActionPerformed(evt);
            }
        });

        txtThreshold.setText("450");
        txtThreshold.setToolTipText("Sets LED warning threshold in mA.");

        btnAbout.setText("About");
        btnAbout.setToolTipText("");
        btnAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAboutActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout plControlLayout = new javax.swing.GroupLayout(plControl);
        plControl.setLayout(plControlLayout);
        plControlLayout.setHorizontalGroup(
            plControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(plControlLayout.createSequentialGroup()
                .addGroup(plControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(plControlLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnAbout, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnThreshold))
                    .addGroup(plControlLayout.createSequentialGroup()
                        .addGroup(plControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(plControlLayout.createSequentialGroup()
                                .addGap(64, 64, 64)
                                .addComponent(lblCurrent)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblCurrentValue)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblVolts))
                            .addGroup(plControlLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(plControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(plControlLayout.createSequentialGroup()
                                        .addComponent(lblPort)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cmbPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(lblBaud)
                                        .addGap(3, 3, 3)
                                        .addComponent(cmbBaud, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(plControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(plControlLayout.createSequentialGroup()
                                .addComponent(lblVoltsValue)
                                .addGap(76, 76, 76)
                                .addComponent(lblWatts)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblWattsValue))
                            .addGroup(plControlLayout.createSequentialGroup()
                                .addComponent(btnSave)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnStart, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(plControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnStop, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtThreshold))
                .addContainerGap())
        );
        plControlLayout.setVerticalGroup(
            plControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, plControlLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(plControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblWatts)
                    .addComponent(lblWattsValue)
                    .addComponent(lblVolts)
                    .addComponent(lblVoltsValue)
                    .addComponent(lblCurrent)
                    .addComponent(lblCurrentValue))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                .addGroup(plControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblStatus)
                    .addComponent(btnThreshold)
                    .addComponent(txtThreshold, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAbout))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(plControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSave)
                    .addComponent(btnStart)
                    .addComponent(btnStop)
                    .addComponent(cmbBaud, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblBaud)
                    .addComponent(cmbPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPort)))
        );

        plCurrent.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true));
        plCurrent.setLayout(new java.awt.BorderLayout());

        plVoltage.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true));
        plVoltage.setLayout(new java.awt.BorderLayout());

        plWattage.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true));
        plWattage.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(plControl, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(plWattage, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(plVoltage, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(plCurrent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(plCurrent, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(plVoltage, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(plWattage, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(plControl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //Opens serial port and start timer to upgrate graph
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
           }
           //else lblStatus.setText("Error: Serial could not connect.");
           
           System.out.println(serialPort);
        }
        catch (Exception e)
        {
            lblStatus.setText("Error: Serial could not connect.");
        }
        
           
              
        
    }//GEN-LAST:event_btnStartActionPerformed

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
			}
			
		}
    }//GEN-LAST:event_btnStopActionPerformed

    /*Using data in the serialData Array list, saves it to a text file of
     * the users choosing
     */
    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        System.out.print(serialData);
        
        //Create a file chooser
        final JFileChooser fc = new JFileChooser();
        FileFilter ft = new FileNameExtensionFilter( "Text Files", "txt" );
        fc.setFileFilter(ft);

        //In response to a button click:
        int returnVal = fc.showSaveDialog(this);
        
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            System.out.println(file);
            
            try{
                BufferedWriter writer = null;
                writer = new BufferedWriter(new FileWriter(file + ".txt")); //add .txt? 
                String serialStringData = serialData.toString();
                writer.write(serialStringData);  
                writer.close( );  
                JOptionPane.showMessageDialog(this, "The Message was Saved Successfully!",  
                        "Success!", JOptionPane.INFORMATION_MESSAGE);  
            }
            catch(java.io.IOException e) {
                
                JOptionPane.showMessageDialog(this, e);
                
            }
 
            
        } else {
            System.out.println("Save Canceled");
        }
        
    }//GEN-LAST:event_btnSaveActionPerformed

    
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
            this.outStream.write(txtThreshold.getText().getBytes());
            //this.outStream.write('\n');
            //this.outStream.flush();
        }
        catch (Exception e){
            lblStatus.setText("Error setting threshold LED - " + e.toString());
        }
    }//GEN-LAST:event_btnThresholdActionPerformed

   
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
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Interface_Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Interface_Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Interface_Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Interface_Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Interface_Main().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAbout;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnStart;
    private javax.swing.JButton btnStop;
    private javax.swing.JButton btnThreshold;
    private javax.swing.JComboBox cmbBaud;
    private javax.swing.JComboBox cmbPort;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblBaud;
    private javax.swing.JLabel lblCurrent;
    private javax.swing.JLabel lblCurrentValue;
    private javax.swing.JLabel lblPort;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblVolts;
    private javax.swing.JLabel lblVoltsValue;
    private javax.swing.JLabel lblWatts;
    private javax.swing.JLabel lblWattsValue;
    private javax.swing.JPanel plControl;
    private javax.swing.JPanel plCurrent;
    private javax.swing.JPanel plVoltage;
    private javax.swing.JPanel plWattage;
    private javax.swing.JTextField txtThreshold;
    // End of variables declaration//GEN-END:variables
}

