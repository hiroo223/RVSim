import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.undo.UndoManager;

public class Main extends JFrame {
	
	private UndoManager undoManager = new UndoManager(); // UndoManager を追加
	JTextPane textArea; // JTextPaneに変更
    JTextArea consoleArea;
    static JTextPane detailConsole; // JTextPaneに変更
    static JTextArea logArea;
    public JTabbedPane editorTabbedPane;
    
    private static int detailAndLogFontSize = 14; // 詳細情報とログエリアのフォントサイズの初期値	
    
    String fileName = "新規ファイル"; // 初期ファイル名
    private boolean isModified = false; // ファイルが変更されたかどうか
    File currentFile = null; // 現在のファイル（保存先）
    private int fontSize = 14; // フォントサイズの初期値
    
    EditorPanel editorPanel;
   
    
    private void createApp() {
    	setTitle(fileName + " - RVSim");
    	setSize(800, 600);
    	setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    	setLayout(new GridBagLayout());
    	setMinimumSize(new Dimension(800, 600));
    
    	GridBagConstraints gbc = new GridBagConstraints();
    
    	// メニューバーを設定
    	setJMenuBar(MenuBarCreator.createMenuBar(Main.this));
    
    	// 左側のエディタ部分をタブ付きパネルに変更
    	editorTabbedPane = new JTabbedPane();
    
    	// エディタタブ
    	editorPanel = new EditorPanel(this);
    	editorTabbedPane.addTab("エディタ", editorPanel);
    
    	// 詳細タブ（詳細情報 + ログエリア）
    	JPanel detailPanel = new JPanel(new BorderLayout());
    
    	// 詳細情報エリア（上部 70%）
    	detailConsole = new JTextPane();
    	detailConsole.setEditable(false); // 書き込み不可に設定
    	detailConsole.setText("詳細情報がここに表示されます...");
    
    	// 折り返しを無効化するカスタム EditorKit を設定
    	detailConsole.setEditorKit(new StyledEditorKit() {
    		@Override
    		public ViewFactory getViewFactory() {
    			return new StyledViewFactory();
    		}
    	});
    
    	JScrollPane detailScrollPane = new JScrollPane(detailConsole);
    	detailScrollPane.setPreferredSize(new Dimension(0, 0)); // 上部のリサイズを自動調整

    
    	// ログエリア（下部 30%）
    	logArea = new JTextArea();
    	logArea.setEditable(false); // 書き込み不可に設定
    	logArea.setText("ログがここに表示されます...");
    	JScrollPane logScrollPane = new JScrollPane(logArea);
    	logScrollPane.setPreferredSize(new Dimension(0, 150)); // 下部を固定サイズに設定

    	// 詳細パネル全体にエリアを配置
    	detailPanel.add(detailScrollPane, BorderLayout.CENTER); // 上部を中央に配置
    	detailPanel.add(logScrollPane, BorderLayout.SOUTH);     // ログエリアを下部に配置

    	editorTabbedPane.addTab("詳細", detailPanel);
	
    	setFontAndSize("Noto Sans JP", Font.PLAIN, detailAndLogFontSize);
	
    	// エディタ部分のタブ付きパネルを追加
    	gbc.weightx = 0.65; // 横方向の比率を65%に設定
    	gbc.weighty = 1.0;  // 縦方向はフル（1.0）に使用
    	gbc.fill = GridBagConstraints.BOTH; // 余白を埋めるように全方向へ展開
    	gbc.gridx = 0; // 左側（最初の列）
    	gbc.gridy = 0; // 最初の行
    	add(editorTabbedPane, gbc);

    	// 右側のタブ付きパネルを追加
    	JTabbedPane tabbedPane = new JTabbedPane();
    	tabbedPane.addTab("レジスタ", new RegisterPanel());
    	tabbedPane.addTab("浮動小数点レジスタ", new FloatingPointRegisterPanel());
    	tabbedPane.addTab("メモリ", new MemoryPanel());
    
    	gbc.weightx = 0.35; // 横方向の比率を35%に設定
    	gbc.gridx = 1; // 右側（次の列）
    	add(tabbedPane, gbc);

    	// ショートカットキーを設定
    	setKeyboardShortcuts();

    	setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // 自動で閉じないように設定
    	addWindowListener(new WindowAdapter() {
    		@Override
    		public void windowClosing(WindowEvent e) {
    			// 変更があった場合、確認メッセージを表示
    			if (isModified) {
    				int option = JOptionPane.showConfirmDialog(
    						Main.this,
    						"保存されていない変更がありますが、終了してもよろしいですか？",
    						"確認",
    						JOptionPane.YES_NO_OPTION,
    						JOptionPane.WARNING_MESSAGE
    						);

    				// "はい" を選択した場合のみ終了
    				if (option == JOptionPane.YES_OPTION) {
    					dispose(); // リソースを解放し、ウィンドウを閉じる
    				}
    			} else {
    				// 変更がない場合はそのまま終了
    				dispose(); // リソースを解放し、ウィンドウを閉じる
    			}
    		}
    	});
    
    	// アイコンを取得
        try {
            ImageIcon originalIcon = new ImageIcon("resources/images/icon.png"); // パスを修正
            Image resizedImage = originalIcon.getImage().getScaledInstance(128, 128, Image.SCALE_SMOOTH);
            setIconImage(resizedImage);
            System.out.println("Icon set successfully.");
        } catch (Exception e) {
            System.out.println("Failed to load icon: " + e.getMessage());
        }
        
    	setVisible(true);
    }
    
