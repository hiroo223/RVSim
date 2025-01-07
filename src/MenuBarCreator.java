import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import processingUnit.Assembler;
import processingUnit.ChangeBaseInstruction;
import processingUnit.Data_box;
import processingUnit.Execution;

//Tupleクラス
class Tuple {
    int realLineNumber;
    boolean isMultiline;

    public Tuple(int realLineNumber, boolean isMultiline) {
        this.realLineNumber = realLineNumber;
        this.isMultiline = isMultiline;
    }
}


public class MenuBarCreator {
    private static Data_box data;
    private static Execution exe;
    private static int pc = 0;
    
    static String[] MemoryValues;
    static String[] DataValues;
    
    static String[] BaseMemoryValues;
    static String[] BaseDataValues;
    
    static String[] firstValuse;
    static String[] firstFloatValues;
    
    static Assembler assembler;
    
    static Main m;
    static boolean flag;
    private static boolean finalFlag = false;
    
    // グローバル変数としてのデフォルトディレクトリ
    static File defaultDirectory = new File(System.getProperty("user.home") + "/Documents");
    
    // グローバル変数としてMapを追加
    private static Map<Integer, Tuple> lineMapping = new HashMap<>();

    
    //メニューバーの作成.
    public static JMenuBar createMenuBar(Main main) {
        JMenuBar menuBar = new JMenuBar();
        
        // Assemblerクラスのオブジェクトを生成
        assembler = new Assembler();
        data = new Data_box();
        m = main;

        // ファイルメニューの作成
        JMenu fileMenu = new JMenu("ファイル");
        menuBar.add(fileMenu);

     // メニューアイテムにショートカットを設定し、レイアウトを整える
        JMenuItem newFileItem = new JMenuItem("新規ファイル");
        newFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
        fileMenu.add(newFileItem);
        newFileItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 新しいウィンドウを開き、新しいメイン画面を作成
                m.newFile();
            }
        });

        JMenuItem saveItem = new JMenuItem("保存");
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        fileMenu.add(saveItem);
        saveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                m.saveFile();
            }
        });

        JMenuItem saveAsItem = new JMenuItem("名前を付けて保存");
        saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK));
        fileMenu.add(saveAsItem);
        saveAsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                m.saveasnemeFile(); // "名前を付けて保存"処理を呼び出す
            }
        });

        JMenuItem openFileItem = new JMenuItem("ファイルの読み込み");
        openFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK ));
        fileMenu.add(openFileItem);
        openFileItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadFile();
            }
        });

        
        // 設定メニューの作成
        JMenu settingsMenu = new JMenu("設定");
        menuBar.add(settingsMenu);
        
        JMenuItem settingItem_open = new JMenuItem("設定を開く");
        settingsMenu.add(settingItem_open);
        settingItem_open.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		openSettingWindow();
        	}
        });
        

        // 実行メニューの作成
        JMenu runMenu = new JMenu("実行");
        menuBar.add(runMenu);

        // アセンブルメニューアイテム
        JMenuItem runItem_assemble = new JMenuItem("アセンブル");
        runItem_assemble.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK));
        runMenu.add(runItem_assemble);
        runItem_assemble.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		assemblytoCode();
        	}
        });

        // 実行メニューアイテム
        JMenuItem runItem_exe = new JMenuItem("実行");
        runItem_exe.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK));
        runMenu.add(runItem_exe);
        runItem_exe.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		allExe();
        	}
        });

        // ステップ実行メニューアイテム
        JMenuItem runItem_sexe = new JMenuItem("ステップ実行");
        runItem_sexe.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK));
        runMenu.add(runItem_sexe);
        runItem_sexe.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		stepExe(false);
        	}
        });

        // リセットメニューアイテム
        JMenuItem runItem_reset = new JMenuItem("リセット");
        runItem_reset.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK));
        runMenu.add(runItem_reset);
        runItem_reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reset();
            }
        });


        menuBar.add(runMenu);

        // ヘルプメニューの作成
        JMenu helpMenu = new JMenu("ヘルプ");
        
        // ヘルプウィンドウメニューアイテム
        JMenuItem helpWindowItem = new JMenuItem("ヘルプを開く");
        helpWindowItem.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		Help.showHelpWindow();
        		
        	}
        });
        helpMenu.add(helpWindowItem);
        
        menuBar.add(helpMenu);
        

        return menuBar;
    }
    
	// 設定パネルの作成
	static void openSettingWindow() {
	    JFrame settingsFrame = new JFrame("設定");
	    settingsFrame.setSize(600, 600); // ウィンドウサイズを大きく変更
	    settingsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    settingsFrame.setMinimumSize(new Dimension(400, 500));
	
	    JPanel mainPanel = new JPanel();
	    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
	
	    JPanel typeSelectionPanel = createTypeSelectionPanel();
	    JPanel registerFontSizePanel = createRegisterFontSizePanel(); // レジスタ・メモリの文字サイズ設定
	    JPanel logFontSizePanel = createLogFontSizePanel(); // 詳細・ログの文字サイズ設定
	    JPanel directoryPanel = createDirectorySelectionPanel();
	    JPanel hideRegisterPanel = createHideRegisterPanel();
	    JPanel registerDisplayOrderPanel = createRegisterDisplayOrderPanel(); // レジスタ表示形式パネル
	
	    // ボタンサイズを統一
	    Dimension buttonSize = new Dimension(100, 30);
	
	    // 保存ボタン
	    JButton saveButton = new JButton("保存");
	    saveButton.setFont(new Font("Noto Sans JP", Font.PLAIN, 14)); // フォントを設定
	    saveButton.setPreferredSize(buttonSize); // ボタンサイズを統一
	    saveButton.addActionListener(e -> {
	        String selectedType = getSelectedType(typeSelectionPanel);
	        int registerFontSize = getSelectedFontSize(registerFontSizePanel);
	        int logFontSize = getSelectedFontSize(logFontSizePanel);
	        int[] selectedRegisters = getSelectedRegisters(hideRegisterPanel);
	        int[] selectedFloatRegisters = getSelectedFloatRegisters(hideRegisterPanel);
	        String selectedOrder = getSelectedRegisterDisplayOrder(registerDisplayOrderPanel);
	
	        // タイプに応じた設定
	        applyTypeSettings(selectedType);
	
	        // フォントサイズ設定
	        RegisterPanel.setFontAndSize("Noto Sans JP", Font.PLAIN, registerFontSize);
	        FloatingPointRegisterPanel.setFontAndSize("Noto Sans JP", Font.PLAIN, registerFontSize);
	        MemoryPanel.setFontAndSize("Noto Sans JP", Font.PLAIN, registerFontSize);
	        
	
	        // ログエリアと詳細エリアのフォントサイズ設定
	        Main.setFontAndSize("Noto Sans JP", Font.PLAIN, logFontSize);
	
	        // 非表示設定
	        RegisterPanel.setHiddenRegisters(selectedRegisters);
	        FloatingPointRegisterPanel.setHiddenRegisters(selectedFloatRegisters);
	
	        // レジスタ表示形式設定
	        if ("デフォルト順".equals(selectedOrder)) {
	            RegisterPanel.switchToDefaultOrder();
	            FloatingPointRegisterPanel.switchToDefaultOrder();
	        } else if ("名前順".equals(selectedOrder)) {
	            RegisterPanel.switchToGroupedOrder();
	            FloatingPointRegisterPanel.switchToGroupedOrder();
	        }
	
	        settingsFrame.dispose();
	    });
	
	    // リセットボタン
	    JButton resetButton = new JButton("リセット");
	    resetButton.setFont(new Font("Noto Sans JP", Font.PLAIN, 14)); // フォントを設定
	    resetButton.setPreferredSize(buttonSize); // ボタンサイズを統一
	    resetButton.addActionListener(e -> {
	        resetSettings(typeSelectionPanel, registerFontSizePanel, logFontSizePanel, hideRegisterPanel, registerDisplayOrderPanel);
	    });
	
	    // ボタンを配置するパネル
	    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
	    buttonPanel.add(saveButton);
	    buttonPanel.add(resetButton);
	
	    mainPanel.add(typeSelectionPanel);
	    mainPanel.add(registerFontSizePanel);
	    mainPanel.add(logFontSizePanel);
	    mainPanel.add(directoryPanel);
	    mainPanel.add(registerDisplayOrderPanel); // レジスタ表示形式パネルを追加
	    mainPanel.add(hideRegisterPanel);
	    mainPanel.add(buttonPanel); // ボタンパネルを追加
	
	    settingsFrame.add(mainPanel);
	    settingsFrame.setVisible(true);
	}
	// レジスタ表示形式設定パネルの作成
	private static JPanel createRegisterDisplayOrderPanel() {
	    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
	    JLabel label = new JLabel("レジスタ表示形式: ");
	    String[] options = {"デフォルト順", "名前順"};
	    JComboBox<String> comboBox = new JComboBox<>(options);
	    comboBox.setSelectedIndex(0); // デフォルトは「デフォルト順」
	    panel.add(label);
	    panel.add(comboBox);
	    return panel;
	}
	
    // レジスタ・メモリの文字サイズ設定パネルの作成
    private static JPanel createRegisterFontSizePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel("レジスタ・メモリの文字サイズ: ");
        Integer[] fontSizes = {10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30};
        JComboBox<Integer> comboBox = new JComboBox<>(fontSizes);
        comboBox.setSelectedItem(14); // デフォルトサイズ
        panel.add(label);
        panel.add(comboBox);
        return panel;
    }

    // 詳細・ログの文字サイズ設定パネルの作成
    private static JPanel createLogFontSizePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel("詳細・ログの文字サイズ: ");
        Integer[] fontSizes = {10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30};
        JComboBox<Integer> comboBox = new JComboBox<>(fontSizes);
        comboBox.setSelectedItem(14); // デフォルトサイズ
        panel.add(label);
        panel.add(comboBox);
        return panel;
    }

 // リセット設定
    private static void resetSettings(JPanel typePanel, JPanel registerFontSizePanel, JPanel logFontSizePanel, JPanel hideRegisterPanel, JPanel registerDisplayOrderPanel) {
        JComboBox<String> typeComboBox = (JComboBox<String>) typePanel.getComponent(1);
        JComboBox<Integer> registerFontSizeComboBox = (JComboBox<Integer>) registerFontSizePanel.getComponent(1);
        JComboBox<Integer> logFontSizeComboBox = (JComboBox<Integer>) logFontSizePanel.getComponent(1);
        JList<String> registerList = (JList<String>) ((JScrollPane) hideRegisterPanel.getComponent(1)).getViewport().getView();
        JComboBox<String> displayOrderComboBox = (JComboBox<String>) registerDisplayOrderPanel.getComponent(1);

        typeComboBox.setSelectedIndex(0);
        registerFontSizeComboBox.setSelectedItem(14);
        logFontSizeComboBox.setSelectedItem(14);
        registerList.clearSelection();
        displayOrderComboBox.setSelectedIndex(0);

        RegisterPanel.resetHiddenRegisters();
        RegisterPanel.switchToDefaultOrder();
    }


    // タイプ選択パネルの作成
    private static JPanel createTypeSelectionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel("タイプを選択: ");
        String[] types = {"RV32IMFD","RV32IM", "RV32IF", "RV32ID", "RV32I"};
        JComboBox<String> comboBox = new JComboBox<>(types);
        panel.add(label);
        panel.add(comboBox);
        return panel;
    }

    // ディレクトリ選択パネルの作成
    private static JPanel createDirectorySelectionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel("現在のディレクトリ: " + defaultDirectory.getAbsolutePath());
        JButton button = new JButton("ディレクトリを設定");
        button.addActionListener(e -> {
            JFileChooser directoryChooser = new JFileChooser();
            directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            directoryChooser.setCurrentDirectory(defaultDirectory);

            int result = directoryChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedDirectory = directoryChooser.getSelectedFile();
                if (selectedDirectory.isDirectory()) {
                    defaultDirectory = selectedDirectory;
                    label.setText("現在のディレクトリ: " + selectedDirectory.getAbsolutePath());
                }
            }
        });
        panel.add(button);
        panel.add(label);
        return panel;
    }

    // 非表示レジスタ設定パネルに整数レジスタと浮動小数点レジスタを分けて表示
    private static JPanel createHideRegisterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 整数レジスタラベルとリスト
        JLabel integerLabel = new JLabel(
            "<html>非表示にする整数レジスタを選択<br><small>(複数選択する場合はCtrlキーもしくはShiftキーを押しながら選択してください)</small></html>",
            JLabel.CENTER
        );
        integerLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(integerLabel);

        String[] integerRegisters = {
            "x0 (zero)", "x1 (ra)", "x2 (sp)", "x3 (gp)", "x4 (tp)", "x5 (t0)",
            "x6 (t1)", "x7 (t2)", "x8 (s0)", "x9 (s1)", "x10 (a0)", "x11 (a1)",
            "x12 (a2)", "x13 (a3)", "x14 (a4)", "x15 (a5)", "x16 (a6)", "x17 (a7)",
            "x18 (s2)", "x19 (s3)", "x20 (s4)", "x21 (s5)", "x22 (s6)", "x23 (s7)",
            "x24 (s8)", "x25 (s9)", "x26 (s10)", "x27 (s11)", "x28 (t3)", "x29 (t4)",
            "x30 (t5)", "x31 (t6)"
        };

        DefaultListModel<String> integerModel = new DefaultListModel<>();
        for (String reg : integerRegisters) {
            integerModel.addElement(reg);
        }
        JList<String> integerList = new JList<>(integerModel);
        integerList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane integerScrollPane = new JScrollPane(integerList);
        panel.add(integerScrollPane);

        // 浮動小数点レジスタラベルとリスト
        JLabel floatLabel = new JLabel(
            "<html>非表示にする浮動小数点レジスタを選択<br><small>(複数選択する場合はCtrlキーもしくはShiftキーを押しながら選択してください)</small></html>",
            JLabel.CENTER
        );
        floatLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(floatLabel);

        String[] floatRegisters = {
            "f0 (ft0)", "f1 (ft1)", "f2 (ft2)", "f3 (ft3)", "f4 (ft4)", "f5 (ft5)", 
            "f6 (ft6)", "f7 (ft7)", "f8 (fs0)", "f9 (fs1)", "f10 (fa0)", "f11 (fa1)",
            "f12 (fa2)", "f13 (fa3)", "f14 (fa4)", "f15 (fa5)", "f16 (fa6)", "f17 (fa7)",
            "f18 (fs2)", "f19 (fs3)", "f20 (fs4)", "f21 (fs5)", "f22 (fs6)", "f23 (fs7)",
            "f24 (fs8)", "f25 (fs9)", "f26 (fs10)", "f27 (fs11)", "f28 (ft8)", "f29 (ft9)",
            "f30 (ft10)", "f31 (ft11)"
        };

        DefaultListModel<String> floatModel = new DefaultListModel<>();
        for (String reg : floatRegisters) {
            floatModel.addElement(reg);
        }
        JList<String> floatList = new JList<>(floatModel);
        floatList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane floatScrollPane = new JScrollPane(floatList);
        panel.add(floatScrollPane);

        // 必要に応じてリストへのアクセスを提供
        panel.putClientProperty("integerList", integerList);
        panel.putClientProperty("floatList", floatList);

        return panel;
    }

    // ヘルパーメソッド: 選択したタイプを取得
    private static String getSelectedType(JPanel panel) {
        JComboBox<String> comboBox = (JComboBox<String>) panel.getComponent(1);
        return (String) comboBox.getSelectedItem();
    }

    // ヘルパーメソッド: 選択したフォントサイズを取得
    private static int getSelectedFontSize(JPanel panel) {
        JComboBox<Integer> comboBox = (JComboBox<Integer>) panel.getComponent(1);
        return (Integer) comboBox.getSelectedItem();
    }

    // ヘルパーメソッド: 選択したレジスタを取得
    private static int[] getSelectedRegisters(JPanel panel) {
        JList<String> list = (JList<String>) ((JScrollPane) panel.getComponent(1)).getViewport().getView();
        return list.getSelectedIndices();
    }
    
    // ヘルパーメソッド: 選択した浮動小数点レジスタを取得
    private static int[] getSelectedFloatRegisters(JPanel panel) {
        JList<String> floatList = (JList<String>) panel.getClientProperty("floatList");
        return floatList.getSelectedIndices();
    }

    // ヘルパーメソッド: 選択された表示形式を取得
    private static String getSelectedRegisterDisplayOrder(JPanel panel) {
        JComboBox<String> comboBox = (JComboBox<String>) panel.getComponent(1);
        return (String) comboBox.getSelectedItem();
    }
    
    // タイプ設定の適用
    private static void applyTypeSettings(String selectedType) {
        if ("RV32I".equals(selectedType)) {
            assembler.setStatus(data, false, false, false);
            
        } else if ("RV32IM".equals(selectedType)) {
            assembler.setStatus(data, true, false, false);
            
        } else if ("RV32IF".equals(selectedType)) {
            assembler.setStatus(data, false, true, false);
            
        } else if ("RV32ID".equals(selectedType)) {
            assembler.setStatus(data, false, false, true);
            
        } else if ("RV32IMFD".equals(selectedType)) {
            assembler.setStatus(data, true, true, true);
            
        }
    }
    
    // ファイルを開く
    static void loadFile() {
        String currentTime = new SimpleDateFormat("HH:mm").format(new Date());
        m.consoleArea.append("<" + currentTime + "> ");
        JFileChooser fileChooser = new JFileChooser();

        // グローバル変数からデフォルトディレクトリを設定
        fileChooser.setCurrentDirectory(defaultDirectory);

        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile.getName().endsWith(".s")) { // 拡張子が .s か確認
                try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                    // テキストエリアにファイルの内容を設定
                    m.textArea.setText(content.toString());
                    m.consoleArea.append("ファイルが読み込まれました: " + selectedFile.getName() + "\n");

                    // Mainクラスのファイル名とタイトルを更新
                    m.currentFile = selectedFile;
                    // ファイル名から拡張子を除いた名前を取得
                    m.fileName = selectedFile.getName().replaceFirst("[.][^.]+$", "");
                    // タイトルをフルパスから拡張子を除いた形式で設定
                    m.setTitle(m.currentFile.getAbsolutePath().replaceFirst("[.][^.]+$", "") + " - RVSim");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    m.consoleArea.append("error L1 :ファイルの読み込みに失敗しました。 : " + selectedFile.getName() + "\n");
                }
            } else {
                m.consoleArea.append("error L2 : .sファイルを選択してください。\n");
            }
        }
    }
    
    // デフォルトディレクトリを変更するメソッド（ダイアログを使用）
    static void setDefaultDirectory() {
        JFileChooser directoryChooser = new JFileChooser();

        // ディレクトリ選択モードに設定
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // 現在のデフォルトディレクトリを初期表示に設定
        directoryChooser.setCurrentDirectory(defaultDirectory);

        int result = directoryChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = directoryChooser.getSelectedFile();
            if (selectedDirectory.isDirectory()) {
                defaultDirectory = selectedDirectory;
                m.consoleArea.append("デフォルトディレクトリが変更されました: " + selectedDirectory.getAbsolutePath() + "\n");
            } else {
                m.consoleArea.append("error D2 : 無効なディレクトリが選択されました。\n");
            }
        } else {
            m.consoleArea.append("デフォルトディレクトリの変更がキャンセルされました。\n");
        }
    }
    
 // ステップ実行.
    static int stepExe(boolean exetype) {

        if (!flag) {
            String currentTime = new SimpleDateFormat("HH:mm").format(new Date());
            m.consoleArea.append("<" + currentTime + "> error E1 : アセンブルされていません。\n");
            return 0;
        }

        // 現在のPCとレジスタの状態を保持
        int previousPC = pc;
        String[] previousValues = new String[32];
        String[] previousFloatValues = new String[32]; // 浮動小数点レジスタの状態を保持

        for (int i = 0; i < 32; i++) {
            previousValues[i] = String.format("0x%08X", data.x[i]);
            previousFloatValues[i] = String.format("0x%016X", data.fx[i]); // 倍精度の値を保持
        }

        String[] previousMemoryValues = MemoryValues.clone(); // メモリの状態を保持
        String[] previousDataValues = DataValues.clone(); // データメモリの状態を保持

        // 初回のみオブジェクトを生成
        if (previousPC == 0) {
            data.resetFlag(); // フラグの初期化
            if (finalFlag) finalFlag = false;

            // レジスタの初期値を保存
            firstValuse = new String[32];
            for (int i = 0; i < 32; i++) {
                firstValuse[i] = String.format("0x%08X", data.x[i]);
            }

            firstFloatValues = new String[32];
            for (int i = 0; i < 32; i++) {
                firstFloatValues[i] = String.format("0x%016X", data.fx[i]);
            }
            exe = new Execution(data); // 初回のみオブジェクトを生成
        }

        if (finalFlag) {
        	/*
            if (!exetype) {
                String[] values = new String[32];
                String[] floatValues = new String[32];
                for (int i = 0; i < 32; i++) {
                    values[i] = String.format("0x%08X", data.x[i]);
                    floatValues[i] = String.format("0x%016X", data.fx[i]);
                }

                // メモリおよびレジスタのパネルを更新
                MemoryPanel.updateMemory(MemoryValues, DataValues, pc);
                RegisterPanel.updateRegister(values);
                RegisterPanel.updateRegisterColor(firstValuse, values);
                FloatingPointRegisterPanel.updateRegister(floatValues);
                FloatingPointRegisterPanel.updateRegisterColor(firstFloatValues, floatValues);
            }
            */
            return -1;
        }

        // ステップ実行を行う
        pc = exe.exe(pc, 1);

        // 実行時にエラーが起きた場合
        if (!data.err.isEmpty()) {
            for (String element : data.err) {
                m.consoleArea.append(element + "\n");
            }
            finalFlag = true;
            return 1;
        }

        if(!exetype) {
        	String[] values = new String[32];
            String[] floatValues = new String[32];
            boolean registersUpdated = false;
            boolean memoryUpdated = false;

            // レジスタ更新チェック
            for (int i = 0; i < 32; i++) {
                values[i] = String.format("0x%08X", data.x[i]);
                floatValues[i] = String.format("0x%016X", data.fx[i]);

                if (!previousValues[i].equals(values[i]) || !previousFloatValues[i].equals(floatValues[i])) {
                    registersUpdated = true;
                }
            }

            // メモリ更新チェック
            for (int i = 0; i < 0x1000 / 4; i++) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < 4; j++) {
                    sb.append(String.format("%02X", data.MEM[i * 4 + j]));
                }
                MemoryValues[i] = "0x" + sb.toString();
                if (!previousMemoryValues[i].equals(MemoryValues[i])) {
                    memoryUpdated = true;
                }
            }

            for (int i = 0; i < 0x1000 / 4; i++) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < 4; j++) {
                    int index = i * 4 + j + 0x9000;
                    sb.append(String.format("%02X", data.MEM[index]));
                }
                DataValues[i] = "0x" + sb.toString();
                if (!previousDataValues[i].equals(DataValues[i])) {
                    memoryUpdated = true;
                }
            }

        	// 必要な場合のみ更新
            if (registersUpdated) {
                RegisterPanel.updateRegister(values);
                RegisterPanel.updateRegisterColor(previousValues, values);
                FloatingPointRegisterPanel.updateRegister(floatValues);
                FloatingPointRegisterPanel.updateRegisterColor(previousFloatValues, floatValues);
            }

            if (memoryUpdated) {
                MemoryPanel.updateDataRegion(DataValues);
            }
            
            stepExeWithHighlight(pc);
            
        	// ログ生成
            StringBuilder logMessage = new StringBuilder();
            logMessage.append(String.format("pc : %04x => %04x\n", previousPC, pc));
            
            boolean hasChanges = false; // 変更があったかどうかを追跡
            for (int i = 0; i < 32; i++) {
                if (!previousValues[i].equals(values[i])) {
                    logMessage.append(String.format("x%d %s => %s\n", i, previousValues[i], values[i]));
                    hasChanges = true;
                }
                if (!previousFloatValues[i].equals(floatValues[i])) {
                    logMessage.append(String.format("fx%d %s => %s\n", i, previousFloatValues[i], floatValues[i]));
                    hasChanges = true;
                }
            }
            
            // メモリの変化を検出してログに記録
            for (int i = 0; i < MemoryValues.length; i++) {
                if (!previousMemoryValues[i].equals(MemoryValues[i])) {
                    logMessage.append(String.format("Memory[%d]: %s => %s\n", i, previousMemoryValues[i], MemoryValues[i]));
                    hasChanges = true;
                }
            }

            for (int i = 0; i < DataValues.length; i++) {
                if (!previousDataValues[i].equals(DataValues[i])) {
                    logMessage.append(String.format("Data[%d]: %s => %s\n", i, previousDataValues[i], DataValues[i]));
                    hasChanges = true;
                }
            }
            if (registersUpdated || memoryUpdated) {
                //logMessage.append("更新が検出されました。\n");
            } else {
                logMessage.append("変更なし\n");
            }

            m.logArea.append(logMessage.toString() + "\n");
        }
        
        if (previousPC == 0) {
            String currentTime = new SimpleDateFormat("HH:mm").format(new Date());
            m.consoleArea.append("\n<" + currentTime + "> 実行ファイル名  " + m.fileName + "\n");
        }
        

        if (exe.finalPc == pc) {
        	m.consoleArea.append("\nステップ数は" + exe.count + "でした。\n" );
        	

            if(exetype) {

            	String[] values = new String[32];
                String[] floatValues = new String[32];

                // レジスタ更新
                for (int i = 0; i < 32; i++) {
                    values[i] = String.format("0x%08X", data.x[i]);
                    floatValues[i] = String.format("0x%016X", data.fx[i]);
                }

                for (int i = 0; i < 0x1000 / 4; i++) {
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < 4; j++) {
                        sb.append(String.format("%02X", data.MEM[i * 4 + j]));
                    }
                    MemoryValues[i] = "0x" + sb.toString();
                }

                for (int i = 0; i < 0x1000 / 4; i++) {
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < 4; j++) {
                        int index = i * 4 + j + 0x9000;
                        sb.append(String.format("%02X", data.MEM[index]));
                    }
                    DataValues[i] = "0x" + sb.toString();
                }

                // 更新処理
                RegisterPanel.updateRegister(values);
                RegisterPanel.updateRegisterColor(previousValues, values);
                FloatingPointRegisterPanel.updateRegister(floatValues);
                FloatingPointRegisterPanel.updateRegisterColor(previousFloatValues, floatValues);
                MemoryPanel.updateDataRegion(DataValues);
                
                finalFlag = true;
                
            }
            
            if(!finalFlag) {
                finalFlag = true;
                
            }
         
            return -1;
        }

        
        if (data.print_string) {
            // バイトリストを作成
            List<Byte> byteList = new ArrayList<>();

            for (int i = 0; data.MEM[data.x[11] + i] != 0; i++) {
                byteList.add((byte) data.MEM[data.x[11] + i]); // メモリから1バイトずつ取得
            }

            try {
                // バイトリストをバイト配列に変換
                byte[] byteArray = new byte[byteList.size()];
                for (int i = 0; i < byteList.size(); i++) {
                    byteArray[i] = byteList.get(i);
                }

                // Shift-JISエンコードを適用してデコード
                String decodedString = new String(byteArray, "Shift-JIS");
                m.consoleArea.append(decodedString); // コンソールに表示
            } catch (Exception e) {
                e.printStackTrace();
                m.consoleArea.append("エンコーディングエラー: " + e.getMessage());
            }

            data.print_string = false;
        }


        if(data.print_char) {
    	    char character = (char) data.MEM[data.x[11]]; // メモリからデータを取得し、charに変換
    	    m.consoleArea.append(Character.toString(character)); // 文字を文字列としてコンソールに追加
    	    data.print_char = false;

        }
        
        if(data.print_hex) {
        	m.consoleArea.append(Integer.toHexString(data.x[11]));
        	data.print_hex = false;
        	
        }
        
        if(data.print_int) {
        	m.consoleArea.append(String.valueOf(data.x[11]));
        	data.print_int = false;
        	
        }

        return 1;
    }

    // 実行
    static void allExe() {
    	
    	if (!flag) {
            String currentTime = new SimpleDateFormat("HH:mm").format(new Date());
            m.consoleArea.append("<" + currentTime + "> error E1 : アセンブルされていません。\n");
            return ;
        }
    	
    	// 現在のPCとレジスタの状態を保持
        int previousPC = pc;
        String[] previousValues = new String[32];
        String[] previousFloatValues = new String[32]; // 浮動小数点レジスタの状態を保持

        for (int i = 0; i < 32; i++) {
            previousValues[i] = String.format("0x%08X", data.x[i]);
            previousFloatValues[i] = String.format("0x%016X", data.fx[i]); // 倍精度の値を保持
        }

        String[] previousMemoryValues = MemoryValues.clone(); // メモリの状態を保持
        String[] previousDataValues = DataValues.clone(); // データメモリの状態を保持

    	stepExe(true);
    	
    	while(stepExe(true) != -1);
    	
    	String[] values = new String[32];
        String[] floatValues = new String[32];
        boolean registersUpdated = false;
        boolean memoryUpdated = false;

        // レジスタ更新チェック
        for (int i = 0; i < 32; i++) {
            values[i] = String.format("0x%08X", data.x[i]);
            floatValues[i] = String.format("0x%016X", data.fx[i]);

            if (!previousValues[i].equals(values[i]) || !previousFloatValues[i].equals(floatValues[i])) {
                registersUpdated = true;
            }
        }

        // メモリ更新チェック
        for (int i = 0; i < 0x1000 / 4; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < 4; j++) {
                sb.append(String.format("%02X", data.MEM[i * 4 + j]));
            }
            MemoryValues[i] = "0x" + sb.toString();
            if (!previousMemoryValues[i].equals(MemoryValues[i])) {
                memoryUpdated = true;
            }
        }

        for (int i = 0; i < 0x1000 / 4; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < 4; j++) {
                int index = i * 4 + j + 0x9000;
                sb.append(String.format("%02X", data.MEM[index]));
            }
            DataValues[i] = "0x" + sb.toString();
            if (!previousDataValues[i].equals(DataValues[i])) {
                memoryUpdated = true;
            }
        }

    	// 必要な場合のみ更新
        if (registersUpdated) {
            RegisterPanel.updateRegister(values);
            RegisterPanel.updateRegisterColor(previousValues, values);
            FloatingPointRegisterPanel.updateRegister(floatValues);
            FloatingPointRegisterPanel.updateRegisterColor(previousFloatValues, floatValues);
        }
    	
    }
    
    
    static void reset() {
        if (!flag) {
            String currentTime = new SimpleDateFormat("HH:mm").format(new Date());
            m.consoleArea.append("<" + currentTime + "> error E1 : アセンブルされていません。\n");
            return;
        }

        pc = 0; // pcの初期化

        // DataValues と BaseDataValues の比較・変更
        if (DataValues != null && BaseDataValues != null) {
            for (int i = 0; i < DataValues.length; i++) {
                if (!DataValues[i].equals(BaseDataValues[i])) {
                    DataValues[i] = "0x00000000";
                    data.MEM[i * 4 + 0x9000] = 0;
                    data.MEM[i * 4 + 0x9001] = 0;
                    data.MEM[i * 4 + 0x9002] = 0;
                    data.MEM[i * 4 + 0x9003] = 0;
                }
            }
        }

        // レジスタを初期化
        String[] values = new String[32];
        String[] floatValues = new String[32];
        for (int i = 0; i < 32; i++) {
            values[i] = "0x00000000";
            floatValues[i] = "0x0000000000000000";
            data.x[i] = 0;
            data.fx[i] = 0;
            if (i == 2) {
                values[i] = "0x00007FFF"; // スタックポインタ初期化
                data.x[i] = 0x7fff;
            }
        }
        
        MemoryPanel.updateMemory(MemoryValues, DataValues, pc);
        RegisterPanel.updateRegister(values);
        FloatingPointRegisterPanel.updateRegister(floatValues); // 浮動小数点レジスタの更新

        
        RegisterPanel.resetRegisterColors();
        FloatingPointRegisterPanel.resetRegisterColors();
        // detailConsole の行ごとに色付け
        StyledDocument doc = m.detailConsole.getStyledDocument();
        SimpleAttributeSet blueSet = new SimpleAttributeSet();
        StyleConstants.setForeground(blueSet, Color.BLUE); // 青色に設定
        SimpleAttributeSet defaultSet = new SimpleAttributeSet();
        StyleConstants.setForeground(defaultSet, Color.BLACK); // 黒色に設定

        // 全テキストをデフォルト色に戻す
        doc.setCharacterAttributes(0, doc.getLength(), defaultSet, false);

        String[] lines = m.detailConsole.getText().split("\n");
        int startOffset = 0;

        try {
            // 最初の行を青色に設定
            int firstLineEndOffset = getLineEndOffset(m.detailConsole, 0);
            doc.setCharacterAttributes(startOffset, firstLineEndOffset - startOffset, blueSet, false);
            startOffset = firstLineEndOffset;

            // 次の行が数字で始まらない場合に青色を付ける
            if (lines.length > 1) {
                int secondLineStartOffset = getLineStartOffset(m.detailConsole, 1);
                int secondLineEndOffset = getLineEndOffset(m.detailConsole, 1);
                String secondLineText = m.detailConsole.getText(secondLineStartOffset, secondLineEndOffset - secondLineStartOffset).trim();

                if (!secondLineText.isEmpty() && !Character.isDigit(secondLineText.charAt(0))) {
                    doc.setCharacterAttributes(secondLineStartOffset, secondLineEndOffset - secondLineStartOffset, blueSet, false);
                }
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        // ログの内容をリセット
        m.logArea.setText(""); // logArea のテキストを全て削除

        String currentTime = new SimpleDateFormat("HH:mm").format(new Date());
        m.consoleArea.append("\n<" + currentTime + "> リセットできました\n");
    }
    
    // アセンブラ
    static void assemblytoCode() {
        String currentTime = new SimpleDateFormat("HH:mm").format(new Date());
        m.consoleArea.append("\n<" + currentTime + "> ");
        
        data.resetDataBox();

        data = assembler.Assemble(m.textArea.getText(),data);

        if (!data.err.isEmpty()) {
            for (String element : data.err) {
                m.consoleArea.append(element + "\n");
            }
        } else {
        	
        	
            m.consoleArea.append("アセンブルできました\n");
            
            
            
            MemoryValues = new String[0x1000 / 4];
            DataValues = new String[0x1000 / 4];
            
            BaseMemoryValues = new String[0x1000 / 4];
            BaseDataValues = new String[0x1000 / 4];

            for (int i = 0; i < 0x1000 / 4; i++) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < 4; j++) {
                    sb.append(String.format("%02X", data.MEM[i * 4 + j]));
                }
                MemoryValues[i] = "0x" + sb.toString();
                BaseMemoryValues[i] = "0x" + sb.toString();
            }
            
            

            for (int i = 0; i < 0x1000 / 4; i++) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < 4; j++) {
                    int index = i * 4 + j + 0x9000;
                    if (index >= data.MEM.length) {
                        break;
                    }
                    sb.append(String.format("%02X", data.MEM[index]));
                }
                DataValues[i] = "0x" + sb.toString();
                BaseDataValues[i] = "0x" + sb.toString();
            }
            
            
            
            ChangeBaseInstruction CBI = new ChangeBaseInstruction(data);
            CBI.exe();

            StyledDocument doc = m.detailConsole.getStyledDocument();

            StringBuilder allText = new StringBuilder();
            int number = 0;

            // 基準として最大のBase命令の長さを取得
            int maxBaseInstructionLength = 0;
            for (String baseInstruction : data.BIS) {
                if (baseInstruction.length() > maxBaseInstructionLength) {
                    maxBaseInstructionLength = baseInstruction.length();
                }
            }
            
            int counter=0;

            for (int i = 0; i < data.BIS.size(); i++) {
                String address = String.format("%04x", number * 4);
                String baseInstruction = data.BIS.get(i);
                String originalInstruction = (i < data.OIS.size()) ? data.OIS.get(i) : "";
                
                lineMapping.put(number, new Tuple(counter++, false));

                // ラベルがあるか確認
                String label = data.label_map.get(number * 4);
                boolean hasLabel = label != null;

             // 行がラベルで始まる場合のみラベル部分を削除
                if (originalInstruction.matches("^[a-zA-Z1-9_]+:\\s*.*")) {
                    originalInstruction = originalInstruction.replaceFirst("^[a-zA-Z1-9_]+:\\s*", "").trim();
                }


                // ラベルがある場合、ラベル行のみを出力
                if (hasLabel) {
                    String labelLine = String.format("%s\t|                      \t| %s\n", address, label);
                    allText.append(labelLine);
                    address = ""; // ラベル行でアドレスを出力したので次の行ではアドレスを空に
                    lineMapping.put(number, new Tuple(counter++, true));

                }

                // Base命令の長さを20文字になるように調整
                StringBuilder paddedBaseInstruction = new StringBuilder(baseInstruction);
                while (paddedBaseInstruction.length() < 20) {
                    paddedBaseInstruction.append(" ");
                }

                // 通常の行の結合（ラベルがあった場合はアドレスを空に）
                String lineText = String.format("%s\t| %s\t| %s\n", address, paddedBaseInstruction, originalInstruction);
                allText.append(lineText);

                number++;
            }

            try {
                doc.remove(0, doc.getLength()); // テキスト全体をクリア
                doc.insertString(0, allText.toString(), null); // 新しいテキストを挿入

                // 属性セットの定義
                SimpleAttributeSet blueSet = new SimpleAttributeSet();
                StyleConstants.setForeground(blueSet, Color.BLUE); // 青色に設定

                SimpleAttributeSet blackSet = new SimpleAttributeSet();
                StyleConstants.setForeground(blackSet, Color.BLACK); // 黒色に設定

                // テキストの行ごとに処理
                String[] lines = allText.toString().split("\n");
                int offset = 0;

                boolean isFirstBlock = true; // 最初の行ブロックの判定

                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];

                    if (isFirstBlock) {
                        // 最初の行（またはブロック）の処理
                        doc.setCharacterAttributes(offset, line.length(), blueSet, true);
                        offset += line.length() + 1; // 次の行に進む

                        // 次の行が命令行であれば、それも青色にする
                        if (i + 1 < lines.length && !lines[i + 1].matches("^\\s*[0-9a-fA-F]{4}\\s*\\|.*")) {
                            i++; // 次の行も処理する
                            line = lines[i];
                            doc.setCharacterAttributes(offset, line.length(), blueSet, true);
                            offset += line.length() + 1; // 次の行に進む
                        }

                        isFirstBlock = false; // 最初のブロックが終了
                    } else {
                    	// その他の行を黒色に設定
                        doc.setCharacterAttributes(offset, line.length(), blackSet, true);
                        offset += line.length() + 1; // 改行を考慮
                    }
                }

            } catch (BadLocationException e) {
                e.printStackTrace();
            }

            
            MemoryPanel.updateMemory(MemoryValues, DataValues, pc);
            

            flag = true;
            
            //System.out.println("初期化されてるよ\n");
        	//System.out.println("Basedata : "+BaseDataValues[0]);
        	
        	/*
        	// アセンブラ出力を解析してMapに登録
            lineMapping.clear();
            int lineNumber = 0;

            for (int i = 0; i < data.BIS.size(); i++) {
                String baseInstruction = data.BIS.get(i);
                String originalInstruction = data.OIS.get(i);

                // ラベル行の判定（ラベル行の場合は次の行の対応）
                boolean isMultiline = originalInstruction.trim().isEmpty() || originalInstruction.matches(".*:\\s*.*");

                lineMapping.put(lineNumber, new Tuple(i, isMultiline));

                // 複数行の場合は行数を増やさない
                if (!isMultiline) {
                    lineNumber++;
                }
            }
            */
            /*
            System.out.println("=== lineMapping 内容 ===");
            for (Map.Entry<Integer, Tuple> entry : lineMapping.entrySet()) {
                int key = entry.getKey();
                Tuple value = entry.getValue();
                System.out.printf("PC: %04X, Real Line Number: %d, Is Multiline: %b%n",
                        key, value.realLineNumber, value.isMultiline);
            }
            System.out.println("========================");
            */
        	
        	// タブを管理しているコンポーネント (例: JTabbedPane) の詳細タブを選択
            m.editorTabbedPane.setSelectedIndex(1); // "詳細" タブのインデックスを指定

            
            pc=0;

            // ログの内容をリセット
            m.logArea.setText("************************************\n"
            					+ "アセンブルできました\n"
            					+ "************************************\n"); // logArea のテキストを全て削除
        }
    }
    
    
    static void stepExeWithHighlight(int pc) {
        Tuple lineInfo = lineMapping.get(pc / 4);
        if (lineInfo == null) return;

        int realLineNumber = lineInfo.realLineNumber;
        boolean isMultiline = lineInfo.isMultiline;

        StyledDocument doc = m.detailConsole.getStyledDocument();
        SimpleAttributeSet blueSet = new SimpleAttributeSet();
        StyleConstants.setForeground(blueSet, Color.BLUE);

        SimpleAttributeSet defaultSet = new SimpleAttributeSet();
        StyleConstants.setForeground(defaultSet, Color.BLACK);

        try {
            // 全テキストをデフォルト色に戻す
            doc.setCharacterAttributes(0, doc.getLength(), defaultSet, false);

            // 現在の行に色を付ける
            int startOffset = getLineStartOffset(m.detailConsole, realLineNumber);
            int endOffset = getLineEndOffset(m.detailConsole, realLineNumber);
            doc.setCharacterAttributes(startOffset, endOffset - startOffset, blueSet, false);

            if (isMultiline) {
                // isMultiline が true の場合、realLineNumber - 1 の行にも色を付ける
                if (realLineNumber > 0) {
                    int prevStartOffset = getLineStartOffset(m.detailConsole, realLineNumber - 1);
                    int prevEndOffset = getLineEndOffset(m.detailConsole, realLineNumber - 1);
                    doc.setCharacterAttributes(prevStartOffset, prevEndOffset - prevStartOffset, blueSet, false);
                }

                // 次の行も色を付ける
                int nextLine = realLineNumber + 1;
                while (nextLine < doc.getDefaultRootElement().getElementCount()) {
                    int nextStartOffset = getLineStartOffset(m.detailConsole, nextLine);
                    int nextEndOffset = getLineEndOffset(m.detailConsole, nextLine);

                    // 次の行のテキスト内容を取得
                    String nextLineText = m.detailConsole.getText(nextStartOffset, nextEndOffset - nextStartOffset).trim();

                    // 条件: 数字以外で始まる場合に色を付ける
                    if (!nextLineText.isEmpty() && !Character.isDigit(nextLineText.charAt(0))) {
                        doc.setCharacterAttributes(nextStartOffset, nextEndOffset - nextStartOffset, blueSet, false);
                        nextLine++;
                    } else {
                        break; // 数字で始まる場合は終了
                    }
                }
            }

        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private static int getLineStartOffset(JTextPane textPane, int line) throws BadLocationException {
        StyledDocument doc = textPane.getStyledDocument();
        Element root = doc.getDefaultRootElement();
        return root.getElement(line).getStartOffset();
    }

    private static int getLineEndOffset(JTextPane textPane, int line) throws BadLocationException {
        StyledDocument doc = textPane.getStyledDocument();
        Element root = doc.getDefaultRootElement();
        return root.getElement(line).getEndOffset();
    }


}
