import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

public class Help {

    // ページデータのマップ（カテゴリとHTML内容を関連付け）
    private static final Map<String, String> pages = new HashMap<>();
    private static final Highlighter.HighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
    private static int currentHitIndex = 0;
    private static List<Integer> hitPositions = new ArrayList<>();
    private static String lastQuery = ""; // 前回の検索ワードを保持

    // ページ名とファイルパスのマッピングを定義
    private static final Map<String, String> pageFiles = Map.of(
        "基本操作", "resources/html/basic.html",
        "ショートカット", "resources/html/shortcuts.html",
        "FAQ", "resources/html/FAQ.html",
        "公式ドキュメント", "resources/html/official_Document.html",
        "疑似命令", "resources/html/pseudo_Instructions.html",
        "基本命令セット", "resources/html/RV32I.html",
        "M拡張命令セット", "resources/html/RV32M.html",
        "F,D拡張命令セット", "resources/html/RV32FD.html",
        "システムコール命令", "resources/html/systemcall.html"
    );

    // 順序付けられたカテゴリリスト
    private static final List<String> orderedCategories = List.of(
        "基本操作",
        "ショートカット",
        "FAQ",
        "公式ドキュメント",
        "疑似命令",
        "基本命令セット",
        "M拡張命令セット",
        "F,D拡張命令セット",
        "システムコール命令"
    );
    

    static {
	    // HTMLファイルを読み込んでページデータに格納
	    for (Map.Entry<String, String> entry : pageFiles.entrySet()) {
	        try {
	            String content = Files.lines(Paths.get(entry.getValue()))
	                    .collect(Collectors.joining("\n"));
	            pages.put(entry.getKey(), content);
	        } catch (IOException e) {
	            System.err.println("Error reading file: " + entry.getValue());
	            e.printStackTrace();
	        }
	    }
	}

 // ヘルプウィンドウを表示するメソッド
    public static void showHelpWindow() {
        // メインフレームの設定
        JFrame helpFrame = new JFrame("ヘルプ");
        helpFrame.setSize(800, 600);
        helpFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        helpFrame.setLayout(new BorderLayout());

        // サイドバー（カテゴリボタン一覧）の設定
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(200, 0));

        // コンテンツエリア（HTMLを表示するエリア）の設定
        JEditorPane contentArea = new JEditorPane();
        contentArea.setContentType("text/html");
        contentArea.setEditable(false);

        // ボタンの統一サイズを設定
        Dimension buttonSize = new Dimension(180, 40);
        for (String category : orderedCategories) { // 順序付けたリストを使用
            JButton button = new JButton(category);
            button.setMaximumSize(buttonSize); // ボタンの最大サイズを統一
            button.addActionListener(e -> {
                contentArea.setText(pages.get(category));
                resetHighlight(contentArea); // ハイライトをリセット
                contentArea.setCaretPosition(0); // 初期位置を一番上に設定
            });
            sidebar.add(button);
        }

        // デフォルトのページを設定
        contentArea.setText(pages.get("基本操作"));
        contentArea.setCaretPosition(0); // 初期位置を一番上に設定

        // 検索バーと検索ボタンを含むヘッダーの設定
        JPanel header = new JPanel();
        header.setLayout(new BorderLayout());
        JTextField searchField = new JTextField();
        JButton searchButton = new JButton("検索");

        JLabel hitCounter = new JLabel("0/0"); // 検索ヒット数のカウンター
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.add(hitCounter, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        header.add(searchPanel, BorderLayout.CENTER);

        // コンテンツエリアをスクロール可能にする
        JScrollPane contentScrollPane = new JScrollPane(contentArea);

        // 検索ボタンのクリック時の動作
        searchButton.addActionListener(e -> performSearch(contentArea, searchField, hitCounter, helpFrame));

        // 検索フィールドのエンターキー押下時の動作
        searchField.addActionListener(e -> performSearch(contentArea, searchField, hitCounter, helpFrame));

        // エンターキーが押された場合に検索を実行（検索フィールドにフォーカスがない場合も含む）
        helpFrame.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performSearch(contentArea, searchField, hitCounter, helpFrame);
                }
            }
        });

        // フレームにコンポーネントを配置
        helpFrame.add(header, BorderLayout.NORTH);
        helpFrame.add(sidebar, BorderLayout.WEST);
        helpFrame.add(contentScrollPane, BorderLayout.CENTER);

        helpFrame.setVisible(true); // フレームを表示
    }

    // 検索処理を実行するメソッド
    private static void performSearch(JEditorPane contentArea, JTextField searchField, JLabel hitCounter, JFrame helpFrame) {
        String query = searchField.getText().toLowerCase().trim();
        if (!query.isEmpty()) {
            try {
                if (!query.equals(lastQuery)) {
                    // 新しい検索ワードの場合
                    resetHighlight(contentArea); // ハイライトをリセット
                    hitPositions.clear(); // ヒット位置リストをクリア
                    Highlighter highlighter = contentArea.getHighlighter();
                    String text = contentArea.getDocument().getText(0, contentArea.getDocument().getLength()).toLowerCase();

                    int pos = 0;
                    while ((pos = text.indexOf(query, pos)) >= 0) {
                        hitPositions.add(pos);
                        highlighter.addHighlight(pos, pos + query.length(), highlightPainter); // ハイライト追加
                        pos += query.length();
                    }

                    if (!hitPositions.isEmpty()) {
                        currentHitIndex = 0;
                        hitCounter.setText("1/" + hitPositions.size());
                        scrollToHighlight(contentArea, hitPositions.get(currentHitIndex));
                    } else {
                        // 結果が見つからない場合
                        hitCounter.setText("0/0");
                        JOptionPane.showMessageDialog(helpFrame, "検索結果が見つかりませんでした", "検索", JOptionPane.INFORMATION_MESSAGE);
                    }

                    lastQuery = query; // 新しい検索ワードを記録
                } else {
                    // 同じ検索ワードの場合は次の結果へ移動
                    currentHitIndex = (currentHitIndex + 1) % hitPositions.size();
                    hitCounter.setText((currentHitIndex + 1) + "/" + hitPositions.size());
                    scrollToHighlight(contentArea, hitPositions.get(currentHitIndex));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // ハイライト位置にスクロールするメソッド
    private static void scrollToHighlight(JEditorPane contentArea, int position) {
        try {
            Rectangle viewRect = contentArea.modelToView(position);
            if (viewRect != null) {
                contentArea.scrollRectToVisible(viewRect);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ハイライトをリセットするメソッド
    private static void resetHighlight(JEditorPane contentArea) {
        Highlighter highlighter = contentArea.getHighlighter();
        highlighter.removeAllHighlights();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Help::showHelpWindow);
    }
}
