package hsvtests;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

public class MainFrame {
	
	static JFrame frame;
	JPanel panel;
	JLabel upperLabel, lowerLabel;
	static JLabel hLowVal, sLowVal,vLowVal,hUpVal,sUpVal,vUpVal;
	static JSlider h_lower;
	static JSlider h_upper;
	static JSlider s_lower;
	static JSlider s_upper;
	static JSlider v_lower;
	static JSlider v_upper;
	
	public MainFrame() {
		frame = new JFrame();
		panel = new JPanel();
		upperLabel = new JLabel("LowerValues");
		lowerLabel = new JLabel("UpperValues");
		h_lower = new JSlider(0,255);
		s_lower = new JSlider(0,255);
		v_lower = new JSlider(0,255);
		h_upper = new JSlider(0,255);
		s_upper = new JSlider(0,255);
		v_upper = new JSlider(0,255);
		hLowVal=new JLabel("0");
		sLowVal=new JLabel("0");
		vLowVal=new JLabel("0");
		hUpVal=new JLabel("0");
		sUpVal=new JLabel("0");
		vUpVal=new JLabel("0");
		
		panel.add(lowerLabel);
		panel.add(h_lower);
		panel.add(hLowVal);
		panel.add(s_lower);
		panel.add(sLowVal);
		panel.add(v_lower);
		panel.add(vLowVal);
		panel.add(upperLabel);
		panel.add(h_upper);
		panel.add(hUpVal);
		panel.add(s_upper);
		panel.add(sUpVal);
		panel.add(v_upper);
		panel.add(vUpVal);
		frame.add(panel);
		frame.setSize(210,350);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
	}
	public static int[] getValues(){
		int[] values = new int[6];
		values[0]=h_lower.getValue();
		values[1]=s_lower.getValue();
		values[2]=v_lower.getValue();
		values[3]=h_upper.getValue();
		values[4]=s_upper.getValue();
		values[5]=v_upper.getValue();
		return values;
		
	}
	public static void updateValues(){
		hLowVal.setText(""+h_lower.getValue());
		sLowVal.setText(""+s_lower.getValue());
		vLowVal.setText(""+v_lower.getValue());
		hUpVal.setText(""+h_upper.getValue());
		sUpVal.setText(""+h_upper.getValue());
		vUpVal.setText(""+h_upper.getValue());
	}
}
