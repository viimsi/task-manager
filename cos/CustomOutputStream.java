package cos;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import javax.swing.JTextArea;

public class CustomOutputStream extends OutputStream {
    private JTextArea textArea;
    private PrintStream printStream;

    public CustomOutputStream(JTextArea textArea) {
        this.textArea = textArea;
        this.printStream = new PrintStream(this, true, StandardCharsets.UTF_8);
    }

    public PrintStream getPrintStream() {
        return printStream;
    }

    @Override
    public void write(int b) {
        // This method should not be called directly
        throw new UnsupportedOperationException("Call getPrintStream().write(int)");
    }

    @Override
    public void write(byte[] b, int off, int len) {
        String text = new String(b, off, len, StandardCharsets.UTF_8);
        textArea.append(text);
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
}