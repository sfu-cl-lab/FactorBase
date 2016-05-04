package ca.sfu.jbn.editor;

import javax.swing.*;

import ca.sfu.jbn.model.TesterModel;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * Provides a little display/editor for notes in the session workbench. This
 * may be elaborated in the future to allow marked up text.
 *
 * @author Joseph Ramsey
 * @version $Revision$ $Date$
 */
public class TesterEditor extends JPanel {

    /**
     * The model for the note.
     */
    private TesterModel testerModel;


    /**
     * Constructs the editor given the model
     *
     * @param noteModel
     */
    public TesterEditor(TesterModel testerModel) {
        this.testerModel = testerModel;
        setup();
    }

    //============================ Private Methods =========================//


    private boolean isLegal(String text) {
//        if (!NamingProtocol.isLegalName(text)) {
//            JOptionPane.showMessageDialog(this, NamingProtocol.getProtocolDescription() + ": " + text);
//            return false;
//        }
        return true;
    }


    private void setup() {
        Font font = new Font("Monospaced", Font.PLAIN, 14);
        final JTextPane textPane = new JTextPane(testerModel.getNote());
        final JTextField field = new StringTextField(testerModel.getName(), 20);

        field.setFont(font);
        textPane.setFont(font);
        textPane.setCaretPosition(textPane.getStyledDocument().getLength());

        JScrollPane scroll = new JScrollPane(textPane);
        scroll.setPreferredSize(new Dimension(400, 400));

        field.addFocusListener(new FieldListener(field));

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(Box.createVerticalStrut(10));

        Box box = Box.createHorizontalBox();
        box.add(new JLabel(" Name: "));
        box.add(field);
        box.add(Box.createHorizontalGlue());

        this.add(box);
        this.add(Box.createVerticalStrut(10));

        Box box1 = Box.createHorizontalBox();
        box1.add(new JLabel(" Note Area: "));
        box1.add(Box.createHorizontalGlue());

        this.add(box1);
        this.add(scroll);
    }

    //============================= Inner Class ============================//

    private class FieldListener extends FocusAdapter  {

        private String current;
        private JTextField field;

        public FieldListener(JTextField field) {
            this.field = field;
            this.current = field.getText();
        }



        @Override
		public void focusLost(FocusEvent evt) {
            doAction();
        }


        private void doAction() {
            String text = field.getText();
            if(current.equals(text)){
                return;
            }
            if (isLegal(text)) {
                current = text;
                testerModel.setName(text);
                TesterEditor.this.firePropertyChange("changeNodeLabel", null, text);
            } else {
               field.setText(current);
            }
        }
    }


}
