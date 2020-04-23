
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class IDE80 extends JFrame {


    /** assumes that class has a noArg constructor<br/>
     * assumes that method is not overloaded ...<br/>
     * and requires one String arg
     */
    protected static Object invokeMethodOnClass(String className, String methodName, String value) throws Exception {
        Class clazz = Class.forName(className);

        // needs a no-params constructor
        Object instance = clazz.newInstance();

        Method foundMeth = null;
        Method[] meths = clazz.getDeclaredMethods();
        for(Method meth : meths) {
            if ( meth.getName().equals(methodName) ) {
                foundMeth = meth;
                break;
            }
        }

        return foundMeth.invoke(instance, new Object[] { value } );
    }




    public static void main(String[] args) {

        try {


            File file = (File)invokeMethodOnClass("XtsJ80FileSystem", "resolveCPMPath", "c:bmp.pas");
            System.out.println( "CPM File : "+ file.getPath() );

            invokeMethodOnClass("JavaPascalCompiler", "compile", "c:bmp.pas");

            


            // Class clazz = Class.forName("JavaPascalCompiler");

            // // needs a no-params constructor
            // Object instance = clazz.newInstance();

            // Method foundMeth = null;
            // Method[] meths = clazz.getDeclaredMethods();
            // for(Method meth : meths) {
            //     if ( meth.getName().equals("compile") ) {
            //         foundMeth = meth;
            //         break;
            //     }
            // }

            // System.out.println( foundMeth.getName() +" => "+ foundMeth.toString() );

            // System.out.println("JavaPascalCompiler is available ;-) ");
            // System.out.println("try to compile ");

            // foundMeth.invoke(instance, new Object[] { "c:bmp.pas" } );

            System.out.println("JavaPascalCompiler is available ;-) ");
        } catch(ClassNotFoundException ex) {
            System.out.println("JavaPascalCompiler is not available :-( ");
        } catch(InstantiationException | IllegalAccessException ex) {
            System.out.println("JavaPascalCompiler is not instanciable :-( ");
        } catch(InvocationTargetException ex) {
            System.out.println("JavaPascalCompiler.compile() is not invokable :-( ");
        } catch(Exception ex) {
            System.out.println("JavaPascalCompiler.compile() failed ");
        }



        IDE80 xmlEditor = new IDE80();
        xmlEditor.setVisible(true);
    }

    public IDE80() {

        super("Xtase Turbo Pascal Editor (IDE80)");
        setSize(800, 600);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout());

        PascalTextPane textPane = new PascalTextPane();

        JScrollPane scroller = new JScrollPane(textPane);
        panel.add(scroller);

        add(panel);

        try {
            String text = "";

            String path = "../jni80/distro/C/0/JUKE.PAS";
            if ( ! new File( path ).exists() ) {
                path = "./C/0/JUKE.PAS";
            }


            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            while( (line = reader.readLine()) != null ) {
                // text += line+"\r";
                text += line+"\n"; // BEWARE @ SAVE !!!!!
            }
            reader.close();
            if ( text.length() > 1 ) {
                text = text.substring(0, text.length()-1);
            }

            // System.out.println(text.substring(0, 250));


            textPane.setText(text);
        } catch (Exception exp) {
            exp.printStackTrace();
            textPane.setText("test for Method, Method. And again Method");
        }

    }
}