    // 内部クラスとして StyledViewFactory を定義（EditorPanelで既に定義されているものと同じもの）
    static class StyledViewFactory implements ViewFactory {
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
    
    /*
    private void createTrackingWindow() {
    	// ログウィンドウの作成
        JFrame logFrame = new JFrame("ログ");
        logFrame.setSize(400, 200); // 小さいサイズ
        logFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 閉じた時にアプリ終了しない

        // テキストエリア（読み取り専用）
        logArea = new JTextArea();
        logArea.setEditable(false); // 書き込み不可に設定
        logArea.append("アプリケーションのログをここに表示します。\n");
        logArea.append("現在のレジスタやメモリの変化した記録を残します。\n");
        logArea.append("ex)\n<pc:0x0004> 変化なし\n");
        logArea.append("<pc:0x0008> x1:0x00000000 => 0x00000010");

        // スクロールバーの追加
        JScrollPane scrollPane = new JScrollPane(logArea);
        logFrame.add(scrollPane, BorderLayout.CENTER);

        // ウィンドウを表示
        logFrame.setVisible(true);
    }
     */
    
    
    // ログPanelと詳細Panelのフォントサイズを変更するメソッド.
   public static void setFontAndSize(String fontName, int fontStyle, int fontSize) {

        // 新しいフォントを設定して、detailConsole と logArea に適用
        Font newFont = new Font(fontName, fontStyle, fontSize);
        detailConsole.setFont(newFont);
        logArea.setFont(newFont);

        // System.out.println("Detail and Log font size updated to: " + detailAndLogFontSize);
    }

    //キーボードショートカットの設定.
    private void setKeyboardShortcuts() {
        // JTextPane に関連付けられたドキュメントを取得
        StyledDocument document = textArea.getStyledDocument();

        // ドキュメントに UndoableEditListener を登録
        document.addUndoableEditListener(e -> undoManager.addEdit(e.getEdit()));

        // 各アクションの定義
        Action saveAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveFile();
            }
        };

