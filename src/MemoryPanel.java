import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class MemoryPanel extends JScrollPane {
    static JTextField[] memoryFields;
    static JTextField[] dataFields;
    private static int previousPcIndex = -1;
    private static JPanel memoryPanel;
    static String selectedMode = "メモリ"; // 現在の選択モードを保存するフィールド
    private static final int MEMORY_START = 0x0000;
    private static final int MEMORY_END = 0x1000;
    private static final int DATA_START = 0x9000;
    private static final int DATA_END = 0x9FFF;
    private boolean isInitialized = false; // 初期化フラグ

    private static Font fieldFont = new Font("Noto Sans JP", Font.PLAIN, 14); // デフォルトのフォント

    public MemoryPanel() {
        memoryPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        getVerticalScrollBar().setUnitIncrement(25);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);

        // 選択ボックスの追加
        JComboBox<String> comboBox = new JComboBox<>(new String[] { "メモリ", "データ" });
        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedMode = (String) comboBox.getSelectedItem(); // 現在の選択モードを更新
                updateView(selectedMode);
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        memoryPanel.add(comboBox, gbc); // 上部に選択ボックスを配置
        gbc.gridwidth = 1;

        setViewportView(memoryPanel);

        // メモリとデータの初期表示
        memoryFields = new JTextField[(MEMORY_END - MEMORY_START) / 4 + 1];
        dataFields = new JTextField[(DATA_END - DATA_START) / 4 + 1];
        initializeFields(); // 初期値の設定（0で埋める）
        updateView("メモリ"); // 初期状態で「メモリ」を表示
    }

    private void initializeFields() {
        // 初回のみ初期値を設定
        if (!isInitialized) {
            for (int i = 0; i < memoryFields.length; i++) {
                memoryFields[i] = new JTextField("00 00 00 00"); // 初期値
                memoryFields[i].setEditable(false);
            }
            for (int i = 0; i < dataFields.length; i++) {
                dataFields[i] = new JTextField("00 00 00 00"); // 初期値
                dataFields[i].setEditable(false);
            }
            isInitialized = true; // 初期化済みフラグを設定
        }
    }

    public static void updateView(String mode) {
        memoryPanel.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);

        // フォント設定の再適用
        setFontAndSize(fieldFont.getFontName(), fieldFont.getStyle(), fieldFont.getSize());

        JComboBox<String> comboBox = new JComboBox<>(new String[] { "メモリ", "データ" });
        comboBox.setSelectedItem(mode);
        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedMode = (String) comboBox.getSelectedItem();
                updateView(selectedMode);
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        memoryPanel.add(comboBox, gbc);
        gbc.gridwidth = 1;

        // インジケータラベル (+3 +2 +1 +0)
        gbc.gridx = 1;
        gbc.gridy = 1;
        JLabel[] offsetLabels = { new JLabel("+3"), new JLabel("+2"), new JLabel("+1"), new JLabel("+0") };
        for (JLabel label : offsetLabels) {
            label.setFont(fieldFont);
            memoryPanel.add(label, gbc);
            gbc.gridx++;
        }

        JTextField[] fieldsToDisplay;
        int startAddress, endAddress;

        if ("メモリ".equals(mode)) {
            fieldsToDisplay = memoryFields;
            startAddress = MEMORY_START;
            endAddress = MEMORY_END;
        } else {
            fieldsToDisplay = dataFields;
            startAddress = DATA_START;
            endAddress = DATA_END;
        }

        // 各行のアドレスとデータを表示
        for (int i = 0; i < fieldsToDisplay.length; i++) {
            int address = startAddress + i * 4;

            // アドレスラベル
            gbc.gridx = 0;
            gbc.gridy = i + 2; // 選択パネルとインジケータ行の下に配置
            JLabel addressLabel = new JLabel(String.format("0x%04X", address));
            addressLabel.setFont(fieldFont);
            memoryPanel.add(addressLabel, gbc);

            // データフィールド
            String[] byteValues = fieldsToDisplay[i].getText().split(" ");
            gbc.gridx = 1;
            for (int j = 0; j < 4; j++) {
                JTextField byteField = new JTextField(byteValues[j]);
                byteField.setFont(fieldFont);
                byteField.setEditable(false);
                memoryPanel.add(byteField, gbc);
                gbc.gridx++;
            }
        }

        memoryPanel.revalidate();
        memoryPanel.repaint();
    }

    public static void setFontAndSize(String fontName, int fontStyle, int fontSize) {
        fieldFont = new Font(fontName, fontStyle, fontSize);

        // メモリフィールドとデータフィールドのフォントを更新
        if (memoryFields != null) {
            for (JTextField field : memoryFields) {
                field.setFont(fieldFont);
            }
        }
        if (dataFields != null) {
            for (JTextField field : dataFields) {
                field.setFont(fieldFont);
            }
        }

        // memoryPanel 内のすべての JLabel と JTextField を対象にフォントを変更
        for (int i = 0; i < memoryPanel.getComponentCount(); i++) {
            java.awt.Component comp = memoryPanel.getComponent(i);
            if (comp instanceof JLabel) {
                ((JLabel) comp).setFont(fieldFont);
            } else if (comp instanceof JTextField) {
                ((JTextField) comp).setFont(fieldFont);
            }
        }

        // 必要に応じて再描画
        memoryPanel.revalidate();
        memoryPanel.repaint();
    }

    public static void updateMemory(String[] memoryValues, String[] dataValues, int pc) {
        // メモリ値を更新
        for (int i = 0; i < Math.min(memoryFields.length, memoryValues.length); i++) {
            if (memoryValues != null) {
                memoryFields[i].setText(formatToBytes(memoryValues[i].substring(2)));
            }
        }

        // データ値を更新
        for (int i = 0; i < Math.min(dataFields.length, dataValues.length); i++) {
            if (dataValues != null) {
                dataFields[i].setText(formatToBytes(dataValues[i].substring(2)));
            }
        }

        // 現在表示されているモードに基づいて表示を更新
        JTextField[] fieldsToDisplay;
        String[] valuesToUpdate;
        if ("メモリ".equals(selectedMode)) {
            fieldsToDisplay = memoryFields;
            valuesToUpdate = memoryValues;
        } else {
            fieldsToDisplay = dataFields;
            valuesToUpdate = dataValues;
        }
        
        updateView(selectedMode);

        // 必要に応じて再描画
        memoryPanel.revalidate();
        memoryPanel.repaint();
    }
    
    public static void updateDataRegion(String[] dataValues) {
        // データ値を更新（0x9000以降）
        for (int i = 0; i < Math.min(dataFields.length, dataValues.length); i++) {
            if (dataValues != null) {
                String newText = formatToBytes(dataValues[i].substring(2));
                if (!dataFields[i].getText().equals(newText)) { // 変更がある場合のみ更新
                    dataFields[i].setText(newText);
                }
            }
        }

        // 表示がデータモードの場合のみビューを更新
        if ("データ".equals(selectedMode)) {
            updateView("データ");
        }

        // 再描画の必要がある場合のみ更新
        memoryPanel.revalidate();
        memoryPanel.repaint();
    }



    private static String formatToBytes(String hexValue) {
        // 8桁の16進数を2桁ずつ分割して表示形式を整える
        return hexValue.replaceAll("(.{2})(?!$)", "$1 ");
    }
}

