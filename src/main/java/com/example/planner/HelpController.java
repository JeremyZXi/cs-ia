package com.example.planner;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ResourceBundle;
import com.vladsch.flexmark.ext.tables.TablesExtension;

public class HelpController{

    @FXML
    private WebView webView;

    private Parser markdownParser;
    private HtmlRenderer markdownRenderer;


    public void initialize() {
        // flexmark setup
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, List.of(
                TablesExtension.create()
        ));

        markdownParser = Parser.builder(options).build();
        markdownRenderer = HtmlRenderer.builder(options).build();


        WebEngine engine = webView.getEngine();

        // paths inside your resources folder (adjust if needed)
        String mdPath = "/com/example/planner/help.md";
        String cssPath = "/com/example/planner/help.css";

        String markdown = loadTextResource(mdPath);
        String css = loadTextResource(cssPath);

        if (markdown == null) {
            markdown = "# Help\n\nCould not load help.md from resources.";
        }
        if (css == null) {
            css = """
                  body { font-family: system-ui, -apple-system, Segoe UI, sans-serif; padding: 16px; }
                  h1, h2, h3 { margin-top: 1.2em; }
                  code, pre { font-family: Consolas, Menlo, monospace; }
                  """;
        }

        // markdown -> HTML body
        Node document = markdownParser.parse(markdown);
        String htmlBody = markdownRenderer.render(document);

        // wrap with full HTML + CSS
        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                %s
                    </style>
                </head>
                <body>
                %s
                </body>
                </html>
                """.formatted(css, htmlBody);

        engine.loadContent(html, "text/html");
    }

    /** Utility to read a text resource from the classpath into a String. */
    private String loadTextResource(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) return null;
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                return sb.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
