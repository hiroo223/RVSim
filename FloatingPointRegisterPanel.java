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

public class FloatingPointRegisterPanel extends JScrollPane {
    static JTextField[] registerFields; // JTextFieldの配列を追加
    private static JLabel[] registerLabels; // JLabelの配列を追加
    private static String[] registerValues; // レジスタの値 (16進数で保持)

    private static Font fieldFont = new Font("Noto Sans JP", Font.PLAIN, 14); // デフォルトのフォント
    private static String displayFormat = "16"; // 表示形式 (16: 16進数, float, double)
    
    // 非表示レジスタのインデックスを保持するセット
    private static Set<Integer> hiddenRegisters = new HashSet<>();
    private static boolean isDefaultOrder = true; // デフォルト順フラグ
    private static int[] reorderedIndices = {0, 1, 2, 3, 4, 5, 6, 7, 28, 29, 30, 31, 8, 9, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 10, 11, 12, 13, 14, 15, 16, 17};
    private static String[] reorderedRegisters = {
        "ft0 (f0)", "ft1 (f1)", "ft2 (f2)", "ft3 (f3)", "ft4 (f4)", "ft5 (f5)", "ft6 (f6)", "ft7 (f7)",
        "ft8 (f28)", "ft9 (f29)", "ft10 (f30)", "ft11 (f31)",
        "fs0 (f8)", "fs1 (f9)", "fs2 (f18)", "fs3 (f19)", "fs4 (f20)", "fs5 (f21)", "fs6 (f22)", "fs7 (f23)",
        "fs8 (f24)", "fs9 (f25)", "fs10 (f26)", "fs11 (f27)",
        "fa0 (f10)", "fa1 (f11)", "fa2 (f12)", "fa3 (f13)", "fa4 (f14)", "fa5 (f15)", "fa6 (f16)", "fa7 (f17)"
    };

    public FloatingPointRegisterPanel() {
        JPanel registerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        getVerticalScrollBar().setUnitIncrement(25); // スクロールの速さを調節
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);

        // 表示形式選択ボックスの追加
        String[] formats = {"16進数", "float", "double"};
        JComboBox<String> formatSelector = new JComboBox<>(formats);
        formatSelector.setFont(new Font("Noto Sans JP", Font.PLAIN, 14));
        formatSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selected = (String) formatSelector.getSelectedItem();
                displayFormat = selected.equals("float") ? "float" : (selected.equals("double") ? "double" : "16");
                updateRegisterFormat(); // 表示形式を更新
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        registerPanel.add(formatSelector, gbc);

        // レジスタラベルとフィールドの初期化
        gbc.gridwidth = 1;
        String[] registers = {
    	    "ft0 (f0)", "ft1 (f1)", "ft2 (f2)", "ft3 (f3)", "ft4 (f4)", "ft5 (f5)", "ft6 (f6)", "ft7 (f7)",
    	    "fs0 (f8)", "fs1 (f9)", "fa0 (f10)", "fa1 (f11)", "fa2 (f12)", "fa3 (f13)", "fa4 (f14)", "fa5 (f15)",
    	    "fa6 (f16)", "fa7 (f17)", "fs2 (f18)", "fs3 (f19)", "fs4 (f20)", "fs5 (f21)", "fs6 (f22)", "fs7 (f23)",
    	    "fs8 (f24)", "fs9 (f25)", "fs10 (f26)", "fs11 (f27)", "ft8 (f28)", "ft9 (f29)", "ft10 (f30)", "ft11 (f31)"
	    };
        registerValues = new String[registers.length];
        registerFields = new JTextField[registers.length];
        registerLabels = new JLabel[registers.length];

