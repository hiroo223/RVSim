import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class EditorPanel extends JPanel {
	LineNumberView lineNumberView;

    public EditorPanel(Main m) {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // タイトルラベル
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        add(new JLabel("エディター", JLabel.CENTER), gbc);

        // JTextPane (Main.textArea) の初期化
        m.textArea = new JTextPane();
        m.textArea.setFont(new Font("Noto Sans JP", Font.PLAIN, 16));

        
        // 折り返しを無効化するカスタム EditorKit を設定
        m.textArea.setEditorKit(new StyledEditorKit() {
            @Override
            public ViewFactory getViewFactory() {
                return new StyledViewFactory();
            }
        });

        JScrollPane textScrollPane = new JScrollPane(m.textArea);
        textScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        textScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        lineNumberView = new LineNumberView(m.textArea);
        textScrollPane.setRowHeaderView(lineNumberView);

        
        // ハイライトするキーワードと色を設定
        Map<String, Color> keywordColors = new HashMap<>();
        
        // Rタイプ
        keywordColors.put("or", Color.RED);
        keywordColors.put("and", Color.RED);
        keywordColors.put("xor", Color.RED);
        keywordColors.put("add", Color.RED);
        keywordColors.put("sub", Color.RED);
        keywordColors.put("srl", Color.RED);
        keywordColors.put("sra", Color.RED);
        keywordColors.put("sll", Color.RED);
        keywordColors.put("slt", Color.RED);
        keywordColors.put("sltu", Color.RED);
        
        keywordColors.put("mul", Color.RED);       // 乗算
        keywordColors.put("mulh", Color.RED);     // 高位乗算
        keywordColors.put("mulhsu", Color.RED);   // 符号付きx符号なし高位乗算
        keywordColors.put("mulhu", Color.RED);    // 符号なし高位乗算
        keywordColors.put("div", Color.RED);      // 符号付き除算
        keywordColors.put("divu", Color.RED);     // 符号なし除算
        keywordColors.put("rem", Color.RED);      // 符号付き剰余
        keywordColors.put("remu", Color.RED);     // 符号なし剰余
        
        keywordColors.put("fmadd.s", Color.RED);   // 浮動小数点乗算加算
        keywordColors.put("fmsub.s", Color.RED);   // 浮動小数点乗算減算
        keywordColors.put("fmmsub.s", Color.RED);  // 浮動小数点負の乗算減算
        keywordColors.put("fmmadd.s", Color.RED);  // 浮動小数点負の乗算加算
        keywordColors.put("fadd.s", Color.RED);    // 浮動小数点加算
        keywordColors.put("fsub.s", Color.RED);    // 浮動小数点減算
        keywordColors.put("fmul.s", Color.RED);    // 浮動小数点乗算
        keywordColors.put("fdiv.s", Color.RED);    // 浮動小数点除算
        keywordColors.put("fsgnj.s", Color.RED);   // 浮動小数点符号設定
        keywordColors.put("fsgnjn.s", Color.RED);  // 浮動小数点符号反転設定
        keywordColors.put("fsgnjx.s", Color.RED);  // 浮動小数点符号 XOR 設定
        keywordColors.put("fmin.s", Color.RED);    // 浮動小数点最小値
        keywordColors.put("fmax.s", Color.RED);    // 浮動小数点最大値
        keywordColors.put("fsqrt.s", Color.RED);   // 浮動小数点平方根
        keywordColors.put("fmv.w.x", Color.RED);   // 浮動小数点移動
        
        keywordColors.put("fmadd.d", Color.RED);   // 倍精度浮動小数点乗算加算
        keywordColors.put("fmsub.d", Color.RED);   // 倍精度浮動小数点乗算減算
        keywordColors.put("fnmsub.d", Color.RED);  // 倍精度浮動小数点負の乗算減算
        keywordColors.put("fnmadd.d", Color.RED);  // 倍精度浮動小数点負の乗算加算
        keywordColors.put("fadd.d", Color.RED);    // 倍精度浮動小数点加算
        keywordColors.put("fsub.d", Color.RED);    // 倍精度浮動小数点減算
        keywordColors.put("fmul.d", Color.RED);    // 倍精度浮動小数点乗算
        keywordColors.put("fdiv.d", Color.RED);    // 倍精度浮動小数点除算
        keywordColors.put("fsqrt.d", Color.RED);   // 倍精度浮動小数点平方根
        keywordColors.put("fsgnj.d", Color.RED);   // 倍精度浮動小数点符号設定
        keywordColors.put("fsgnjn.d", Color.RED);  // 倍精度浮動小数点符号反転設定
        keywordColors.put("fsgnjx.d", Color.RED);  // 倍精度浮動小数点符号 XOR 設定
        keywordColors.put("fmin.d", Color.RED);    // 倍精度浮動小数点最小値
        keywordColors.put("fmax.d", Color.RED);    // 倍精度浮動小数点最大値
        keywordColors.put("fcvt.w.d", Color.RED);  // 倍精度→符号付き整数変換
        keywordColors.put("fcvt.wu.d", Color.RED); // 倍精度→符号なし整数変換
        keywordColors.put("fcvt.d.w", Color.RED);  // 符号付き整数→倍精度変換
        keywordColors.put("fcvt.d.wu", Color.RED); // 符号なし整数→倍精度変換
        keywordColors.put("fmv.x.d", Color.RED);   // 倍精度浮動小数点から整数レジスタへの移動
        keywordColors.put("fmv.d.x", Color.RED);   // 整数レジスタから倍精度浮動小数点レジスタへの移動
        keywordColors.put("fclass.d", Color.RED);  // 倍精度浮動小数点の分類

        
        // Iタイプ
        keywordColors.put("ori", Color.BLUE);
        keywordColors.put("andi", Color.BLUE);
        keywordColors.put("xori", Color.BLUE);
        keywordColors.put("addi", Color.BLUE);
        keywordColors.put("subi", Color.BLUE);
        keywordColors.put("slti", Color.BLUE);
        keywordColors.put("sltiu", Color.BLUE);
        keywordColors.put("jalr", Color.BLUE);
        keywordColors.put("lb", Color.BLUE);
        keywordColors.put("lh", Color.BLUE);
        keywordColors.put("lw", Color.BLUE);
        keywordColors.put("flw", Color.BLUE);
        keywordColors.put("fld", Color.BLUE);
        keywordColors.put("lbu", Color.BLUE);
        keywordColors.put("lhu", Color.BLUE);
        keywordColors.put("ecall", Color.BLUE);
        
        
        // Sタイプ
        keywordColors.put("sw", new Color(0, 100, 0)); // DARK GREEN
        keywordColors.put("sh", new Color(0, 100, 0)); 
        keywordColors.put("sb", new Color(0, 100, 0));
        keywordColors.put("fsw", new Color(0, 100, 0));
        keywordColors.put("fsd", new Color(0, 100, 0));
        
        
        
        // Bタイプ
        keywordColors.put("beq", Color.ORANGE);
        keywordColors.put("bne", Color.ORANGE);
        keywordColors.put("blt", Color.ORANGE);
        keywordColors.put("bge", Color.ORANGE);
        keywordColors.put("bltu", Color.ORANGE);
        keywordColors.put("bgeu", Color.ORANGE);
        
        
        // Uタイプ
        keywordColors.put("lui", Color.MAGENTA);
        keywordColors.put("auipc", Color.MAGENTA);
        
        
        // Jタイプ
        keywordColors.put("jal", new Color(255, 105, 180)); // HOT PINK

        
        
        // 疑似命令
        keywordColors.put("la", new Color(0, 206, 209)); // Cyan
        keywordColors.put("nop", new Color(0, 206, 209)); 
        keywordColors.put("li", new Color(0, 206, 209)); 
        keywordColors.put("mv", new Color(0, 206, 209)); 
        keywordColors.put("not", new Color(0, 206, 209)); 
        keywordColors.put("neg", new Color(0, 206, 209)); 
        keywordColors.put("seqz", new Color(0, 206, 209)); 
        keywordColors.put("snez", new Color(0, 206, 209)); 
        keywordColors.put("sltz", new Color(0, 206, 209)); 
        keywordColors.put("sgtz", new Color(0, 206, 209)); 
        keywordColors.put("beqz", new Color(0, 206, 209)); 
        keywordColors.put("bnez", new Color(0, 206, 209)); 
        keywordColors.put("blez", new Color(0, 206, 209)); 
        keywordColors.put("bgez", new Color(0, 206, 209)); 
        keywordColors.put("bltz", new Color(0, 206, 209)); 
        keywordColors.put("bgtz", new Color(0, 206, 209)); 
        keywordColors.put("bgt", new Color(0, 206, 209)); 
        keywordColors.put("ble", new Color(0, 206, 209)); 
        keywordColors.put("bgtu", new Color(0, 206, 209)); 
        keywordColors.put("bleu", new Color(0, 206, 209)); 
        keywordColors.put("j", new Color(0, 206, 209)); 
        keywordColors.put("jr", new Color(0, 206, 209)); 
        keywordColors.put("ret", new Color(0, 206, 209)); 
        keywordColors.put("call", new Color(0, 206, 209)); 
        keywordColors.put("tail", new Color(0, 206, 209)); 
        keywordColors.put("fence", new Color(0, 206, 209)); 
 
     // 暗い紫色に設定 (Color(128, 0, 128))
        Color darkPurple = new Color(128, 0, 128);

        // レジスタ名を暗い紫色に設定
        keywordColors.put("x0", darkPurple); // zero
        keywordColors.put("x1", darkPurple); // ra
        keywordColors.put("x2", darkPurple); // sp
        keywordColors.put("x3", darkPurple); // gp
        keywordColors.put("x4", darkPurple); // tp
        keywordColors.put("x5", darkPurple); // t0
        keywordColors.put("x6", darkPurple); // t1
        keywordColors.put("x7", darkPurple); // t2
        keywordColors.put("x8", darkPurple); // s0
        keywordColors.put("x9", darkPurple); // s1
        keywordColors.put("x10", darkPurple); // a0
        keywordColors.put("x11", darkPurple); // a1
        keywordColors.put("x12", darkPurple); // a2
        keywordColors.put("x13", darkPurple); // a3
        keywordColors.put("x14", darkPurple); // a4
        keywordColors.put("x15", darkPurple); // a5
        keywordColors.put("x16", darkPurple); // a6
        keywordColors.put("x17", darkPurple); // a7
        keywordColors.put("x18", darkPurple); // s2
        keywordColors.put("x19", darkPurple); // s3
        keywordColors.put("x20", darkPurple); // s4
        keywordColors.put("x21", darkPurple); // s5
        keywordColors.put("x22", darkPurple); // s6
        keywordColors.put("x23", darkPurple); // s7
        keywordColors.put("x24", darkPurple); // s8
        keywordColors.put("x25", darkPurple); // s9
        keywordColors.put("x26", darkPurple); // s10
        keywordColors.put("x27", darkPurple); // s11
        keywordColors.put("x28", darkPurple); // t3
        keywordColors.put("x29", darkPurple); // t4
        keywordColors.put("x30", darkPurple); // t5
        keywordColors.put("x31", darkPurple); // t6

        // tレジスタ（t0〜t6）も暗い紫色に設定
        keywordColors.put("t0", darkPurple);
        keywordColors.put("t1", darkPurple);
        keywordColors.put("t2", darkPurple);
        keywordColors.put("t3", darkPurple);
        keywordColors.put("t4", darkPurple);
        keywordColors.put("t5", darkPurple);
        keywordColors.put("t6", darkPurple);


        // sレジスタ（s0〜s11）も暗い紫色に設定
        keywordColors.put("s0", darkPurple);
        keywordColors.put("s1", darkPurple);
        keywordColors.put("s2", darkPurple);
        keywordColors.put("s3", darkPurple);
        keywordColors.put("s4", darkPurple);
        keywordColors.put("s5", darkPurple);
     	keywordColors.put("s6", darkPurple);
     	keywordColors.put("s7", darkPurple);
     	keywordColors.put("s8", darkPurple);
     	keywordColors.put("s9", darkPurple);
     	keywordColors.put("s10", darkPurple);
     	keywordColors.put("s11", darkPurple);
     	
     	 // sレジスタ（s0〜s11）も暗い紫色に設定
        keywordColors.put("a0", darkPurple);
        keywordColors.put("a1", darkPurple);
        keywordColors.put("a2", darkPurple);
        keywordColors.put("a3", darkPurple);
        keywordColors.put("a4", darkPurple);
        keywordColors.put("a5", darkPurple);
     	keywordColors.put("a6", darkPurple);
     	keywordColors.put("a7", darkPurple);

     	// その他のレジスタ名を追加
     	keywordColors.put("ra", darkPurple);  // 戻りアドレス
     	keywordColors.put("zero", darkPurple); // 常に0
     	keywordColors.put("sp", darkPurple);  // スタックポインタ
     	keywordColors.put("gp", darkPurple);  // グローバルポインタ
     	keywordColors.put("tp", darkPurple);  // スレッドポインタ
     	
     // レジスタ名を暗い紫色に設定
     	keywordColors.put("f0", darkPurple);  // f0
     	keywordColors.put("f1", darkPurple);  // f1
     	keywordColors.put("f2", darkPurple);  // f2
     	keywordColors.put("f3", darkPurple);  // f3
     	keywordColors.put("f4", darkPurple);  // f4
     	keywordColors.put("f5", darkPurple);  // f5
     	keywordColors.put("f6", darkPurple);  // f6
     	keywordColors.put("f7", darkPurple);  // f7
     	keywordColors.put("f8", darkPurple);  // f8
     	keywordColors.put("f9", darkPurple);  // f9
     	keywordColors.put("f10", darkPurple); // f10
     	keywordColors.put("f11", darkPurple); // f11
     	keywordColors.put("f12", darkPurple); // f12
     	keywordColors.put("f13", darkPurple); // f13
     	keywordColors.put("f14", darkPurple); // f14
     	keywordColors.put("f15", darkPurple); // f15
     	keywordColors.put("f16", darkPurple); // f16
     	keywordColors.put("f17", darkPurple); // f17
     	keywordColors.put("f18", darkPurple); // f18
     	keywordColors.put("f19", darkPurple); // f19
     	keywordColors.put("f20", darkPurple); // f20
     	keywordColors.put("f21", darkPurple); // f21
     	keywordColors.put("f22", darkPurple); // f22
     	keywordColors.put("f23", darkPurple); // f23
     	keywordColors.put("f24", darkPurple); // f24
     	keywordColors.put("f25", darkPurple); // f25
     	keywordColors.put("f26", darkPurple); // f26
     	keywordColors.put("f27", darkPurple); // f27
     	keywordColors.put("f28", darkPurple); // f28
     	keywordColors.put("f29", darkPurple); // f29
     	keywordColors.put("f30", darkPurple); // f30
     	keywordColors.put("f31", darkPurple); // f31

     	// ft0 ~ ft11
     	keywordColors.put("ft0", darkPurple); // ft0
     	keywordColors.put("ft1", darkPurple); // ft1
     	keywordColors.put("ft2", darkPurple); // ft2
     	keywordColors.put("ft3", darkPurple); // ft3
     	keywordColors.put("ft4", darkPurple); // ft4
     	keywordColors.put("ft5", darkPurple); // ft5
     	keywordColors.put("ft6", darkPurple); // ft6
     	keywordColors.put("ft7", darkPurple); // ft7
     	keywordColors.put("ft8", darkPurple); // ft8
     	keywordColors.put("ft9", darkPurple); // ft9
     	keywordColors.put("ft10", darkPurple); // ft10
     	keywordColors.put("ft11", darkPurple); // ft11

     	// fs0 ~ fs11
     	keywordColors.put("fs0", darkPurple); // fs0
     	keywordColors.put("fs1", darkPurple); // fs1
     	keywordColors.put("fs2", darkPurple); // fs2
     	keywordColors.put("fs3", darkPurple); // fs3
     	keywordColors.put("fs4", darkPurple); // fs4
     	keywordColors.put("fs5", darkPurple); // fs5
     	keywordColors.put("fs6", darkPurple); // fs6
     	keywordColors.put("fs7", darkPurple); // fs7
     	keywordColors.put("fs8", darkPurple); // fs8
     	keywordColors.put("fs9", darkPurple); // fs9
     	keywordColors.put("fs10", darkPurple); // fs10
     	keywordColors.put("fs11", darkPurple); // fs11


     	keywordColors.put("fa0", darkPurple); // fa0
     	keywordColors.put("fa1", darkPurple); // fa1
     	keywordColors.put("fa2", darkPurple); // fa2
     	keywordColors.put("fa3", darkPurple); // fa3
     	keywordColors.put("fa4", darkPurple); // fa4
     	keywordColors.put("fa5", darkPurple); // fa5
     	keywordColors.put("fa6", darkPurple); // fa6
     	keywordColors.put("fa7", darkPurple); // fa7

     	keywordColors.put("word", Color.BLACK);  // スレッドポインタ

     	m.textArea.getDocument().addDocumentListener((SimpleDocumentListener) e -> lineNumberView.repaint());
     	m.textArea.addComponentListener(new java.awt.event.ComponentAdapter() {
     	    @Override
     	    public void componentResized(java.awt.event.ComponentEvent e) {
     	        lineNumberView.revalidate();
     	        lineNumberView.repaint();
     	    }
     	});

        m.textArea.getDocument().addDocumentListener(new DocumentListener() {
            private Timer timer;
            private long lastModifiedTime = 0;  // 最後に変更があった時間

            @Override
            public void insertUpdate(DocumentEvent e) {
                scheduleHighlight(e.getOffset(), e.getLength());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                scheduleHighlight(e.getOffset(), e.getLength());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                scheduleHighlight(e.getOffset(), e.getLength());
            }

            private void scheduleHighlight(int offset, int length) {
                // タイマーが存在し、かつ間隔が短すぎる場合は新たにタイマーを発火させない
                if (timer != null) {
                    timer.stop();
                }

                long currentTime = System.currentTimeMillis();
                if (currentTime - lastModifiedTime < 300) {  // 変更が300ms以内なら次のイベントを処理しない
                    return;  // 入力が早すぎる場合は何もしない
                }
                
                lastModifiedTime = currentTime;

                timer = new Timer(300, event -> {  // 300ms後にハイライト処理を実行
                    highlightText(offset, length);
                    m.updateTitle(true);
                    timer.stop();
                });

                timer.setRepeats(false);  // 繰り返しなし
                timer.start();
            }
            
            private void highlightText(int offset, int length) {
                StyledDocument doc = m.textArea.getStyledDocument();
                Style defaultStyle = m.textArea.addStyle("Default", null);
                StyleConstants.setForeground(defaultStyle, Color.BLACK);

                // コメントのスタイル
                Style commentStyle = m.textArea.addStyle("Comment", null);
                StyleConstants.setForeground(commentStyle, Color.GRAY);

                SwingUtilities.invokeLater(() -> {
                    try {
                        // 全テキスト取得
                        String text = doc.getText(0, doc.getLength());

                        // 処理範囲を限定
                        int start = Math.max(0, offset - 50); // 50文字前から処理
                        int end = Math.min(text.length(), offset + length + 50); // 50文字後まで処理
                        doc.setCharacterAttributes(start, end - start, defaultStyle, true);

                        // コメント部分のハイライト（# をコメントとして扱う）
                        int lineStart = 0;
                        while (lineStart < text.length()) {
                            int lineEnd = text.indexOf('\n', lineStart);
                            if (lineEnd == -1) {
                                lineEnd = text.length();
                            }

                            // '#' を見つけた場合、その行の終端まで灰色に変更
                            int commentStart = text.indexOf("#", lineStart);
                            if (commentStart != -1 && commentStart < lineEnd) {
                                doc.setCharacterAttributes(commentStart, lineEnd - commentStart, commentStyle, true);
                            }

                            lineStart = lineEnd + 1; // 次の行に進む
                        }

                        // キーワードに一致する部分をハイライト
                        for (Map.Entry<String, Color> entry : keywordColors.entrySet()) {
                            String keyword = entry.getKey();
                            Color color = entry.getValue();

                            Style highlightStyle = m.textArea.addStyle(keyword, null);
                            StyleConstants.setForeground(highlightStyle, color);

                            // キーワードに完全一致する箇所のみをハイライト（後に`,`が付いていても許可）
                            String regex = "\\b" + keyword + "\\b(?=\\s|,|$)"; // 単語境界 + 空白, コンマ, または行末
                            int index = 0;

                            while ((index = text.indexOf(keyword, index)) != -1) {
                                // コメント部分を無視する（競合時は灰色を優先）
                                boolean insideComment = false;

                                // コメントの開始位置を確認
                                int commentStart = text.lastIndexOf("#", index);
                                if (commentStart != -1 && commentStart < index) {
                                    // キーワードがコメント内に存在する場合
                                    insideComment = text.indexOf('\n', commentStart) == -1 || index < text.indexOf('\n', commentStart);
                                }

                                // コメント部分をスキップ
                                if (!insideComment) {
                                    // 前後の文字が単語境界か確認しつつ、`,`が続いても許容
                                    if ((index == 0 || !Character.isLetterOrDigit(text.charAt(index - 1))) &&
                                        (index + keyword.length() >= text.length() ||
                                         !Character.isLetterOrDigit(text.charAt(index + keyword.length()))) &&
                                        (index + keyword.length() >= text.length() ||
                                         (Character.isWhitespace(text.charAt(index + keyword.length())) ||
                                          text.charAt(index + keyword.length()) == ','))) {
                                        doc.setCharacterAttributes(index, keyword.length(), highlightStyle, true);
                                    }
                                }

                                index += keyword.length();
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }





/**
 * レジスタキーワードかどうかを判定
 * - 直前に `0x` のような十六進数の表記が含まれる場合は除外
 */
	private boolean isRegisterKeyword(String keyword, String text, int index) {
	    // 前の文字列が "0x" または他の無効な条件を持つ場合をチェック
	    if (index > 1 && text.substring(index - 2, index).equals("0x")) {
	        return false; // 十六進数の一部
	    }
	
	    // 数字部分を確認
	    if (keyword.matches("x\\d+")) {
	        int regNumber = Integer.parseInt(keyword.substring(1)); // "x"を除去して数字を抽出
	        return regNumber >= 0 && regNumber <= 31; // 0〜31の範囲内であれば有効
	    }
	    return false;
	}



        });


        // エディタ部分のレイアウト
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.7;
        gbc.gridy = 1;
        add(textScrollPane, gbc);

        // コンソールラベル
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.gridy = 2;
        add(new JLabel("コンソール", JLabel.CENTER), gbc);

        // コンソールエリア
        m.consoleArea = new JTextArea();
        m.consoleArea.setFont(new Font("MS Gothic", Font.PLAIN, 16));
        m.consoleArea.setEditable(false);

        JScrollPane consoleScrollPane = new JScrollPane(m.consoleArea);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.3;
        gbc.gridy = 3;
        add(consoleScrollPane, gbc);
    }
    
    
    
    // 内部クラスとして StyledViewFactory を定義
    private static class StyledViewFactory implements ViewFactory {
        @Override
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                if (kind.equals(AbstractDocument.ContentElementName)) {
                    return new LabelView(elem);
                } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                    return new ParagraphView(elem) {
                        @Override
                        public void layout(int width, int height) {
                            super.layout(Short.MAX_VALUE, height); // 折り返し幅を最大値に設定
                        }

                        @Override
                        public float getMinimumSpan(int axis) {
                            return super.getPreferredSpan(axis); // 折り返さないように最小幅を変更
                        }
                    };
                } else if (kind.equals(AbstractDocument.SectionElementName)) {
                    return new BoxView(elem, View.Y_AXIS);
                } else if (kind.equals(StyleConstants.ComponentElementName)) {
                    return new ComponentView(elem);
                } else if (kind.equals(StyleConstants.IconElementName)) {
                    return new IconView(elem);
                }
            }
            return new LabelView(elem);
        }
        
        
    }
    
}
