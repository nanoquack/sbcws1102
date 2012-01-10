package sbc.gui;

import java.lang.reflect.Method;



public class Main {

	public static void main(String[] args) {
		if(args.length!=1){
			System.err.println("Main Port has to be specified");
			System.exit(1);
		}
		createFrame(args[0]);
	}

	protected static void createFrame(String factoryInfo) {
		MainFrame frame = new MainFrame();
		frame.setVisible(true);
		frame.setFactoryInfo(factoryInfo);
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