        Action newfileAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	newFile();
            }
        };

        Action loadfileAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MenuBarCreator.loadFile();
            }
        };

        Action assembleAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MenuBarCreator.assemblytoCode();
            }
        };

        Action exeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MenuBarCreator.allExe();
            }
        };

        Action stepexeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MenuBarCreator.stepExe(false);
            }
        };
        
        Action resetAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	MenuBarCreator.reset();
            }
        };

        Action undoAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canUndo()) {
                    undoManager.undo();
                    // System.out.println("Undo performed");
                } else {
                    // System.out.println("Nothing to undo");
                }
            }
        };

        Action redoAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canRedo()) {
                    undoManager.redo();
                    // System.out.println("Redo performed");
                } else {
                    // System.out.println("Nothing to redo");
                }
            }
        };
        
        Action increaseFontAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if(fontSize != 40) {
            		fontSize++; // フォントサイズを1増やす
            	}
                updateFontSize();
                // System.out.println("あ");
            }
        };

        Action decreaseFontAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if(fontSize != 10) {
            		fontSize--; // フォントサイズを1増やす
            	}
                updateFontSize();
                // System.out.println("い");
            }
        };

        // グローバルショートカットの設定
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), "saveAction");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), "newfileAction");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK), "loadfileAction");
        
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK), "assembleAction");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK), "exeAction");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK), "stepexeAction");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK), "resetAction");
        
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.CTRL_DOWN_MASK), "decreaseFontAction");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SEMICOLON, KeyEvent.CTRL_DOWN_MASK), "increaseFontAction");
        
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK), "undoAction");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK), "redoAction");

        
        actionMap.put("saveAction", saveAction);
        actionMap.put("newfileAction", newfileAction);
        actionMap.put("assembleAction", assembleAction);
        actionMap.put("exeAction", exeAction);
        actionMap.put("stepexeAction", stepexeAction);
        actionMap.put("loadfileAction", loadfileAction);
        actionMap.put("undoAction", undoAction);
        actionMap.put("redoAction", redoAction);
        actionMap.put("increaseFontAction", increaseFontAction);
        actionMap.put("decreaseFontAction", decreaseFontAction);
        actionMap.put("resetAction", resetAction);

        // テキストエリアにローカルショートカットを設定
        textArea.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK), "Undo");
        textArea.getActionMap().put("Undo", undoAction);

        textArea.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK), "Redo");
        textArea.getActionMap().put("Redo", redoAction);
        
        updateFontSize();
    }
    
    //新規ファイル.
    void newFile() {
    	try {
            // 現在実行中のJavaコマンドを取得し、同じプログラムを新たに起動
            String javaBin = System.getProperty("java.home") + "/bin/java";
            String classPath = System.getProperty("java.class.path");
            String className = Main.class.getName();

            ProcessBuilder processBuilder = new ProcessBuilder(
                javaBin, "-cp", classPath, className
            );

            // 新しいプロセスとして実行
            processBuilder.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //エディタのフォントを変更するメソッド.
    private void updateFontSize() {
        Font newFont = new Font("Noto Sans JP", Font.PLAIN, fontSize); // フォントを設定
        textArea.setFont(newFont); // JTextPaneのフォントを変更
        editorPanel.lineNumberView.setFontSize(fontSize);

        // MemoryPanelのフォントサイズを変更
        // メモリパネルが別クラスなら、メモリパネルのsetFontAndSizeメソッドを呼び出す
        // MemoryPanel.setFontAndSize(newFont);
    }
    
    // タイトルを更新する
    void updateTitle(boolean modified) {
        if (currentFile == null) {
            // ファイルが未保存の場合は「*」を付けて表示
            setTitle((modified ? fileName + "*" : fileName) + " - RVSim");
        } else {
            // ファイルが保存済みの場合は絶対パスを表示し、拡張子を除去
            String path = currentFile.getAbsolutePath().replaceFirst("[.][^.]+$", ""); // 拡張子を除く
            setTitle((modified ? path + "*" : path) + " - RVSim");
        }
        isModified = modified;
    }

    // ファイルを保存
    void saveFile() {
        if (isModified) { // 変更がある場合のみ保存
            if (currentFile == null) {
                // 初回保存の場合、「名前を付けて保存」ダイアログを表示
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setSelectedFile(new File(fileName + ".s"));
                int result = fileChooser.showSaveDialog(this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    currentFile = fileChooser.getSelectedFile();
                    fileName = currentFile.getName().replaceFirst("[.][^.]+$", ""); // 拡張子を除いたファイル名
                } else {
                    return; // キャンセルの場合は保存しない
                }
            }
            // ファイルを保存
            try (FileWriter writer = new FileWriter(currentFile)) {
                writer.write(textArea.getText());
                updateTitle(false); // タイトルを更新
                // System.out.println("File saved: " + currentFile.getAbsolutePath());
            } catch (IOException ex) {
                ex.printStackTrace();
                // System.out.println("Error: Failed to save file.");
            }
        }
    }
    
    // 名前を付けて保存
    void saveasnemeFile() {
    	// 初回保存の場合、「名前を付けて保存」ダイアログを表示
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(fileName + ".s"));
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            fileName = currentFile.getName().replaceFirst("[.][^.]+$", ""); // 拡張子を除いたファイル名
        } else {
            return; // キャンセルの場合は保存しない
        }
     // ファイルを保存
        try (FileWriter writer = new FileWriter(currentFile)) {
            writer.write(textArea.getText());
            updateTitle(false); // タイトルを更新
            // System.out.println("File saved: " + currentFile.getAbsolutePath());
        } catch (IOException ex) {
            ex.printStackTrace();
            // System.out.println("Error: Failed to save file.");
        }
    }

    public static void main(String[] args) {
    	Main main=new Main();
    	main.createApp();
    	
    	//main.createTrackingWindow();

        
    }
}
