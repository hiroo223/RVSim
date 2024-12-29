package processingUnit;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.StringTokenizer;

public class Context { // 字句解析を行うクラス
    private BufferedReader reader; // 行を読み込むオブジェクト
    private StringTokenizer st;    // 文字列を単語に区切るオブジェクト
    private String token;          // 現在のトークンを格納する変数
    public int lineNumber = 1;     // 現在の行番号を格納する変数
    private String delim;          // 区切り文字列
    private String currentLineString; // 現在の行全体の文字列を格納する変数

    public Context(String text, String delim) { // コンストラクタ
        this.delim = delim;
        reader = new BufferedReader(new StringReader(text));
        toNext(); // 最初のトークンを読み token へ格納
    }

    // 次のトークンを探し、見つかれば文字列として token に格納するメソッド
    public void toNext() {
        try {
            while ((st == null || !st.hasMoreTokens()) && reader.ready()) {
                // 新しい行を読み込む
                String line = reader.readLine();
                if (line == null) { // 行が存在しない場合
                    token = null;
                    return;
                }

                // 行全体を保持（コメントを含む場合はそれも含めて）
                currentLineString = line;

                // #がある場合、その行の残り部分をスキップして次の行に進む
                int commentIndex = line.indexOf("#");
                if (commentIndex != -1) {
                    line = line.substring(0, commentIndex).trim(); // コメント部分を削除
                }

                // コメントだけの行であれば次の行に進む
                if (line.isEmpty()) {
                    lineNumber++; // 空行なので次の行に進む
                    continue;
                }

                st = new StringTokenizer(line, delim); // 行を区切り文字で分割
                lineNumber++; // 新しい行に進むので行番号を更新
            }
            // 次のトークンを取得
            token = (st != null && st.hasMoreTokens()) ? st.nextToken() : null;
        } catch (Exception e) {
            e.printStackTrace();
            token = null;
        }
    }

    // 次の行の最初のトークンを取得するメソッド
    public void toNextLine() {
        try {
            while (reader.ready()) {
                // 新しい行を読み込む
                String line = reader.readLine();
                if (line == null) { // 行が存在しない場合
                    token = null;
                    return;
                }

                // 行全体を保持（コメントを含む場合はそれも含めて）
                currentLineString = line;

                // #がある場合、その行の残り部分をスキップして次の行に進む
                int commentIndex = line.indexOf("#");
                if (commentIndex != -1) {
                    line = line.substring(0, commentIndex).trim(); // コメント部分を削除
                }

                // コメントだけの行であれば次の行に進む
                if (line.isEmpty()) {
                    lineNumber++; // 空行なので次の行に進む
                    continue;
                }

                st = new StringTokenizer(line, delim); // 行を区切り文字で分割
                lineNumber++; // 新しい行に進むので行番号を更新

                // 最初のトークンを取得
                if (st.hasMoreTokens()) {
                    token = st.nextToken();
                    return;
                }
            }
            token = null; // 次の行が存在しない場合
        } catch (Exception e) {
            e.printStackTrace();
            token = null;
        }
    }

    // 現在のトークンを返すメソッド
    public String currentToken() {
        return token;
    }

    // 現在のトークンと引数文字列（正規表現可）が等しければ true を返し，
    // 等しくないか，現在のトークンが null の場合 false を返すメソッド
    public boolean match(String s) {
        return (token != null) ? token.matches(s) : false;
    }

    // 現在の行番号を返すメソッド
    public int currentLine() {
        return lineNumber - 1;
    }

    // 現在の行全体の文字列を返すメソッド
    public String getLineString() {
        return currentLineString;
    }
}
