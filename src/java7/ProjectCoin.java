package java7;

import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static java.lang.System.*;

/**
 * Created by IntelliJ IDEA.
 * User: chris
 * Date: 11/07/14
 */
public class ProjectCoin {

    public static void main(String[] args) {
        out.println("Binary literals:");
        out.println("0b010101010 = " + 0b010101010);
        out.println("0B11111 = " + 0B11111);
        out.println("A long in binary notation: " + 0b1010000101000101101000010100010110100001010001011010000101000101L);

        out.println("Underscores in numbers:");
        out.println("1_2_3_4_5 = " + 1_2_3_4_5);
        out.println("0x1_2_3_4_5 = " + 0x1_2_3_4_5);
        out.println("0b0_1_0 = " + 0b0_1_0);

        out.println("Switching on strings:");
        switch ("foo") {
            case "bar":
                out.println("You can't see me");
                break;
            case "foo":
                out.println("Hello world");
                break;
        }

        out.println("Diamond notation:");
        Map<String,String> map = new HashMap<>();
        map.put("hello", "world");
        out.println(map.get("hello"));

        class GenericConstructor<T> {
            private final T head;
            <U extends List<? extends T>> GenericConstructor(U arg) {
                this.head = arg.get(0);
            }
        }
        List<String> list = new ArrayList<>();
        list.add("hello");
        out.println(new GenericConstructor<>(list).head);

        out.println("Try with resources:");
        class MyInputStream extends ByteArrayInputStream {
            public MyInputStream(byte[] buf) {
                super(buf);
            }

            @Override
            public void close() throws IOException {
                super.close();
                System.out.println("Closing!");
            }
        }
        try (
                InputStream in = new MyInputStream(new byte[] {1,2,3,4});
                InputStream in2 = new MyInputStream(new byte[] {5,6,7,8});
        ) {
            int b;
            while ((b = in.read()) != -1) {
                out.println(b);
           }
        } catch (IOException e) {
            e.printStackTrace();
        }

        class ExceptionThrowingInputStream extends InputStream {
            public ExceptionThrowingInputStream() {
            }

            @Override
            public int read() throws IOException {
                throw new IOException("READ!");
            }

            @Override
            public void close() throws IOException {
                throw new IOException("CLOSE!");
            }
        }
        try (
                InputStream in = new ExceptionThrowingInputStream();
        ) {
            int b;
            while ((b = in.read()) != -1) {
                out.println(b);
           }
        } catch (IOException e) {
            out.println("Thrown exception: " +e.getMessage());
            for (Throwable th : e.getSuppressed()) {
                out.println("Suppressed exception: " + th.getMessage());
            }
        }


        out.println("Handling multiple exception types:");
        class Ex1 extends Exception {
            public void commonMethod(){}
            public void ex1method(){}
        }
        class Ex2 extends Exception {
            public void commonMethod(){}
            public void ex2method(){}
        }
        class Foo {
            public void doSomething() throws Ex1,Ex2 {
                throw new Ex1();
            }
        }
        try {
            new Foo().doSomething();
        } catch (Ex1|Ex2 e) {
            // Note: cannot call any of Ex1/Ex2's methods because
            // the exception is cast as Exception (Throwable?)
            out.println("Exception class: "+e.getClass());
        }

        out.println("Rethrowing multiple exception types:");
        class Bar {
            public void callDoSomething() throws Ex1, Ex2 {
                try {
                    new Foo().doSomething();
                } catch (Exception e) {
                    // compiler knows that it must be an Ex1 or Ex2
                    throw e;
                }
            }
        }
        try {
            new Bar().callDoSomething();
        } catch (Ex1|Ex2 e) {
            // Note: cannot call any of Ex1/Ex2's methods because
            // the exception is cast as Exception (Throwable?)
            out.println("Exception class: "+e.getClass());
        }
    }
}
