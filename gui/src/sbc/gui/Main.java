package sbc.gui;

import java.lang.reflect.Method;


public class Main {

	public static void main(String[] args) {
		createFrame();
	}

	protected static void createFrame() {
		MainFrame frame = new MainFrame();
		frame.setVisible(true);
//		try {
//            Class c = Class.forName("sbc.JmsMain");
//            Object instance=c.newInstance();
//            Method m[] = c.getDeclaredMethods();
//
//            for (int i = 0; i < m.length; i++)
//            System.out.println(m[i].toString());
//            Object[] arglist=new Object[1];
//            arglist[0]=null;
//            m[0].invoke(instance, arglist);
//            
//         }
//         catch (Throwable e) {
//            e.printStackTrace();
//         }
	}
}
