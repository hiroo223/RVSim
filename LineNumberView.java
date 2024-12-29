import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;

class LineNumberView extends JComponent {
    private static final int MARGIN = 5;
    private final JTextComponent textComponent;
    private FontMetrics fontMetrics;

    public LineNumberView(JTextComponent textComponent) {
        this.textComponent = textComponent;

        // フォントとそのメトリクスを取得
        Font font = textComponent.getFont();
        fontMetrics = textComponent.getFontMetrics(font);

        textComponent.getDocument().addDocumentListener((SimpleDocumentListener) e -> repaint());
        textComponent.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                revalidate();
                repaint();
            }
        });

        // ボーダー設定
        Insets i = textComponent.getInsets();
        Border outer = BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY);
        Border inner = BorderFactory.createEmptyBorder(i.top, MARGIN, i.bottom, MARGIN - 1);
        setBorder(BorderFactory.createCompoundBorder(outer, inner));

        setOpaque(true);
        setBackground(Color.WHITE);
        setFont(font);
    }
    

    public void setFontSize(int fontSize) {
        // 1. 新しいフォントを作成
        Font currentFont = textComponent.getFont();
        Font newFont = currentFont.deriveFont((float) fontSize); // 新しいフォントサイズで作成

        // 2. テキストコンポーネントと行番号ビューのフォントを設定
        textComponent.setFont(newFont);  // テキストエリアのフォントを変更
        this.setFont(newFont);           // 行番号ビューのフォントも変更

        // 3. フォントメトリクスを更新
        fontMetrics = textComponent.getFontMetrics(newFont);

        // 4. レイアウトの再計算と再描画
        textComponent.revalidate();
        textComponent.repaint();
        this.revalidate();
        this.repaint();
    }


    private int getComponentWidth() {
        int lineCount = getLineCount();
        int maxDigits = Math.max(3, String.valueOf(lineCount).length());
        Insets i = getInsets();
        return maxDigits * fontMetrics.stringWidth("0") + i.left + i.right;
    }

    private int getLineCount() {
        Element root = textComponent.getDocument().getDefaultRootElement();
        return root.getElementCount();
    }

    private int getLineAtPoint(int y) {
        Element root = textComponent.getDocument().getDefaultRootElement();
        int pos = textComponent.viewToModel2D(new Point2D.Double(0, y));
        return root.getElementIndex(pos);
    }

    @Override public Dimension getPreferredSize() {
        return new Dimension(getComponentWidth(), textComponent.getHeight());
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(getBackground());
        Rectangle clip = g.getClipBounds();
        g.fillRect(clip.x, clip.y, clip.width, clip.height);

        g.setColor(getForeground());
        Insets insets = getInsets();
        int baseY = clip.y;

        // 行数とフォント情報を取得
        int startLine = getLineAtPoint(baseY);
        int endLine = getLineAtPoint(baseY + clip.height);
        int lineHeight = fontMetrics.getHeight();
        int ascent = fontMetrics.getAscent();
        
        try {
            // yの位置を調整するため、エディタの実際の行の描画位置を取得する
            Rectangle startLineRect = textComponent.modelToView2D(textComponent.getDocument().getDefaultRootElement().getElement(startLine).getStartOffset()).getBounds();
            int y = startLineRect.y; // 実際の行のY座標を取得

            // 行番号を描画
            for (int line = startLine; line <= endLine; line++) {
                String lineNumber = String.valueOf(line + 1);
                int x = getComponentWidth() - insets.right - fontMetrics.stringWidth(lineNumber);
                g.drawString(lineNumber, x, y + ascent); // ascent を追加して正確な位置に描画
                y += lineHeight; // 次の行に移動
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}

// 簡略化された DocumentListener のためのインターフェース
@FunctionalInterface
interface SimpleDocumentListener extends javax.swing.event.DocumentListener {
    void update(javax.swing.event.DocumentEvent e);

    @Override default void insertUpdate(javax.swing.event.DocumentEvent e) { update(e); }
    @Override default void removeUpdate(javax.swing.event.DocumentEvent e) { update(e); }
    @Override default void changedUpdate(javax.swing.event.DocumentEvent e) { update(e); }
}