        for (int i = 0; i < registers.length; i++) {
            registerValues[i] = "0x0000000000000000"; // 初期値

            gbc.gridx = 0;
            gbc.gridy = i + 1;
            gbc.weightx = 0.3;
            JLabel label = new JLabel(registers[i]);
            registerLabels[i] = label;
            registerPanel.add(label, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.7;
            JTextField valueField = new JTextField(registerValues[i]);
            valueField.setEditable(false);
            registerFields[i] = valueField;
            registerPanel.add(valueField, gbc);
        }

        setFontAndSize("Noto Sans JP", Font.PLAIN, 14);
        setViewportView(registerPanel);
    }

    
 // デフォルト順に切り替える
    public static void switchToDefaultOrder() {
        isDefaultOrder = true;
        updateRegisterOrder();
    }

    // グループ順に切り替える
    public static void switchToGroupedOrder() {
        isDefaultOrder = false;
        updateRegisterOrder();
    }

    // 表示順を更新するメソッド
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
        // レジスタ値を16進数で保持
        for (int i = 0; i < newValues.length; i++) {
            registerValues[i] = newValues[i];
        }
        updateRegisterFormat(); // 現在のフォーマットを適用
    }
    

    // 表示形式に基づいて値を更新
    private static void updateRegisterFormat() {
        for (int i = 0; i < registerFields.length; i++) {
            registerFields[i].setText(formatValue(registerValues[i]));
        }
    }

    // フォーマットに応じた値の変換
    private static String formatValue(String hexValue) {
        long longValue = Long.parseUnsignedLong(hexValue.replace("0x", ""), 16);

        if ("float".equals(displayFormat)) {
            int lowerBits = (int) (longValue & 0xFFFFFFFFL); // 下位32ビット抽出
            return String.valueOf(Float.intBitsToFloat(lowerBits));
        } else if ("double".equals(displayFormat)) {
            return String.valueOf(Double.longBitsToDouble(longValue)); // 倍精度浮動小数点
        } else {
            return String.format("0x%016X", longValue); // 16進数形式
        }
    }

    // フォントサイズとフォントを設定するメソッド
    public static void setFontAndSize(String fontName, int fontStyle, int fontSize) {
        fieldFont = new Font(fontName, fontStyle, fontSize);
        for (JLabel label : registerLabels) {
            label.setFont(fieldFont);
        }
        for (JTextField field : registerFields) {
            field.setFont(fieldFont);
        }
    }

    public static void updateRegisterColor(String[] previousValues, String[] newValues) {
        for (int i = 0; i < registerFields.length; i++) {
            String previousValue = previousValues[i];
            String rawValue = newValues[i]; // 更新する値 (16進数のまま)

            // 現在の表示形式に合わせて値をフォーマット
            String formattedValue = formatValue(rawValue);
            registerFields[i].setText(formattedValue); // フォーマット済みの値を設定

            // 値に変化があったか確認
            if (!previousValue.equals(rawValue)) {
                // 変化があった場合、薄い青色に変更
                registerFields[i].setBackground(new Color(173, 216, 230)); // 薄い青色
            } else {
                // 変化がなかった場合、元の色（薄い灰色）に戻す
                registerFields[i].setBackground(new Color(240, 240, 240)); // 元の色（薄い灰色）
            }
        }
    }
    
 // 非表示設定を適用
    private static void updateVisibility() {
        for (int i = 0; i < registerFields.length; i++) {
            boolean hidden = hiddenRegisters.contains(i);
            registerFields[i].setVisible(!hidden);
            registerLabels[i].setVisible(!hidden);
        }
    }
    
 // 非表示レジスタを設定
    public static void setHiddenRegisters(int[] indices) {
        hiddenRegisters.clear();
        for (int index : indices) {
            hiddenRegisters.add(index);
        }
        updateVisibility();
    }

    // 非表示レジスタをリセット
    public static void resetHiddenRegisters() {
        hiddenRegisters.clear();
        updateVisibility();
    }

 // 値の色をデフォルトに戻すメソッド
    public static void resetRegisterColors() {
        for (JTextField field : registerFields) {
            field.setBackground(new Color(240, 240, 240)); // デフォルトの薄い灰色
        }
    }
    
}
