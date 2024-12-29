import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class RegisterPanel extends JScrollPane {
    static JTextField[] registerFields; // JTextFieldの配列を追加
    private static JLabel[] registerLabels; // JLabelの配列を追加
    private static Set<Integer> hiddenRegisters = new HashSet<>();
    private static Font fieldFont = new Font("Noto Sans JP", Font.PLAIN, 14); // デフォルトのフォント

    private static boolean isDefaultOrder = true; // 現在の表示形式を記録するフラグ
    private static final String[] reorderedRegisters = { // 新しい並び順
        "t0  (x05)", "t1  (x06)", "t2  (x07)", "t3  (x28)", "t4  (x29)", "t5  (x30)", "t6  (x31)",
        "s0  (x08)", "s1  (x09)", "s2  (x18)", "s3  (x19)", "s4  (x20)", "s5  (x21)", "s6  (x22)", "s7  (x23)", 
        "s8  (x24)", "s9  (x25)", "s10 (x26)", "s11 (x27)",
        "a0  (x10)", "a1  (x11)", "a2  (x12)", "a3  (x13)", "a4  (x14)", "a5  (x15)", "a6  (x16)", "a7  (x17)",
        "zero(x00)", "ra  (x01)", "sp  (x02)", "gp  (x03)", "tp  (x04)"
    };
    private static final int[] reorderedIndices = {5, 6, 7, 28, 29, 30, 31, 8, 9, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 10, 11, 12, 13, 14, 15, 16, 17, 0, 1, 2, 3, 4};

    private static String[] registerValues = { // レジスタの初期値
        "0x00000000", "0x00000000", "0x00007FFF", "0x10000000", "0x00000000",
        "0x00000000", "0x00000000", "0x00000000", "0x00000000", "0x00000000",
        "0x00000000", "0x00000000", "0x00000000", "0x00000000", "0x00000000",
        "0x00000000", "0x00000000", "0x00000000", "0x00000000", "0x00000000",
        "0x00000000", "0x00000000", "0x00000000", "0x00000000", "0x00000000",
        "0x00000000", "0x00000000", "0x00000000", "0x00000000", "0x00000000",
        "0x00000000", "0x00000000"
    };

    private static String format = "16"; // デフォルトの進数形式 (16進数)

    public RegisterPanel() {
        JPanel registerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        getVerticalScrollBar().setUnitIncrement(25); // スクロールの速さを調節
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);

        // 進数選択ボックスを作成
        String[] formats = {"16進数", "10進数", "2進数"};
        JComboBox<String> formatSelector = new JComboBox<>(formats);
        formatSelector.setFont(new Font("Noto Sans JP", Font.PLAIN, 14));
        formatSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedFormat = (String) formatSelector.getSelectedItem();
                if ("10進数".equals(selectedFormat)) {
                    format = "10";
                } else if ("16進数".equals(selectedFormat)) {
                    format = "16";
                } else if ("2進数".equals(selectedFormat)) {
                    format = "2";
                }
                updateRegisterFormat(); // 表示形式を更新
            }
        });

        // 進数選択ボックスを上部に配置
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        registerPanel.add(formatSelector, gbc);

        // レジスタラベルとフィールドの初期化
        gbc.gridwidth = 1;
        String[] registers = {"zero(x00)", "ra  (x01)", "sp  (x02)", "gp  (x03)", "tp  (x04)", "t0  (x05)", "t1  (x06)", "t2  (x07)",
            "s0  (x08)", "s1  (x09)", "a0  (x10)", "a1  (x11)", "a2  (x12)", "a3  (x13)", "a4  (x14)", "a5  (x15)",
            "a6  (x16)", "a7  (x17)", "s2  (x18)", "s3  (x19)", "s4  (x20)", "s5  (x21)", "s6  (x22)", "s7  (x23)",
            "s8  (x24)", "s9  (x25)", "s10(x26)", "s11(x27)", "t3  (x28)", "t4  (x29)", "t5  (x30)", "t6  (x31)"};

        registerFields = new JTextField[registers.length];
        registerLabels = new JLabel[registers.length];

        for (int i = 0; i < registers.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i + 1;
            gbc.weightx = 0.3;
            JLabel label = new JLabel(registers[i]);
            registerLabels[i] = label;
            registerPanel.add(label, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.7;
            JTextField valueField = new JTextField(formatValue(registerValues[i]));
            valueField.setEditable(false);
            registerFields[i] = valueField;
            registerPanel.add(valueField, gbc);
        }

        setFontAndSize("Noto Sans JP", Font.PLAIN, 14);
        setViewportView(registerPanel);
    }

    // デフォルトの順番 (x0-x31 順) に切り替える
    public static void switchToDefaultOrder() {
        isDefaultOrder = true;
        updateRegisterOrder();
    }
    
    // グループ順 (t0, t1,...,s0, s1,...,a0, a1,...) に切り替える
    public static void switchToGroupedOrder() {
        isDefaultOrder = false;
        updateRegisterOrder();
    }
    
 // 表示順を更新するメソッド (内部的に使用)
    private static void updateRegisterOrder() {
        JPanel parentPanel = (JPanel) registerFields[0].getParent();
        GridBagLayout layout = (GridBagLayout) parentPanel.getLayout();

        parentPanel.removeAll(); // 全てのコンポーネントを一旦削除

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);

        if (isDefaultOrder) {
            // デフォルト順に配置
            for (int i = 0; i < registerFields.length; i++) {
                gbc.gridx = 0;
                gbc.gridy = i + 1;
                gbc.weightx = 0.3;
                parentPanel.add(registerLabels[i], gbc);

                gbc.gridx = 1;
                gbc.weightx = 0.7;
                parentPanel.add(registerFields[i], gbc);
            }
        } else {
            // グループ順に配置
            for (int i = 0; i < reorderedIndices.length; i++) {
                int originalIndex = reorderedIndices[i];

                gbc.gridx = 0;
                gbc.gridy = i + 1;
                gbc.weightx = 0.3;
                parentPanel.add(registerLabels[originalIndex], gbc);

                gbc.gridx = 1;
                gbc.weightx = 0.7;
                parentPanel.add(registerFields[originalIndex], gbc);

                // ラベルを更新
                registerLabels[originalIndex].setText(reorderedRegisters[i]);
            }
        }

        parentPanel.revalidate();
        parentPanel.repaint();
    }


    // レジスタの値を更新
    public static void updateRegister(String[] newValues) {
        registerValues = newValues;
        updateRegisterFormat(); // 表示形式を更新
    }

    // フォントサイズとフォントを設定するメソッドを追加
    public static void setFontAndSize(String fontName, int fontStyle, int fontSize) {
        fieldFont = new Font(fontName, fontStyle, fontSize);
        for (JLabel label : registerLabels) {
            label.setFont(fieldFont);
        }
        for (JTextField field : registerFields) {
            field.setFont(fieldFont);
        }
    }

    // 値の色を更新
    public static void updateRegisterColor(String[] previousValues, String[] newValues) {
        for (int i = 0; i < registerFields.length; i++) {
            registerFields[i].setText(formatValue(newValues[i]));

            if (!previousValues[i].equals(newValues[i])) {
                registerFields[i].setBackground(new Color(173, 216, 230)); // 薄い青色
            } else {
                registerFields[i].setBackground(new Color(240, 240, 240)); // 薄い灰色
            }
        }
    }

    // 非表示設定
    public static void setHiddenRegisters(int[] indices) {
        hiddenRegisters.clear();
        for (int index : indices) {
            hiddenRegisters.add(index);
        }
        updateVisibility();
    }

    public static Set<Integer> getHiddenRegisterIndices() {
        return new HashSet<>(hiddenRegisters);
    }

    public static void resetHiddenRegisters() {
        hiddenRegisters.clear();
        updateVisibility();
    }

    private static void updateVisibility() {
        for (int i = 0; i < registerFields.length; i++) {
            boolean hidden = hiddenRegisters.contains(i);
            registerFields[i].setVisible(!hidden);
            registerLabels[i].setVisible(!hidden);
        }
    }

    // 表示形式を更新
    private static void updateRegisterFormat() {
        for (int i = 0; i < registerFields.length; i++) {
            registerFields[i].setText(formatValue(registerValues[i]));
        }
    }

    // 値を進数形式にフォーマット
    private static String formatValue(String value) {
        int intValue = Integer.parseUnsignedInt(value.replace("0x", ""), 16);
        if ("10".equals(format)) {
            return String.valueOf(intValue); // 10進数形式
        } else if ("2".equals(format)) {
            return Integer.toBinaryString(intValue); // 2進数形式
        } else {
            return String.format("0x%08X", intValue); // 16進数形式
        }
    }
    public static void resetRegisterColors() {
        for (JTextField field : registerFields) {
            field.setBackground(new Color(240, 240, 240)); // デフォルトの薄い灰色
        }
    }
}
